package src.sparatest;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupRfVna;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteComplex;
import xoc.dta.datatypes.dsp.Graph;
import xoc.dta.datatypes.dsp.GraphType;
import xoc.dta.datatypes.dsp.Trace;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IRfVnaResults;
import xoc.dta.resultaccess.datatypes.MultiSiteSParameter;

/**
 * This test method performs 2-port S parameter test and plot S parameter to Signal Analyzer.
 *
 * @param  startFrequency       Start of the sweep frequency
 * @param  stopFrequency        Stop of the sweep frequency
 * @param  frequencyStep        increment of frequency from start to stop
 * @param  sPort1                one port in the 2-port S parameter test
 * @param  sPort2                another port in the 2-port S parameter test
 * @param  stimPower            stimulus power for S parameter test
 * @param  useDSAPI             whether use DSAPI to create setup
 *
 * @version 1.0.0
 * @author 308770
 *
 */

public class SPara extends TestMethod {

    public IMeasurement measurement;

    @In public long startFrequency = 500000000L;
    @In public long stopFrequency = 5900000000L;
    @In public long frequencyStep = 100000000L; //100MHz Step

    @In public String sPort1 = "RF_IN1";
    @In public String sPort2 = "RF_OUT";

    @In public double stimPower = -30.0;

    public String tsName = "";

    //    private MultiSiteLong debugMode;
    private boolean isReleaseTester = false;

    public boolean useDSAPI = true;
    private int numberOfSteps = 0;


    @Override
    public void setup()
    {
        if(useDSAPI)
        {
            tsName = context.getTestSuiteName();

            println(tsName + "::setup() is called on " + context.getActiveSites().length + "sites");

            IDeviceSetup ds = DeviceSetupFactory.createInstance();

            ISetupRfVna rfVna = ds.addRfVna(sPort1);
            rfVna.setConfigPortPort2(sPort2);
            //            rfVna.setConnect(true);

            for(long i=startFrequency; i<=stopFrequency; i=i+frequencyStep)
            {
                ISetupRfVna.ISParameter sPara = rfVna.sParameter("sPara_" + i);
                sPara.setFrequency(i*1.0);
                sPara.setStimPower(stimPower);
                sPara.setExpectedMaxPower(stimPower + 15);
                sPara.setBandwidthOfInterest(50e3); //100 kHz
                sPara.setResultAveraging(1);
                //                sPara.setReceiverPath(SetupReceiverPath.direct);
                //                sPara.setReceiverAttenuation(0);

                ds.waitCall(10e-3);
                ds.actionCall(sPara);
                //                ds.actionCall(sPara);
                numberOfSteps += 1;
            }
            measurement.setSetups(ds);

            System.out.println("numberOfSteps = " + numberOfSteps);
        }
    }

    @Override
    public void update()
    {
//         isReleaseTester = context.testProgram().variables().getBoolean("enableReleaseTester").get();
    }

