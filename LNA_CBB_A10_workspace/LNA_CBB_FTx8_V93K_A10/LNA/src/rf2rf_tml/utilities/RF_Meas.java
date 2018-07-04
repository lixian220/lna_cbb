package src.rf2rf_tml.utilities;

import java.util.ArrayList;
import java.util.List;

import src.rf2rf_tml.RF2RFBurst_TEST.StepMeas;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.resultaccess.IRfMeasResults;


/**
 * This class provide multiple methods to do the test result processing for RF.
 * It has 2 member methods to address P1dB and IIP test data calculations
 *
 *
 * @since 1.0.0
 * @author 308770
 *
 */
public class RF_Meas extends RF_SETUP{
    public MultiSiteDouble gainP1dB;
    public MultiSiteDouble gainIIP;
    public MultiSiteDouble InLoss;
    public MultiSiteDouble OutLoss;
    public int[] activeSites;
    public String testName;


    /**
     * This method is used to calculate P1dB measure results
     *
     * @param meas     StepMeas type, store measurement setup information
     * @param rfResult IRfMeasResults type, interface to get the raw data from measure instrument
     * @param rfOUT    ATE measure port name for measure instruments
     * @return {@code List<MultisiteDouble>} value of InputP1dB and OutputP1dB
     *
     * @since 1.0.0
     */
    public List<MultiSiteDouble> P1dB_Calc(StepMeas meas,IRfMeasResults rfResult,String rfOUT)
    {
        List<MultiSiteDouble> Rslt_List = new ArrayList<>();
        MultiSiteDouble rslt_IP1dB = new MultiSiteDouble(999);
        MultiSiteDouble rslt_OP1dB = new MultiSiteDouble(999);
        MultiSiteDouble power = new MultiSiteDouble(999);
        int NumP1dB = super.numPoints;
        double power_step = super.stepPowerdBm;
        String measName = testName +"_Meas_"+meas.getId();

        MultiSiteDouble[] rslt_p1dB = new MultiSiteDouble[NumP1dB];
        for(int i =0;i<NumP1dB;i++)
        {
            power = rfResult.cwPower(measName+"_Index"+i).getPower(rfOUT).getElement(0);
            rslt_p1dB[i] = power.add(OutLoss).add(InLoss);

        }

        MultiSiteDouble[] power_theory = new MultiSiteDouble[NumP1dB];

        for(int i =0; i<NumP1dB; i++)
        {
            power_theory[i] = gainP1dB.add(meas.InPow+i*power_step);
        }

        for(int site:activeSites)
        {
            double p1dBm = 999;
            for(int m = 0;m<NumP1dB;m++)
            {

//                if(rslt_p1dB[m+1].get(site)-rslt_p1dB[m].get(site)<1.2)
//                {
//                    double p1dBm = meas.InPow+(m+1)*2.0 + inLoss.get(site);
////                    println("Index>>>>>>"+m);
////                    println(testSuiteName+": Site "+site+">>>InputPower_P1dBm = "+p1dBm);
//                    rslt.set(site,p1dBm);
//                    break;
//                }

                double tmp = power_theory[m].get(site)-rslt_p1dB[m].get(site);

                if( tmp>0.95 /*&& power.get(site)>-10 */)
                {
                    p1dBm = meas.InPow+m*power_step - InLoss.get(site);
                    rslt_IP1dB.set(site,p1dBm);//with loss
                    rslt_OP1dB.set(site,p1dBm+gainP1dB.get(site));//with loss
                    break;//check multiSite !!!!!
                }
            }

        }
        Rslt_List.add(0,rslt_IP1dB);
        Rslt_List.add(1,rslt_OP1dB);


        return Rslt_List;


    }



