package dr.evomodel.epidemiology.casetocase;

import dr.evolution.util.Date;
import dr.evolution.util.Taxa;
import dr.evolution.util.Taxon;
import dr.evolution.util.Units;
import dr.inference.distribution.GammaDistributionModel;
import dr.inference.distribution.TruncatedNormalDistributionModel;
import dr.inference.model.Parameter;
import dr.math.IntegrableUnivariateFunction;
import dr.math.RiemannApproximation;
import dr.math.UnivariateFunction;
import dr.xml.*;

/**
 * New class for farms, discarding most of what's left of the original model and using numerical integration rather
 * than a discrete timescale based on days.
 *
 * User: Matthew Hall
 * Date: 07/09/2012
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */

public class LesionDatedFarmCase extends AbstractCase {

    public LesionDatedFarmCase(String name, Date examDate, Date cullDate, TruncatedNormalDistributionModel
            infectiousDate, Double oldestLesionAge, GammaDistributionModel incubationPeriod, Taxa associatedTaxa){
        this.name = name;
        //The time value for end of these days is the numerical value of these dates plus 1.
        this.examDate = examDate;
        endOfInfectiousDate = cullDate;
        this.infectiousDate = infectiousDate;
        this.associatedTaxa = associatedTaxa;
        this.oldestLesionAge = oldestLesionAge;
        this.incubationPeriod = incubationPeriod;
        infectionDate = new InfectionDatePDF();
    }

    public Date getLatestPossibleInfectionDate() {
        Double doubleDate = examDate.getTimeValue();
        return Date.createTimeSinceOrigin(doubleDate, Units.Type.DAYS, examDate.getOrigin());
    }

    public void installNumericalIntegrator(RiemannApproximation integrator) {
        numericalIntegrator = integrator;
    }

    public Taxa getAssociatedTaxa() {
        return associatedTaxa;
    }

    public boolean culledYet(int day) {
        return day>endOfInfectiousDate.getTimeValue()+1;
    }

    public double infectiousCDF(double time){
        return infectiousDate.cdf(time);
    }

    /* Probability that infection occurred between 'earliestInfection' and 'latestInfection' given that the case is
    infectious by 'infectiousBy' (NOT that it actually becomes infectious at that time). */

    public double periodInfectionDistribution(double earliestInfection, double latestInfection, double infectiousBy){
        JointDistribution tempDist = new JointDistribution(infectiousBy);
        return tempDist.evaluateIntegral(earliestInfection, latestInfection);
    }

    /* Probability distribution of infection date being 'argument' given that the case is infectious by
    'infectiousBy'. (NOT that it actually becomes infectious at that time). */

    public double infectionDistribution(double argument, double infectiousBy){
        JointDistribution tempDist = new JointDistribution(infectiousBy);
        return tempDist.evaluate(argument);
    }

    /*Parser.*/

