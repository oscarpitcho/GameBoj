
package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * Final class, implements Component, in charge of simulating the
 * BootRomController. Component in charge of reading in the BootRom and
 * redirecting to the cartridge once booting is done.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class BootRomController implements Component {

	private final Cartridge cartridge;
	private final Rom bootRom;
	private boolean booting;

	/**
	 * Constructor to create a bootRomController linked to a cartridge.
	 * 
	 * @param cartridge
	 *            : The cartridge with which the bootRomController will be linked.
	 * 
	 * @throws NullPointerException
	 *             if the cartridge in argument is null.
	 */
	public BootRomController(Cartridge cartridge) {
		Objects.requireNonNull(cartridge);
		this.cartridge = cartridge;
		bootRom = new Rom(BootRom.DATA);
		booting = true;
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		if (address >= AddressMap.BOOT_ROM_START && address < AddressMap.BOOT_ROM_END && booting) {
			return bootRom.read(address);
		} else
			return cartridge.read(address);
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits8(data);
		Preconditions.checkBits16(address);
		if (address == AddressMap.REG_BOOT_ROM_DISABLE && booting) {
			booting = false;
		} else {
			cartridge.write(address, data);
		}

	}

}
