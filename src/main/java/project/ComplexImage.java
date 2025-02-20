package project;

import mt.Image;

public class ComplexImage {
    protected mt.Image real;    //Image object to store real part
    protected mt.Image imag;    //Image object to store imaginary part
    protected String name;      //Name of the image
    protected int width;       
    protected int height;

    public ComplexImage(int width, int height, String name) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.real = new mt.Image(this.width, this.height, this.name);
        this.imag = new mt.Image(this.width, this.height, this.name);
    }

    public ComplexImage(int width, int height, String name, float[] bufferReal, float[] bufferImag) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.real = new mt.Image(this.width, this.height, this.name, bufferReal);
        this.imag = new mt.Image(this.width, this.height, this.name, bufferImag);
    }

    // Copy constructor
    public ComplexImage(ComplexImage otherComplexImage) {
        this.width = otherComplexImage.getWidth();
        this.height = otherComplexImage.getHeight();
        this.name = otherComplexImage.getName();
        this.real = new mt.Image(this.width, this.height, this.name, otherComplexImage.getReal());
        this.imag = new mt.Image(this.width, this.height, this.name, otherComplexImage.getImag());
    }

    // Constructor for cropping
    public ComplexImage(int width, int height, String name, float[] bufferReal, float[] bufferImag, int inputWidth, int inputHeight) {
        this.width = width;
        this.height = height;
        this.name = name;
        this.real = new mt.Image(inputWidth, inputHeight, this.name, bufferReal);
        this.real.setBufferFromCenterArea(width, height, bufferReal, inputWidth, inputHeight);
        this.imag = new mt.Image(inputWidth, inputHeight, this.name, bufferReal);
        this.imag.setBufferFromCenterArea(width, height, bufferImag, inputWidth, inputHeight);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getName() {
        return this.name;
    }

    public float[] getReal() {
        return this.real.buffer();
    }

    public float[] getImag() {
        return this.imag.buffer();
    }

    private Image calculateMagnitude(boolean logFlag) {
        Image result = new Image(this.width, this.height, "Image Mag. Dummy");
        for (int y = this.real.minIndexY(); y <= this.real.maxIndexY(); ++y) {
            for (int x = this.real.minIndexX(); x <= this.real.maxIndexX(); ++x) {
                float magnitude = (float)Math.sqrt(Math.pow(this.real.atIndex(x, y), 2.0) + Math.pow(this.imag.atIndex(x, y), 2.0));
                if(logFlag){
                    magnitude = (float)Math.log(magnitude);
                }
                result.setAtIndex(x, y, magnitude);
            }
        }
        return result;
    }

    private mt.Image calculatePhase() {
        mt.Image result = new mt.Image(this.width, this.height, "Image Mag. Dummy");
        for (int y = this.real.minIndexY(); y <= this.real.maxIndexY(); ++y) {
            for (int x = this.real.minIndexX(); x <= this.real.maxIndexX(); ++x) {
                float phase = (float)Math.atan2(this.imag.atIndex(x, y), this.real.atIndex(x, y));
                result.setAtIndex(x, y, phase);
            }
        }
        return result;
    }

    public float[] getMagnitude() {
        return this.calculateMagnitude(false).buffer();
    }

    public float[] getLogMagnitude() {
        return this.calculateMagnitude(true).buffer();
    }

    public float[] getPhase() {
        return this.calculatePhase().buffer();
    }

    private mt.Image swapQuadrants(mt.Image input) {
        int width = input.width();
        int height = input.height();
        int shiftX = width / 2;
        int shiftY = height / 2;
        mt.Image result = new mt.Image(width, height, "Image Dummy");
        for (int y = input.minIndexY(); y <= input.maxIndexY(); ++y) {
            for (int x = input.minIndexX(); x <= input.maxIndexX(); ++x) {
                int shiftedX = (x + shiftX) % width;
                int shiftedY = (y + shiftY) % height;
                result.setAtIndex(x, y, input.atIndex(shiftedX, shiftedY));
            }
        }
        return result;
    }

    public void fftShift() {
        this.real = this.swapQuadrants(this.real);
        this.imag = this.swapQuadrants(this.imag);
    }

    private mt.Image setOutPixelsToZero(mt.Image input, int lines, int axis) {
        int width = input.width();
        int height = input.height();
        mt.Image result = new mt.Image(width, height, "Image Dummy");
        if (axis == 0) {
            for (int y = input.minIndexY(); y <= input.maxIndexY(); ++y) {
                for (int x = input.minIndexX() + lines; x <= (input.maxIndexX() - lines); ++x) {
                    result.setAtIndex(x, y, input.atIndex(x, y));
                }
            }
        }
        else if (axis == 1) {
            for (int y = input.minIndexY() + lines; y <= (input.maxIndexY() - lines); ++y) {
                for (int x = input.minIndexX(); x <= input.maxIndexX(); ++x) {
                    result.setAtIndex(x, y, input.atIndex(x, y));
                }
            }
        }
        
        return result;
    }

    public void setOuterToZero(int lines, int axis) {
        if (axis != 0 && axis != 1) {
            throw new RuntimeException("Only 0 (x-axis) and 1 (y-axis) values are supported for axis");
        }
        this.real = this.setOutPixelsToZero(this.real, lines, axis);
        this.imag = this.setOutPixelsToZero(this.imag, lines, axis);
    }
}
