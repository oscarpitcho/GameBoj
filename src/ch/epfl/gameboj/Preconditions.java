
package ch.epfl.gameboj;

/**
 * Interface
 * 
 * Various static methods for ensuring program runs only with valid arguments.
 * 
 * @author: Oscar Pitcho (288225)
 * @author: Nizar Ghandri (283161)
 */
public interface Preconditions {

	/**
	 * Method to ensure statement is true or throw error.
	 * 
	 * @param b
	 *            Verified statement
	 * 
	 * @throws IllegalArgumentException
	 *             if given argument is false
	 */
	public static void checkArgument(boolean b) {
		if (!b) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Method used to check if the argument can be written as an unsigned byte.
	 * 
	 * @param number
	 *            The verified value
	 * 
	 * @return The argument.
	 * 
	 * @throws IllegalArgumentException
	 *             if argument cannot be written as unsigned 8 bits number.
	 */
	public static int checkBits8(int number) {
		checkArgument(!(number > 0xFF|| number < 0));
		return number;
	}

	/**
	 * Method used to check if the argument can be written using a 16 bits unsigned
	 * binary number.
	 * 
	 * @param number
	 *            The verified value
	 * 
	 * @return The argument.
	 * 
	 * @throws IllegalArgumentException
	 *             if argument cannot be written as unsigned 16 bits number.
	 */
	public static int checkBits16(int number) {
		checkArgument(!(number > 0xFFFF || number < 0));
		return number;
	}

}
