
package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Final class, implements Component, which represents a cartridge of the
 * GameBoy on which a Rom is tored.
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar Ghandri (283161)
 */
public final class Cartridge implements Component {

	private final Component mbc1;
	private final static int [] RAM_SIZES = new int [] {
			0, 2_048, 8_192, 32_768
	};

	private final static int CARTRIDGE_TYPE_ADDRESS = 0x147;
	private final static int RAM_SIZES_ADDRESS = 0x149;

	// Private constructor used in the method .cartridgeOfFile.
	private Cartridge(Component component) {
		this.mbc1 = component;
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		return mbc1.read(address);
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);
		mbc1.write(address, data);
	}

	/**
	 * Method to create a new cartridge from a file given in argument.
	 * 
	 * @param file
	 *            The file whose contents are converted into a rom.
	 * 
	 * @return A new cartridge with a mbcoController that is connected to the rom
	 *         with the contents of the file.
	 * 
	 * @throws IOException
	 *             if the file given in argument cannot be found.
	 * 
	 * @throws IllegalArgumentException
	 *             if the cartridge is not of the correct type.
	 */
	public static Cartridge ofFile(File file) throws IOException, IllegalArgumentException {
		try (InputStream dataFromFile = new FileInputStream(file)) {
			byte[] romContents = dataFromFile.readAllBytes();
			Component mbcXController;
			if(romContents[CARTRIDGE_TYPE_ADDRESS] == 0)
				mbcXController =  new MBC0(new Rom(romContents));
			else if(romContents[CARTRIDGE_TYPE_ADDRESS] >= 1 && romContents[CARTRIDGE_TYPE_ADDRESS] < 4)
				mbcXController =  new MBC1(new Rom(romContents), RAM_SIZES[romContents[RAM_SIZES_ADDRESS]]);
			else
				throw new IllegalArgumentException();
			return new Cartridge(mbcXController);
		}

	}
}
