/*
 * ChainSamplingLikelihoodParser.java
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

import dr.evomodel.epidemiology.stutteringchains.ChainSamplingLikelihood;
import dr.inference.model.Likelihood;
import dr.inference.model.Parameter;
import dr.xml.*;

/**
 *
 */
public class ChainSamplingLikelihoodParser extends AbstractXMLObjectParser {

    public static final String TRIALS = "trials";
    public static final String INDICATORS = "indicators";
    public static final String COUNTS = "counts";
    public static final String PROPORTION = "proportion";
    public static final String VALUES = "values";
    public static final String ON_LOGIT_SCALE = "onLogitScale";

    public String getParserName() {
        return ChainSamplingLikelihood.CHAIN_SAMPLING_LIKELIHOOD;
    }

    public Object parseXMLObject(XMLObject xo) throws XMLParseException {

        final boolean onLogitScale = xo.getAttribute(ON_LOGIT_SCALE, false);

        XMLObject cxo = xo.getChild(COUNTS);
        Parameter countsParam = (Parameter) cxo.getChild(Parameter.class);

        cxo = xo.getChild(PROPORTION);
        Parameter proportionParam = (Parameter) cxo.getChild(Parameter.class);

        if (proportionParam.getDimension() != 1 && proportionParam.getDimension() != countsParam.getDimension()) {
            throw new XMLParseException("Proportion dimension (" + proportionParam.getDimension() + ") " +
                    "must equal 1 or counts dimension (" + countsParam.getDimension() + ")");
        }
        
        XMLObject ixo = xo.getChild(INDICATORS);
        Parameter indicators = (Parameter) ixo.getChild(Parameter.class);

        if (indicators.getDimension() != 1 && indicators.getDimension() != countsParam.getDimension()) {
            throw new XMLParseException("Indicators dimension (" + indicators.getDimension() + ") " +
                    "must equal 1 or counts dimension (" + countsParam.getDimension() + ")");
        }

        Parameter trialsParam;
        cxo = xo.getChild(TRIALS);
        if (cxo != null) {
            if (cxo.hasAttribute(VALUES)) {
                int[] tmp = cxo.getIntegerArrayAttribute(VALUES);
                double[] v = new double[tmp.length];
                for (int i = 0; i < tmp.length; ++i) {
                    v[i] = tmp[i];
                }
                trialsParam = new Parameter.Default(v);
            } else {
                trialsParam = (Parameter) cxo.getChild(Parameter.class);
            }
        } else {
            trialsParam = new Parameter.Default(1.0);
        }
        
        if (trialsParam.getDimension() <= 1) {
            trialsParam.setDimension(countsParam.getDimension());
        }
        if (trialsParam.getDimension() != countsParam.getDimension()) {
            throw new XMLParseException("Trials dimension (" + trialsParam.getDimension()
                    + ") must equal counts dimension (" + countsParam.getDimension() + ")");
        }


        return new ChainSamplingLikelihood(trialsParam, indicators, proportionParam, countsParam, onLogitScale);

    }

    //************************************************************************
    // AbstractXMLObjectParser implementation
    //************************************************************************

    public XMLSyntaxRule[] getSyntaxRules() {
        return rules;
    }

    private final XMLSyntaxRule[] rules = {
            AttributeRule.newBooleanRule(ON_LOGIT_SCALE, true),
            new ElementRule(COUNTS,
                    new XMLSyntaxRule[]{new ElementRule(Parameter.class)}),
            new ElementRule(PROPORTION,
                    new XMLSyntaxRule[]{new ElementRule(Parameter.class)}),
            new XORRule(
                    new ElementRule(TRIALS,
                            new XMLSyntaxRule[]{AttributeRule.newIntegerArrayRule(VALUES, false),})
                    ,
                    new ElementRule(TRIALS,
                            new XMLSyntaxRule[]{new ElementRule(Parameter.class)}
                    )),
    };

    public String getParserDescription() {
        return "Calculates the likelihood of some data given some parametric or empirical distribution.";
    }

    public Class getReturnType() {
        return Likelihood.class;
    }
}
