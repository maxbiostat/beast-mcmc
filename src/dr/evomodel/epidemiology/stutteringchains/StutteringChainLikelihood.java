/*
 * StutteringChainLikelihood.java
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

import dr.evomodel.epidemiology.stutteringchains.StutteringChainDistribution;
import dr.inference.loggers.Loggable;
import dr.inference.model.AbstractModelLikelihood;
import dr.inference.model.Parameter;
import dr.inference.model.Variable;
import dr.inference.model.Model;

/**
 * A likelihood function for disease clusters under a stuttering chain model (Blumberg, 2013, PLoS Comp Bio)
 *
 * Takes a latent vector of cluster sizes Y, a set of indicators 'indicators' and parameters 'R0' and 'omega'
 * Only computes the likelihood for the Y_i for which indicators[i] == 1
 *
 * @author Luiz Max Carvalho
 */

public class StutteringChainLikelihood extends AbstractModelLikelihood implements Loggable {
	
	public static final String STUTTERING_CHAIN_LIKELIHOOD = "stutteringChainLikelihood";
	
	public StutteringChainLikelihood(Parameter R0, Parameter omega, Parameter latent, Parameter indicators) {
	    this(STUTTERING_CHAIN_LIKELIHOOD, R0, omega, latent, indicators);
	}
    
	 public StutteringChainLikelihood(String name, Parameter R0, Parameter omega, Parameter latent, Parameter indicators) {
		 super(name);

		if(latent.getSize() != indicators.getSize()) throw new RuntimeException("Dimension mismatch");
		 
		this.R0 = R0;
		addVariable(R0);
		R0.addBounds(new Parameter.DefaultBounds(Double.POSITIVE_INFINITY, 0.0, 1)); // TODO R0 could be [0, 1]
		
		this.omega = omega;
		addVariable(omega);
		omega.addBounds(new Parameter.DefaultBounds(Double.POSITIVE_INFINITY, 0.0, 1));
		
		this.latent = latent;
		addVariable(latent);
		latent.addBounds(new Parameter.DefaultBounds(1.0, 0.0, latent.getSize()));

		this.indicators = indicators;
		addVariable(indicators);
		indicators.addBounds(new Parameter.DefaultBounds(1.0, 0.0, indicators.getSize()));
	}

	public Model getModel() {
		return this;
	}

	public double getLogLikelihood() {
        if (!likelihoodKnown) {
            logLikelihood = calculateLogLikelihood();
            likelihoodKnown = true;
        }
        return logLikelihood;
	}

	private double calculateLogLikelihood() {

		double r = R0.getParameterValue(0);
		double w = omega.getParameterValue(0);
		StutteringChainDistribution dist = new StutteringChainDistribution(r, w);
		
		double lp = 0.0;
		for(int i = 0; i < latent.getSize(); i++){
			lp += (indicators.getParameterValue(i) > 0) ? dist.logPdf(latent.getParameterValue(i)) : 0.0; 
		}
		return lp;
	}

	public void makeDirty() {
	     likelihoodKnown = false;
	}

	protected void handleModelChangedEvent(Model model, Object object, int index) {
	   likelihoodKnown = false;
	}

    protected void storeState() {
        storedLikelihoodKnow = likelihoodKnown;
        storedLogLikelihood = logLikelihood;

    }
    protected void restoreState() {
        likelihoodKnown = storedLikelihoodKnow;
        logLikelihood = storedLogLikelihood;

    }

    protected void handleVariableChangedEvent(Variable variable, int index, Parameter.ChangeType type) {
        likelihoodKnown = false;
    }

	protected void acceptState() {
	}
	
    // ****************************************************************
    // Private and protected stuff
    // ****************************************************************
	
	Parameter R0 = null;
	Parameter omega = null;
	Parameter latent = null;
	Parameter indicators = null;
    private boolean likelihoodKnown;
    private boolean storedLikelihoodKnow;
    private double logLikelihood;
    private double storedLogLikelihood;
	   
}
