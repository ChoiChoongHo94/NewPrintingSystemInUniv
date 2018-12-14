package Printing;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Alarm {
	File audioFile = new File("./alarm/DOORBELL.wav");
	AudioInputStream stream;
	Clip clip;

	public Alarm() {
		try {
			clip = AudioSystem.getClip();
			stream = AudioSystem.getAudioInputStream(audioFile);
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}

	void playAlarm() {
		try
	    {
	        clip.open(stream);
	        clip.start();
	        while (!clip.isRunning())
	            Thread.sleep(10);
	        while (clip.isRunning())
	            Thread.sleep(10);
	        clip.close();
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}
}
