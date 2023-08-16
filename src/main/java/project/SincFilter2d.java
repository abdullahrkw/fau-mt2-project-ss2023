package project;

import mt.LinearImageFilter;
import org.apache.commons.math3.analysis.function.Sinc;

public class SincFilter2d extends LinearImageFilter{

    public SincFilter2d(int filterSize, float downScale) {

        super(filterSize, filterSize, "Sinc2d (" + filterSize + ", " + downScale + ")");

        Sinc s = new Sinc(true);

        /* your code here, get inspiration in exercise 4 if you don't remember */
        for (int y = minIndexY; y < minIndexY + height(); ++y) {
			for (int x = minIndexX; x < minIndexX + width(); ++x) {
				setAtIndex(x, y, (float) (s.value(x/downScale) * s.value(y/downScale)));
			}
		}

        normalize();
    }
}
