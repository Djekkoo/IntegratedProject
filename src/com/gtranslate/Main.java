package com.gtranslate;

import java.io.IOException;
import java.io.InputStream;

import javazoom.jl.decoder.JavaLayerException;

public class Main {
	public static void main(String[] args) throws JavaLayerException, IOException {
		Audio audio = Audio.getInstance();
		InputStream sound  = audio.getAudio("Sander detected", Language.ENGLISH);
		audio.play(sound);		
	}
}
