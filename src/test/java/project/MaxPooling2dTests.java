/*
 * Copyright (C) 2022 Zhengguo Tan <zhengguo.tan@fau.de>
 *
 * Distributed under terms of the GPLv3 license.
 */
package project;

import mt.Image;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MaxPooling2dTests {

    @Test
    void testMaxPooling2d() {

        // change input and expectedOutput to modify the tests
        float[][] input = {
                {130, 5, 15, 73},
                {73, 173, 146, 11},
                {62, 103, 118, 50}
        };
        float[][] expectedOutput = {
                {173, 173, 146},
                {173, 173, 146}
        };

        // define input image
        Image img = new Image(input[0].length, input.length, "input image");
        System.out.println(">> input: ");
        for (int h = 0; h < img.height(); h++) {
            for (int w = 0; w < img.width(); w++) {
                img.setAtIndex(w, h, input[h][w]);
                System.out.printf(String.format("%5.1f", img.atIndex(w, h)) + ", ");
            }
            System.out.println();
        }

        // define & apply filter
        MaxPooling2d mp = new MaxPooling2d(2, 2, 1, 1);

        Image output = mp.apply(img);

        // display expected output
        System.out.println(">> expected output: ");
        for (int h = 0; h < expectedOutput.length; h++) {
            for (int w = 0; w < expectedOutput[0].length; w++) {
                System.out.printf(String.format("%5.1f", expectedOutput[h][w]) + ", ");
            }
            System.out.println();
        }

        // display output and reshape to 2D array for comparison with expected output
        System.out.println(">> output: ");
        float[][] outputArray = new float[output.height()][output.width()];
        for (int h = 0; h < output.height(); h++) {
            for (int w = 0; w < output.width(); w++) {
                float val = output.atIndex(w, h);
                System.out.printf(String.format("%5.1f", val) + ", ");
                outputArray[h][w] = val;
            }
            System.out.println();
        }

        // assert equality of output and expected output
        Assertions.assertArrayEquals(expectedOutput, outputArray);
    }

}
