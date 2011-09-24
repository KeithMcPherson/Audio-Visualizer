

public class MusicMath {

    public static Complex[] computeFFT(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = computeFFT(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = computeFFT(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
    
	public static FreqAmpPair[] computeFrequencySet(Complex[] sampleData, int n, float h, float T, float sample_rate){
		FreqAmpPair[] frequencies = new FreqAmpPair[n/2];
		Complex[] transformedData = MusicMath.computeFFT(sampleData);
		double[] combinedData = new double[n/2];
		for (int j = 0; j < n/2; j++) {
			combinedData[j] = Math.abs(transformedData[j].re() + transformedData[j].im());
			double amplitude = 2 * combinedData[j]/n;
			double frequency = j * h / T * sample_rate;
			frequencies[j] = new FreqAmpPair(frequency, amplitude);
			}
		return frequencies;
	}
	
    public static boolean hitNote(double[] data, double noteFreq, float sample_rate, float T, float n, float h) {
		double maxFreq = 0;
		double maxAmp = 0;

		double freqMax = noteFreq * Math.pow(1.059463, 6);
		double freqMin = noteFreq * Math.pow(1.059463, -6);
		for (int j = 0;  j< 1024; j++) {

			double amplitude = 2 * data[j]/n;
			double frequency = j * h / T * sample_rate;
			
			if (frequency < freqMax && frequency > freqMin && frequency > 72 && amplitude > maxAmp)
			{
				maxFreq = frequency;
				maxAmp = amplitude;
			}
		}
		System.out.println(maxFreq);

		if ( getNote(maxFreq).equals(getNote(noteFreq)) )
			return true;
		else 
			return false;
	}
    
    public static double getLoudestFrequency(FreqAmpPair[] data) {
    	
		double maxFreq = 0;
		double maxAmp = 0;
    	for (int j = 0;  j< data.length; j++) {
			double frequency = data[j].getFrequency();
			double amplitude = data[j].getAmplitude();
			if (frequency > 72 && amplitude > maxAmp)
			{
				maxFreq = frequency;
				maxAmp = amplitude;
			}
		}
    	return maxFreq;
    }
    
	public static String getNote(double frequency)
	{
		String note = null;
		int semitonesOffA = (int) Math.round(Math.log(frequency/440) / Math.log(1.059463)) % 12;
		switch (semitonesOffA) {
		case -11:
			note = "A#";
			break;
		case -10:
			note = "B";
			break;
		case -9:
			note = "C";
			break;
		case -8:
			note = "C#";
			break;
		case -7:
			note = "D";
			break;
		case -6:
			note = "D#";
			break;
		case -5:
			note = "E";
			break;
		case -4:
			note = "F";
			break;
		case -3:
			note = "F#";
			break;
		case -2:
			note = "G";
			break;
		case -1:
			note = "G#";
			break;
		case 0:
			note = "A";
			break;
		case 1:
			note = "A#";
			break;
		case 2:
			note = "B";
			break;
		case 3:
			note = "C";
			break;
		case 4:
			note = "C#";
			break;
		case 5:
			note = "D";
			break;
		case 6:
			note = "D#";
			break;
		case 7:
			note = "E";
			break;
		case 8:
			note = "F";
			break;
		case 9:
			note = "F#";
			break;
		case 10:
			note = "G";
			break;
		case 11:
			note = "G#";
			break;
		default:
			note = "Unknown note";
			break;
		}
		return note;
	}
}
