
package ch.epfl.gameboj.component.memory;

import java.util.Objects;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * Final class, implements Component, which provides access to a Ram which has
 * been given a portion of the address field
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class RamController implements Component {

	private final Ram ram;
	private final int startAddress;
	private final int endAddress;

	/**
	 * Constructor used to create a ram controller for a ram.
	 * 
	 * @param ram
	 *            Ram which the intance will manage.
	 * 
	 * @param startAddress
	 *            16 bits unsigned int: Address at which ram will begin (included).
	 * 
	 * @param endAddress
	 *            16 bits unsigned int: Address at which ram will end (excluded).
	 * 
	 * @throws NullPointerException
	 *             if ram is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if startAddress or endAddress (or both) is not an unsigned 16
	 *             bits number or
	 */
	public RamController(Ram ram, int startAddress, int endAddress) {
		Objects.requireNonNull(ram);
		Preconditions.checkBits16(startAddress);
		Preconditions.checkBits16(endAddress);
		Preconditions.checkArgument(endAddress - startAddress <= ram.size());
		Preconditions.checkArgument(startAddress <= endAddress);
		this.ram = ram;
		this.startAddress = startAddress;
		this.endAddress = endAddress;
	}

	/**
	 * Constructors allows allocating all the addresses to the ram controller from
	 * startAdress till the last address
	 * 
	 * @param ram
	 *            Ram with which the RamController is attached.
	 * 
	 * @param startAddress
	 *            Address at which the RamController begins (included).
	 */
	public RamController(Ram ram, int startAddress) {
		this(ram, startAddress, startAddress + ram.size());
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		if (address < startAddress || address >= endAddress)
			return Component.NO_DATA;
		return ram.read(address - startAddress);
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits8(data);
		Preconditions.checkBits16(address);
		if (address >= startAddress && address < endAddress) {
			ram.write(address - startAddress, data);
		}
	}

}