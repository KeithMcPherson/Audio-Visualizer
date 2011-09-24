/*
 * See http://keithmac.com/?p=238
 * 
 * Sections of this code came from various places online, specifically the Complex class as well as the FFT algorithm
 * I can't Seem to find their sources but I'm sure I'll stumble across them eventually.
 * 
 * Author: Keith McPherson + others
 */

import java.applet.*;
import java.awt.*;
import java.util.ArrayList;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Mixer.Info;

public class AudioVisualizer extends Applet implements Runnable {
	static final int SAMPLE_SIZE = (int) Math.pow(2, 12);
	static final int LOW_FREQ = 100;
	static final int HIGH_FREQ = 10000;
	static final int NUM_BARS = 20;

	Thread graphics;
	// Create a global buffer size
	final int EXTERNAL_BUFFER_SIZE = 12800000;
	TargetDataLine targetDataLine = null;
	AudioFormat audioFormat;
	ArrayList<Integer> ampsToVisualize = new ArrayList<Integer>(NUM_BARS);

	int maxAmp = 1;
	int minAmp = maxAmp;

	public void init() {
		setSize(640, 480);
		setBackground(Color.darkGray);

		for (int i = 0; i < NUM_BARS; i++)
			ampsToVisualize.add(i, 1);

		audioFormat = new AudioFormat(96000.0F, 16, 1, true, false);

		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(audioFormat);
		} catch (Exception e2) {
			System.out.println("Error : Unable to start acqusition -> " + e2);
		}

		targetDataLine.start();

	}

	public void start() {
		graphics = new Thread(this);
		graphics.start();
		readAudio();
	}

	public void stop() {
		graphics = null;
	}

	public void destroy() {

	}

	public void paint(Graphics g) {
		for (int i = 0; i < ampsToVisualize.size(); i++) {
			g.setColor(Color.WHITE);
			g.fillRect(10 + i * 30, 10, 10, (ampsToVisualize.get(i) - minAmp /2) * 480 / maxAmp);
		}
		System.out.println(maxAmp + " " + minAmp);
	}

	@Override
	public void run() {

		while (true)
			while (targetDataLine != null && audioFormat != null) {
				readAudio();
				repaint();

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			}
	}

	public void readAudio() {

		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
		targetDataLine.read(abData, 0, SAMPLE_SIZE);

		// Calculate the sample rate
		float sample_rate = audioFormat.getSampleRate();
		// System.out.println("sample rate = "+sample_rate);

		// Calculate the length in seconds of the sample
		float T = SAMPLE_SIZE / audioFormat.getFrameRate();
		// System.out.println("T = "+T+
		// " (length of sampled sound in seconds)");

		// Calculate the number of equidistant points in time
		int n = (int) (T * sample_rate) / 2;
		// System.out.println("n = "+n+" (number of equidistant points)");

		// Calculate the time interval at each equidistant point
		float h = (T / n);
		// System.out.println("h = "+h+" (length of each time interval in seconds)");

		// Determine the original Endian encoding format
		boolean isBigEndian = audioFormat.isBigEndian();

		// this array is the value of the signal at time i*h
		int x[] = new int[n];

		// convert each pair of byte values from the byte array to an Endian
		// value
		for (int i = 0; i < n * 2; i += 2) {
			int b1 = abData[i];
			int b2 = abData[i + 1];
			if (b1 < 0)
				b1 += 0x100;
			if (b2 < 0)
				b2 += 0x100;

			int value;

			// Store the data based on the original Endian encoding format
			if (!isBigEndian)
				value = (b1 << 8) + b2;
			else
				value = b1 + (b2 << 8);
			x[i / 2] = value;
		}

		// do the DFT for each value of x sub j and store as f sub j
		Complex f[] = new Complex[x.length];
		for (int j = 0; j < x.length; j++) {
			f[j] = new Complex(x[j], 0);
		}

		FreqAmpPair[] freqSet = MusicMath.computeFrequencySet(f, n, h, T, sample_rate);
		//peakAmplitudes(freqSet, LOW_FREQ, HIGH_FREQ);
		averageAmplitudes(freqSet, LOW_FREQ, HIGH_FREQ);
	}

	public void averageAmplitudes(FreqAmpPair[] freqSet, int lowFreq, int highFreq) {
		minAmp = maxAmp;
		for (int i = 0; i < ampsToVisualize.size(); i++) {
			int tempSum = 0;
			int numberOfSums = 0;
			for (int j = 0; j < freqSet.length; j++) {
				if (freqSet[j].frequency > lowFreq
						&& freqSet[j].frequency < (i + 1)
								* (highFreq - lowFreq) / ampsToVisualize.size()) {
					tempSum += freqSet[j].amplitude;
					numberOfSums++;
				}
			}
			ampsToVisualize.set(i, tempSum / (numberOfSums + 1));
			if (tempSum / (numberOfSums + 1) > maxAmp)
				maxAmp = tempSum / (numberOfSums + 1);
			if (tempSum / (numberOfSums + 1) < minAmp)
				minAmp = tempSum / (numberOfSums + 1);
			tempSum = 0;
			numberOfSums = 0;
		}
	}
	
	public void peakAmplitudes(FreqAmpPair[] freqSet, int lowFreq, int highFreq) {
		minAmp = maxAmp;
		for (int i = 0; i < ampsToVisualize.size(); i++) {
			int rangeMaxAmp = 0;
			for (int j = 0; j < freqSet.length; j++) {
				if (freqSet[j].frequency > lowFreq && freqSet[j].frequency < (i + 1) * (highFreq - lowFreq) / ampsToVisualize.size()) {
					rangeMaxAmp = (int) freqSet[j].amplitude;
				}
			}
			ampsToVisualize.set(i, rangeMaxAmp);
			if (rangeMaxAmp > maxAmp)
				maxAmp = rangeMaxAmp;
		}
	}

}