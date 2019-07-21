package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.ImageView;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Final class in charge of running the emulator with the display and keyboard.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 * 
 *
 */
public final class Main extends Application {
	/**
	 * Main method to run the emulator
	 * 
	 * @param args
	 *            should contain only a single argument, the name of the game to be
	 *            run.
	 * 
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}
	

	@Override
	public void start(Stage primaryStage) throws Exception {
		List<String> ROM = getParameters().getRaw();
		if (ROM.size() != 1) {
			System.exit(1);
		}
		GameBoy gameboy = new GameBoy(Cartridge.ofFile(new File(ROM.get(0))));
		ImageView image = new ImageView();
		
		image.setFitHeight(2 * LcdController.LCD_HEIGHT);
		image.setFitWidth(2 * LcdController.LCD_WIDTH);
		
		Map<KeyCode, Key> keyMap = new HashMap<>();
		keyMap.put(KeyCode.LEFT, Key.LEFT);
		keyMap.put(KeyCode.RIGHT, Key.RIGHT);
		keyMap.put(KeyCode.UP, Key.UP);
		keyMap.put(KeyCode.DOWN, Key.DOWN);
		Map<String, Key> stringMap = new HashMap<>();
		stringMap.put("A", Key.A);
		stringMap.put("B", Key.B);
		stringMap.put("S", Key.START);
		stringMap.put(" ", Key.SELECT);
		
		image.setOnKeyPressed(e -> {
			if (keyMap.containsKey(e.getCode()))
				gameboy.joypad().keyPressed(keyMap.get(e.getCode()));
			else if (stringMap.containsKey(e.getText().toUpperCase()))
				gameboy.joypad().keyPressed(stringMap.get(e.getText().toUpperCase()));
		});
		
		image.setOnKeyReleased(e -> {
			if (keyMap.containsKey(e.getCode()))
				gameboy.joypad().keyReleased(keyMap.get(e.getCode()));
			else if (stringMap.containsKey(e.getText().toUpperCase()))
				gameboy.joypad().keyReleased(stringMap.get(e.getText().toUpperCase()));
		});
		
		BorderPane display_layout = new BorderPane(image);
		display_layout.setCenter(image);
		Scene display = new Scene(display_layout);
		
		AnimationTimer timer = new AnimationTimer() {
			long start = System.nanoTime();

			@Override
			public void handle(long now) {
				long elapsed = now - start;
				gameboy.runUntil((long) (GameBoy.CYCLES_PER_NANOSECOND * elapsed));
				image.setImage(ImageConverter.convert(gameboy.lcdController().currentImage()));
			}
		};
		
		primaryStage.setScene(display);
		primaryStage.show();
		image.requestFocus();
		timer.start();
	}

}
