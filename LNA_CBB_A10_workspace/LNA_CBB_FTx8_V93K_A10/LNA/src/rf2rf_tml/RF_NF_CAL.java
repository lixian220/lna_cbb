package src.rf2rf_tml;

import java.util.Arrays;
import java.util.List;

import src.rf2rf_tml.RF2RFBurst_TEST.StepMeas;
import src.rf2rf_tml.utilities.MapDatalog;
import src.rf2rf_tml.utilities.UtMethods;
import src.rfcbb_tml.com.CalData;
import xoc.dta.TestMethod;
import xoc.dta.datatypes.MultiSiteDouble;

/**
 * This test method provide a sub function to finish NF calibration.
 * It can be called by user in test method.
 *
 * @since 1.0.0
 * @author 308770
 *
 */



public class RF_NF_CAL extends TestMethod{

    protected String testsuiteName = "";
    protected MultiSiteDouble in_Y;
    protected MultiSiteDouble Y;
    protected MultiSiteDouble act_enr;
    protected String keyName;
    protected MultiSiteDouble inLoss;
    protected MultiSiteDouble nfEff;
    protected MultiSiteDouble mGain_HG1;
    protected MultiSiteDouble mGain_LG3;
    protected int[] activeSites;

    MapDatalog mDataMap = MapDatalog.getInstance();


    /**
     *  {@code NF_CAL} is a method to do NF calibration.
     *  It gets setup information and data information from input arguments and do the NF calculation and calibration.
     *  After Calibration, it save calibration data to global data container.
     *
     * @param meas              setup parameter group
     * @param mCalData          data container for calibration data
     *
     *
     * @since 1.0.0
     */
    public void NF_CAL(StepMeas meas,CalData mCalData){
        meas.ptd_Rslt.setTestText("Y_HG1");
        meas.ptd_Rslt.evaluate(in_Y);
        meas.ptd_Rslt.setTestText("Y_LG3");
        meas.ptd_Rslt.evaluate(Y);

        mDataMap.setValue(keyName, "Y_HG1", in_Y);
        mDataMap.setValue(keyName, "Y_LG3", Y);

        MultiSiteDouble Y1 = mDataMap.getMeanValue(keyName, "Y_HG1", activeSites);//eliminate "NA" data
        MultiSiteDouble Y2 = mDataMap.getMeanValue(keyName, "Y_LG3", activeSites);

        MultiSiteDouble g_GainH =mCalData.getValue(0,keyName,"golden_Gain_HG1",0.0);
        MultiSiteDouble g_GainL =mCalData.getValue(0,keyName,"golden_Gain_LG",0.0);
        MultiSiteDouble g_NfH =mCalData.getValue(0,keyName,"golden_NF_HG1",0.0);
        MultiSiteDouble g_NfL =mCalData.getValue(0,keyName,"golden_NF_LG",0.0);

        MultiSiteDouble InLoss_Com = mCalData.getValue(keyName, "cal_InLoss",0.0);
        MultiSiteDouble OutLoss_HG = mCalData.getValue( keyName, "cal_OutLoss",meas.Freq1/1.0e6);
        MultiSiteDouble OutLoss_LG = mCalData.getValue( keyName, "cal_OutLossL",meas.Freq1/1.0e6);
        MultiSiteDouble TotalLoss_HG;
        MultiSiteDouble TotalLoss_LG;
        TotalLoss_HG = InLoss_Com.add(OutLoss_HG);
        TotalLoss_LG = InLoss_Com.add(OutLoss_LG);

        MultiSiteDouble F1 = UtMethods.db2linear(g_NfH);
        MultiSiteDouble F2 = UtMethods.db2linear(g_NfL);
        MultiSiteDouble G1 = UtMethods.db2linear(g_GainH);
        MultiSiteDouble G2 = UtMethods.db2linear(g_GainL);
        MultiSiteDouble ENR = UtMethods.db2linear(act_enr);
        List<MultiSiteDouble>tmpList = Calc_inloss_Fr( F1, F2, Y1, Y2, G1, G2, ENR);


        inLoss = tmpList.get(0);
        nfEff = tmpList.get(1);

        MultiSiteDouble totalLossM = g_GainH.subtract(mGain_HG1).add(TotalLoss_HG);
        MultiSiteDouble outLossM = totalLossM.subtract(inLoss);
        MultiSiteDouble totalLossM_LG = g_GainL.subtract(mGain_LG3).add(TotalLoss_LG);
        MultiSiteDouble outLossM_LG = totalLossM_LG.subtract(inLoss);

        meas.ptd_Rslt.setTestText("InLoss");
        meas.ptd_Rslt.evaluate(inLoss);
        meas.ptd_Rslt.setTestText("OutLoss_M");
        meas.ptd_Rslt.evaluate(outLossM);
        meas.ptd_Rslt.setTestText("OutLoss_M_LG");
        meas.ptd_Rslt.evaluate(outLossM_LG);
        meas.ptd_Rslt.setTestText("NFeff");
        meas.ptd_Rslt.evaluate(nfEff);

        mDataMap.setValue(keyName, "cal_InLoss",inLoss);
        mDataMap.setValue(keyName, "cal_OutLoss",meas.Freq1/1.0e6,outLossM);
        mDataMap.setValue(keyName, "cal_OutLossL",meas.Freq1/1.0e6,outLossM_LG);
        mDataMap.setValue(keyName, "cal_NFeff",nfEff);

        System.out.println(keyName+":In_loss  = "+inLoss);
        System.out.println(keyName+":cal_OutLossM  = "+outLossM);
        System.out.println(keyName+":cal_OutLossM_LG  = "+outLossM_LG);
        System.out.println(keyName+":F Receiver  = "+nfEff);

    }






