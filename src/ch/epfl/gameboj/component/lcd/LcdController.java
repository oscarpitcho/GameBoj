package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.Objects;


import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * Final class in charge of simulating the LCdController of the GameBoy
 * 
 * @author Oscar Pitcho (288225)
 * @author Nizar ghandri (283161)
 */
public final class LcdController implements Component, Clocked {

	public static final int LCD_WIDTH = 160;
	public static final int LCD_HEIGHT = 144;
	public static final int BACKGROUND_SIZE = 256;
	
	private static final long LINE_DRAW_DURATION = 114;
	private static final int TILE_INDEX_CORRECTION = 0x80;
	private static final int LY_MAX_VALUE = 154;
	private static final int SPRITEX_OFFSET = 8;
	private static final int SPRITEY_OFFSET = 16;
	private static final int SPRITE_SQUARE_DIMENSIONS = 8;
	private static final int SPRITE_RECT_HEIGHT = 16;
	private static final int WINX_OFFSET = 7;
	private static final int TILE_SIZE = 8;
	private static final int NBR_BGWIN_TILES_LINE = BACKGROUND_SIZE / TILE_SIZE;
	private static final int MAX_DISPLAYED_WIN_TILES = LCD_WIDTH / TILE_SIZE;
	private static final int BYTES_IN_TILE = 16;
	private static final int SPRITES_IN_OAM = 40;
	private static final int MAX_SPRITE_LINE = 10;
	private static final long MODE0_DURATION = 51;
	private static final long MODE2_DURATION = 20;
	private static final long MODE3_DURATION = 43;
	private static final long MODE1_STEP_DURATION = 114;
	private static final int MODE_ENCODING_SIZE = 3;

	private final Cpu cpu;
	private final Ram videoRam;
	private final Ram OAM;
	private Bus bus;

	private int copyCounter = Integer.MAX_VALUE;

	private LcdImage image;
	private LcdImage.Builder nextImageBuilder;

	private int winY;
	private long cycleOnWakeUp;
	private long nextNonIdleCycle;

	private final RegisterFile<Reg> reg = new RegisterFile<>(Reg.values());

	// Private enums for the registers and their bits.
	private enum Reg implements Register {
		LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX;

		private enum LCDCBits implements Bit {
			BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS,
		}

		private enum STATBits implements Bit {
			MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED
		}

	}

	// Private enum for the modes of the LCdController.
	private enum LcdMode {
		MODE0, MODE1, MODE2, MODE3
	}

	// Private enum for the information of a sprite.
	private enum SpriteInfo {
		Y, X, Index, Features;

		private enum FeaturesBits implements Bit {
			UNUSED0, UNUSED1, UNUSED2, UNUSED3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
		}
	}

