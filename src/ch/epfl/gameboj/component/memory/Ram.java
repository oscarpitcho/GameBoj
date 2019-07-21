
package ch.epfl.gameboj.component.memory;


import ch.epfl.gameboj.Preconditions;

/**
 * Final class that simulates the Ram component. In charge of providing a Random
 * access and reusable storage (possible to rewrite contents).
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class Ram {
	private final byte[] ram;

	public Ram(int size) {
		Preconditions.checkArgument(size >= 0);
		ram = new byte[size];
	}

	/**
	 * Public accessor method for the size of the ram.
	 * 
	 * @return Size of the ram.
	 */
	public int size() {
		return ram.length;
	}

	/**
	 * Allows accessing data stored at a given address.
	 * 
	 * @param index
	 *            Index of desired address.
	 * 
	 * @return Content of ram at the specified address as an unsigned 8 bits int.
	 * 
	 * @throws IndexoutofBoundException
	 *             if index is negative or bigger or equal to the ram size.
	 * 
	 * @throws IllegalArgumentException
	 *             if index is not a 16 bits unsigned int.
	 */

	public int read(int index) {
		return Byte.toUnsignedInt(ram[index]);
	}

	/**
	 * Writes the argument (value) at address (index).
	 * 
	 * @param index
	 *            Index at which the data should be written.
	 * 
	 * @param value
	 *            Data that will be stored as a signed byte in the Ram.
	 * 
	 * @throws IllegalArgumentExceotion
	 *             if the value is not an unsigned 8 bits number.or the index is not
	 *             an unsigned 16 bits value.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is not present in the ram.
	 */

	public void write(int index, int value) {
		Preconditions.checkBits8(value);
		ram[index] = (byte) value;
	}

}