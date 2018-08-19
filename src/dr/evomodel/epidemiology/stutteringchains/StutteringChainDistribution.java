/*
 * StutteringChainDistribution.java
 *
 * Copyright (c) 2002-2015 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.evomodel.epidemiology.stutteringchains;
import dr.math.GammaFunction;

import dr.math.*;
import dr.math.distributions.Distribution;

public class StutteringChainDistribution implements Distribution {
	double mean;
    double omega;

    public StutteringChainDistribution(double mean, double omega) {
        this.mean = mean;
        this.omega = omega;
    }
        public double pdf(double x) {
        return pdf(x, mean, omega);
    }

    public double logPdf(double x) {
        return logPdf(x, mean, omega);
    }

    public double cdf(double x) {
        return cdf(x, mean, omega);
    }
    public double quantile(double y) {
        throw new RuntimeException("Not implemented!");
    }
    public double mean() {
        return  1 + mean/(1-mean);
    }

    public double variance() {
        return (mean* (1 + mean/omega))/Math.pow(1-mean,3);
    }

    public UnivariateFunction getProbabilityDensityFunction() {
        throw new RuntimeException();
    }

    public static double pdf(double x, double mean, double omega) {
        if (x < 1)  return 0;
        return Math.exp(logPdf(x, mean, omega));
    }

    public static double logPdf(double x, double mean, double omega) {
        if (x < 1)  return Double.NEGATIVE_INFINITY;
        double log_const = GammaFunction.lnGamma(omega*x + x -1) - (GammaFunction.lnGamma(omega*x) + GammaFunction.lnGamma(x + 1));
        double log_dens = (x-1)*Math.log(mean/omega) - (omega*x + x -1)*Math.log(1 + mean/omega);
        return log_const + log_dens; 
    }

    public static double cdf(double x, double mean, double omega) {
    	return Double.NaN; // TODO implement via NegativeBinomial DF
    }
}