    /**
     * this method is the algorithm to calculate inloss and effective NoiseFigure of receiver
     *
     *
     *
     * @param F1    NF for high Gain mode in linear
     * @param F2    NF for low Gain mode in linear
     * @param Y1    Y factor for high Gain mode in linear
     * @param Y2    Y factor for Low Gain mode  in linear
     * @param G1    Gain for high Gain mode in linear
     * @param G2    Gain for low Gain mode in linear
     * @param ENR   ENR value for Hot noise test in linear
     *
     *
     * @return two elements in{@code List<MultiSiteDouble>}, 1st is Inloss in dB, 2nd is effective NF for receiver in dB
     */

    private List<MultiSiteDouble> Calc_inloss_Fr( MultiSiteDouble F1, MultiSiteDouble F2, MultiSiteDouble Y1, MultiSiteDouble Y2,
            MultiSiteDouble G1, MultiSiteDouble G2, MultiSiteDouble ENR){
        /// Xdb = ENRdb - Input Loss
        /// Z = Freceiver -1
        // F = (Xlinear)/(Y-1) - (Freceiver-1)/Gain
        // F = X/(Y-1) - Z/G ;

        //        MultiSiteDouble G1= UtMethods.db2linear(G1db);
        //        MultiSiteDouble G2= UtMethods.db2linear(G2db);
        //        MultiSiteDouble F1= UtMethods.db2linear(F1db);
        //        MultiSiteDouble F2= UtMethods.db2linear(F2db);

        MultiSiteDouble tmp1 = G1.divide(Y1.subtract(1));
        MultiSiteDouble tmp2 = G2.divide(Y2.subtract(1));

        MultiSiteDouble X = (F1.multiply(G1).subtract(F2.multiply(G2))).divide(tmp1.subtract(tmp2));
        MultiSiteDouble Z = X.multiply(tmp1).subtract(F1.multiply(G1));

        MultiSiteDouble In_loss_db = UtMethods.linear2db(ENR).subtract(UtMethods.linear2db(X));
        MultiSiteDouble Freceiver = Z.add(1);


        return Arrays.asList(In_loss_db,UtMethods.linear2db(Freceiver));

    }
    @Override
    public void execute() {
        // TODO Auto-generated method stub

    }

}
