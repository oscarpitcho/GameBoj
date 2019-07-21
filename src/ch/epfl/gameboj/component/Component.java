
package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * Interface implemented by all components, declares basic functionalities of
 * components such as read and write,
 * 
 * @author Oscar Pitcho (288225)
 * 
 * @author Nizar Ghandri (283161)
 */
public interface Component {
	public static final int NO_DATA = 0x100;

	/**
	 * Abstract method used to access contents stored at address, if it is assigned
	 * to the component.
	 *
	 * @param address
	 *            memory accessed
	 * 
	 * @return Returns byte stored at the address, NO_DATA if the component has no
	 *         memory at this address.
	 * 
	 * @throws IllegalArgumentException
	 *             if argument is not an unsigned 16 bits value.
	 */
	int read(int address);

	/**
	 * Stores the value in the given address. Does nothing if the component has no
	 * memory mapped at this address or does not allow writing at this address.
	 * 
	 * @param address
	 *            Memory that is being written to.
	 * 
	 * @param data
	 *            Data written at the address.
	 * 
	 * @throws IllegalArgumentException
	 *             if address is not an unsigned 16 bits number or data is not an
	 *             unsigned 8 bits number
	 */
	void write(int address, int data);

	/**
	 * Attaches the instance of type Component to the bus given in argument.
	 * 
	 * @param bus
	 *            Bus which the component will be attached to.
	 */
	default void attachTo(Bus bus) {
		bus.attach(this);
	}

}
