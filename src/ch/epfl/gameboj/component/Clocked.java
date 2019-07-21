
package ch.epfl.gameboj.component;

/**
 * Interface to be implemented by all components that are clocked i.e. follow
 * the pace of the clock.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri ((283161)
 */
public interface Clocked {

	/**
	 * Method to have the component to evolve by executing all the operations it
	 * should to during the cycle given in argument.
	 * 
	 * @param cycle
	 *            : index of cycle for which the component should do its
	 *            instructions.
	 */
	void cycle(long cycle);

}
