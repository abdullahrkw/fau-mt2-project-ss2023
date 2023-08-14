/*
 * Copyright (C) 2022 Bruno Riemenschneider <bruno.riemenschneider@fau.de>; Zhengguo Tan <zhengguo.tan@fau.de>; Jinho Kim <jinho.kim@fau.de>
 *
 * Distributed under terms of the GPLv3 license.
 */
package project;

import lme.DisplayUtils;
import mt.Signal;
import mt.Image;

import java.util.Arrays;

public class Project {
    public static void main(String[] args) {
        (new ij.ImageJ()).exitWhenQuitting(true);
        ComplexImage kSpace = ProjectHelpers.LoadKSpace("kdata.h5");

        /* Implement your code based on the project description */

        // Display KSpace Real and Imaginary part as Image
        DisplayUtils.showImage(kSpace.getReal(), "KSpace: Real Part", kSpace.getWidth());
        DisplayUtils.showImage(kSpace.getImag(), "KSpace: Imag Part", kSpace.getWidth());

        // Display magnitude and phase
        DisplayUtils.showImage(kSpace.getMagnitude(), "KSpace: Magnitude", kSpace.getWidth());
        DisplayUtils.showImage(kSpace.getLogMagnitude(), "KSpace: LogMagnitude", kSpace.getWidth());
        DisplayUtils.showImage(kSpace.getPhase(), "KSpace: Phase", kSpace.getWidth());

        // // ComplexSignal
        ComplexSignal cSignal = new ComplexSignal(256, "Complex Signal");
        cSignal.generateSine(5);
        DisplayUtils.showArray(cSignal.getReal(), "Re(s)", 0, 1);
        DisplayUtils.showArray(cSignal.getImag(), "Im(s)", 0, 1);
        DisplayUtils.showArray(cSignal.getMagnitude(), "|s|", 0, 1);
        
        // 1D FFTShift and FFT
        ComplexSignal fft = ProjectHelpers.FFT1D(cSignal);
        DisplayUtils.showArray(fft.getMagnitude(), "fft-|s|", 0, 1);
        fft.fftShift1d();
        DisplayUtils.showArray(fft.getMagnitude(), "shiftedfft-|s|", 0, 1);

        // From Spatial Frequency Domain to Image Domain
        kSpace.fftShift();
        ComplexImage rImage = ProjectHelpers.InverseFFT2D(kSpace);
        rImage.fftShift();
        DisplayUtils.showImage(rImage.getMagnitude(), "Reconst. Image: Magnitude", rImage.getWidth());
        DisplayUtils.showImage(rImage.getPhase(), "Reconst. Image: Phase", rImage.getWidth());
        DisplayUtils.showImage(rImage.getReal(), "Reconst. Image: Real Part", rImage.getWidth());
        DisplayUtils.showImage(rImage.getImag(), "Reconst. Image: Imag Part", rImage.getWidth());

        // From Image Domain to Spatial Frequency Domain
        rImage.fftShift();
        ComplexImage rKSpace = ProjectHelpers.FFT2D(rImage);
        rKSpace.fftShift();
        DisplayUtils.showImage(rKSpace.getLogMagnitude(), "Reconst. KSpace: LogMagnitude", rKSpace.getWidth());
        DisplayUtils.showImage(rKSpace.getPhase(), "Reconst. KSpace: Phase", rKSpace.getWidth());
    }
}
