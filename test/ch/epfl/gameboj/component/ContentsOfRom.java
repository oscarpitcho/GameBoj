package ch.epfl.gameboj.component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ContentsOfRom {

	public static void main(String[] args) throws IOException {
		InputStream stream = new FileInputStream("02-interrupts.s");
		List<Integer> contents  = new ArrayList<>();
		int c;
		while((c = stream.read()) != -1) {
			contents.add(c);
		}
		stream.close();
		for(int i = 0; i < 200; i++) {
			if(contents.get(i)!= 0 && contents.get(i) != 0xffffffff)
			 System.out.println("i = " + i + " : " + Integer.toHexString(contents.get(i)));
		}
		System.out.println(contents.get(157));
		System.out.println(contents.get(159));

	}

}
