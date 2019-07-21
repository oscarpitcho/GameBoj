
package ch.epfl.gameboj.bits;

import java.util.Objects;
import ch.epfl.gameboj.Preconditions;

/**
 * Public final and non instantiable class that provides static methods for bit
 * manipulation.
 *
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 *
 */
public final class Bits {
	
	private static int[] helper = new int[] { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0,
			0x70, 0xF0, 0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58, 0xD8, 0x38, 0xB8, 0x78,
			0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4, 0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4,
			0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C, 0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC, 0x02,
			0x82, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2, 0x0A, 0x8A,
			0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA, 0x06, 0x86, 0x46,
			0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16, 0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E, 0xCE,
			0x2E, 0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E, 0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21,
			0xA1, 0x61, 0xE1, 0x11, 0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49, 0xC9, 0x29, 0xA9,
			0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9, 0x79, 0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65,
			0xE5, 0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D, 0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED,
			0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD, 0x7D, 0xFD, 0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3, 0x13,
			0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73, 0xF3, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B, 0x9B,
			0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07, 0x87, 0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57,
			0xD7, 0x37, 0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F, 0xEF, 0x1F, 0x9F, 0x5F, 0xDF,
			0x3F, 0xBF, 0x7F, 0xFF, };

	private Bits() {};

	/**
	 * Returns a masked int with 1 at the position of argument.
	 *
	 * @param index
	 *            : Index of the one in the masked bit.
	 *
	 * @return Masked int with a single with a one, at given index.
	 *
	 * @throws IndexOutOfBoundsException
	 *             if index is not between 0 (included) and 32 (excluded).
	 */
	public static int mask(int index) {
		Objects.checkIndex(index, Integer.SIZE);
		return 1 << index;
	}

	/**
	 * Method to obtain the binary value of a number at given index
	 *
	 * @param bits
	 *            Value whose contents are analyzed.
	 *
	 * @param index
	 *            Index of bit that is examined.
	 *
	 * @return boolean Value of bits at index, true for 1 and false for 0.
	 *
	 * @throws IndexOutOfBoundsException
	 *             if index is not between 0 (included) and 32 (excluded).
	 *
	 */
	public static boolean test(int bits, int index) {
		Objects.checkIndex(index, Integer.SIZE);
		return !((bits & mask(index)) == 0);
	}

	/**
	 * Method to obtain the binary value of a int at the position of the index of
	 * bit.
	 *
	 * @param bits
	 *            Value whose contents are analyzed.
	 *
	 * @param bit
	 *            Instance of type Bit, index of examined bit is given by the index
	 *            of the argument bit.
	 *
	 * @return boolean value of bits at index, true for 1 and false for 0.
	 *
	 * @throws IndexOutOfBoundsException
	 *             if index of bit is not between 0 (included) and 32 (excluded).
	 *
	 */
	public static boolean test(int bits, Bit bit) {
		return test(bits, bit.index());
	}

	/**
	 * Method to set the value of a bit at a given position.
	 *
	 * @param bits
	 *            Value which will be modified.
	 *
	 * @param index
	 *            index of bit in argument bits which is set.
	 *
	 * @param newValue
	 *            bit at given index set at 1 if true, 0 if false.
	 *
	 * @return argument bits after the bit at specified index has been set.
	 *
	 * @throws IndexOutOfBoundsException
	 *             if index is not between 0 (included) and 32 (excluded).
	 */
	public static int set(int bits, int index, boolean newValue) {
		Objects.checkIndex(index, Integer.SIZE);
		int mask = mask(index);
		bits = bits & ~mask;
		return newValue ? bits | mask : bits;
	}

	/**
	 * Method to return the first size LSBs.
	 *
	 * @param size
	 *            Number of bits that will be returned
	 *
	 * @param bits
	 *            Value which which will be cut.
	 *
	 * @return Number consisting of the first size bits of the argument bits.
	 *
	 * @throws IllegalArgumentException
	 *             if size is not between 0 (included) and 32 (included).
	 */
	public static int clip(int size, int bits) {
		Preconditions.checkArgument(size >= 0);
		Preconditions.checkArgument(size <= Integer.SIZE);
		return size == Integer.SIZE ? bits : ((mask(size) - 1) & bits);
	}

