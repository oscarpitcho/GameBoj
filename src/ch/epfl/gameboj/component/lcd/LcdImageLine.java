package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.bits.Bits;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

/**
 * Public final immutable class which represents a line of pixels.
 *
 * @author Oscar Pitcho
 * @author Nizar Ghandri
 */
public final class LcdImageLine {
	
	private static final int ORIGINAL_PALETTE = 0b11100100;
	private static final int NBR_COLORS = 4;

	private final BitVector msb;
	private final BitVector lsb;
	private final BitVector opacity;
	

	/**
	 * Constructor to generate the line from the three bitVectors given in argument.
	 *
	 * @param msb
	 *            bitVector which represents the msbs of the bits coding the color.
	 *
	 * @param lsb
	 *            bitVector which represents the lsbs of the bits coding the color.
	 *
	 * @param opacity
	 *            bitVector giving the opacity of each pixel.
	 *
	 * @throws IllegalArgumentException
	 *             if the three argument do not have the same size.
	 */
	public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
		Preconditions.checkArgument(msb.size() == lsb.size() && lsb.size() == opacity.size());
		this.msb = msb;
		this.lsb = lsb;
		this.opacity = opacity;
	}

	/**
	 * Accessor to return the size of the the lcdLine.
	 *
	 * @return the size of the lcdLine i.e. the number of pixels which it
	 *         represents.
	 */
	public int size() {
		return msb.size();
	}

	/**
	 * Accessor returning the bitVector of the msb.
	 *
	 * @return the bitVector of the msb.
	 */
	public BitVector getMsb() {
		return msb;
	}

	/**
	 * Accessor returning the bitVector of the lsb.
	 *
	 * @return the bitVector of the lsb.
	 */
	public BitVector getLsb() {
		return lsb;
	}

	/**
	 * Accessor returning the bitVector of the opacity.
	 *
	 * @return the bitVector of the opacity.
	 */
	public BitVector getOpacity() {
		return opacity;
	}

	/**
	 * Method to perform a logical shift on an instance of lcdImageLine. This shifts
	 * the given distance in the same direction.
	 *
	 * @param distance
	 *            of the shift, positive for left shifts and negative for right
	 *            shifts,
	 *
	 * @return A new instance of LcdImageLine after having performed the shift.
	 */
	public LcdImageLine shift(int distance) {
		return new LcdImageLine(msb.shift(distance), lsb.shift(distance), opacity.shift(distance));
	}

	/**
	 * Method to perform a wrapped extraction on an instance of LcdImageLine.
	 *
	 * @param index
	 *            at which the extraction should start.
	 *
	 * @param size
	 *            of the extraction.
	 *
	 * @return A new LcdImageLine after having performed the wrapped extraction.
	 *
	 * @throws IllegalArgumentException
	 *             if the size is not a multiple of 32 or the size is not strictly
	 *             positive.
	 */
	public LcdImageLine extractWrapped(int index, int size) {
		return new LcdImageLine(msb.extractWrapped(index, size), lsb.extractWrapped(index, size),
				opacity.extractWrapped(index, size));
	}

	/**
	 * Method to compose two LcdImageLines using the opacity of the one given in
	 * argument. At pixels where the opacity is one, it takes the value of the
	 * argument, where it is 0 it takes the value of the instance.
	 *
	 * @param above
	 *            Other LcdImageLine which will be used for the composition.
	 *
	 * @return A new LcdImageLine which is the result of the composition.
	 *
	 * @throws NullPointerException
	 *             if the argument is null.
	 *
	 * @throws IllegalArgumentException
	 *             if the argument and the instance this is called on do not have
	 *             the same size.
	 */
	public LcdImageLine below(LcdImageLine above) {
		return below(above, above.opacity);
	}

	/**
	 * Method to compose two LcdImageLines using for opacity the second argument. At
	 * pixels where the opacity is one, it takes the value of the argument above,
	 * where it is 0 it takes the value of the instance.
	 *
	 * @param above
	 *            Other LcdImageLine which will be used for the composition.
	 *
	 * @return A new LcdImageLine which is the result of the composition.
	 *
	 * @throws NullPointerException
	 *             if either of the arguments is null.
	 *
	 * @throws IllegalArgumentException
	 *             if the arguments and the instance don't have all the same size.
	 */
	public LcdImageLine below(LcdImageLine above, BitVector opacity) {
		Objects.requireNonNull(above);
		Objects.requireNonNull(opacity);
		Preconditions.checkArgument(this.size() == above.size() && this.size() == opacity.size());
		BitVector newMsb = (opacity.and(above.msb)).or((opacity.not()).and(this.msb));
		BitVector newLsb = (opacity.and(above.lsb)).or((opacity.not()).and(this.lsb));
		return new LcdImageLine(newMsb, newLsb, (this.opacity.or(opacity)));
	}

	/**
	 * Method to perform a mapping of the colors in the instance this is called on.
	 *
	 * @param colorPalette
	 *            8 bits int which represents the function that rules the mapping.
	 *            Each pair of bits with increasing index dictates to which color
	 *            the color of corresponding index should be mapped to.
	 *
	 * @return A new LcdImageLine after the mapping has been done.
	 */
	public LcdImageLine mapColors(int colorPalette) {
		Preconditions.checkBits8(colorPalette);
		if (colorPalette == ORIGINAL_PALETTE)
			return this;
		BitVector newMsb = new BitVector(size());
		BitVector newLsb = new BitVector(size());
		for (int i = 0; i < NBR_COLORS; ++i) {
			BitVector mask = (Bits.test(i, 0) ? lsb : lsb.not()).and(Bits.test(i, 1) ? msb : msb.not());
			if (Bits.test(colorPalette, 2 * i))
				newLsb = newLsb.or(mask);
			if (Bits.test(colorPalette, 2 * i + 1))
				newMsb = newMsb.or(mask);
		}
		return new LcdImageLine(newMsb, newLsb, opacity);
	}

	/**
	 * Method to combine the instance with another LcdImageLine of the same size.
	 * 
	 * @param index
	 *            (included in the first) where the line begins.
	 * 
	 * @param that
	 *            The second instance that is used in the join.
	 * 
	 * @return A new LcdImageLine of the same size with lsb, msb and opacity all
	 *         joined at the specified index.
	 */
	public LcdImageLine join(int index, LcdImageLine that) {
		Objects.requireNonNull(that);
		Preconditions.checkArgument(this.size() == that.size());
		BitVector newMsb = cutAndGet(msb, that.msb, index);
		BitVector newLsb = cutAndGet(lsb, that.lsb, index);
		BitVector opacity = cutAndGet(this.opacity, that.opacity, index);
		return new LcdImageLine(newMsb, newLsb, opacity);

	}

	@Override
	public boolean equals(Object that) {
		return that instanceof LcdImageLine && msb.equals(((LcdImageLine) that).msb)
				&& lsb.equals(((LcdImageLine) that).lsb) && opacity.equals(((LcdImageLine) that).opacity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(msb, lsb, opacity);
	}

	// Private method to make the equivalent of a join between BitVectors.
	private BitVector cutAndGet(BitVector firstPart, BitVector secondPart, int index) {
		BitVector mask = new BitVector(size(), true).shift(index);
		return firstPart.and(mask.not()).or(secondPart.and(mask));
	}

	/**
	 * Final builder class to build an instance of LcdImageLine.
	 * 
	 * @author Oscar Pitcho
	 * @author Nizar Ghandri
	 */
	public final static class Builder {

		private BitVector.Builder msb;
		private BitVector.Builder lsb;

		/**
		 * Constructor to create a builder of the given size.
		 * 
		 * @param size
		 *            of the LcdImageLine which will be built.
		 * 
		 * @throws IllegalArgumentException
		 *             if the size is not a multiple of 32 or the size is not strictly
		 *             positive.
		 */
		public Builder(int size) {
			Preconditions.checkArgument(Math.floorMod(size, Integer.SIZE) == 0 && size >= 0);
			this.msb = new BitVector.Builder(size);
			this.lsb = new BitVector.Builder(size);
		}

		/**
		 * Method to set the bytes of lsb and msb at a given index *
		 * 
		 * @param index
		 *            of the bytes which are set (note there are 4 valid indices per
		 *            int).
		 * 
		 * @param msb
		 *            value the msb at specified index will take.
		 * 
		 * @param lsb
		 *            value the lsb will take at the specified index.
		 * 
		 * @return The instance being built.
		 * 
		 * @throws IndexOutOfBoundsException
		 *             if the specified index is not valid.
		 * 
		 * @throws IllegalArgumentException
		 *             if either lsb or msb is not an 8 bits value.
		 * 
		 * @throws IllegalStateException
		 *             if the builder has already been built.
		 */
		public Builder setBytes(int index, int msb, int lsb) {
			Preconditions.checkBits8(lsb);
			Preconditions.checkBits8(msb);
			this.msb.setByte(index, msb);
			this.lsb.setByte(index, lsb);
			return this;
		}

		/**
		 * Method to build a new instance of LcdImageLine.
		 * 
		 * @return Returns a new instance of LcdImageLine using the contents of the
		 *         builder.
		 * 
		 * @throws IllegalStateException
		 *             if the builder has already been built.
		 */
		public LcdImageLine build() {
			BitVector a = msb.build();
			BitVector b = lsb.build();
			return new LcdImageLine(a, b, a.or(b));
		}
	}
}
