
package ch.epfl.gameboj.component.cpu;

import static ch.epfl.gameboj.Preconditions.*;

import java.util.Objects;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Public final and non instantiable class in charge of simulating the
 * functionalities of the ALU.
 * 
 * @author Oscar Pitcho (28825)
 * @author Nizar Ghandri (283161)
 */
public final class Alu {

	private final static int MAX_VALUE_VALUE_FLAGS = 0x1000000;

	private Alu() {};

	/**
	 * Enum representing the different flags with their respective indices used in
	 * the valueFlag format.
	 * 
	 * @author Oscar Pitcho (288225)
	 * @author Nizar Ghandri (283161)
	 */
	public enum Flag implements Bit {
		UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z
	}

	/**
	 * Enum representing the directions left and right.
	 * 
	 * @author Oscar Pitcho (288225)
	 * @author Nizar Ghandri (283161)
	 *
	 */
	public enum RotDir {
		LEFT, RIGHT
	}

	/**
	 * Method to create a mask corresponding to the values of the different ZNHC
	 * bits.
	 * 
	 * @param z
	 *            : Boolean, value of Z, true for 1, false for 0.
	 * 
	 * @param n
	 *            : Boolean, value of N, true for 1, false for 0.
	 * 
	 * @param h
	 *            : Boolean, value of H, true for 1, false for 0.
	 * 
	 * @param c
	 *            : Boolean, value of C, true for 1, false for 0.
	 * 
	 * @return Number in valueFlag format with value being 0, Flags ZNHC of the
	 *         arguments
	 */
	public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
		int zMask = z ? Flag.Z.mask() : 0;
		int nMask = n ? Flag.N.mask() : 0;
		int hMask = h ? Flag.H.mask() : 0;
		int cMask = c ? Flag.C.mask() : 0;
		return zMask | nMask | hMask | cMask;
	}

	/**
	 * Method to extract the value component of an int.
	 * 
	 * @param valueFlag
	 *            Number containing flags and value.
	 * 
	 * @return Value component of the argument.
	 * 
	 * @throws IllegalArgumentException
	 *             if the argument is negative or greater than 0x1000000 (included)
	 *             or the first 4 LSBs are not 0.
	 */
	public static int unpackValue(int valueFlags) {
		checkArgument(valueFlags >= 0 && valueFlags < MAX_VALUE_VALUE_FLAGS && Bits.clip(4, valueFlags) == 0);
		return Bits.extract(valueFlags, 8, 16);
	}

	/**
	 * Method to extract the flag component of an int.
	 * 
	 * @param valueFlag
	 *            Number containing flags and value.
	 * 
	 * @return flag component of the argument.
	 * 
	 * @throws IllegalArgumentException
	 *             if the argument is negative or greater than 0x1000000 (included)
	 *             or the first four LSBs are not 0.
	 */
	public static int unpackFlags(int valueFlags) {
		checkArgument(valueFlags >= 0 && valueFlags < MAX_VALUE_VALUE_FLAGS && Bits.clip(4, valueFlags) == 0);
		return Bits.clip(8, valueFlags);

	}

	/**
	 * Method used to add two 8 bits value with potentially an added carryIn.
	 * 
	 * @param l
	 *            First value .
	 * 
	 * @param r
	 *            Second value
	 * 
	 * @param c0
	 *            Addition will be made with a carry in if true.
	 * 
	 * @return The sum of the arguments l and r in the valueFlag format, value being
	 *         8 bits long. With flags Z0HC
	 * 
	 * @throws IllegalArgumentException
	 *             if either r or l is not an unsigned 8 bits number.
	 */
	public static int add(int l, int r, boolean c0) {
		checkBits8(l);
		checkBits8(r);
		int carryIn = Bits.set(0, 0, c0);
		int value = Bits.clip(8, l + r + carryIn);
		boolean z = value == 0;
		boolean h = (Bits.clip(4, l) + Bits.clip(4, r) + carryIn) > 0xF;
		boolean c = carry8Bits(l, r, carryIn);

		return packValueZNHC(value, z, false, h, c);
	}

	/**
	 * Method used to add two 8 bits value WITHOUT carry in.
	 * 
	 * @param l
	 *            First value.
	 * 
	 * @param r
	 *            Second value.
	 * 
	 * @return Sum of the two numbers in the valueFlag format, value being 8 bits
	 *         long. With flags Z0HC.
	 * 
	 * @throws IllegalArgumentException
	 *             if either l or r is not an unsigned 8 bits number.
	 */
	public static int add(int l, int r) {
		return add(l, r, false);
	}

	/**
	 * Method used to sum two 16 bits value, with flags being those of the sum of
	 * the 8 LSBs.
	 * 
	 * @param l
	 *            First value.
	 * 
	 * @param r
	 *            Second value.
	 * 
	 * @return Sum of the arguments in 16 bits in the valueFlag format, value being
	 *         16 bits long. With flags 00HC. The flags correspond to the sum of the
	 *         first 8 LSBs
	 * 
	 * @throws IllegalArgumentException
	 *             if either l or r is not an unsigned 16 bits number.
	 */
	public static int add16L(int l, int r) {
		checkBits16(l);
		checkBits16(r);
		boolean h = Bits.clip(4, l) + Bits.clip(4, r) > 0xF;
		boolean c = carry8Bits(Bits.clip(8, l), Bits.clip(8, r), 0);

		return packValueZNHC(Bits.clip(16, l + r), false, false, h, c);
	}

	/**
	 * Method used to sum two 16 bits value, with flags being those of the sum of
	 * the 8 MSBs.
	 * 
	 * @param l
	 *            First value.
	 * 
	 * @param r
	 *            Second value.
	 * 
	 * @return Sum of the arguments in 16 bits in the valueFlag format, value being
	 *         16 bits long. With flags 00HC. The flags correspond to the sum of the
	 *         first 8 MSBs
	 * 
	 * @throws IllegalArgumentException
	 *             if either l or r is not an unsigned 16 bits number.
	 */
	public static int add16H(int l, int r) {
		checkBits16(l);
		checkBits16(r);
		int carryIn = Bits.set(0, 0, Bits.clip(8, l) + Bits.clip(8, r) > 0xFF);
		boolean h = Bits.extract(l, 8, 4) + Bits.extract(r, 8, 4) + carryIn > 0xF;
		boolean c = carry8Bits(Bits.extract(l, 8, 8), Bits.extract(r, 8, 8), carryIn);

		return packValueZNHC(Bits.clip(16, l + r), false, false, h, c);

	}

	/**
	 * Method used to take the difference between the first and second argument
	 * potentially with a borrowIn
	 * 
	 * @param l
	 *            First value.
	 * 
	 * @param r
	 *            Second value, the one being subtracted form the first.
	 * 
	 * @param b0
	 *            Borrow in if true, no borrow in if false
	 * 
	 * @return The difference between two arguments with borrowIn in the valueFlag
	 *         format, value being 8 bits long. With flags Z1HC.
	 * 
	 * @throws IllegalArgumentException
	 *             if either l or r is not an unsigned 8 bits number.
	 */
	public static int sub(int l, int r, boolean b0) {
		checkBits8(l);
		checkBits8(r);
		int borrowIn = Bits.set(0, 0, b0);
		int value = Bits.clip(8, l - r - borrowIn);
		boolean z = Bits.clip(8, l - (r + borrowIn)) == 0;
		boolean h = Bits.clip(4, l) - borrowIn < Bits.clip(4, r);
		boolean c = l - borrowIn < r;
		return packValueZNHC(value, z, true, h, c);

	}

	/**
	 * Method used to take the difference between the first and second argument
	 * without borrow in.
	 * 
	 * @param l
	 *            First value.
	 * 
	 * @param r
	 *            Second value, the one being subtracted form the first.
	 * 
	 * @return The difference between two in the valueFlag format, value being 8
	 *         bits long. With flags Z1HC.
	 * 
	 * @throws IllegalArgumentException
	 *             if either l or r is not an unsigned 8 bits number.
	 */
	public static int sub(int l, int r) {
		return sub(l, r, false);
	}

	/**
	 * Method to transform binary number in valueFlag format to BCD number in
	 * valueFlag format.
	 * 
	 * @param value
	 *            8 bits number to be converted to BCD
	 * 
	 * @param n
	 *            Value of N flag.
	 * 
	 * @param h
	 *            Value of H flag.
	 * 
	 * @param c
	 *            Value of C flag.
	 * 
	 * @return The BCD equivalent of the value argument.
	 * 
	 * @throws IllegalArgumentException
	 *             if value is not an unsigned 8 bits number.
	 */
	public static int bcdAdjust(int value, boolean n, boolean h, boolean c) {
		checkBits8(value);
		boolean fixL = h || (!n && Bits.clip(4, value) > 9);
		boolean fixH = c || (!n && value > 0x99);
		int fix = 0x60 * Bits.set(0, 0, fixH) + 0x06 * Bits.set(0, 0, fixL);
		int va = n ? Bits.clip(8, value - fix) : Bits.clip(8, value + fix);
		boolean z = va == 0;
		return packValueZNHC(va, z, n, false, fixH);
	}

	/**
	 * Method to perform a bitwise AND between the two 8 bits number.
	 * 
	 * @param l
	 *            First value.
	 * 
	 * @param r
	 *            Second value.
	 * 
	 * @return Result of bitwise AND between two arguments in valueFlag format with
	 *         Z010.
	 * 
	 * @throws IllegalArgumentException
	 *             if either l or r (or both) is not an unsigned 8 bit number.
	 */
	public static int and(int l, int r) {
		checkBits8(r);
		checkBits8(l);
		int value = l & r;
		boolean z = value == 0;
		return packValueZNHC(value, z, false, true, false);
	}

	/**
	 * Method to perform a bitwise OR between the two 8 bits number.
	 * 
	 * @param l
	 *            First value.
	 * 
	 * @param r
	 *            Second value.
	 * 
	 * @return Result of bitwise OR in valueFlag format with Z000.
	 * 
	 * @throws IllegalArgumentException
	 *             if either l or r is not an unsigned 8 bits number.
	 */
	public static int or(int l, int r) {
		checkBits8(l);
		checkBits8(r);
		int value = l | r;
		boolean z = value == 0;
		return packValueZNHC(value, z, false, false, false);
	}

	/**
	 * Method to perform a bitwise XOR between the two 8 bits number.
	 * 
	 * @param l
	 *            First value.
	 * 
	 * @param r
	 *            Second value.
	 * 
	 * @return Result of bitwise XOR in valueFlag format with Z000.
	 * 
	 * @throws IllegalArgumentException
	 *             if either l or r is not an unsigned 8 bits number.
	 */
	public static int xor(int l, int r) {
		checkBits8(r);
		checkBits8(l);
		int value = l ^ r;
		boolean z = value == 0;
		return packValueZNHC(value, z, false, false, false);
	}

	/**
	 * Method to perform a single logical shift left on an 8 bits number.
	 * 
	 * @param value
	 *            Value being shifted.
	 * 
	 * @return Number in valueFlag format, value is the argument after the shift and
	 *         8 bits long. With flags Z00C, C being the value of the ejected bit.
	 *
	 * @throws IllegalArgumentException
	 *             if value is not an unsigned 8 bits number.
	 */
	public static int shiftLeft(int value) {
		checkBits8(value);
		boolean c = (value << 1) > 0xFF;
		value = Bits.clip(8, (value << 1));
		boolean z = value == 0;
		return packValueZNHC(value, z, false, false, c);
	}

	/**
	 * Method to perform a single arithmetic shift right on an 8 bits number.
	 * 
	 * @param value
	 *            Value being shifted.
	 * 
	 * @return Number in valueFlag format, value is the argument after the shift and
	 *         8 bits long. With flags Z00C, C being the value of the ejected bit.
	 * 
	 * 
	 * @throws IllegalArgumentException
	 *             if value is not an unsigned 8 bits number.
	 */
	public static int shiftRightA(int value) {
		checkBits8(value);
		boolean c = Bits.test(value, 0);
		boolean newMSB = value > 0x7F;
		value = value >>> 1;
		value = Bits.set(value, 7, newMSB);
		boolean z = value == 0;
		return packValueZNHC(value, z, false, false, c);
	}

	/**
	 * Method to perform a single logical shift right on an 8 bits number.
	 * 
	 * @param value
	 *            value being shifted.
	 * 
	 * @return Number in valueFlag format where the value is the argument after
	 *         being shifted and 8 bits long. With flags Z00C C being the value of
	 *         the ejected bit.
	 * 
	 * @throws IllegalArgumentException
	 *             if value is not an unsigned 8 bits number.
	 * 
	 */
	public static int shiftRightL(int value) {
		checkBits8(value);
		boolean c = Bits.test(value, 0);
		value = value >>> 1;
		boolean z = value == 0;
		return packValueZNHC(value, z, false, false, c);
	}

	/**
	 * Method to perform a single rotation left or right on an 8 bits number.
	 * 
	 * @param d
	 *            Instance of RotDir, defines the direction.
	 * 
	 * @param value
	 *            Value being rotated.
	 * 
	 * @return Number in valueFlag format with the value being argument after
	 *         rotation and 8 bits long. With flags Z00C C being the bit that
	 *         changed sides.
	 * 
	 * @throws IllegalArgumentException
	 *             if the value is not an unsigned 8 bits number.
	 */
	public static int rotate(RotDir d, int value) {
		checkBits8(value);
		boolean c = d == RotDir.RIGHT ? Bits.test(value, 0) : Bits.test(value, 7);
		int rotated = d == RotDir.RIGHT ? Bits.rotate(8, value, -1) : Bits.rotate(8, value, 1);
		boolean z = rotated == 0;
		return packValueZNHC(rotated, z, false, false, c);
	}

	/**
	 * Method to perform a rotation through carry in on an 8 bits number in a given
	 * direction.
	 * 
	 * @param d
	 *            Instance of RotDir: defines the direction.
	 * 
	 * @param value
	 *            Value being rotated.
	 * 
	 * @param c
	 *            Defines the initial value of the carry, true for 1 and false for
	 *            0. otherwise.
	 * 
	 * @return Number in valueFlag format, value being 8 bits long, where the value
	 *         is the argument after rotation WITH its carry (9 bits rotation). With
	 *         flags Z00C C being the value of the MSB (bit at index C) after the
	 *         rotation.
	 * 
	 * @throws IllegalArgumentExceptionepfl
	 *             mast if the argument is not an unsigned 8 bits number.
	 */
	public static int rotate(RotDir d, int value, boolean c) {
		checkBits8(value);
		int rotated = value | (Bits.set(0, 0, c) << 8);
		rotated = d == RotDir.LEFT ? Bits.rotate(9, rotated, 1) : Bits.rotate(9, rotated, -1);
		boolean flagC = Bits.test(rotated, 8);
		rotated = Bits.clip(8, rotated);
		boolean z = rotated == 0;
		return packValueZNHC(rotated, z, false, false, flagC);
	}

	/**
	 * Method to swap the 4 MSBs and LSBs of an 8 bits number,
	 * 
	 * @param value
	 *            Number whose MSBs and LSBs are swapped.
	 * 
	 * @return Number in value flag format, value being an 8 bits number which is
	 *         the argument with MSBs and LSBs swapped. With flags Z000.
	 * 
	 * @throws IllegalArgumentException
	 *             if the argument is not an unsigned 8 bits number.
	 */
	public static int swap(int value) {
		checkBits8(value);
		value = value >>> 4 | (Bits.clip(4, value) << 4);
		boolean z = value == 0;
		return packValueZNHC(value, z, false, false, false);
	}

	/**
	 * Method to test value of a given bit in an 8 bits number.
	 * 
	 * @param value
	 *            Number whose content is examined.
	 * 
	 * @param bitIndex
	 *            Index of bit which is examined.
	 * 
	 * @return Number in valueFlag format with value being 0, flags Z010 Z being one
	 *         if and only if the bit a the given index was 0.
	 * 
	 * @throws IllegalArgumentException
	 *             if the value is not an unsigned 8 bits number
	 */
	public static int testBit(int value, int bitIndex) {
		checkBits8(value);
		Objects.checkIndex(bitIndex, 8);
		boolean z = !Bits.test(value, bitIndex);
		return packValueZNHC(0, z, false, true, false);
	}

	// Method to combine an int and values of flag into a single valueFlag number.
	private static int packValueZNHC(int value, boolean z, boolean n, boolean h, boolean c) {
		return (value << 8) | maskZNHC(z, n, h, c);
	}
	
	private static boolean carry8Bits(int a, int b, int carry) {
		return a + b + carry > 0xFF;
	}

}
