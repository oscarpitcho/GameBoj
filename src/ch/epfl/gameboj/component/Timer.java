
package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * Final class, implements Component and Clocked, which represents the Timer
 * component in the GamBoy. It is responsible for managing the counters that
 * regulate some functionalities of the GameBoy.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class Timer implements Component, Clocked {

	private final Cpu cpu;
	private int mainCounter;
	private int TIMA;
	private int TMA;
	private int TAC;

	/**
	 * Public constructor to create a timer which is connected to a cpu.
	 * 
	 * @param cpu
	 *            The CPU with which the new timer will be connected.
	 * 
	 * @throws NullPointerException
	 *             if the cpu given in argument is null.
	 */
	public Timer(Cpu cpu) {
		Objects.requireNonNull(cpu);
		this.cpu = cpu;
	}

	@Override
	public void cycle(long cycle) {
		boolean state = state();
		mainCounter = Bits.clip(16, mainCounter + 4);
		incIfChange(state);
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		switch (address) {
		case AddressMap.REG_DIV:
			return Bits.extract(mainCounter, 8, 8);
		case AddressMap.REG_TIMA:
			return TIMA;
		case AddressMap.REG_TMA:
			return TMA;
		case AddressMap.REG_TAC:
			return TAC;
		default:
			return NO_DATA;
		}
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits8(data);
		Preconditions.checkBits16(address);
		boolean state = state();
		switch (address) {
		case AddressMap.REG_DIV: {
			mainCounter = 0;
			incIfChange(state);
		}
		break;
		case AddressMap.REG_TIMA:
			TIMA = data;
			break;
		case AddressMap.REG_TMA:
			TMA = data;
			break;
		case AddressMap.REG_TAC: {
			TAC = Bits.clip(3, data);
			incIfChange(state);
		}
		}
	}

	private boolean state() {
		return Bits.test(TAC, 2) && Bits.test(mainCounter, bitPosition());
	}

	private int bitPosition() {
		switch(Bits.clip(2, TAC)) {
		case 0b00:
			return 9;
		case 0b01:
			return 3;
		case 0b10:
			return 5;
		case 0b11:
			return 7;
		default:
			return 0;
		}
	}
	private void incIfChange(boolean state) {
		if (state & !state()) {
			if (TIMA == 0xFF) {
				cpu.requestInterrupt(Interrupt.TIMER);
				TIMA = TMA;
			} else
				TIMA = Bits.clip(8, TIMA + 1);
		}
	}
}
