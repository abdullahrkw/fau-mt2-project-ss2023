package project;

import mt.Signal;

public class ComplexSignal {
    protected mt.Signal real;    //Image object to store real part
    protected mt.Signal imag;    //Image object to store imaginary part
    protected String name;      //Name of the image

    public ComplexSignal(int length, String name) {
        boolean isPowerOf2 = this.checkIfPowerOfTwo(length);
        if (!isPowerOf2) {
			throw new RuntimeException("Signal length must be Power of 2 for FFT");
		}
        this.name = name;
        this.real = new mt.Signal(length, this.name);
        this.imag = new mt.Signal(length, this.name);
    }

    public ComplexSignal(float[] signalReal, float[] signalImag, String name) {
        if (signalReal.length != signalImag.length) {
            throw new RuntimeException("Real and Imaginary Signals should be of same length");
        }
        boolean isPowerOf2 = this.checkIfPowerOfTwo(signalReal.length);
        if (!isPowerOf2) {
			throw new RuntimeException("Signal length must be Power of 2 for FFT");
		}
        this.name = name;
        this.real = new mt.Signal(signalReal, this.name);
        this.imag = new mt.Signal(signalImag, this.name);
    }

    private boolean checkIfPowerOfTwo(int length) {
        boolean isPowerOf2 = (length > 0) && ((length & (length - 1)) == 0);
        return isPowerOf2;
    }

    public float[] getReal() {
        return this.real.buffer();
    }

    public float[] getImag() {
        return this.imag.buffer();
    }

    public String getName() {
        return this.name;
    }

    public int getSize() {
        return this.real.size();
    }

    public void generateSine(int numWaves) {
        int numSamples = this.real.size();
        mt.Signal coolSinWave = new mt.SineWave(1 * numWaves, numSamples)
			.plus(new mt.SineWave(2 * numWaves, numSamples).times(-1.0f/2.0f))
			.plus(new mt.SineWave(3 * numWaves, numSamples).times(+1.0f/3.0f))
			.plus(new mt.SineWave(4 * numWaves, numSamples).times(-1.0f/4.0f))
			.plus(new mt.SineWave(5 * numWaves, numSamples).times(+1.0f/5.0f));
        for (int i = 0; i < coolSinWave.size() ; ++i) {
            this.real.setAtIndex(i, coolSinWave.atIndex(i));
            this.imag.setAtIndex(i, 0);
        }
    }

    private Signal calculateMagnitude() {
        mt.Signal result = new mt.Signal(this.real.size(), "Image Mag. Dummy");
        for (int y = this.real.minIndex(); y <= this.real.maxIndex(); ++y) {
            float magnitude = (float)Math.sqrt(Math.pow(this.real.atIndex(y), 2.0) + Math.pow(this.imag.atIndex(y), 2.0));
            result.setAtIndex(y, magnitude);    
        }
        return result;
    }

    public float[] getMagnitude() {
        return this.calculateMagnitude().buffer();
    }

    private mt.Signal swap(mt.Signal input) {
        float[] array = input.buffer();
        int length = array.length;
        int shiftAmount = length / 2; /* N/2 */
        float[] shiftedArray = new float[length];

        for (int i = 0; i < length; i++) {
            int newIndex = (i + shiftAmount) % length;
            shiftedArray[i] = array[newIndex];
        }
        mt.Signal shiftedSignal = new mt.Signal(shiftedArray, input.name());
        return shiftedSignal;
    }

    public void fftShift1d() {
        this.real = this.swap(this.real);
        this.imag = this.swap(this.imag);
    }
}