	/**
	 * Public constructor in charge of creating an LcdController.
	 * 
	 * @param cpu
	 *            The cpu with which the LcdController will be connected
	 * 
	 * @throws NullPointerException
	 *             if the argument cpu is null.
	 */
	public LcdController(Cpu cpu) {
		Objects.requireNonNull(cpu);
		this.cpu = cpu;
		this.image = (new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH)).build();
		this.nextImageBuilder = new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH);
		this.videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
		this.OAM = new Ram(AddressMap.OAM_RAM_SIZE);
		nextNonIdleCycle = Long.MAX_VALUE;
		winY = 0;
	}

	@Override
	public void attachTo(Bus bus) {
		Objects.requireNonNull(bus);
		this.bus = bus;
		bus.attach(this);
	}

	@Override
	public void cycle(long cycle) {
		if (copyCounter < 160) {
			OAM.write(copyCounter, bus.read(Bits.make16(reg.get(Reg.DMA), copyCounter)));
			++copyCounter;
		}
		if (cycle == nextNonIdleCycle && isEnabled())
			reallyCycle();
		else if (nextNonIdleCycle == Long.MAX_VALUE && isEnabled()) {
			setMode(LcdMode.MODE2);
			winY = 0;
			nextNonIdleCycle = cycle;
			cycleOnWakeUp = cycle % (LINE_DRAW_DURATION * LY_MAX_VALUE);
			reallyCycle();
		}
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		if (address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END)
			return reg.get(extractReg(address));
		else if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END)
			return videoRam.read(address - AddressMap.VIDEO_RAM_START);
		else if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END)
			return OAM.read(address - AddressMap.OAM_START);
		return NO_DATA;
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);
		if (address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
			Reg r = extractReg(address);
			switch (r) {
			case LY:
				break;
			case STAT:
				int mask5MSB = 0xF8;
				reg.set(Reg.STAT, (data & mask5MSB) | Bits.clip(MODE_ENCODING_SIZE, reg.get(Reg.STAT)));
				break;
			case LCDC:
				reg.set(Reg.LCDC, data);
				if (!isEnabled()) {
					setMode(LcdMode.MODE0);
					setLYLYC(0, Reg.LY);
					nextNonIdleCycle = Long.MAX_VALUE;
				}
				break;
			case LYC:
				setLYLYC(data, Reg.LYC);
				break;
			case DMA:
				reg.set(Reg.DMA, data);
				copyCounter = 0;
				break;
			default:
				reg.set(r, data);
			}
		} else if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END)
			videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
		else if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END)
			OAM.write(address - AddressMap.OAM_START, data);

	}

	/**
	 * Accessor which returns the current image being displayed by the
	 * LcdController.
	 * 
	 * @return The image being displayed by the LcdController.
	 */
	public LcdImage currentImage() {
		return image;
	}

	// Method in charge of cycling the Lcdcontroller
	private void reallyCycle() {
		long cycleMode = nextNonIdleCycle - cycleOnWakeUp;
		int lineIndex = (int) (cycleMode / LINE_DRAW_DURATION) % (LY_MAX_VALUE);
		switch (getMode(cycleMode)) {
		case MODE0: {
			nextNonIdleCycle += MODE0_DURATION;
			setMode(LcdMode.MODE0);
			raiseStatInterrupt(Reg.STATBits.INT_MODE0);

		}
		break;
		case MODE1: {
			nextNonIdleCycle += MODE1_STEP_DURATION;
			setMode(LcdMode.MODE1);
			if (lineIndex == LCD_HEIGHT)
				cpu.requestInterrupt(Interrupt.VBLANK);
			setLYLYC(lineIndex, Reg.LY);
			raiseStatInterrupt(Reg.STATBits.INT_MODE1);
		}
		break;
		case MODE2: {
			nextNonIdleCycle += MODE2_DURATION;
			if (lineIndex == 0) {
				image = nextImageBuilder.build();
				nextImageBuilder = new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH);
				winY = 0;
			}
			setMode(LcdMode.MODE2);
			raiseStatInterrupt(Reg.STATBits.INT_MODE2);
			setLYLYC(lineIndex, Reg.LY);

		}
		break;
		case MODE3: {
			nextNonIdleCycle += MODE3_DURATION;
			nextImageBuilder.setLine(lineIndex, computeLine(lineIndex));
			setMode(LcdMode.MODE3);
		}
		break;
		}
	}

	// Method to set the mode of the LcdController.
	private void setMode(LcdMode mode) {
		reg.setBit(Reg.STAT, Reg.STATBits.MODE0, Bits.test(mode.ordinal(), 0));
		reg.setBit(Reg.STAT, Reg.STATBits.MODE1, Bits.test(mode.ordinal(), 1));
	}

	// Method to return the mode in which the LcdController should be depending on
	// the current cycle.
	private LcdMode getMode(long cycle) {
		long step = cycle % LINE_DRAW_DURATION; 
		int lineIndex = (int) (cycle / LINE_DRAW_DURATION) % LY_MAX_VALUE;
		if (lineIndex >= LCD_HEIGHT)
			return LcdMode.MODE1;
		else if (step < MODE2_DURATION && step >= 0)
			return LcdMode.MODE2;
		else if (step < MODE2_DURATION + MODE3_DURATION && step >= MODE2_DURATION)
			return LcdMode.MODE3;
		else
			return LcdMode.MODE0;
	}

	// Method to extract the corresponding reg from an address.
	private Reg extractReg(int address) {
		Preconditions.checkArgument(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END);
		return Reg.values()[address - AddressMap.REGS_LCDC_START];
	}

	// Method to modify LY or LYC and do the following verifications.
	private void setLYLYC(int value, Reg r) {
		Preconditions.checkArgument(r == Reg.LY || r == Reg.LYC);
		reg.set(r, value);
		if (reg.get(Reg.LYC) == reg.get(Reg.LY)) {
			reg.setBit(Reg.STAT, Reg.STATBits.LYC_EQ_LY, true);
			raiseStatInterrupt(Reg.STATBits.INT_LYC);
		} else
			reg.setBit(Reg.STAT, Reg.STATBits.LYC_EQ_LY, false);
	}

	// Method to test if the Lcd screen in on.
	private boolean isEnabled() {
		return reg.testBit(Reg.LCDC, Reg.LCDCBits.LCD_STATUS);
	}

	// Private method to compute a line of pixels in the screen.
	private LcdImageLine computeLine(int lineIndex) {
		Objects.checkIndex(lineIndex, LCD_HEIGHT);

		LcdImageLine.Builder computedLine = new LcdImageLine.Builder(BACKGROUND_SIZE);
		LcdImageLine.Builder windowLine = new LcdImageLine.Builder(LCD_WIDTH);
		LcdImageLine bgSprites = new LcdImageLine.Builder(LCD_WIDTH).build();
		LcdImageLine fgSprites = new LcdImageLine.Builder(LCD_WIDTH).build();

		int backGroundTiles = AddressMap.BG_DISPLAY_DATA[reg.testBit(Reg.LCDC, Reg.LCDCBits.BG_AREA) ? 1 : 0];
		int windowTiles = AddressMap.BG_DISPLAY_DATA[reg.testBit(Reg.LCDC, Reg.LCDCBits.WIN_AREA) ? 1 : 0];
		int tileSource = AddressMap.TILE_SOURCE[reg.testBit(Reg.LCDC, Reg.LCDCBits.TILE_SOURCE) ? 1 : 0];

		int lineInBackGround = (lineIndex + reg.get(Reg.SCY)) % BACKGROUND_SIZE;
		int tileNumberBg = ((lineInBackGround / TILE_SIZE) * NBR_BGWIN_TILES_LINE);
		int lineInBgTile = (lineInBackGround % TILE_SIZE);
		int tileNumberWin = (winY / TILE_SIZE) * NBR_BGWIN_TILES_LINE;
		int lineInWinTile = (winY % TILE_SIZE);

		int WX = Math.max(0, reg.get(Reg.WX) - WINX_OFFSET);
		int WY = reg.get(Reg.WY);
		boolean drawWindow = reg.testBit(Reg.LCDC, Reg.LCDCBits.WIN) && 0 <= WX && WX < LCD_WIDTH && WY <= lineIndex;
		boolean drawSprites = reg.testBit(Reg.LCDC, Reg.LCDCBits.OBJ);
		boolean drawBg = reg.testBit(Reg.LCDC, Reg.LCDCBits.BG);
		boolean squareSprite = !reg.testBit(Reg.LCDC, Reg.LCDCBits.OBJ_SIZE);
		for (int i = 0; i < NBR_BGWIN_TILES_LINE && (drawBg || drawWindow); ++i) {
			if (drawBg) {
				int tileBgIndex = correctIndex(read(backGroundTiles + tileNumberBg + i));
				int byteAddress = getTileAddress(tileSource, tileBgIndex, lineInBgTile);
				addBytesToLine(computedLine, i, byteAddress);
			}
			if (drawWindow && i < MAX_DISPLAYED_WIN_TILES) {
				int tileWinIndex = correctIndex(read(windowTiles + tileNumberWin + i));
				int byteAddressWin = getTileAddress(tileSource, tileWinIndex, lineInWinTile);
				addBytesToLine(windowLine, i, byteAddressWin);
			}

		}
		if (drawSprites) {
			int[] spritesToDraw = spritesIntersectingLine(lineIndex);
			for (int s : spritesToDraw) {
				int index = Bits.clip(8, s);
				boolean bg = Bits.test(spriteElement(index, SpriteInfo.Features), SpriteInfo.FeaturesBits.BEHIND_BG);
				if (bg)
					bgSprites = spriteLine(index, lineIndex, squareSprite).below(bgSprites);
				else
					fgSprites = spriteLine(index, lineIndex, squareSprite).below(fgSprites);
			}
		}
		winY = drawWindow ? winY + 1 : winY;
		LcdImageLine line = computedLine.build().extractWrapped(reg.get(Reg.SCX), LCD_WIDTH)
				.mapColors(reg.get(Reg.BGP));
		if (drawSprites)
			line = bgSprites.below(line, bgSprites.getOpacity().not().or(line.getOpacity()));
		if (drawWindow)
			line = line.join(WX, windowLine.build().shift(WX)).mapColors(reg.get(Reg.BGP));
		if (drawSprites)
			line = line.below(fgSprites);
		return line;
	}

	// Method to add LSB and MSB from the specified address at the given index in
	// the builder.
	private void addBytesToLine(LcdImageLine.Builder builder, int indexInLine, int address) {
		builder.setBytes(indexInLine, getImageByte(address + 1), getImageByte(address));
	}

	// Private method to correct the index of a window or bg tile
	private int correctIndex(int index) {
		if (!reg.testBit(Reg.LCDC, Reg.LCDCBits.TILE_SOURCE))
			return index < TILE_INDEX_CORRECTION ? index + TILE_INDEX_CORRECTION : index - TILE_INDEX_CORRECTION;
		return index;
	}

	// Method to compute an LcdImageLine with a single sprite.
	private LcdImageLine spriteLine(int index, int lineIndex, boolean squareSprite) {

		int spriteX = spriteElement(index, SpriteInfo.X) - SPRITEX_OFFSET;
		int spriteY = spriteElement(index, SpriteInfo.Y) - SPRITEY_OFFSET;
		int indexInVRAM = spriteElement(index, SpriteInfo.Index);
		int config = spriteElement(index, SpriteInfo.Features);
		int lineInSprite = lineIndex - spriteY;

		LcdImageLine.Builder builder = new LcdImageLine.Builder(LCD_WIDTH);
		int[] lsbMsb = symmertricAndReverseContent(config, lineInSprite, indexInVRAM, squareSprite);
		builder.setBytes(0, lsbMsb[1], lsbMsb[0]);
		return builder.build().shift(spriteX)
				.mapColors(Bits.test(config, SpriteInfo.FeaturesBits.PALETTE) ? reg.get(Reg.OBP1) : reg.get(Reg.OBP0));
	}

	private int getTileAddress(int tileSource, int tileIndex, int lineInTile) {
		return tileSource + tileIndex * BYTES_IN_TILE + 2 * lineInTile;
	}

	// Method to return the bytes stored and the given address after having reversed
	// them.
	private int getImageByte(int address) {
		return Bits.reverse8(read(address));
	}

	// Method to return the msb and lsb of a sprite after having applied the correct
	// modifications.
	private int[] symmertricAndReverseContent(int config, int lineInSprite, int indexInVRAM, boolean squareSprite) {
		boolean flipHor = Bits.test(config, SpriteInfo.FeaturesBits.FLIP_H);
		boolean flipVer = Bits.test(config, SpriteInfo.FeaturesBits.FLIP_V);
		int size = squareSprite ? SPRITE_SQUARE_DIMENSIONS : SPRITE_RECT_HEIGHT;
		int lineWithFlip = flipVer ? size - 1 - lineInSprite : lineInSprite;
		int address = getTileAddress(AddressMap.TILE_SOURCE[1], indexInVRAM, lineWithFlip);
		int msb = getImageByte(address + 1);
		int lsb = getImageByte(address);
		if (flipHor) {
			msb = Bits.reverse8(msb);
			lsb = Bits.reverse8(lsb);
		}
		return new int[] { lsb, msb };
	}

	// Method to find the sprites intersecting a given line that should be
	// displayed.
	private int[] spritesIntersectingLine(int lineIndex) {
		int[] firstArray = new int[MAX_SPRITE_LINE];
		int intersectingSprites = 0;
		int size = reg.testBit(Reg.LCDC, Reg.LCDCBits.OBJ_SIZE) ? SPRITE_RECT_HEIGHT : SPRITE_SQUARE_DIMENSIONS;
		for (int i = 0; i < SPRITES_IN_OAM && intersectingSprites < MAX_SPRITE_LINE; ++i) {
			int spriteY = spriteElement(i, SpriteInfo.Y) - SPRITEY_OFFSET;
			int spriteX = spriteElement(i, SpriteInfo.X);
			if (lineIndex >= spriteY && lineIndex < spriteY + size)
				firstArray[intersectingSprites++] = Bits.make16(spriteX, i);
		}
		Arrays.sort(firstArray, 0, intersectingSprites);
		return Arrays.copyOf(firstArray, intersectingSprites);
	}

	// Method to raise LCD_STAT if the bit in question of Reg.STAT is true.
	private void raiseStatInterrupt(Reg.STATBits m) {
		if (reg.testBit(Reg.STAT, m))
			cpu.requestInterrupt(Interrupt.LCD_STAT);

	}

	// Private method to return the different information of a sprite in the OAM.
	private int spriteElement(int index, SpriteInfo info) {
		return OAM.read(index * SpriteInfo.values().length + info.ordinal());
	}
}
