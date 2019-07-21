

package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * Class that represents the GameBoy in its entirety.
 * 
 * @author Oscar Pitcho (288225)
 * @author NIzar Ghandri (283161)
 */
public final class GameBoy {

	private final Bus bus;
	private final Cpu cpu;
	private final RamController workRam;
	private final RamController echoRam;
	private final BootRomController bootRomController;
	private final Timer timer;
	private final LcdController lcdController;
	private final Joypad joypad;
	private long cyclesSimulated;
	public static final long CYCLES_PER_SECOND = 0x10_0000;
	public static final double CYCLES_PER_NANOSECOND = CYCLES_PER_SECOND /  1_000_000_000.0;
	/**
	 * Constructor in charge of instantiating all components of the GameBoy and
	 * attaches them to the bus.
	 * 
	 * @param cartridge
	 * 
	 * @throws NullPointerException
	 *             if cartridge is null.
	 */
	public GameBoy(Cartridge cartridge) {
		Objects.requireNonNull(cartridge);

		Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
		bus = new Bus();
		cpu = new Cpu();
		timer = new Timer(cpu);
		bootRomController = new BootRomController(cartridge);
		workRam = new RamController(ram, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
		echoRam = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
		lcdController = new LcdController(cpu);
		joypad = new Joypad(cpu);
		Component[] components = new Component[] { cpu, timer, bootRomController, workRam, echoRam, lcdController, joypad};

		for (Component component : components) {
			component.attachTo(bus);
		}
	}

	/**
	 * Accessor which returns the bus attribute of the instance.
	 * 
	 * @return Bus : bus attribute of the instance.
	 */
	public Bus bus() {
		return bus;
	}

	/**
	 * Method to run the cpu until it reaches the cycle specified.
	 * 
	 * @param cycles
	 *            : Cycle the cpu will reach (excluded) after method runs.
	 * 
	 * @throws IllegalArgumentException
	 *             if the argument specified is strictly inferior to the number of
	 *             cycles that has already been run.
	 * 
	 */
	public void runUntil(long cycles) {
 		Preconditions.checkArgument(cycles >= cycles());
		for (long i = cyclesSimulated; i < cycles; i++) {
			timer.cycle(i);
			lcdController.cycle(i);
			cpu.cycle(i);
		}
		cyclesSimulated = cycles;
	}

	/**
	 * Accessor for the attribute cpu.
	 * 
	 * @return The cpu attribute of the instance.
	 */
	public Cpu cpu() {
		return cpu;
	}

	/**
	 * Accessor to return the number of cycles which has been simulated.
	 * 
	 * @return The number of cycles simulated. Note: The cycles start at 0 hence the
	 *         cycles have been simulated up to return value - 1.
	 */
	public long cycles() {
		return cyclesSimulated;
	}

	/**
	 * Accessor for the timer of the instance.
	 *
	 * @return timer attribute of the instance.
	 */
	public Timer timer() {
		return timer;
	}
	
	/**
	 * Accessor for the LcdController of the instance.
	 * 
	 * @return LcdController attribute of the instance.
	 */
	public LcdController lcdController() {
		return lcdController;
	}
	
	/**
	 * Accessor for the Joypad attribute of the instance.
	 * 
	 * @return Joypad attribute of the instance.
	 */
	public Joypad joypad () {
		return joypad;
	}

}
