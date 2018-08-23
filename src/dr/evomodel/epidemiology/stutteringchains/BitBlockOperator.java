/*
 * BitBlockOperator.java
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

import dr.inference.model.Parameter;
import dr.math.MathUtils;
import dr.inference.operators.SimpleMCMCOperator;

/**
 * Given a set of indicators (with |indicators| = N), computes K = sum(indicators), proposes a new K_prime
 * and returns the string corresponding that K_prime.
 * Example: K = 5, N = 8 gives indicators = [1 1 1 1 1 0 0 0] 
 * @author Luiz Max Carvalho
 */
public class BitBlockOperator extends SimpleMCMCOperator {

    private final Parameter indicators;
    private final int blockSize;

    public BitBlockOperator(Parameter indicators, int blockSize, double weight) {
        this.indicators = indicators;
        this.blockSize = blockSize;
        setWeight(weight);
        
        if (blockSize > indicators.getSize()) throw new IllegalArgumentException("Block size too large");
                
    }
    
    public String getPerformanceSuggestion() {
        return "";
    }

    public String getOperatorName() {
        return "bitBlock(" + indicators.getParameterName() + ")";
    }
    
    public double doOperation() {
    	
        boolean Boundary = false;
        
        final int N = indicators.getSize();
        final int K = getK(indicators);
        
        if(K > N) throw new RuntimeException("K is bigger than N! Parameter is probably not binary");
        if(K < 0) throw new RuntimeException("K is less than 0");
        
        final int Kprime;
        
        if( (K-blockSize) < 0  ||  (K + blockSize) > N) Boundary = true;
        
        if(Boundary) {
        	boolean lowerBoundary = false;
        	if((K-blockSize) < 0) lowerBoundary = true;
        	if(lowerBoundary) {
        		Kprime = K + blockSize;
        	}else {
        		Kprime = K - blockSize;
        	}
        }else {
        	double sign = MathUtils.nextDouble();
            final int w = (sign < 0.5) ? -blockSize : blockSize; 
            Kprime = K + w;
        }
        
        for (int i = 0; i < N; i++) {
            indicators.setParameterValue(i, (i <= (Kprime - 1)) ? 1.0 : 0.0);
        }

        final double hastingsRatio = (Boundary) ? 0.0 : -Math.log(2); // HR is either 1 or 1/2

        return hastingsRatio;
    }

    private int getK(Parameter x) {
    	int N = x.getSize();
    	int K = 0;
    	for(int i = 0; i < N; i++ ) {
    		K += (x.getParameterValue(i) > 0) ? 1: 0;
    	}
    	return K;
    }
}
