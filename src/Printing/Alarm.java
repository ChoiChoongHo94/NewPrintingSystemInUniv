package Printing;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Alarm {
	File audioFile = new File("./alarm/DOORBELL.wav");

	void playAlarm() {
		try
	    {
			Clip clip = AudioSystem.getClip();
			AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
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
