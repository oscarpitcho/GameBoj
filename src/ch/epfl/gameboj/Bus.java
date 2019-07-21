package ch.epfl.gameboj;



import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * Final class that simulates the bus, component in charge of linking the
 * components to one another.
 * @author Nizar Ghandri
 * @author Oscar Pitcho (288225)
 */
public final class Bus {
	private final ArrayList<Component> components = new ArrayList<Component>();

	private final static int RAM_END = 0x80000;

	/**
	 * Method to add a component in the array of component of the instance.
	 * 
	 * @param component
	 *            Component being added to the bus.
	 * 
	 * @throws NullPointerException
	 *             if component is null.
	 */
	public void attach(Component component) {
		components.add(Objects.requireNonNull(component));
	}

	/**
	 * Method to access contents of an address.
	 * 
	 * @param address
	 *            Address which whose contents are returned.
	 * 
	 * @return Contents of address or NO_DATA if no component has been assigned this
	 *         address.
	 * 
	 * @throws IllegalArgumentException
	 *             if address is not an unsigned 16 bits number.
	 */
	public int read(int address) {
		Preconditions.checkBits16(address);
		Objects.checkIndex(address, RAM_END);
		for (Component component : components) {
			int data = component.read(address);
			if (data != Component.NO_DATA) {
				return data;
			}
		}
		return 0xFF;
	}

	/**
	 * Method to write byte of data at given address.
	 * 
	 * @param address
	 *            16 bits unsigned int: address being written to.
	 * 
	 * @param data
	 *            8 bits unsigned int: data being stored at the address in argument.
	 * 
	 * @throws IllegalArgumentException
	 *             if address is not unsigned 16 bits number or data is not an
	 *             unsigned 8 bits number.
	 */

	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);
		for (Component component : components) {
			component.write(address, data);
		}
	}
}