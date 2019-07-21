
package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * Class that simulates the Rom (storage).
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class Rom {
	private final byte[] rom;

	/**
	 * Constructor instantiates rom with array passed in argument.
	 * 
	 * @param data
	 *            Array which will be used as the rom.
	 * 
	 * @throws NullPointerException
	 *             if data is null.
	 */
	public Rom(byte[] data) {
		rom = Arrays.copyOf(Objects.requireNonNull(data), data.length);
	}

	/**
	 * Accessor method for the size of the rom.
	 * 
	 * @return Size of the rom as int.
	 */
	public int size() {
		return rom.length;
	}

	public int read(int index) {
		Objects.checkIndex(index, rom.length);
		return Byte.toUnsignedInt(rom[index]);
	}

	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);
	}

}