    /**
     * This method is used to calculate IIP measure results
     *
     * @param meas          StepMeas type, store measurement setup information
     * @param rfResult      IRfMeasResults type, interface to get the IIP3 raw data from measure instrument
     * @param rfReslutIIP2  IRfMeasResults type, interface to get the IIP2 raw data from measure instrument
     * @param rfOUT         ATE measure port name for measure instruments
     * @return {@code MultiSiteDouble} for IIP2 or IIP3 measure results
     *
     * @since 1.0.0
     */
    public MultiSiteDouble IIP_Calc(StepMeas meas,IRfMeasResults rfResult,IRfMeasResults rfReslutIIP2,String rfOUT){

        MultiSiteDouble powerI2 = new MultiSiteDouble(999);
//        MultiSiteDouble powerF2 = new MultiSiteDouble(999);
        MultiSiteDouble IIP_H = new MultiSiteDouble(999);
        MultiSiteDouble rslt = new MultiSiteDouble(999);

        /*
         * use formula :
         * IIP3 = (3Pin+Gain-IM3)/2  --------> (Pout-IM3)/2+Pin
         * IIP2 = (2Pin+Gain-IM2)/2  --------> (Pout-IM2)+Pin
         */
        String measName = testName +"_Meas_"+meas.getId();
        if(meas.getId().contains("IIP2"))
        {
//            powerF2 = rfReslutIIP2.cwPower(measName+"_F2").getPower(rfOUT).getElement(0);
            powerI2 = rfReslutIIP2.cwPower(measName+"_I2").getPower(rfOUT).getElement(0);
            IIP_H = (gainIIP.add(meas.InPow*2).subtract(powerI2));
//            IIP_H = (powerF2.subtract(powerI2)).add(meas.InPow);
        }
        else if(meas.getId().contains("IIP3"))
        {
//            powerF2 = rfResult.cwPower(measName+"_F2").getPower(rfOUT).getElement(0);
            powerI2 = rfResult.cwPower(measName+"_I2").getPower(rfOUT).getElement(0);
            IIP_H = (gainIIP.add(meas.InPow*3).subtract(powerI2)).divide(2.0);
//            IIP_H = (powerF2.subtract(powerI2)).divide(2.0).add(meas.InPow);
        }
        rslt=IIP_H.subtract(InLoss);
//        rslt = IIP_H;
//      if(testSuiteName.contains("LB1_IN1"))
//      {
//        if(messageLogLevel>=0)
//        {
//            MultiSiteWaveComplex waveform_F1 = rfResult.cwPower(measName + "_"+meas.getId()).getComplexWaveform(rfOUT).getElement(0);
//            MultiSiteWaveComplex waveform_F2 = rfResult.cwPower(measName +"_F2").getComplexWaveform(rfOUT).getElement(0);
//            MultiSiteWaveComplex waveform_I1 = rfResult.cwPower(measName + "_"+meas.getId()+"_I1").getComplexWaveform(rfOUT).getElement(0);
//            MultiSiteWaveComplex waveform_I2 = rfResult.cwPower(measName +"_I2").getComplexWaveform(rfOUT).getElement(0);
//            MultiSiteSpectrum spect_F1 = waveform_F1.spectrum(SpectrumUnit.dBm);
//            MultiSiteSpectrum spect_F2 = waveform_F2.spectrum(SpectrumUnit.dBm);
//            MultiSiteSpectrum spect_I1 = waveform_I1.spectrum(SpectrumUnit.dBm);
//            MultiSiteSpectrum spect_I2 = waveform_I2.spectrum(SpectrumUnit.dBm);
//            spect_F1.plot("IIP_spect_F1", context.getTestSuiteName());
//            spect_F2.plot("IIP_spect_F2", context.getTestSuiteName());
//            spect_I1.plot("IIP_spect_I1", context.getTestSuiteName());
//            spect_I2.plot("IIP_spect_I2", "I2_SPEC"/*context.getTestSuiteName()*/);
//            meas.ptd_Rslt.setTestText("IIP_spect_F2");
//            meas.ptd_Rslt.evaluate(powerF2);
//            meas.ptd_Rslt.setTestText("IIP_spect_I2");
//            meas.ptd_Rslt.evaluate(powerI2);
//        }
//    }
        return rslt;

    }


}
