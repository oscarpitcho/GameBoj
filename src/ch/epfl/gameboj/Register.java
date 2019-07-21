package ch.epfl.gameboj;

/**
 * Interface for registers stored in a bench using an enumeration.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghardri (283161)
 */
public interface Register {

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

}
