package project;

import mt.LinearImageFilter;

public class LinearComplexImageFilter {
    protected mt.LinearImageFilter filter;

    public LinearComplexImageFilter(LinearImageFilter filter) {
        this.filter = filter;
    }

    public ComplexImage apply(ComplexImage image) {
        ComplexImage result = new ComplexImage(image.getWidth(), image.getHeight(), image.getName());
		for (int y = 0; y < result.getHeight(); ++y) {
			for (int x = 0; x < result.getWidth(); ++x) {
				float sumReal = 0.f;
                float sumImag = 0.f;
				for (int yPrime = this.filter.minIndexY(); yPrime <= this.filter.maxIndexY(); ++yPrime) {
					for (int xPrime = this.filter.minIndexX(); xPrime <= this.filter.maxIndexX(); ++xPrime) {
						sumReal += image.real.atIndex(x - xPrime, y - yPrime) * this.filter.atIndex(xPrime, yPrime);
                        sumImag += image.imag.atIndex(x - xPrime, y - yPrime) * this.filter.atIndex(xPrime, yPrime);
					}
				}
	    		result.real.setAtIndex(x, y, sumReal);
                result.imag.setAtIndex(x, y, sumImag);
			}
		}
        return result;
	}
}
