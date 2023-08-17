/*
 * Copyright (C) 2022 Bruno Riemenschneider <bruno.riemenschneider@fau.de>; Zhengguo Tan <zhengguo.tan@fau.de>; Jinho Kim <jinho.kim@fau.de>
 *
 * Distributed under terms of the GPLv3 license.
 */
package project;

import lme.DisplayUtils;
import mt.Image;

public class Project {
    public static void main(String[] args) {
        (new ij.ImageJ()).exitWhenQuitting(true);
        ComplexImage kSpace = ProjectHelpers.LoadKSpace("kdata.h5");

        /* Implement your code based on the project description */

        // Display KSpace Real, Imaginary part, Magnitude, LogMagnitude, Phase as Image
        DisplayUtils.showImage(kSpace.getReal(), "KSpace: Real Part", kSpace.getWidth());
        DisplayUtils.showImage(kSpace.getImag(), "KSpace: Imag Part", kSpace.getWidth());

        DisplayUtils.showImage(kSpace.getMagnitude(), "KSpace: Magnitude", kSpace.getWidth());
        DisplayUtils.showImage(kSpace.getLogMagnitude(), "KSpace: LogMagnitude", kSpace.getWidth());
        DisplayUtils.showImage(kSpace.getPhase(), "KSpace: Phase", kSpace.getWidth());

        // ComplexSignal and 1D FFTShift and 1DFFT
        ComplexSignal cSignal = new ComplexSignal(256, "Complex Signal");
        cSignal.generateSine(5);
        DisplayUtils.showArray(cSignal.getReal(), "Re(s)", 0, 1);
        DisplayUtils.showArray(cSignal.getImag(), "Im(s)", 0, 1);
        DisplayUtils.showArray(cSignal.getMagnitude(), "|s|", 0, 1);
        
        ComplexSignal fft = ProjectHelpers.FFT1D(cSignal);
        DisplayUtils.showArray(fft.getMagnitude(), "fft-|s|", 0, 1);
        fft.fftShift1d();
        DisplayUtils.showArray(fft.getMagnitude(), "shiftedfft-|s|", 0, 1);

        // From KSpace to MR Image Domain
        ComplexImage rImage = Project.convertKSpace2ImageSpace(kSpace);
        DisplayUtils.showImage(rImage.getMagnitude(), "Reconst. Image: Magnitude", rImage.getWidth());
        DisplayUtils.showImage(rImage.getPhase(), "Reconst. Image: Phase", rImage.getWidth());
        DisplayUtils.showImage(rImage.getReal(), "Reconst. Image: Real Part", rImage.getWidth());
        DisplayUtils.showImage(rImage.getImag(), "Reconst. Image: Imag Part", rImage.getWidth());

        // From MR Image to KSpace Domain
        ComplexImage rKSpace = Project.convertImageSpace2KSpace(rImage);
        DisplayUtils.showImage(rKSpace.getLogMagnitude(), "Reconst. KSpace: LogMagnitude", rKSpace.getWidth());
        DisplayUtils.showImage(rKSpace.getPhase(), "Reconst. KSpace: Phase", rKSpace.getWidth());

        // Sinc Filter applied to MR Image and then its KSpace is reconstructed
        SincFilter2d realFilter = new SincFilter2d(31, 4.0f);
        LinearComplexImageFilter complexFilter = new LinearComplexImageFilter(realFilter);
        ComplexImage sincFilteredImage = complexFilter.apply(rImage);
        DisplayUtils.showImage(sincFilteredImage.getMagnitude(), "Sinc Filt. Image: Magnitude", sincFilteredImage.getWidth());

        ComplexImage filteredKSpace = Project.convertImageSpace2KSpace(sincFilteredImage);
        DisplayUtils.showImage(filteredKSpace.getLogMagnitude(), "Sinc Filt. Image KSpace Reconst.: LogMagnitude", filteredKSpace.getWidth());
    
        // Box Multiplication in KSpace and then its MR Image is constructed
        ComplexImage boxMultipliedKSpace = new ComplexImage(kSpace);
        boxMultipliedKSpace.setOuterToZero(96, 0);
        boxMultipliedKSpace.setOuterToZero(96, 1);
        DisplayUtils.showImage(boxMultipliedKSpace.getLogMagnitude(), "KSpace Box Multp.: LogMagnitude", boxMultipliedKSpace.getWidth());

        ComplexImage boxMultipliedKSpaceImage = Project.convertKSpace2ImageSpace(boxMultipliedKSpace);
        DisplayUtils.showImage(boxMultipliedKSpaceImage.getMagnitude(), "KSpace Filt. Image: Magnitude", boxMultipliedKSpaceImage.getWidth());
        
        // Cropped kSpace and then its MR Image is constructed
        ComplexImage croppedKSpace = new ComplexImage(64, 64, kSpace.getName(), kSpace.getReal(), kSpace.getImag(), kSpace.getWidth(), kSpace.getHeight());
        DisplayUtils.showImage(croppedKSpace.getLogMagnitude(), "Cropped KSpace: LogMagnitude", croppedKSpace.getWidth());

        ComplexImage croppedKSpaceImage = Project.convertKSpace2ImageSpace(croppedKSpace);
        DisplayUtils.showImage(croppedKSpaceImage.getMagnitude(), "Cropped KSpace Image: Magnitude", croppedKSpaceImage.getWidth());
        
        // Apply MaxPool2d to MR Image
        float[] mag = rImage.getMagnitude();
        Image mrMagImage = new Image(rImage.getWidth(), rImage.getHeight(), "magnitude of mrImage");
        mrMagImage.setBuffer(mag);

        MaxPooling2d mp = new MaxPooling2d(4, 4, 4, 4);
        Image mrMagImageMaxPooled = mp.apply(mrMagImage);
        DisplayUtils.showImage(mrMagImageMaxPooled.buffer(), "MaxPooled2d Image: Magnitude", mrMagImageMaxPooled.width());
    }

    public static ComplexImage convertKSpace2ImageSpace(ComplexImage kSpace) {
        ComplexImage copyKSpace = new ComplexImage(kSpace);
        copyKSpace.fftShift();
        ComplexImage rImage = ProjectHelpers.InverseFFT2D(copyKSpace);
        rImage.fftShift();
        return rImage;
    }

    public static ComplexImage convertImageSpace2KSpace(ComplexImage mrImage) {
        ComplexImage copyMRImage = new ComplexImage(mrImage);
        copyMRImage.fftShift();
        ComplexImage rKSpace = ProjectHelpers.FFT2D(copyMRImage);
        rKSpace.fftShift();
        return rKSpace;
    }
}