    public static XMLObjectParser PARSER = new AbstractXMLObjectParser() {

        public static final String NAME = "name";
        public static final String CULL_DAY = "cullDay";
        public static final String EXAMINATION_DAY = "examinationDay";
        public static final String INCUBATION_PERIOD_DISTRIBUTION = "incubationPeriodDistribution";
        public static final String INFECTIOUS_DATE_DISTRIBUTION = "infectiousDateDistribution";
        public static final String OLDEST_LESION_AGE = "oldestLesionAge";

        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            String farmName = (String) xo.getAttribute(NAME);
            final Date cullDate = (Date) xo.getElementFirstChild(CULL_DAY);
            final Date examinationDate = (Date) xo.getElementFirstChild(EXAMINATION_DAY);
            final Parameter oldestLesionAgeParameter = (Parameter) xo.getElementFirstChild(OLDEST_LESION_AGE);
            Taxa tempTaxa = new Taxa();
            for(int i=0; i<xo.getChildCount(); i++){
                if(xo.getChild(i) instanceof Taxon){
                    tempTaxa.addTaxon((Taxon)xo.getChild(i));
                }
            }
            final Taxa associatedTaxa = tempTaxa;
            final GammaDistributionModel incubationPeriodDistribution =
                    (GammaDistributionModel) xo.getElementFirstChild(INCUBATION_PERIOD_DISTRIBUTION);
            final TruncatedNormalDistributionModel infectiousDateDistribution =
                    (TruncatedNormalDistributionModel) xo.getElementFirstChild(INFECTIOUS_DATE_DISTRIBUTION);
            return new LesionDatedFarmCase(farmName, examinationDate, cullDate, infectiousDateDistribution,
                    oldestLesionAgeParameter.getParameterValue(0), incubationPeriodDistribution, associatedTaxa);
        }

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return rules;
        }

        private final XMLSyntaxRule[] rules = {
                new StringAttributeRule(NAME, "The unique identifier for this farm"),
                new ElementRule(CULL_DAY, Date.class, "The date this farm was culled", false),
                new ElementRule(EXAMINATION_DAY, Date.class, "The date this farm was examined", false),
                new ElementRule(Taxon.class, 0, Integer.MAX_VALUE),
                new ElementRule(INFECTIOUS_DATE_DISTRIBUTION, TruncatedNormalDistributionModel.class, "The " +
                        "probability distribution of the infection date, in days since the date origins specified" +
                        "in the ", false),
                new ElementRule(INCUBATION_PERIOD_DISTRIBUTION, GammaDistributionModel.class, "The probability " +
                        "distribution of the incubation period of this farm", false),
                new ElementRule(OLDEST_LESION_AGE, Parameter.class, "The estimated oldest lesion date as determined" +
                        "by investigating vets")
        };

        @Override
        public String getParserDescription() {
            return "Parses XML (generated by oldMacDonald.py - contact mdhall272@gmail.com if interested) for " +
                    "descriptions of cases. Incubation periods are gamma distributed, dates of infectiousness have a " +
                    "truncated normal distribution with a maximum value equal to the time of sampling. Infection date" +
                    " is estimated by adding these together.";
        }

        @Override
        public Class getReturnType() {
            return LesionDatedFarmCase.class;
        }

        public String getParserName() {
            return NEW_MODEL_FARM;
        }
    };

    /* Probability for the infection date taking the value 'argument' and the incubation period being at most
     * 't2' minus 'argument'; i.e. infected at 'argument' and infectious before 't2'. */

    public class JointDistribution implements IntegrableUnivariateFunction{

        public JointDistribution(double t2){
            this.t2=t2;
        }

        public double evaluateIntegral(double a, double b) {
            return numericalIntegrator.integrate(this, a, b);
        }

        public double evaluate(double argument) {
            return infectionDate.evaluate(argument)*incubationPeriod.cdf(t2-argument);
        }

        public double getLowerBound() {
            return Double.NEGATIVE_INFINITY;
        }

        public double getUpperBound() {
            return t2;
        }

        private double t2;
    }


    /* Inner class for the probability distribution of the sum of the infectious date and minus the incubation period,
    i.e. the infection date. Not implementing Distribution since calculation of the mean, etc. is unnecessary.
     */

    public class InfectionDatePDF implements UnivariateFunction{

        public InfectionDatePDF(){
        }

        public double evaluate(double argument) {
            if(numericalIntegrator==null){
                throw new RuntimeException("Numerical integrator not specified.");
            } else {
                L_x_fi underlyingFunction = new L_x_fi(argument);
                return numericalIntegrator.integrate(underlyingFunction,argument,examDate.getTimeValue()+1);
            }
        }

        public double getLowerBound() {
            return Double.NEGATIVE_INFINITY;
        }

        public double getUpperBound() {
            return Double.POSITIVE_INFINITY;
        }
    }

    /* Calculation of InfectionDatePDF requires another integral */

    public class L_x_fi implements UnivariateFunction{

        public L_x_fi(double currentT){
            this.currentT = currentT;
        }

        public double evaluate(double argument) {
            if(argument<currentT || argument>examDate.getTimeValue()+1){
                return 0;
            } else {
                return incubationPeriod.pdf(argument-currentT)*infectiousDate.pdf(argument);
            }
        }

        public double getLowerBound() {
            return Double.NEGATIVE_INFINITY;
        }

        public double getUpperBound() {
            return Double.POSITIVE_INFINITY;
        }
        private double currentT;
    }

    public static final String NEW_MODEL_FARM = "newModelFarm";
    private Date examDate;
    private Date endOfInfectiousDate;
    private Double oldestLesionAge;
    private TruncatedNormalDistributionModel infectiousDate;
    private InfectionDatePDF infectionDate;
    private Taxa associatedTaxa;
    private RiemannApproximation numericalIntegrator;
    private GammaDistributionModel incubationPeriod;

}