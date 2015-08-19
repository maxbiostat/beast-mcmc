package dr.evomodel.antigenic.phyloClustering.MCMCOperators;

import cern.jet.random.Beta;
import dr.evomodel.antigenic.phyloClustering.TreeClusteringVirusesPrior;
import dr.evomodel.antigenic.phyloClustering.misc.obsolete.AGLikelihoodTreeCluster;
import dr.evomodel.antigenic.phyloClustering.misc.obsolete.TreeClusterGibbsOperator;
import dr.evomodel.tree.TreeModel;
import dr.inference.model.MatrixParameter;
import dr.inference.model.Parameter;
import dr.inference.operators.GibbsOperator;
import dr.inference.operators.MCMCOperator;
import dr.inference.operators.OperatorFailedException;
import dr.inference.operators.SimpleMCMCOperator;
import dr.inference.operators.MCMCOperator.Utils;
import dr.xml.AbstractXMLObjectParser;
import dr.xml.AttributeRule;
import dr.xml.ElementRule;
import dr.xml.XMLObject;
import dr.xml.XMLObjectParser;
import dr.xml.XMLParseException;
import dr.xml.XMLSyntaxRule;


/**
 * A Gibbs operator for allocation of items to clusters under a distance dependent Chinese restaurant process.
 *
 * @author Charles Cheung
 * @author Trevor Bedford
 */
public class ProbSitesGibbsOperator  extends SimpleMCMCOperator implements GibbsOperator {
	
    public final static String CLASSNAME_OPERATOR = "ProbSitesGibbsOperator";

    private TreeClusteringVirusesPrior clusterPrior;
    private Parameter probSites;
    private int numSites;
    
    public ProbSitesGibbsOperator(double weight, TreeClusteringVirusesPrior clusterPrior_in, Parameter probSites_in) {  	
    	clusterPrior = clusterPrior_in;
    	probSites = probSites_in;
    	numSites = clusterPrior.getNumSites();
        setWeight(weight);
   
    }
    

	public double doOperation() throws OperatorFailedException { 
		
//		clusterPrior.sampleCausativeStates();
       int[] causalCount = clusterPrior.getCausalCount();
       int[] nonCausalCount = clusterPrior.getNonCausalCount();
		       
       //int numSites = 330;
       int numSites = clusterPrior.getNumSites();
 	   int whichSite = (int) (Math.floor(Math.random()*numSites)); //choose from possibilities

 	   //SHOULD GET IT FROM THE PRIOR SPECIFICATION COZ THEY SHOULD MATCH
 	   double value = Beta.staticNextDouble(causalCount[whichSite]+1, nonCausalCount[whichSite]+1); //posterior
 	   
 	   probSites.setParameterValue(whichSite, value);
 	   
 	  // System.out.println("hehe: " + whichSite + "," + probSites.getParameterValue(whichSite));
 	   
       
		return 0;
	}
	
	public void accept(double deviation) {
    	super.accept(deviation);	
	}

	public void reject() {
    	super.reject();	
	}

     
     //MCMCOperator INTERFACE
     public final String getOperatorName() {
         return CLASSNAME_OPERATOR;
     }



     public String getPerformanceSuggestion() {
         if (Utils.getAcceptanceProbability(this) < getMinimumAcceptanceLevel()) {
             return "";
         } else if (Utils.getAcceptanceProbability(this) > getMaximumAcceptanceLevel()) {
             return "";
         } else {
             return "";
         }
     }

 
    
 

     public static XMLObjectParser PARSER = new AbstractXMLObjectParser() {


    	public final static String PROBSITES = "probSites";

        public String getParserName() {
             return CLASSNAME_OPERATOR;
        }

         /* (non-Javadoc)
          * @see dr.xml.AbstractXMLObjectParser#parseXMLObject(dr.xml.XMLObject)
          */
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
         	
         	//System.out.println("Parser run. Exit now");
         	//System.exit(0);

             double weight = xo.getDoubleAttribute(MCMCOperator.WEIGHT);
             
             XMLObject cxo = xo.getChild(PROBSITES);
             Parameter probSites = (Parameter) cxo.getChild(Parameter.class);

             TreeClusteringVirusesPrior clusterPrior = (TreeClusteringVirusesPrior) xo.getChild(TreeClusteringVirusesPrior.class);

             return new ProbSitesGibbsOperator(weight, clusterPrior, probSites);

         }

         //************************************************************************
         // AbstractXMLObjectParser implementation
         //************************************************************************

         public String getParserDescription() {
             return "An operator that updates the probability of sites given a beta distribution.";
         }

         public Class getReturnType() {
             return ProbSitesGibbsOperator.class;
         }


         public XMLSyntaxRule[] getSyntaxRules() {
             return rules;
         }

         private final XMLSyntaxRule[] rules = {
                 AttributeRule.newDoubleRule(MCMCOperator.WEIGHT),
 	            new ElementRule(TreeClusteringVirusesPrior.class),
                new ElementRule(PROBSITES, Parameter.class),
         };
     
     };


 
     public int getStepCount() {
         return 1;
     }


}