	/**
	 * Method to extract subsequence of bits of a binary number.
	 *
	 * @param bits
	 *            Value from which the subsequence is extracted.
	 *
	 * @param start
	 *            Index at which the subsequence begins.
	 *
	 * @param size
	 *            Size of the subsequence that is extracted.
	 *
	 * @return 32 Subsequence of bits contained between start (included) and start +
	 *         size (excluded).
	 *
	 * @throws IndexOutOfBoundsException
	 *             if start < 0 or if start + size >= 32.
	 */

	public static int extract(int bits, int start, int size) {
		Objects.checkFromIndexSize(start, size, Integer.SIZE);
		int clipped = clip(size + start, bits) >>> start;
		return clipped;
	}

	/**
	 * Method to perform a circular shift on a size bits value.
	 *
	 * @param size
	 *            Size of the number on which the rotation occurs, without leading
	 *            zeroes.
	 *
	 * @param bits
	 *            Number on which the rotation occurs.
	 *
	 * @param distance
	 *            Number of rotations, negative for right rotations and positive for
	 *            left rotations.
	 *
	 * @return The argument bits after having gone through the rotations.
	 *
	 * @throws IllegalArgumentException
	 *             if the size is not between 1 (included) and 32 (included).
	 *
	 * @throws IllegalArgumentException
	 *             if the argument bits is not a number of size bits (potentially
	 *             with leading zeroes).
	 *
	 */
	public static int rotate(int size, int bits, int distance) {
		Preconditions.checkArgument(size >= 1 && size <= Integer.SIZE);
		Preconditions.checkArgument(clip(size, bits) == bits);
		int rotationDistance = Math.floorMod(distance, size);
		int newbits = clip(size, bits) << (rotationDistance) | clip(size, bits) >>> (size - rotationDistance);
		return clip(size, newbits);
	}

	/**
	 * Method to return complement of 8 bits number.
	 *
	 * @param bits
	 *            Number whose complement is taken.
	 *
	 * @return Complement of argument, only on the first 8 bits, following are
	 *         zeroes..
	 *
	 * @throws IllegalArgumentException
	 *             if bits is not an unsigned 8 bits number.
	 */
	public static int complement8(int bits) {
		Preconditions.checkBits8(bits);
		return clip(8, ~bits);
	}

	/**
	 * Method to merge two 8 bits sequences in one 16 bits number (unsigned).
	 *
	 * @param highB
	 *            Number which will form 8 MSBs of return value.
	 *
	 * @param lowB
	 *            Number which will form 8 LSBs of return value.
	 *
	 * @return 16 bits number formed by merging the two arguments.
	 *
	 * @throws IllegalArgumentException
	 *             if either highB or lowB (or both) is not an unsigned 8 bits
	 *             number.
	 */
	public static int make16(int highB, int lowB) {
		Preconditions.checkBits8(highB);
		Preconditions.checkBits8(lowB);
		highB = highB << 8;
		return highB | lowB;
	}

	/**
	 * Method to extend 8 bits number into signed 32 bits number.
	 *
	 * @param b
	 *            Number which is extended to 32 bits.
	 *
	 * @return A 32 bits number whose value on the 8th bit has been copied into the
	 *         remaining.
	 *
	 * @throws IllegalArguemntException
	 *             is not an unsigned 8 bits number.
	 */
	public static int signExtend8(int b) {
		Preconditions.checkBits8(b);
		int returnV = (int) (byte) b;
		return returnV;
	}

	/**
	 * Method to reverse an 8 bits number.
	 *
	 * @param Number
	 *            which will be reversed.
	 *
	 * @return The argument after having been reversed
	 *
	 * @throws IllegalArgumentException
	 *             if b is not an unsigned 8 bits number.
	 *
	 */
	public static int reverse8(int b) {
		Preconditions.checkBits8(b);
		return helper[b];
	}

}
