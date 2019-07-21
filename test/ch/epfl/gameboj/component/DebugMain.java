package ch.epfl.gameboj.component;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class DebugMain {
	public static void main(String[] args) throws IOException {
		System.out.println(args.length + " arguments :");
		for(int i  = 0; i < args.length - 1; i++) {
			File romFile = new File(args[i]);
			long cycles = Long.parseLong(args[12]);

			GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
			Component printer = new DebugPrintComponent();
			printer.attachTo(gb.bus());
			while (gb.cycles() < cycles) {
				long nextCycles = Math.min(gb.cycles() + 17556, cycles);
				gb.runUntil(nextCycles);
				gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
			}
		}
	}
}

