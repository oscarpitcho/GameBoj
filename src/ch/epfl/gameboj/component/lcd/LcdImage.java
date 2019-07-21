package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * Final class representing an image displayed by the LcdScreen.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class LcdImage {

	private final int height;
	private final int width;
	private final List<LcdImageLine> image;

	/**
	 * Public constructor to build and LcdImage with given dimensions form a list of
	 * LcdImageLine
	 * 
	 * @param height
	 *            of the image.
	 * 
	 * @param width
	 *            of the image.
	 * 
	 * @param image
	 *            List of LcdimageLine, each representing the a row of pixels.
	 * 
	 * @throws NullPointerException
	 *             : if the list is null, if the height or the width are 0 or
	 *             negative, if the size of the list is not equal to height.
	 */
	public LcdImage(int height, int width, List<LcdImageLine> image) {
		Objects.requireNonNull(image);
		Preconditions.checkArgument(height > 0 && width > 0);
		Preconditions.checkArgument(image.size() == height);
		for (LcdImageLine i : image) {
			if (i.size() != width)
				throw new IllegalArgumentException();
		}
		this.height = height;
		this.width = width;
		this.image = Collections.unmodifiableList(new ArrayList<>(image));
	}

	/**
	 * Accessor to return the height attribute of the instance.
	 * 
	 * @return the height attribute of the instance.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Accessor to return the width attribute of the instance.
	 * 
	 * @return the width attribute of the instance.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Method to access the color stored at a particular pixes
	 * 
	 * @param x
	 *            coordinate of the pixel
	 * 
	 * @param y
	 *            coordinate of the pixel
	 * 
	 * @return The value of the color at the desired position.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the given position is not contained in the image.
	 */
	public int get(int x, int y) {
		Objects.checkIndex(x, width);
		Objects.checkIndex(y, height);
		LcdImageLine line = image.get(y);
		return (Bits.set(0, 0, line.getLsb().testBit(x)) | Bits.set(0, 1, line.getMsb().testBit(x)));
	}

	@Override
	public int hashCode() {
		return Objects.hash(image.hashCode(), width, height);
	}

	@Override
	public boolean equals(Object that) {
		return that instanceof LcdImage && this.width == ((LcdImage) that).width
				&& this.height == ((LcdImage) that).height && this.image.equals(((LcdImage) that).image);
	}

	/**
	 * Final static builder class of the LcdImage.
	 * 
	 * @author Oscar Pitcho (288225)
	 * @author Nizar Ghandri (283161)
	 */
	public static final class Builder {
		private final int height;
		private final int width;
		private final List<LcdImageLine> image;

		/**
		 * Constructor to create a new builder for an image of given width and size.
		 * 
		 * @param height
		 *            of the image to be built.
		 * 
		 * @param width
		 *            of the image to be built.
		 * 
		 * @throws IllegalArgumentException
		 *             if either height or width is less or equal to 0.
		 */
		public Builder(int height, int width) {
			Preconditions.checkArgument(height > 0 && width > 0);
			this.height = height;
			this.width = width;
			image = new ArrayList<>();
			image.addAll(Collections.nCopies(height, new LcdImageLine(new BitVector(width, false),
					new BitVector(width, false), new BitVector(width, false))));
		}

		/**
		 * Method to assign an LcdImageLine to one of the rows of the LcdImage.
		 * 
		 * @param index
		 *            of the row which is set.
		 * 
		 * @param i
		 *            : LcdLine set at the specified index.
		 * 
		 * @return the instance of the builder after having added the LcdImageLine.
		 * 
		 * @throws NullPointerException
		 *             i is null.
		 * 
		 * @throws IndexOutOfBoundsException
		 *             is index is not between 0 (included) and height (excluded).
		 * 
		 * @throws IllegalArgumentException
		 *             if the LcdImageLine's size is not equal to the width of the image being built. 
		 */
		public Builder setLine(int index, LcdImageLine i) {
			Objects.requireNonNull(i);
			Objects.checkIndex(index, height);
			Preconditions.checkArgument(i.size() == width);
			image.set(index, i);
			return this;
		}

		/**
		 * Method to build the instance of the builder and return the LcdImage.
		 * 
		 * @return the new LcdImage built through the builder.
		 */
		public LcdImage build() {
			return new LcdImage(height, width, image);
		}
	}
}
