package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * Public final class which represents the Joypad.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class Joypad implements Component {

	private final Cpu cpu;
	private int lines[];
	private int REG_P1;
	private static int ACTIVE_KEY_ENCODING_SIZE = 4;

	/**
	 * Public enum which represents the keys on the Joypad.
	 * 
	 * @author Oscar Pitcho (288225)
	 * @author Nizar Ghandri (283161)
	 */
	public static enum Key {
		RIGHT, LEFT, UP, DOWN, A, B, SELECT, START;

		private int column() {
			return this.ordinal() % 4;
		}

		private int line() {
			return Bits.extract(this.ordinal(), 2, 1);
		}
	}

	/**
	 * Public constructor to create a joypad attached to a cpu.
	 * 
	 * @param cpu
	 *            with which the joypad is attached
	 * 
	 * @throws NullPointerException
	 *             if the argument is null.
	 */
	public Joypad(Cpu cpu) {
		Objects.requireNonNull(cpu);
		this.cpu = cpu;
		REG_P1 = 0;
		lines = new int[2];
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		return address == AddressMap.REG_P1 ? Bits.complement8(REG_P1) : NO_DATA;
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);
		if (address == AddressMap.REG_P1) {
			int dataNormalized = Bits.complement8(data);
			int writableBitsMask = 0b00110000;
			int oldReg = REG_P1;
			REG_P1 = (dataNormalized & writableBitsMask);
			updateReg();
			raiseInterrupt(oldReg);
		}
	}

	/**
	 * Method to simulate the press of a key.
	 * 
	 * @param key
	 *            which has been pressed
	 * 
	 */
	public void keyPressed(Key key) {
		changeStateAndThrow(key, true);
	}

	/**
	 * Method to simulate the release of a key.
	 * 
	 * @param key
	 *            which has been released.
	 */
	public void keyReleased(Key key) {
		changeStateAndThrow(key, false);
	}

	// Private method to change the state of P1 and raise interrupts.
	private void changeStateAndThrow(Key key, boolean pressKey) {
		int line = lines[key.line()];
		lines[key.line()] = Bits.set(line, key.column(), pressKey);
		int oldReg = REG_P1;
		updateReg();
		raiseInterrupt(oldReg);
	}

	private void raiseInterrupt(int oldReg) {
		int oldActiveKeys = Bits.clip(ACTIVE_KEY_ENCODING_SIZE, oldReg);
		int newActiveKeys = Bits.clip(ACTIVE_KEY_ENCODING_SIZE, REG_P1);
		
		//The formula below will be different than 0 if any key changed from 0 to 1.
		if (((oldActiveKeys ^ newActiveKeys) & (Bits.complement8(oldActiveKeys) & 0xF)) != 0) {
			cpu.requestInterrupt(Interrupt.JOYPAD);
		}
	}

	private int getLineOr0(int lineIndex) {
		return Bits.test(REG_P1, ACTIVE_KEY_ENCODING_SIZE + lineIndex) ? lines[lineIndex] : 0;
	}

	private void updateReg() {
		REG_P1 = Bits.extract(REG_P1, ACTIVE_KEY_ENCODING_SIZE,
				Byte.SIZE - ACTIVE_KEY_ENCODING_SIZE) << ACTIVE_KEY_ENCODING_SIZE | getLineOr0(0) | getLineOr0(1);
	} 
	

}