package project;

import java.lang.Double;

import mt.Image;

public class MaxPooling2d {

    protected int block_width = 0;
    protected int block_height = 0;
    protected int stride_width = 0;
    protected int stride_height = 0;
    protected String name = "MaxPooling2d";

    public MaxPooling2d(int block_width, int block_height, int stride_width, int stride_height) {

        this.block_width = block_width;
        this.block_height = block_height;
        this.stride_width = stride_width;
        this.stride_height = stride_height;
    }

    public Image apply(Image input) {
        // No Padding (valid mode) at boundary. Incomplete pixels at boundary will be ignored.
        int outputWidth = (int)Math.floor((input.width() - this.block_width)/this.stride_width) + 1;
        int outputHeight = (int)Math.floor((input.height() - this.block_height)/this.stride_height) + 1;
        Image output = new Image(outputWidth, outputHeight, "MaxPooled2d");

        for (int x = 0; x < outputWidth; ++x) {
            for (int y = 0; y < outputHeight; ++y) {
                float max = (float)Double.NEGATIVE_INFINITY;
                for (int startX = x*this.stride_width; startX < x*this.stride_width+this.block_width; ++startX) {
                    for (int startY = y*this.stride_height; startY < y*this.stride_height+this.block_height; ++startY) {
                        if (input.atIndex(startX, startY) > max){
                            max = input.atIndex(startX, startY);
                        }
                    }
                }
                output.setAtIndex(x, y, max);
            }
        }
        return output;
    }
}
