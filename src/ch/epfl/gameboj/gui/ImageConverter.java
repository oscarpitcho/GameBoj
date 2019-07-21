package ch.epfl.gameboj.gui;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * Final class in charge of converting the image of the LcdController in a
 * JavaFX image.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 *
 */
public final class ImageConverter {

	private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF_FF, 0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00 };

	/**
	 * Public method in charge of converting an LcdImage into a JavaFX image.
	 * 
	 * @param image
	 *            LcdImage to be converted into a JavaFX image.
	 * 
	 * @return The converted LcdImage.
	 * 
	 * @throws NullPointerException
	 *             if the argument is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if the image to be converted is not of size LCD_HEIGHT and
	 *             LCD_WIDTH.
	 */
	public static javafx.scene.image.Image convert(LcdImage image) {
		Objects.requireNonNull(image);
		Preconditions.checkArgument(
				image.getHeight() == LcdController.LCD_HEIGHT && image.getWidth() == LcdController.LCD_WIDTH);
		WritableImage imageToShow = new WritableImage(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT);
		PixelWriter tool = imageToShow.getPixelWriter();
		for (int y = 0; y < imageToShow.getHeight(); ++y) {
			for (int x = 0; x < imageToShow.getWidth(); ++x) {
				tool.setArgb(x, y, COLOR_MAP[image.get(x, y)]);
			}
		}
		return imageToShow;
	}
}
