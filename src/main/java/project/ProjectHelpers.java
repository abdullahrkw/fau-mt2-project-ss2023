/*
 * Copyright (C) 2022 Bruno Riemenschneider <bruno.riemenschneider@fau.de>
 *
 * Distributed under terms of the GPLv3 license.
 */
package project;

import ij.process.FHT;
import ij.process.FloatProcessor;
import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;
import org.apache.commons.math3.complex.Complex;

import java.io.File;

public class ProjectHelpers {

    public static ComplexImage LoadKSpace(String pathHDF5) {
        File file = new File(pathHDF5);
        try (HdfFile hdfFile = new HdfFile(file)) {
            Dataset widthDataset = hdfFile.getDatasetByPath("width");
            Object widthObject = widthDataset.getData();
            Dataset heightDatasset = hdfFile.getDatasetByPath("height");
            Object heightObject = heightDatasset.getData();

            ComplexImage KSpace = new ComplexImage((int) widthObject, (int) heightObject, "MRI k-Space");

            Dataset realDataset = hdfFile.getDatasetByPath("kspace_realpart");
            Object realObject = realDataset.getData();
            KSpace.real.setBufferFrom2dArray((float[][]) realObject);
            Dataset imagDataset = hdfFile.getDatasetByPath("kspace_imagpart");
            Object imagObject = imagDataset.getData();
            KSpace.imag.setBufferFrom2dArray((float[][]) imagObject);

            return KSpace;
        }
    }

    public static ComplexImage InverseFFT2D(ComplexImage input) {
        return complexFFT(input, true);
    }

    public static ComplexImage FFT2D(ComplexImage input) {
        return complexFFT(input, false);
    }

    private static ComplexImage complexFFT(ComplexImage input, boolean inverseTransform) {
        float[] tmpRe = new float[input.width * input.height];
        float[] tmpIm = new float[input.width * input.height];
        if (input.width != input.height) {
            throw new RuntimeException("Our implementation only works for image width equal to image height.");
        }
        var N = input.width;
        c2c2DFFT(input.real.buffer(), input.imag.buffer(), N, tmpRe, tmpIm);

        //That's some dirty forward transforming (reversing array order of inverse transform and adding some scaling)
        if (!inverseTransform) {
            var arrLength = tmpRe.length;
            for (int i = 0; i < arrLength / 2; i++) {
                float temp = tmpRe[i];
                tmpRe[i] = tmpRe[arrLength - i - 1] * (float) arrLength;
                tmpRe[arrLength - i - 1] = temp * (float) arrLength;
                temp = tmpIm[i];
                tmpIm[i] = tmpIm[arrLength - i - 1] * (float) arrLength;
                tmpIm[arrLength - i - 1] = temp * (float) arrLength;
            }
        }

        var output = new ComplexImage(input.width, input.height, "iDFT of " + input.name);
        output.real.setBuffer(tmpRe);
        output.imag.setBuffer(tmpIm);
        return output;
    }


    /**
     * Complex to Complex Inverse Fourier Transform
     * Author: Joachim Wesner
     * Source: ImageJ FFT plugin
     */
    private static void c2c2DFFT(float[] rein, float[] imin, int maxN, float[] reout, float[] imout) {
        FHT fht = new FHT(new FloatProcessor(maxN, maxN));
        float[] fhtpixels = (float[]) fht.getPixels();
        // Real part of inverse transform
        for (int iy = 0; iy < maxN; iy++)
            cplxFHT(iy, maxN, rein, imin, false, fhtpixels);
        fht.inverseTransform();
        // Save intermediate result, so we can do a "in-place" transform
        float[] hlp = new float[maxN * maxN];
        System.arraycopy(fhtpixels, 0, hlp, 0, maxN * maxN);
        // Imaginary part of inverse transform
        for (int iy = 0; iy < maxN; iy++)
            cplxFHT(iy, maxN, rein, imin, true, fhtpixels);
        fht.inverseTransform();
        System.arraycopy(hlp, 0, reout, 0, maxN * maxN);
        System.arraycopy(fhtpixels, 0, imout, 0, maxN * maxN);
    }

    /**
     * Build FHT input for equivalent inverse FFT
     * Author: Joachim Wesner
     * Source: ImageJ FFT plugin
     */
    private static void cplxFHT(int row, int maxN, float[] re, float[] im, boolean reim, float[] fht) {
        int base = row * maxN;
        int offs = ((maxN - row) % maxN) * maxN;
        if (!reim) {
            for (int c = 0; c < maxN; c++) {
                int l = offs + (maxN - c) % maxN;
                fht[base + c] = ((re[base + c] + re[l]) - (im[base + c] - im[l])) * 0.5f;
            }
        } else {
            for (int c = 0; c < maxN; c++) {
                int l = offs + (maxN - c) % maxN;
                fht[base + c] = ((im[base + c] + im[l]) + (re[base + c] - re[l])) * 0.5f;
            }
        }
    }


      // Apply FFT to ComplexSignal
    public static ComplexSignal FFT1D(ComplexSignal signal) {
        var x = toComplex(signal);

        return fromComplex(fft(x), signal, "FFT");
    }

    public static Complex[] toComplex(ComplexSignal signal) {
        Complex[] x = new Complex[signal.getSize()];

        for (int i = signal.real.minIndex(); i < signal.real.minIndex() + signal.real.size(); ++i) {
            x[i] = new Complex(signal.real.atIndex(i), signal.imag.atIndex(i));
        }

        return x;
    }

    // Convert Complex to ComplexSignal type
    public static ComplexSignal fromComplex(Complex[] x, ComplexSignal signal, String name) {
        ComplexSignal output = new ComplexSignal(signal.getSize(), name);
        for (int i = signal.real.minIndex(); i < signal.real.minIndex() + signal.real.size(); ++i) {
            output.real.setAtIndex(i, (float) x[i].getReal());
            output.imag.setAtIndex(i, (float) x[i].getImaginary());
        }
        return output;
    }

    // Reference: https://introcs.cs.princeton.edu/java/97data/FFT.java.html
    // Copyright © 2000–2019, Robert Sedgewick and Kevin Wayne.
    // compute the FFT of x[], assuming its length n is a power of 2
    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1) return new Complex[]{x[0]};

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // compute FFT of even terms
        Complex[] even = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] evenFFT = fft(even);

        // compute FFT of odd terms
        Complex[] odd = even;  // reuse the array (to avoid n log n space)
        for (int k = 0; k < n / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] oddFFT = fft(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = evenFFT[k].add(wk.multiply(oddFFT[k]));
            y[k + n / 2] = evenFFT[k].subtract(wk.multiply(oddFFT[k]));
        }
        return y;
    }
}
