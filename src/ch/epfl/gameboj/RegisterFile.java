
package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Final generic class, with upper bound Register, which represents a bench of
 * registers.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class RegisterFile<E extends Register> {

	private final byte[] registerFile;

	/**
	 * Public constructor which creates the bench with appropriate size.
	 * 
	 * @param allRegs
	 *            : Array of the generic type whose length determines the size of
	 *            the bench.
	 */
	public RegisterFile(E[] allRegs) {
		registerFile = new byte[allRegs.length];
	}

	/**
	 * Accessor to return the content of one of the registers in the bench.
	 * 
	 * @param reg
	 *            : register whose value is returned.
	 * 
	 * @return Value stored in the register given as argument.
	 */
	public int get(E reg) {
		return Byte.toUnsignedInt(registerFile[reg.index()]);
	}

	/**
	 * Setter to modify the value of a register in the bench.
	 * 
	 * @param reg
	 *            : Register which should be modified,
	 * 
	 * @param newValue
	 *            : Value the register in argument should be set to.
	 * 
	 * @throws IllegalArgumentException
	 *             if the value in argument is not an unsigned 8 bits value.
	 */
	public void set(E reg, int newValue) {
		Preconditions.checkBits8(newValue);
		registerFile[reg.index()] = (byte) newValue;
	}

	/**
	 * Method to test the value of a bit in one of the registers.
	 * 
	 * @param reg
	 *            : register whose contents are examined.
	 * 
	 * @param b
	 *            : Argument of type bit which will give index of bit to test
	 * 
	 * @return true if the bit in question is 1 and false if it is 0.
	 */
	public boolean testBit(E reg, Bit b) {
		return Bits.test(get(reg), b);
	}

	/**
	 * Method to modify the value of a bit in one of the registers.
	 * 
	 * @param reg
	 *            : register which is to be modified.
	 * 
	 * @param bit
	 *            : Argument of type Bit which will give index of bit to be
	 *            modified.
	 * 
	 * @param newValue
	 *            : Value the bit should be set to, true = 1 and false = 0.
	 */
	public void setBit(E reg, Bit bit, boolean newValue) {
		set(reg, Bits.set(get(reg), bit.index(), newValue));
	}

}
