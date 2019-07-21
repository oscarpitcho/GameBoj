
package ch.epfl.gameboj.bits;

/**
 * Interface for enums which represent bits.
 * 
 * @author Oscar Pitcho (288225).
 * @author Nizar Ghandri (283161)
 */
public interface Bit {

	/**
	 * Abstract method that exists by default in enum types which will implement the
	 * interface.
	 * 
	 * @return The index of the instance in the enumeration.
	 */
	int ordinal();

	/**
	 * Method to obtain index of instance in the enumeration.
	 * 
	 * @return The index of the instance in the enumeration.
	 */
	default int index() {
		return ordinal();
	}

	/**
	 * Creates an int mask characteristic of the instance this is called on.
	 * 
	 * @return  int with a 1 at position corresponding to index
	 *         of instance.
	 */
	default int mask() {
		return Bits.mask(this.index());

	}
}
