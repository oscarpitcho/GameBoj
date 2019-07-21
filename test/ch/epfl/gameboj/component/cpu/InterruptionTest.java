package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.RandomGenerator;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class InterruptionTest {

	private Bus connect(Cpu cpu, Ram ram) {
		RamController rc = new RamController(ram, 0);
		Bus b = new Bus();
		cpu.attachTo(b);
		rc.attachTo(b);
		return b;
	}

	private void cycleCpu(Cpu cpu, long cycles) {
		for (long c = 0; c < cycles; ++c)
			cpu.cycle(c);
	}


	@Test
	public void deiWorks() {
		Opcode[] instructions = new Opcode[] {
				Opcode.DI, Opcode.EI
		};
		for(int i = 0; i < RandomGenerator.RANDOM_ITERATIONS; i++) {
			int numberOfInstructions = RandomGenerator.randomBit(8) + 4;
			Cpu cpu = new Cpu();
			Ram ram = new Ram(1000);
			Bus bus = connect(cpu, ram);
			int IMEValue = 0;
			for(int j = 0; j < numberOfInstructions; j++) {
				IMEValue = RandomGenerator.randomIntBetweenOneAndZero();
				bus.write(j, instructions[IMEValue].encoding);
			}
			cycleCpu(cpu, instructions[0].cycles * numberOfInstructions);
			assertEquals(IMEValue, cpu._testIMEIFIE()[0]);
			assertEquals(numberOfInstructions, cpu._testGetPcSpAFBCDEHL()[0]);
		}
	}
	@Test
	public void interruptionWorks() {
		Interrupt[] interruptions = new Interrupt[] {
				Interrupt.VBLANK, Interrupt.LCD_STAT,
				Interrupt.TIMER, Interrupt.SERIAL,
				Interrupt.JOYPAD
		};
		for(Interrupt i : interruptions) {
			for(int j = 0; j < RandomGenerator.RANDOM_ITERATIONS; j++) {
				Cpu cpu = new Cpu();
				Ram ram = new Ram(0xFFFF);
				Bus bus = connect(cpu, ram);
				int valueAAfterInterupt = RandomGenerator.randomBit(8);
				bus.write(0, Opcode.LD_SP_N16.encoding);
				bus.write(1, 0xFF);
				bus.write(2, 0xFF);
				bus.write(3, Opcode.EI.encoding);
				bus.write(AddressMap.REG_IE, 1 << i.index());
				
				bus.write(0x40  +  8 * i.index(), Opcode.LD_A_N8.encoding);
				bus.write(0x40  +  8 * i.index() + 1, valueAAfterInterupt);
				bus.write(0x40  +  8 * i.index() + 2, Opcode.RETI.encoding);
				
				assertArrayEquals(new int[] {0, 0, 1 << i.index()}, cpu._testIMEIFIE());
				
				cycleCpu(cpu, Opcode.EI.cycles + Opcode.LD_SP_N16.cycles);
				
				cpu.requestInterrupt(i);
				
				assertArrayEquals(new int[] {1, 1 << i.index(), 1 << i.index()}, cpu._testIMEIFIE());
				
				cycleCpu(cpu, Opcode.LD_SP_N16.cycles + Opcode.LD_A_N8.cycles + Opcode.EI.cycles + Opcode.RETI.cycles + 5);
				
				assertArrayEquals(new int[] {1, 0, 1 << i.index()}, cpu._testIMEIFIE());
				assertEquals(valueAAfterInterupt, cpu._testGetPcSpAFBCDEHL()[2]);
				assertEquals(4, cpu._testGetPcSpAFBCDEHL()[0]);
			}
		}
	}

	@Test
	public void wakeupWorks() {
		Interrupt[] interruptions = new Interrupt[] {
				Interrupt.VBLANK, Interrupt.LCD_STAT,
				Interrupt.TIMER, Interrupt.SERIAL,
				Interrupt.JOYPAD
		};
		for(Interrupt i : interruptions) {
			for(int j = 0; j < RandomGenerator.RANDOM_ITERATIONS; j++) {
				Cpu cpu = new Cpu();
				Ram ram = new Ram(0xFFFF);
				Bus bus = connect(cpu, ram);
				int valueA = RandomGenerator.randomBit(8);
				int valueB = RandomGenerator.randomBit(8);
				bus.write(0, Opcode.LD_SP_N16.encoding);
				bus.write(1, 0xFF);
				bus.write(2, 0xFF);
				bus.write(3, Opcode.EI.encoding);
				bus.write(4, Opcode.HALT.encoding);
				bus.write(5, Opcode.LD_A_N8.encoding);
				bus.write(6, valueA);
				cycleCpu(cpu, Opcode.EI.cycles + Opcode.HALT.cycles + Opcode.LD_A_N8.cycles + Opcode.LD_SP_N16.encoding);
				bus.write(AddressMap.REG_IE, 1 << i.index());
				bus.write(AddressMap.REG_IF, 1 << i.index());
				bus.write(0x40 + 8 * i.index(), Opcode.LD_B_N8.encoding);
				bus.write(0x40 + 8 * i.index() + 1, valueB);
				assertArrayEquals(new int[] {5, 0xFFFF, 0, 0 , 0 , 0 , 0 ,0 , 0, 0}, cpu._testGetPcSpAFBCDEHL());
				cycleCpu(cpu, Opcode.EI.cycles + Opcode.HALT.cycles + Opcode.LD_A_N8.cycles + Opcode.LD_B_N8.cycles + Opcode.LD_SP_N16.cycles);
				assertArrayEquals(new int[] {0x40 + 8 * i.index() + 3, 0xFFFD, 0, 0 , valueB , 0 , 0 ,0 , 0, 0}, cpu._testGetPcSpAFBCDEHL());

			}
		}
	}

	@Test
	public void retiWorks(){
		for(int j = 0; j < 0x7E; j++) {
			Cpu cpu = new Cpu();
			Ram ram = new Ram(0xFFFF);
			Bus bus = connect(cpu, ram);
			int PCValue = RandomGenerator.randomBit(16);
			int PCAddress = j + 0xFF80;
			bus.write(0, Opcode.LD_SP_N16.encoding);
			bus.write(1, Bits.clip(8, PCAddress));
			bus.write(2, Bits.extract(PCAddress, 8, 8));
			bus.write(PCAddress, Bits.clip(8, PCValue));
			bus.write(PCAddress + 1, Bits.extract(PCValue, 8, 8));
			bus.write(3, Opcode.RETI.encoding);
			cycleCpu(cpu, Opcode.LD_SP_N16.cycles + Opcode.RETI.cycles);
			assertEquals(PCValue, cpu._testGetPcSpAFBCDEHL()[0]);
			assertEquals(j + 0xFF80 + 2, cpu._testGetPcSpAFBCDEHL()[1]);
			assertEquals(1, cpu._testIMEIFIE()[0]);
		}
	}

}
