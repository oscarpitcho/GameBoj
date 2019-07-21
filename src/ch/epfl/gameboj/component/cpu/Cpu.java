package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * final class, implements Component and Clocked, which represents the CPU of
 * the GameBoy, in charge of managing and processing all instructions.
 *
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class Cpu implements Component, Clocked {

	private final static int PREFIX_OPCODE = 0xCB;

	private long nextNonIdleCycle;

	private int PC;
	private int SP;

	private boolean IME;

	private int IE; // Interruptions enabled.
	private int IF; // Interruptions raised.

	private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT);
	private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);

	private Bus bus;
	private final Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);

	private enum Reg implements Register {
		A, F, B, C, D, E, H, L
	}

	private final RegisterFile<Reg> reg = new RegisterFile<Reg>(Reg.values());

	private enum Reg16 {
		AF(Reg.A, Reg.F), BC(Reg.B, Reg.C), DE(Reg.D, Reg.E), HL(Reg.H, Reg.L);

		private final Reg reg1;
		private final Reg reg2;

		private Reg16(Reg f, Reg s) {
			reg1 = f;
			reg2 = s;
		}
	}

	/**
	 * Enumeration for the four possible outcomes when updating the flags contained
	 * in F.
	 *
	 * @author Oscar Pitcho (288225)
	 * @author Nizar Ghandri (283161)
	 */
	private enum FlagSrc {
		V0, // Forces the flag to be 0
		V1, // Forces the flag to be 1
		ALU, // uses the flag from the Alu
		CPU // leaves the flag as it is
	}

	/***
	 * General enumeration for the 8 indices of an opcode's encoding.
	 *
	 * @author Oscar Pitcho (288225)
	 * @author Nizar Ghandri
	 */
	private enum OpcodeIndex implements Bit {
		I0, I1, I2, I3, I4, I5, I6, I7;

		private static OpcodeIndex index(int i) {
			Objects.checkIndex(i, 8);
			return OpcodeIndex.values()[i];
		}
	}

	/**
	 * Enumerations which represents all exceptions that can be thrown by the
	 * components of the GameBoy.
	 *
	 * @author Oscar Pitcho (288225)
	 * @author Nizar Ghandri (283161)
	 *
	 */
	public enum Interrupt implements Bit {
		VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
	}

	@Override
	public void cycle(long cycle) {
		/*if(cycle %10_000 == 0)
			System.out.println(nextNonIdleCycle);*/
		if (cycle == nextNonIdleCycle) {
			reallyCycle();
		}
		else if (wakeUpCpu()) {
			nextNonIdleCycle = cycle;
			reallyCycle();
		} else
			return;
	}

	@Override
	public int read(int address) {
		if (AddressMap.HIGH_RAM_START <= address && address < AddressMap.HIGH_RAM_END) {
			return highRam.read(address - AddressMap.HIGH_RAM_START);
		} else if (AddressMap.REG_IE == address) {
			return IE;
		} else if (AddressMap.REG_IF == address) {
			return IF;
		} else
			return NO_DATA;

	}

	@Override
	public void write(int address, int data) {
		if (AddressMap.HIGH_RAM_START <= address && address < AddressMap.HIGH_RAM_END) {
			highRam.write(address - AddressMap.HIGH_RAM_START, data);
		} else if (address == AddressMap.REG_IE) {
			IE = data;
		} else if (address == AddressMap.REG_IF) {
			IF = data;
		}
	}

	@Override
	public void attachTo(Bus bus) {
		Objects.requireNonNull(bus);
		this.bus = bus;
		bus.attach(this);
	}

	/**
	 * Method to raise an interruption by changing its corresponding index in IF to
	 * 1.
	 *
	 * @param i
	 *            Interruption which will be raised.
	 */
	public void requestInterrupt(Interrupt i) {
		IF = Bits.set(IF, i.index(), true);
	}

	/**
	 * Method for testing
	 *
	 * @return The registers of the cpu (except IE and IF) as an array.
	 */
	public int[] _testGetPcSpAFBCDEHL() {
		int[] test = new int[Reg.values().length + 2];

		test[0] = PC;
		test[1] = SP;
		test[2] = reg.get(Reg.A);
		test[3] = reg.get(Reg.F);
		test[4] = reg.get(Reg.B);
		test[5] = reg.get(Reg.C);
		test[6] = reg.get(Reg.D);
		test[7] = reg.get(Reg.E);
		test[8] = reg.get(Reg.H);
		test[9] = reg.get(Reg.L);

		return test;
	}

	/**
	 * Public method for testing.
	 *
	 * @return An array with IME ( 1 for true 0 for false) IF and IE.
	 *
	 */
	protected int[] _testIMEIFIE() {
		int[] test = new int[3];
		test[0] = IME ? 1 : 0;
		test[1] = IF;
		test[2] = IE;
		return test;
	}

	private void reallyCycle() {
		if (IME && findInterruption() != -1)
			manageInterruption();
		else {
			dispatchAndCycle();
		}
	}

	private void dispatch(Opcode opcode) {
		int nextPC = PC + opcode.totalBytes;
		switch (opcode.family) {
		case NOP: {
		}
		break;

		// Loads. Push and Pop.
		case LD_R8_HLR: {
			int value = read8AtHl();
			reg.set(extractReg(opcode, 3), value);
		}
		break;
		case LD_A_HLRU: {
			int value = read8AtHl();
			int r = Bits.clip(16, (reg16(Reg16.HL) + extractHlIncrement(opcode)));
			setReg16(Reg16.HL, r);
			reg.set((Reg.A), value);
		}
		break;
		case LD_A_N8R: {
			int value = read8(Bits.clip(16, AddressMap.REGS_START + read8AfterOpcode()));
			reg.set((Reg.A), value);
		}
		break;
		case LD_A_CR: {
			int value = read8(Bits.clip(16, AddressMap.REGS_START + reg.get(Reg.C)));
			reg.set((Reg.A), value);
		}
		break;
		case LD_A_N16R: {
			int value = read8(read16AfterOpcode());
			reg.set((Reg.A), value);
		}
		break;
		case LD_A_BCR: {
			int value = read8(reg16(Reg16.BC));
			reg.set((Reg.A), value);
		}
		break;
		case LD_A_DER: {
			int value = read8(reg16(Reg16.DE));
			reg.set((Reg.A), value);
		}
		break;
		case LD_R8_N8: {
			int value = read8AfterOpcode();
			reg.set((extractReg(opcode, 3)), value);
		}
		break;
		case LD_R16SP_N16: {
			int value = read16AfterOpcode();
			setReg16SP(extractReg16(opcode), value);
		}
		break;
		case POP_R16: {
			int value = pop16();
			setReg16(extractReg16(opcode), value);
		}
		break;
		case LD_HLR_R8: {
			int value = reg.get(extractReg(opcode, 0));
			write8AtHl(value);
		}
		break;
		case LD_HLRU_A: {
			int value = reg.get(Reg.A);
			write8AtHl(value);
			int r = Bits.clip(16, (reg16(Reg16.HL) + extractHlIncrement(opcode)));
			setReg16(Reg16.HL, r);
		}
		break;
		case LD_N8R_A: {
			int value = reg.get(Reg.A);
			write8(Bits.clip(16, AddressMap.REGS_START + read8AfterOpcode()), value);
		}
		break;
		case LD_CR_A: {
			int value = reg.get(Reg.A);
			write8(Bits.clip(16, AddressMap.REGS_START + reg.get(Reg.C)), value);
		}
		break;
		case LD_N16R_A: {
			int value = reg.get(Reg.A);
			write8(read16AfterOpcode(), value);
		}
		break;
		case LD_BCR_A: {
			int value = reg.get(Reg.A);
			write8(reg16(Reg16.BC), value);
		}
		break;
		case LD_DER_A: {
			int value = reg.get(Reg.A);
			write8(reg16(Reg16.DE), value);
		}
		break;
		case LD_HLR_N8: {
			int value = read8AfterOpcode();
			write8AtHl(value);
		}
		break;
		case LD_N16R_SP: {
			write16(read16AfterOpcode(), SP);
		}
		break;
		case LD_R8_R8: {
			int value = reg.get(extractReg(opcode, 0));
			reg.set(extractReg(opcode, 3), value);
		}
		break;
		case LD_SP_HL: {
			SP = reg16(Reg16.HL);
		}
		break;
		case PUSH_R16: {
			push16(reg16(extractReg16(opcode)));
		}
		break;

		// Add and Increments
		case ADD_A_R8: {
			int valueFlags = Alu.add(reg.get(Reg.A), reg.get(extractReg(opcode, 0)), withCarry(opcode));
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case ADD_A_N8: {
			int valueFlags = Alu.add(reg.get(Reg.A), read8AfterOpcode(), withCarry(opcode));
			setRegFlags(Reg.A, valueFlags);

		}
		break;
		case ADD_A_HLR: {
			int valueFlags = Alu.add(reg.get(Reg.A), read8(reg16(Reg16.HL)), withCarry(opcode));
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case INC_R8: {
			Reg r8 = extractReg(opcode, 3);
			int valueFlags = Alu.add(reg.get(r8), 1);
			reg.set(r8, Alu.unpackValue(valueFlags));
			combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
		}
		break;
		case INC_HLR: {
			int valueFlags = Alu.add(read8AtHl(), 1);
			write8AtHl(Alu.unpackValue(valueFlags));
			combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
		}
		break;
		case INC_R16SP: {
			Reg16 reg = extractReg16(opcode);
			int valueFlags = Alu.add16H(reg16SP(reg), 1);
			setReg16SP(reg, Alu.unpackValue(valueFlags));
		}
		break;
		case ADD_HL_R16SP: {
			Reg16 reg = extractReg16(opcode);
			int valueFlags = Alu.add16H(reg16(Reg16.HL), reg16SP(reg));
			setReg16(Reg16.HL, Alu.unpackValue(valueFlags));
			combineAluFlags(valueFlags, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
		}
		break;
		case LD_HLSP_S8: {
			Reg16 reg = Bits.test(opcode.encoding, 4) ? Reg16.HL : Reg16.AF;
			int signedValue = read8AfterOpcode();
			int flags = Alu.add16L(SP, signedValue);
			int endValue = Bits.clip(16, SP + Bits.signExtend8(signedValue));
			combineAluFlags(flags, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
			setReg16SP(reg, endValue);

		}
		break;

		// Sub. Decrements and Compare
		case SUB_A_R8: {
			int valueFlags = Alu.sub(reg.get(Reg.A), reg.get(extractReg(opcode, 0)), withCarry(opcode));
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case SUB_A_N8: {
			int valueFlags = Alu.sub(reg.get(Reg.A), read8AfterOpcode(), withCarry(opcode));
			setRegFlags(Reg.A, valueFlags);

		}
		break;
		case SUB_A_HLR: {
			int valueFlags = Alu.sub(reg.get(Reg.A), read8AtHl(), withCarry(opcode));
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case DEC_R8: {
			Reg r8 = extractReg(opcode, 3);
			int valueFlags = Alu.sub(reg.get(r8), 1);
			reg.set(r8, Alu.unpackValue(valueFlags));
			combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
		}
		break;
		case DEC_HLR: {
			int valueFlags = Alu.sub(read8AtHl(), 1);
			write8AtHl(Alu.unpackValue(valueFlags));
			combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
		}
		break;
		case CP_A_R8: {
			int valueFlags = Alu.sub(reg.get(Reg.A), reg.get(extractReg(opcode, 0)));
			setFlags(valueFlags);
		}
		break;
		case CP_A_N8: {
			int valueFlags = Alu.sub(reg.get(Reg.A), read8AfterOpcode());
			setFlags(valueFlags);
		}
		break;
		case CP_A_HLR: {
			int valueFlags = Alu.sub(reg.get(Reg.A), read8AtHl());
			setFlags(valueFlags);
		}
		break;
		case DEC_R16SP: {
			Reg16 reg = extractReg16(opcode);
			int value = reg != Reg16.AF ? Bits.clip(16, reg16(reg) - 1) : Bits.clip(16, SP - 1);
			setReg16SP(reg, value);
		}
		break;

		// Bit test and set
		case BIT_U3_R8: {
			int valueFlags = Alu.testBit(reg.get(extractReg(opcode, 0)), getChangeOrTestIndex(opcode).index());
			combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
		}
		break;
		case BIT_U3_HLR: {
			int valueFlags = Alu.testBit(read8AtHl(), getChangeOrTestIndex(opcode).index());
			combineAluFlags(valueFlags, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
		}
		break;
		case CHG_U3_R8: {
			Reg r = extractReg(opcode, 0);
			reg.setBit(r, getChangeOrTestIndex(opcode), isSet(opcode));
		}
		break;
		case CHG_U3_HLR: {
			int value = read8AtHl();
			write8AtHl(Bits.set(value, getChangeOrTestIndex(opcode).index(), isSet(opcode)));
		}
		break;

		// Misc. ALU
		case DAA: {
			int value = Alu.bcdAdjust(reg.get(Reg.A), reg.testBit(Reg.F, Flag.N), reg.testBit(Reg.F, Flag.H),
					reg.testBit(Reg.F, Flag.C));
			setRegFromAlu(Reg.A, value);
			combineAluFlags(value, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);
		}
		break;
		case SCCF: {
			if (manipulateC(opcode))
				combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V1);// TODO perhaps find better way than
			// putting 0.
			else
				combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
		}
		break;

		// And, or, xor, complement
		case AND_A_N8: {
			int valueFlags = Alu.and(reg.get(Reg.A), read8AfterOpcode());
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case AND_A_R8: {
			int valueFlags = Alu.and(reg.get(Reg.A), reg.get(extractReg(opcode, 0)));
			setRegFlags(Reg.A, valueFlags);

		}
		break;
		case AND_A_HLR: {
			int valueFlags = Alu.and(reg.get(Reg.A), read8AtHl());
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case OR_A_R8: {
			int valueFlags = Alu.or(reg.get(Reg.A), reg.get(extractReg(opcode, 0)));
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case OR_A_N8: {
			int valueFlags = Alu.or(reg.get(Reg.A), read8AfterOpcode());
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case OR_A_HLR: {
			int valueFlags = Alu.or(reg.get(Reg.A), read8AtHl());
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case XOR_A_R8: {
			int valueFlags = Alu.xor(reg.get(Reg.A), reg.get(extractReg(opcode, 0)));
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case XOR_A_N8: {
			int valueFlags = Alu.xor(reg.get(Reg.A), read8AfterOpcode());
			setRegFlags(Reg.A, valueFlags);

		}
		break;
		case XOR_A_HLR: {
			int valueFlags = Alu.xor(reg.get(Reg.A), read8AtHl());
			setRegFlags(Reg.A, valueFlags);
		}
		break;
		case CPL: {
			reg.set(Reg.A, Bits.complement8(reg.get(Reg.A)));
			combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
		}
		break;

		// Rotate, shift
		case ROTCA: {
			int valueFlags = Alu.rotate(extractRotDir(opcode), reg.get(Reg.A));
			setRegFromAlu(Reg.A, valueFlags);
			combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
		}
		break;
		case ROTA: {
			int valueFlags = Alu.rotate(extractRotDir(opcode), reg.get(Reg.A), getCarry());
			setRegFromAlu(Reg.A, valueFlags);
			combineAluFlags(valueFlags, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
		}
		break;
		case ROTC_R8: {
			Reg r = extractReg(opcode, 0);
			int valueFlags = Alu.rotate(extractRotDir(opcode), reg.get(r));
			setRegFlags(r, valueFlags);
		}
		break;
		case ROT_R8: {
			Reg r = extractReg(opcode, 0);
			int valueFlags = Alu.rotate(extractRotDir(opcode), reg.get(r), getCarry());
			setRegFlags(r, valueFlags);
		}
		break;
		case ROTC_HLR: {
			int valueFlags = Alu.rotate(extractRotDir(opcode), read8AtHl());
			write8AtHlAndSetFlags(valueFlags);
		}
		break;
		case ROT_HLR: {
			int valueFlags = Alu.rotate(extractRotDir(opcode), read8AtHl(), getCarry());
			write8AtHlAndSetFlags(valueFlags);
		}
		break;
		case SWAP_R8: {
			Reg r = extractReg(opcode, 0);
			int valueFlags = Alu.swap(reg.get(r));
			setRegFlags(r, valueFlags);
		}
		break;
		case SWAP_HLR: {
			int valueFlags = Alu.swap(read8AtHl());
			write8AtHlAndSetFlags(valueFlags);

		}
		break;
		case SLA_R8: {
			Reg r = extractReg(opcode, 0);
			int valueFlags = Alu.shiftLeft(reg.get(r));
			setRegFlags(r, valueFlags);
		}
		break;
		case SRA_R8: {
			Reg r = extractReg(opcode, 0);
			int valueFlags = Alu.shiftRightA(reg.get(r));
			setRegFlags(r, valueFlags);
		}
		break;
		case SRL_R8: {
			Reg r = extractReg(opcode, 0);
			int valueFlags = Alu.shiftRightL(reg.get(r));
			setRegFlags(r, valueFlags);
		}
		break;
		case SLA_HLR: {
			int valueFlags = Alu.shiftLeft(read8AtHl());
			write8AtHlAndSetFlags(valueFlags);
		}
		break;
		case SRA_HLR: {
			int valueFlags = Alu.shiftRightA(read8AtHl());
			write8AtHlAndSetFlags(valueFlags);
		}
		break;
		case SRL_HLR: {
			int valueFlags = Alu.shiftRightL(read8AtHl());
			write8AtHlAndSetFlags(valueFlags);
		}
		break;

		// Jumps
		case JP_HL: {
			nextPC = reg16(Reg16.HL);
		}
		break;
		case JP_N16: {
			nextPC = read16AfterOpcode();
		}
		break;
		case JP_CC_N16: {
			if (conditionRespected(opcode))
				nextPC = read16AfterOpcode();
		}
		break;
		case JR_E8: {
			nextPC = nextPC + Bits.signExtend8(read8AfterOpcode());
		}
		break;
		case JR_CC_E8: {
			if (conditionRespected(opcode))
				nextPC = nextPC + Bits.signExtend8(read8AfterOpcode());
		}
		break;

		// Calls and returns
		case CALL_N16: {
			push16(Bits.clip(16, nextPC));
			nextPC = read16AfterOpcode();

		}
		break;
		case CALL_CC_N16: {
			if (conditionRespected(opcode)) {
				push16(Bits.clip(16, nextPC));
				nextPC = read16AfterOpcode();
			}
		}
		break;
		case RST_U3: {
			push16(nextPC);
			int index = Bits.extract(opcode.encoding, 3, 3);
			nextPC = AddressMap.RESETS[index];
		}
		break;
		case RET: {
			nextPC = pop16();
		}
		break;
		case RET_CC: {
			if (conditionRespected(opcode))
				nextPC = pop16();
		}
		break;

		// Interrupts
		case EDI: {
			IME = Bits.test(opcode.encoding, 3);
		}
		break;
		case RETI: {
			IME = true;
			nextPC = pop16();
		}
		break;
		// Misc control
		case HALT: {
			nextNonIdleCycle = Long.MAX_VALUE;
		}
		break;
		case STOP:
			throw new Error("STOP is not implemented");
		}
		PC = nextPC;
	}

	private static Opcode[] buildOpcodeTable(Kind a) {
		Opcode[] x = new Opcode[256];
		for (Opcode o : Opcode.values()) {
			if (o.kind.equals(a)) {
				x[o.encoding] = o;
			}
		}
		return x;
	}

	// Reads the 8 bits value form the specified address.
	private int read8(int address) {
		return Bits.clip(8, this.bus.read(address));
	}

	// Reads the value stored at the address contained in HL.
	private int read8AtHl() {
		return read8(Bits.make16(reg.get(Reg.H), reg.get(Reg.L)));
	}

	// Reads the byte that follows the opcode's encoding.s
	private int read8AfterOpcode() {
		return read8(PC + 1);
	}

	// Reads the 16 bit value stored at the specified address.
	private int read16(int address) {
		return Bits.make16(read8((Bits.clip(16, address + 1))), read8(address));
	}

	// Reads the two bytes that follow the encoding of the opcode(in PC).
	private int read16AfterOpcode() {
		return read16( PC + 1);
	}

	// Writes an 8 bit value to the specified value.
	private void write8(int address, int v) {
		bus.write(address, v);
	}

	// Writes the 16 bits value given in argument to the specified address.
	// The LSB are stored at the first address and the MSB are stored at the address
	// that immediately follows.
	private void write16(int address, int v) {
		write8(address, Bits.clip(8, v));
		write8(Bits.clip(16, address + 1), Bits.extract(v, 8, 8));
	}

	// Writes the 8 bits value in argument to the address contained in HL.
	private void write8AtHl(int v) {
		write8(reg16(Reg16.HL), v);
	}

	// Pushes a 16 bits value to the stackpile.
	private void push16(int v) {
		SP = Bits.clip(16, SP - 2);
		write16(SP, v);
	}

	// Pops a 16 bits value from the stackpile.
	private int pop16() {
		int a = read16(SP);
		SP = Bits.clip(16, Bits.clip(16, SP + 2));
		return a;
	}

	// Returns the value contained in the 16 bits register given in argument.
	private int reg16(Reg16 r) {
		return Bits.make16(reg.get(r.reg1), reg.get(r.reg2));
	}

	// Returns the value contained in 16 bits register given as argument.
	// If the argument is AF method will return value of SP instead.
	private int reg16SP(Reg16 r) {
		return r != Reg16.AF ? reg16(r) : SP;

	}

	// Sets the register in argument to the value given.
	// If the register is AF the first 4 bits will be cleared.
	private void setReg16(Reg16 r, int newV) {
		int lsb = Bits.clip(8, newV);
		int msb = Bits.extract(newV, 8, 8);
		if (r == Reg16.AF) {
			int flagsMask = 0xF0;
			reg.set(Reg.A, msb);
			reg.set(Reg.F, lsb & flagsMask);
		} else {
			reg.set(r.reg1, msb);
			reg.set(r.reg2, lsb);
		}
	}

	// Sets the register in argument to the value given.
	// If the register is AF will set SP instead.
	private void setReg16SP(Reg16 r, int newV) {
		Preconditions.checkBits16(newV);
		if (r == Reg16.AF)
			SP = newV;
		else
			setReg16(r, newV);
	}

	// Extracts 8 bits register contained in encoding of opcode.
	private Reg extractReg(Opcode opcode, int startBit) {
		switch (Bits.extract(opcode.encoding, startBit, 3)) {
		case 0b000:
			return Reg.B;
		case 0b001:
			return Reg.C;
		case 0b010:
			return Reg.D;
		case 0b011:
			return Reg.E;
		case 0b100:
			return Reg.H;
		case 0b101:
			return Reg.L;
		case 0b111:
			return Reg.A;
		default:
			return null;
		}
	}

	// Extracts 16 bits register contained in encoding of opcode.
	private Reg16 extractReg16(Opcode opcode) {
		switch (Bits.extract(opcode.encoding, 4, 2)) {
		case 0b00:
			return Reg16.BC;
		case 0b01:
			return Reg16.DE;
		case 0b10:
			return Reg16.HL;
		default:
			return Reg16.AF;
		}
	}

	private int extractHlIncrement(Opcode opcode) {
		switch (Bits.extract(opcode.encoding, 4, 1)) {
		case 0:
			return 1;
		case 1:
			return -1;
		default:
			return 0;
		}
	}

	// Sets r8 to the value contained in vf. It must be 8 bits and not 16 bits.
	private void setRegFromAlu(Reg r, int vf) {
		int value = Alu.unpackValue(vf);
		reg.set(r, value);
	}

	// Sets F to the flags of the argument.
	private void setFlags(int valueFlags) {
		int flags = Alu.unpackFlags(valueFlags);
		reg.set(Reg.F, flags);
	}

	// Set the 8 bit register in argument equal to the value of vf and sets the F to
	// the flags of vf.
	private void setRegFlags(Reg r, int vf) {
		setRegFromAlu(r, vf);
		setFlags(vf);
	}

	// This is a test comment.
	// Write the value contained in vf at the address in register HL then copy the
	// flags of vf in F.
	private void write8AtHlAndSetFlags(int vf) {
		write8AtHl(Alu.unpackValue(vf));
		setFlags(vf);
	}

	// Method to combine flags contained int vf, those already in F and set flags to
	// 0 or 1.
	private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
		int v1 = testCombineAluFlags(z, n, h, c, FlagSrc.V1);
		int alu = testCombineAluFlags(z, n, h, c, FlagSrc.ALU);
		int cpu = testCombineAluFlags(z, n, h, c, FlagSrc.CPU);
		int combined = v1 | (vf & alu) | (cpu & reg.get(Reg.F));
		reg.set(Reg.F, Alu.unpackFlags(combined));
	}

	private int testCombineAluFlags(FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c, FlagSrc a) {
		return Alu.maskZNHC(z == a, n == a, h == a, c == a);
	}

	// Method returns true if and only if addition/subtraction is with carry and the
	// flag C is 1.
	private boolean withCarry(Opcode o) {
		return Bits.test(Bits.extract(o.encoding, 3, 1), 0) && getCarry();
	}

	// Method to obtain the direction of rotation from the encoding of the rotation
	// opcodes.
	private RotDir extractRotDir(Opcode opcode) {
		return Bits.test(opcode.encoding, 3) ? RotDir.RIGHT : RotDir.LEFT;
	}

	// Method to obtain value of the flag C in register F. True for 1 false for 0.
	private boolean getCarry() {
		return reg.testBit(Reg.F, Alu.Flag.C);
	}

	// Method to obtain value of the flag Z in register F. True for 1 false for 0.
	private boolean getZero() {
		return reg.testBit(Reg.F, Alu.Flag.Z);
	}

	// Returns true if opcode is a Set instruction, false if it is a reset
	// instruction.
	private boolean isSet(Opcode opcode) {
		if (Bits.extract(opcode.encoding, 6, 1) == 1)
			return true;
		else
			return false;
	}

	// Returns index to set/reset or test for CHG and BIT instructions.
	private OpcodeIndex getChangeOrTestIndex(Opcode opcode) {
		return OpcodeIndex.index(Bits.extract(opcode.encoding, 3, 3));
	}

	// TODO needs to be improved.
	private boolean manipulateC(Opcode o) {
		boolean a = !Bits.test(o.encoding, 3) || (Bits.test(o.encoding, 3) && !getCarry());
		return a;
	}

	// Extracts condition out of opcode and returns true or false depending on
	// whether it is respected or not.
	private boolean conditionRespected(Opcode opcode) {
		if (!hasCondition(opcode))
			return false;
		switch (Bits.extract(opcode.encoding, 3, 2)) {
		case 0b00:
			return !getZero();
		case 0b01:
			return getZero();
		case 0b10:
			return !getCarry();
		case 0b11:
			return getCarry();
		default:
			return false;
		}
	}

	// Checks if the opcode in argument belongs to a family with a condition.
	private boolean hasCondition(Opcode opcode) {
		return opcode.family == Opcode.Family.RET_CC
				|| opcode.family == Opcode.Family.CALL_CC_N16
				|| opcode.family == Opcode.Family.JR_CC_E8
				|| opcode.family == Opcode.Family.JP_CC_N16;

	}

	private void manageInterruption() {
		IME = false;
		int interruptionIndex = findInterruption();
		IF = Bits.set(IF, interruptionIndex, false);
		push16(PC);
		PC = AddressMap.INTERRUPTS[interruptionIndex];
		nextNonIdleCycle += 5;
	}

	private void dispatchAndCycle() {
		int a = read8(PC);
		if (a == PREFIX_OPCODE) {
			a = read8AfterOpcode();
			dispatch(PREFIXED_OPCODE_TABLE[a]);
			nextNonIdleCycle += PREFIXED_OPCODE_TABLE[a].cycles;
		} else {
			dispatch(DIRECT_OPCODE_TABLE[a]);
			nextNonIdleCycle += DIRECT_OPCODE_TABLE[a].cycles;
			if (hasCondition(DIRECT_OPCODE_TABLE[a]) && conditionRespected(DIRECT_OPCODE_TABLE[a]))
				nextNonIdleCycle += DIRECT_OPCODE_TABLE[a].additionalCycles;
		}
	}

	private boolean wakeUpCpu() {
		return nextNonIdleCycle == Long.MAX_VALUE && findInterruption() != -1;
	}

	// Method to check if an enabled instruction is being raised.
	// Returns its index if yes and 5 otherwise.
	// Does not account for the state of IME.
	private int findInterruption() {
		for (Interrupt i : Interrupt.values()) {
			if (Bits.test(IE, i.index()) && Bits.test(IF, i.index()))
				return i.index();
		}
		return -1;
	}

}
