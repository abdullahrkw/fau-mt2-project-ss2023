/*
 * Image.java
 * Copyright (C) 2020 Stephan Seitz <stephan.seitz@fau.de>
 *
 * Distributed under terms of the GPLv3 license.
 */

package mt;

import lme.DisplayUtils;

public class Image extends Signal {

    protected int width;
    protected int height;
    protected int minIndexX = 0;
    protected int minIndexY = 0;
    protected float[] origin = new float[]{0, 0};

    public Image(int width, int height, String name) {
        super(height * width, name);
        this.width = width;
        this.height = height;
    }

    public Image(int width, int height, String name, float[] pixels) {
        super(pixels, name);
        this.width = width;
        this.height = height;
    }

    public void show() {
        DisplayUtils.showImage(buffer, name, width(), origin, spacing(), true);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public long[] shape() {
        return new long[]{width, height};
    }

    public float atIndex(int x, int y) {
        int xIdx = x - minIndexX;
        int yIdx = y - minIndexY;

        if (xIdx < 0 || xIdx >= width() || yIdx < 0 || yIdx >= height()) {
            return 0.f;
        } else {
            return buffer[yIdx * width() + xIdx];
        }
    }

    public void setAtIndex(int x, int y, float value) {
        int xIdx = x - minIndexX;
        int yIdx = y - minIndexY;

        if (xIdx < 0 || xIdx >= width() || yIdx < 0 || yIdx >= height()) {
            throw new RuntimeException("Index out of bounds");
        } else {
            buffer[yIdx * width() + xIdx] = value;
        }
    }

    public Image add(Image image) {
        Image result = new Image(width, height, name() + " + " + image.name());
        result.minIndexX = minIndexX;
        result.minIndexY = minIndexY;

        if (image.width != width || image.height != height) {
            throw new RuntimeException("Images need to have the same size!");
        }
        for (int y = minIndexY; y < minIndexY + height(); ++y) {
            for (int x = minIndexX; x < minIndexX + width(); ++x) {
                float plus = image.atIndex(x, y) + atIndex(x, y);
                result.setAtIndex(x, y, plus);

            }
        }
        return result;
    }

    public void setBuffer(float[] buffer) {
        this.buffer = buffer;
    }

    public void setBufferFrom2dArray(float[][] buffer2d) {
        for (int y = minIndexY; y < minIndexY + height(); ++y) {
            for (int x = minIndexX; x < minIndexX + width(); ++x) {
                this.setAtIndex(x, y, buffer2d[x-minIndexX][y-minIndexY]);
            }
        }
    }

    public int minIndexX() {
        return minIndexX;
    }

    public int minIndexY() {
        return minIndexY;
    }

    public int maxIndexX() {
        return minIndexX + width() - 1;
    }

    public int maxIndexY() {
        return minIndexY + height() - 1;
    }

    public void setOrigin(float x, float y) {
        origin[0] = x;
        origin[1] = y;
    }

    public float[] origin() {
        return origin;
    }

    public void centerOrigin() {
        origin[0] = -width() * spacing * 0.5f;
        origin[1] = -height() * spacing * 0.5f;
    }

    private static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public float interpolatedAt(float x, float y) {

        x -= origin[0];
        y -= origin[1];
        x /= spacing;
        y /= spacing;


        int xFloor = (int) Math.floor(x);
        int yFloor = (int) Math.floor(y);
        int xCeil = (int) Math.ceil(x);
        int yCeil = (int) Math.ceil(y);

        float x1 = lerp(atIndex(xFloor, yFloor), atIndex(xCeil, yFloor), x - xFloor);
        float x2 = lerp(atIndex(xFloor, yCeil), atIndex(xCeil, yCeil), x - xFloor);


        return lerp(x1, x2, y - yFloor);
    }

    public void fft() {
        DisplayUtils.FFT(buffer, name, width(), origin, /*spacing()*/ 1.0f);
    }

    public void setBufferFromCenterArea(int width, int height, float[] buffer, int inputWidth, int inputHeight) {
        if ((width > inputWidth) || (height > inputHeight)) {
            throw new RuntimeException("width and height should be less than (or equal) inputWidth and inputHeight");
        }
        int offsetWidth = (int)((inputWidth - width)/2);
        int offsetHeight = (int)((inputHeight - height)/2);
        // create new buffer for cropped Image
        float[] croppedBuffer = new float[width*height];
        // Fill cropped buffer
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                croppedBuffer[y * width + x] = buffer[(y + offsetHeight) * inputWidth + (x + offsetWidth)];
            }
        }
        // set Image Properties to cropped Image
        this.setBuffer(croppedBuffer);
        this.width = width;
        this.height = height;
        this.minIndexX = 0;
        this.minIndexY = 0;
    }
}