    @Override
    public void execute()
    {
        measurement.execute();

        MultiSiteSParameter sPara;

        IRfVnaResults rfResults;
        String port1 = sPort1;

        rfResults = measurement.rfVna(port1).preserveResults();

//        if (isReleaseTester) {
//            releaseTester();
//        }

        if (useDSAPI)
        {
            MultiSiteComplex[] s11 = new MultiSiteComplex[numberOfSteps];
            MultiSiteComplex[] s12 = new MultiSiteComplex[numberOfSteps];
            MultiSiteComplex[] s21 = new MultiSiteComplex[numberOfSteps];
            MultiSiteComplex[] s22 = new MultiSiteComplex[numberOfSteps];

            double[] xArray = new double[numberOfSteps];
            double[][] s11MagArray = new double[context.getNumberOfConfiguredSites()][numberOfSteps];
            double[][] s11PhaseArray = new double[context.getNumberOfConfiguredSites()][numberOfSteps];
            double[][] s12MagArray = new double[context.getNumberOfConfiguredSites()][numberOfSteps];
            double[][] s12PhaseArray = new double[context.getNumberOfConfiguredSites()][numberOfSteps];
            double[][] s21MagArray = new double[context.getNumberOfConfiguredSites()][numberOfSteps];
            double[][] s21PhaseArray = new double[context.getNumberOfConfiguredSites()][numberOfSteps];
            double[][] s22MagArray = new double[context.getNumberOfConfiguredSites()][numberOfSteps];
            double[][] s22PhaseArray = new double[context.getNumberOfConfiguredSites()][numberOfSteps];



            System.out.println("Printing actionCall Frequencies");
            System.out.println("============================================");

            int idx = 0;
            int k = 0;

            for(long i=startFrequency; i<=stopFrequency; i=i+frequencyStep, idx++)
            {
                sPara = rfResults.sParameter("sPara_"+i).getSParameter(port1).getElement(k);

                s11[idx] = sPara.s11();
                s12[idx] = sPara.s12();
                s21[idx] = sPara.s21();
                s22[idx] = sPara.s22();

                System.out.println(i);

                xArray[idx] = i * 1.0;

                for(int site: context.getActiveSites())
                {
                    s11MagArray[site-1][idx] = sPara.s11Magnitude().get(site);
                    s11PhaseArray[site-1][idx] = sPara.s11Phase().get(site);

                    s12MagArray[site-1][idx] = sPara.s12Magnitude().get(site);
                    s12PhaseArray[site-1][idx] = sPara.s12Phase().get(site);

                    s21MagArray[site-1][idx] = sPara.s21Magnitude().get(site);
                    s21PhaseArray[site-1][idx] = sPara.s21Phase().get(site);

                    s22MagArray[site-1][idx] = sPara.s22Magnitude().get(site);
                    s22PhaseArray[site-1][idx] = sPara.s22Phase().get(site);

                }

                //                System.out.println("Spara = " + sPara);
                //                System.out.println("S11 (mag) = " + sPara.s11Magnitude());
                //                System.out.println("S11 (pha) = " + sPara.s11Phase());
                //                System.out.println();
                //
                //                System.out.println("S12 (mag)= " + sPara.s12Magnitude());
                //                System.out.println("S12 (pha) = " + sPara.s12Phase());
                //                System.out.println();
                //
                //                System.out.println("S21 (mag)= " + sPara.s21Magnitude());
                //                System.out.println("S21 (pha) = " + sPara.s21Phase());
                //                System.out.println();
                //
                //                System.out.println("S22 (mag)= " + sPara.s22Magnitude());
                //                System.out.println("S22 (pha) = " + sPara.s22Phase());
            }
            System.out.println("============================================");

            System.out.println();
            System.out.println();


            for(int site: context.getActiveSites())
            {
                System.out.println("Site " + site + " S11");
                System.out.println("============================================");
                for (int i = 0; i<idx; i++)
                {
                    System.out.println(s11[i].get(site).getReal() + "," + s11[i].get(site).getImaginary());
                }
                System.out.println("============================================");
                System.out.println();
                System.out.println();

                System.out.println("Site " + site + " S12");
                System.out.println("============================================");
                for (int i = 0; i<idx; i++)
                {
                    System.out.println(s12[i].get(site).getReal() + "," + s12[i].get(site).getImaginary());
                }
                System.out.println("============================================");
                System.out.println();
                System.out.println();

                System.out.println("Site " + site + " S21");
                System.out.println("============================================");
                for (int i = 0; i<idx; i++)
                {
                    System.out.println(s21[i].get(site).getReal() + "," + s21[i].get(site).getImaginary());
                }
                System.out.println("============================================");
                System.out.println();
                System.out.println();

                System.out.println("Site " + site + " S22");
                System.out.println("============================================");
                for (int i = 0; i<idx; i++)
                {
                    System.out.println(s22[i].get(site).getReal() + "," + s22[i].get(site).getImaginary());
                }
                System.out.println("============================================");
                System.out.println();
                System.out.println();
            }

            for (int site : context.getActiveSites())
            {
                //S11
                Trace trace = new Trace();
                trace.setName("S11 Magnitude"+k).setData(xArray, s11MagArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
                Graph graph = new Graph("S11 Magnitude"+k);
                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S11 Magnitude").plot();

                trace.setName("S11 Phase"+k).setData(xArray, s11PhaseArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
                graph = new Graph("S11 Phase"+k);
                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S11 Phase").plot();


                //S12
                trace.setName("S12 Magnitude"+k).setData(xArray, s12MagArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
                graph = new Graph("S12 Magnitude"+k);
                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S12 Magnitude").plot();

                trace.setName("S12 Phase"+k).setData(xArray, s12PhaseArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
                graph = new Graph("S12 Phase"+k);
                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S12 Phase").plot();

                //S21
                trace.setName("S21 Magnitude"+k).setData(xArray, s21MagArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
                graph = new Graph("S21 Magnitude"+k);
                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S21 Magnitude").plot();

                trace.setName("S21 Phase"+k).setData(xArray, s21PhaseArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
                graph = new Graph("S21 Phase"+k);
                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S21 Phase").plot();

                //S21
                trace.setName("S22 Magnitude"+k).setData(xArray, s22MagArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
                graph = new Graph("S22 Magnitude"+k);
                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S22 Magnitude").plot();

                trace.setName("S22 Phase"+k).setData(xArray, s22PhaseArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
                graph = new Graph("S22 Phase"+k);
                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S22 Phase").plot();
            }


            //            for (int site : context.getActiveSites())
            //            {
            //                //S11
            //                Trace trace = new Trace();
            //                trace.setName("S11 Magnitude").setData(xArray, s11MagArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
            //                Graph graph = new Graph("S11 Magnitude");
            //                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S11 Magnitude").plot();
            //
            //                trace.setName("S11 Phase").setData(xArray, s11PhaseArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
            //                graph = new Graph("S11 Phase");
            //                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S11 Phase").plot();
            //
            //
            //                //S12
            //                trace.setName("S12 Magnitude").setData(xArray, s12MagArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
            //                graph = new Graph("S12 Magnitude");
            //                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S12 Magnitude").plot();
            //
            //                trace.setName("S12 Phase").setData(xArray, s12PhaseArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
            //                graph = new Graph("S12 Phase");
            //                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S12 Phase").plot();
            //
            //                //S21
            //                trace.setName("S21 Magnitude").setData(xArray, s21MagArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
            //                graph = new Graph("S21 Magnitude");
            //                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S21 Magnitude").plot();
            //
            //                trace.setName("S21 Phase").setData(xArray, s21PhaseArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
            //                graph = new Graph("S21 Phase");
            //                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S21 Phase").plot();
            //
            //                //S21
            //                trace.setName("S22 Magnitude").setData(xArray, s22MagArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
            //                graph = new Graph("S22 Magnitude");
            //                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S22 Magnitude").plot();
            //
            //                trace.setName("S22 Phase").setData(xArray, s22PhaseArray[site-1]).setSite(site).setTestSuite(tsName).setSignal(port1);
            //                graph = new Graph("S22 Phase");
            //                graph.setGraphType(GraphType.SCATTER).add(trace).setComment("S22 Phase").plot();
            //            }

        }
        else
        {
            sPara = rfResults.sParameter("twoPort").getSParameter(port1).getElement(0);

            System.out.println("Printing actionCall:: twoPort");
            System.out.println("============================================");
            System.out.println("Spara = " + sPara);
            System.out.println("S11 (mag) = " + sPara.s11Magnitude());
            System.out.println("S11 (pha) = " + sPara.s11Phase());
            System.out.println();

            System.out.println("S12 (mag)= " + sPara.s12Magnitude());
            System.out.println("S12 (pha) = " + sPara.s12Phase());
            System.out.println();

            System.out.println("S21 (mag)= " + sPara.s21Magnitude());
            System.out.println("S21 (pha) = " + sPara.s21Phase());
            System.out.println();

            System.out.println("S22 (mag)= " + sPara.s22Magnitude());
            System.out.println("S22 (pha) = " + sPara.s22Phase());
            System.out.println("============================================");
            System.out.println();
            System.out.println();
        }


    }

}
