
package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Final class, implements Comonent, which represents the MBC0. A component
 * which provides the link between the Cartridge and the Rom stored on it.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class MBC0 implements Component {

	private final Rom rom;
	private static final int ROM_SIZE = 0x8000;

	/**
	 * Constructor to create an MBC0.
	 * 
	 * @param rom
	 *            Rom which the instance will be the controller of.
	 * 
	 * @throws NullPointerException
	 *             if the rom given in argument is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if the rom is not of size ROM_SIZE.
	 */
	public MBC0(Rom rom) {
		Objects.requireNonNull(rom);
		Preconditions.checkArgument(rom.size() == ROM_SIZE);
		this.rom = rom;
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		return address < ROM_SIZE ? rom.read(address) : NO_DATA;
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);
	}

}
