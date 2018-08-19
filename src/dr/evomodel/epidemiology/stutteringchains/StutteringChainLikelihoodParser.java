/*
 * StutteringChainLikelihoodParser.java
 *
 * Copyright (c) 2002-2016 Alexei Drummond, Andrew Rambaut and Marc Suchard
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

import dr.evomodel.epidemiology.stutteringchains.StutteringChainLikelihood;
import dr.inference.model.Parameter;
import dr.xml.*;

/**
 */
public class StutteringChainLikelihoodParser extends AbstractXMLObjectParser {

    public static final String STUTTERING_CHAIN_LIKELIHOOD = "StutteringChainLikelihood";
    public static final String LATENT = "latent";
    public static final String INDICATORS = "indicators";
    public static final String R0 = "R0";
    public static final String OMEGA = "omega";

    public String getParserName() {
        return STUTTERING_CHAIN_LIKELIHOOD;
    }

    public Object parseXMLObject(XMLObject xo) throws XMLParseException {

        Parameter R0Parameter, omegaParameter, YParameter, indicatorsParameter;

        XMLObject cxo = xo.getChild(R0);
        if (cxo.getChild(0) instanceof Parameter) {
            R0Parameter = (Parameter) cxo.getChild(Parameter.class);
        } else {
            R0Parameter = new Parameter.Default(cxo.getDoubleChild(0));
        }

        cxo = xo.getChild(OMEGA);
        if (cxo.getChild(0) instanceof Parameter) {
            omegaParameter = (Parameter) cxo.getChild(Parameter.class);
        } else {
            omegaParameter = new Parameter.Default(cxo.getDoubleChild(0));
        }

        cxo = xo.getChild(LATENT);
        if (cxo.getChild(0) instanceof Parameter) {
            YParameter = (Parameter) cxo.getChild(Parameter.class);
        } else {
            YParameter = new Parameter.Default(cxo.getDoubleChild(0));
        }
        
        cxo = xo.getChild(INDICATORS);
        if (cxo.getChild(0) instanceof Parameter) {
            indicatorsParameter = (Parameter) cxo.getChild(Parameter.class);
        } else {
        	indicatorsParameter = new Parameter.Default(cxo.getDoubleChild(0));
        }
        return new StutteringChainLikelihood(R0Parameter, omegaParameter, YParameter, indicatorsParameter);
    }

    //************************************************************************
    // AbstractXMLObjectParser implementation
    //************************************************************************

    public XMLSyntaxRule[] getSyntaxRules() {
        return rules;
    }

    private final XMLSyntaxRule[] rules = {
            new ElementRule(R0,
                    new XMLSyntaxRule[]{
                            new XORRule(
                                    new ElementRule(Parameter.class),
                                    new ElementRule(Double.class)
                            )}
            ),
            new ElementRule(OMEGA,
                    new XMLSyntaxRule[]{
                            new XORRule(
                                    new ElementRule(Parameter.class),
                                    new ElementRule(Double.class)
                            )}
            ),
            new ElementRule(LATENT,
                    new XMLSyntaxRule[]{
                            new XORRule(
                                    new ElementRule(Parameter.class),
                                    new ElementRule(Double.class)
                            )}
            ),
            new ElementRule(INDICATORS,
                    new XMLSyntaxRule[]{
                            new XORRule(
                                    new ElementRule(Parameter.class),
                                    new ElementRule(Double.class)
                            )}
            )
    };

    public String getParserDescription() {
        return "Describes a Stutering chain with a given R0 and heterogeneity " +
                "only computes lik(Y_i) for indicators[i] == 1";
    }

    public Class getReturnType() {
        return StutteringChainLikelihood.class;
    }

}