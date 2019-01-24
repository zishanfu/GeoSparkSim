package com.zishanfu.vistrips.tools;

import java.util.Arrays;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;

public class LinearInterpolatedFunction {
    
    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(LinearInterpolatedFunction.class);


    private PolynomialSplineFunction splineFunction;

    private final Coordinate start;
    private final Coordinate end;
    
    private final int numberOfDataPoints;
    
    private boolean strictlyIncrease = true;

    public int getNumberOfDataPoints() {
        return numberOfDataPoints;
    }

    /**
     * @throws IllegalArgumentException
     *             , DimensionMismatchException, NumberIsTooSmallException, NonMonotonicSequenceException
     */
    public LinearInterpolatedFunction(double[] x, double[] y) {
        Preconditions.checkArgument(x.length == y.length, "dimensions mismatch");
        Preconditions.checkArgument(x.length != 0, "dimension zero");

        if (x.length > 1) {
            LinearInterpolator linearInterpolator = new LinearInterpolator();
            if(LOG.isDebugEnabled()){
                LOG.debug(String.format("x={%s}", Arrays.toString(x)));
                LOG.debug(String.format("y={%s}", Arrays.toString(y)));
            }
            try {
            	splineFunction = linearInterpolator.interpolate(x, y);
            }catch(NonMonotonicSequenceException e) {
            	strictlyIncrease = false;
            }
        }
        
        numberOfDataPoints = x.length;

        start = new Coordinate(x[0], y[0]);
        end = new Coordinate(x[x.length - 1], y[y.length - 1]);
    }

    public double value(double x0) {
        if (strictlyIncrease && splineFunction != null && splineFunction.isValidPoint(x0)) {
            return splineFunction.value(x0);
        }
        if (x0 <= start.x) {
            return start.y;
        }
        if (x0 >= end.x) {
            return end.y;
        }
        throw new IllegalStateException("should not reach undefined function range=" + x0);
    }

}
