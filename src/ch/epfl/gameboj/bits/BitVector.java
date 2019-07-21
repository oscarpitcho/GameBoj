package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import ch.epfl.gameboj.Preconditions;

/**
 * Public final immutable class which represents a bitVector of a fixed size.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class BitVector {

	private enum ProlongationType {
		WRAPPED, EXTENDED_ZEROES
	}

	private final int[] bitVector;

	/**
	 * Constructor to create a new bit vector of a given size will all 1 or all 0.
	 * 
	 * @param size
	 *            of the bitVector being created.
	 * 
	 * @param value
	 *            contents of bitVector will be all 1 for true, all 0 for false
	 * 
	 * @throws IllegalArgumentException
	 *             if the size is not a multiple of 32 or not strictly positive.
	 */
	public BitVector(int size, boolean value) {
		Preconditions.checkArgument(size % Integer.SIZE == 0 && size > 0);
		bitVector = new int[size / Integer.SIZE];
		if (value)
			Arrays.fill(bitVector, -1);
		else
			Arrays.fill(bitVector, 0);
	}

	/**
	 * Constructor to create a new BitVector filled with zeroes.
	 * 
	 * @param size
	 *            of the new bitVector.
	 * 
	 * @throws IllegalArgumentException
	 *             if the size is not a multiple of 32 or not strictly positive.
	 */
	public BitVector(int size) {
		this(size, false);
	}

	// Private constructor which takes array for coding purposes
	private BitVector(int[] bitVector) {
		Objects.requireNonNull(bitVector);
		this.bitVector = bitVector;
	}

	/**
	 * Method to obtain the size of the bitVector.
	 * 
	 * @return the number of individual bits in the bitVector.
	 */
	public int size() {
		return bitVector.length * Integer.SIZE;
	}

	/**
	 * Method to test the value of the bit at the specified index.
	 * 
	 * @param index
	 *            of bit which should be tested.
	 * 
	 * @return true if bit at index is 1, false if it is 0.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the specified index isn't contained in the bitVector.
	 */
	public boolean testBit(int index) {
		Objects.checkIndex(index, size());
		return Bits.test(bitVector[index / Integer.SIZE], index % Integer.SIZE);
	}

	/**
	 * Performs a negation of the instance this is called on.
	 * 
	 * @return A new instance of bitVector with all bits in the previous instance
	 *         having been negated.
	 */
	public BitVector not() {
		return binaryOperation(this, (x, y) -> ~x);
	}

	/**
	 * Method to perform a bitwise and between two bitVectors.
	 * 
	 * @param that
	 *            instance of BitVector which will be the second part of the
	 *            conjunction.
	 * 
	 * @return A new instance of BitVector which is the conjunction of the instance
	 *         this is called on and the instance given in argument.
	 * 
	 * @throws NullPointerException
	 *             if the argument is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if the argumentdoesn't have the same size as the instance.
	 */
	public BitVector and(BitVector that) {
		Objects.requireNonNull(that);
		Preconditions.checkArgument((this.size() == that.size()));
		return binaryOperation(that, (x, y) -> x & y);
	}

	/**
	 * Method to perform a bitwise or between two bitVectors.
	 * 
	 * @param that
	 *            instance of BitVector which will be the second part of the
	 *            conjunction.
	 * 
	 * @return A new instance of BitVector which is the disjunction of the instance
	 *         this is called on and the instance given in argument.
	 * 
	 * @throws NullPointerException
	 *             if the argument is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if the argumentdoesn't have the same size as the instance.
	 */
	public BitVector or(BitVector that) {
		Objects.requireNonNull(that);
		Preconditions.checkArgument((this.size() == that.size()));
		return binaryOperation(that, (x, y) -> x | y);
	}

	public BitVector shift(int distance) {
		int[] shifted = extract(-distance, size(), ProlongationType.EXTENDED_ZEROES);
		return new BitVector(shifted);
	}

	/**
	 * Method to obtain the extraction of the instance, when doing an infinite
	 * extension by 0, of a given size starting at the specified index.
	 * 
	 * @param index
	 *            at which the extraction should start.
	 * 
	 * @param size
	 *            of the extraction
	 * 
	 * @return new BitVector which is the result of the extraction
	 * 
	 * @throws IllegalArgumentException
	 *             if the size of the extraction is not a multiple of 32 or not
	 *             strictly positive.
	 */
	public BitVector extractZeroExtended(int index, int size) {
		return new BitVector(extract(index, size, ProlongationType.EXTENDED_ZEROES));
	}

	/**
	 * Method to obtain the extraction of the instance, when doing an infinite
	 * extension by wrapping, of a given size starting at the specified index.
	 * 
	 * @param index
	 *            at which the extraction should start.
	 * 
	 * @param size
	 *            of the extraction
	 * 
	 * @return new BitVector which is the result of the extraction
	 * 
	 * @throws IllegalArgumentException
	 *             if the size of the extraction is not a multiple of 32 or not
	 *             strictly positive.
	 */
	public BitVector extractWrapped(int index, int size) {
		return new BitVector(extract(index, size, ProlongationType.WRAPPED));
	}

	@Override
	public boolean equals(Object that) {
		return (that instanceof BitVector) && Arrays.equals(this.bitVector, ((BitVector) that).bitVector);

	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(bitVector);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int i = bitVector.length - 1; i >= 0; --i) {
			for (int j = 0; j < Integer.numberOfLeadingZeros(bitVector[i]); ++j)
				s.append("0");
			if (bitVector[i] != 0)
				s.append(Integer.toBinaryString(bitVector[i]));
		}
		return s.toString();
	}

	// Private method to perform an extraction of the specified type.
	private int[] extract(int index, int size, ProlongationType prolong) {
		Preconditions.checkArgument(size % Integer.SIZE == 0 && size > 0);
		int[] extracted = new int[size / Integer.SIZE];
		int shiftSize = Math.floorMod(index, Integer.SIZE);
		int reverseShiftSize = Integer.SIZE - shiftSize;
		for (int i = 0; i < size; i += Integer.SIZE) {
			int extendedArrayIndex = Math.floorDiv(i + index, Integer.SIZE);
			if (shiftSize == 0)
				extracted[i / Integer.SIZE] = ((prolong == ProlongationType.EXTENDED_ZEROES)
						&& (index + i < 0 || index + i >= size())) ? 0
								: bitVector[Math.floorMod(extendedArrayIndex, bitVector.length)];
			else
				extracted[i / Integer.SIZE] = (Bits.clip(shiftSize,
						get32(extendedArrayIndex + 1, prolong)) << reverseShiftSize)
						| Bits.extract(get32(extendedArrayIndex, prolong), shiftSize, reverseShiftSize);
		}
		return extracted;
	}

	// Private method to perform a specified binary operation between two
	// bitVectors.
	private BitVector binaryOperation(BitVector that, BinaryOperator<Integer> biOperator) {
		Preconditions.checkArgument(this.size() == that.size());
		int[] result = new int[this.bitVector.length];
		for (int i = 0; i < result.length; ++i)
			result[i] = biOperator.apply(this.bitVector[i], that.bitVector[i]);
		return new BitVector(result);
	}

	// The block of 32 bits in the infinite extension for the specified extension
	// and index.
	private int get32(int index, ProlongationType prolong) {
		if (prolong == ProlongationType.WRAPPED) 
			return bitVector[Math.floorMod(index, bitVector.length)];
		 else 
			return index < 0 || index >= bitVector.length ? 0 : bitVector[index];
		

	}

	/**
	 * Final Builder class to create a bitVector instance.
	 * 
	 * @author Oscar Pitcho (288225)
	 * @author Nizar Ghandri (283161)
	 */
	public final static class Builder {
		
		private static final int BYTE_MASK = 0xFF;
		private static final int BYTES_PER_INT = 4;
		
		private int[] bitVector;

		/**
		 * Constructor to create a new builder of the specified size.
		 * 
		 * @param size
		 *            of the builder, should be positive and a multiple of 32.
		 * 
		 * @throws IllegalArgumentException
		 *             if the size is not a multiple of 32 or the size is not strictly
		 *             positive.
		 */
		public Builder(int size) {
			Preconditions.checkArgument(size % Integer.SIZE == 0 && size > 0);
			bitVector = new int[size / Integer.SIZE];
		}

		/**
		 * Method to set an individual byte in the builder.
		 * 
		 * @param index
		 *            of the byte which should be set (there are 4 indices per int).
		 * 
		 * @param content
		 *            value at which the byte should be set.
		 * 
		 * @return the instance of the builder after the byte has been set.
		 * 
		 * @throws IllegalStateException
		 *             if the builder has already been built.
		 * 
		 * @throws IllegalArgumentException
		 *             if the content is not an 8 bits value.
		 * 
		 * @throws IndexOutOfBoundsException
		 *             if the invex given is not valid (negative or greater or equal to
		 *             size/4.
		 */
		public Builder setByte(int index, int content) {
			if (bitVector == null)
				throw new IllegalStateException();
			Preconditions.checkBits8(content);
			Objects.checkIndex(index, bitVector.length * BYTES_PER_INT);
			int byteStartIndex = (index % BYTES_PER_INT) * 8;
			int byteClearMask = ~(BYTE_MASK << byteStartIndex);
			bitVector[index / BYTES_PER_INT] = content << byteStartIndex | (byteClearMask & bitVector[index / BYTES_PER_INT]);
			return this;
		}

		/**
		 * Method to build the current builder.
		 * 
		 * @return A new instance of BitVector with the same argument as those set until
		 *         now.
		 * 
		 * @throws IllegalStateException
		 *             if the builder has already been built.
		 */
		public BitVector build() {
			if (bitVector == null)
				throw new IllegalStateException();
			int[] temp = Arrays.copyOf(bitVector, bitVector.length);
			bitVector = null;
			return new BitVector(temp);
		}
	}
}
