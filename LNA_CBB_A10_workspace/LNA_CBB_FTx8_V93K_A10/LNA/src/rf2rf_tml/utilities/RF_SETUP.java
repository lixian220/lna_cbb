package src.rf2rf_tml.utilities;

import java.util.Arrays;
import java.util.List;

import src.reg_tools.Reg_Read;
import src.rf2rf_tml.RF2RFBurst_TEST.StepMeas;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupDcVI.IImeas;
import xoc.dsa.ISetupProtocolInterface;
import xoc.dsa.ISetupRfMeas;
import xoc.dsa.ISetupRfMeas.IModPower.SetupReceiverPath;
import xoc.dsa.ISetupRfStim;
import xoc.dsa.ISetupRfStim.IModulated.SetupOptimization;
import xoc.dsa.ISetupRfStim.INoise;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dsa.ISetupWaveformSine;



/**
 * This class provide multiple methods to do the build the RF measure setups for all instruments and operation sequencers.
 * It has multiple member methods to address Gain, IDD, P1dB, Power Rejection, IIP3, IIP2 test setups.
 *
 * @since 1.0.0
 * @author 308770
 *
 */

public class RF_SETUP {


    public String USID = "";
    public String testsuiteName = "";
    public double IF_Freq= 27.12311e6;
    public boolean CalMode = false;
    public String VDDPIN = "VDD1P8";
    public double stepPowerdBm = 0.15;
    public int numPoints = 75;

    double Fres  = 1;

    Reg_Read mRegData = Reg_Read.getInstance();
    public String REGKEY(StepMeas meas,String testsuite){
        String Reg_Key = "";
        if(!meas.Reg.contains("0x")&&!meas.Reg.equals(""))
        {
           Reg_Key = testsuite+'_'+meas.Reg;
        }
        return Reg_Key;
    }


    /**
     * this method used to build up the operation sequencer and test setup for instruments for Gain test
     *
     *
     * @param meas          StepMeas type, read the setup information from testtable
     * @param devSetup      IDeviceSetup interface to build Gain test setup
     * @param setup_stim    ISetupRfStim interface to build Stimulus setup
     * @param setup_meas    ISetupRfMeas interface to build measure setups
     */
    public void GainSetup(StepMeas meas,IDeviceSetup devSetup,ISetupRfStim setup_stim,ISetupRfMeas setup_meas){

        String stimName = testsuiteName +"_Stim_"+meas.getId();
        String measName = testsuiteName +"_Meas_"+meas.getId();
        String Reg_Key = REGKEY(meas,testsuiteName);
        double Gain_BW = 50e3;//KHz
        int K = (int)(Gain_BW/Fres);
        Gain_BW = K* Fres;

        int CW_avg =1;
        double wait_time = 1e-3;

        if(CalMode)
        {
            CW_avg =8;
        }



        /* Register configure  */
            if(!meas.Reg.equals(""))
            {
                devSetup.parallelBegin();
                {

                    devSetup.sequentialBegin();
                    {
                        devSetup.insertOpSeqComment(meas.getId()+": Register configure");

                        ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("TS_Setup"+"_"+meas.getId(), "setups.mipi.mipi");
                        paInterface.addSignalRole("DATA", "SDATA");
                        paInterface.addSignalRole("CLK", "SCLK");
                        ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup"+"_"+meas.getId());

                        if(!meas.Reg.contains("0x"))
                        {

                            meas.Reg = mRegData.m_RegDataMap.get(Reg_Key);

                        }

                        List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
                        for(String perData: listAddrData){
                            List<String> values = Arrays.asList(perData.split("_"));
                            String addr = "<"+values.get(0)+">";
                            String data = "<"+values.get(1)+">";
                            transDigSrc.addTransaction("write","<"+USID+">",addr,data);

                        }
                        devSetup.transactionSequenceCall(transDigSrc,"");

                    }
                    devSetup.sequentialEnd();
                }
                devSetup.parallelEnd();
            }

        /* RF STIM & Meas  */
        devSetup.parallelBegin();
        {
            /************STIM ACTION**********/
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": STIM");

                ISetupRfStim.ICw cwAction = setup_stim.cw(stimName);
                cwAction.setFrequency(meas.Freq1).setPower(meas.InPow);
                devSetup.actionCall(stimName);


            }
            devSetup.sequentialEnd();

            /*  MEAS ACTION  */
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": MEAS");

//                IF_Freq = 3.14e6;
                ISetupRfMeas.ICwPower CwActionFs = setup_meas.cwPower(measName);
                CwActionFs.setFrequency(meas.Freq1).setIfFrequency(IF_Freq).setExpectedMaxPower(meas.MeasRange+10);//.setSamples(SampleSIze);
                CwActionFs.setBandwidthOfInterest(Gain_BW)/*.setIfFilterBandwidth(40.0e6)*/.setResultAveraging(CW_avg);//.setResultAveraging(CW_avg);
                devSetup.waitCall(wait_time);
                devSetup.actionCall(measName);


                /*Reg Reset*/
                if(!meas.RegReset.equals(""))
                {
                    if(!meas.RegReset.contains("0x"))
                    {
                        meas.RegReset=mRegData.m_RegDataMap.get(meas.RegReset);
                    }

                    ISetupProtocolInterface paInterface_reset = devSetup.addProtocolInterface("TS_ReSet"+"_"+meas.getId(), "setups.mipi.mipi");
                    paInterface_reset.addSignalRole("DATA", "SDATA");
                    paInterface_reset.addSignalRole("CLK", "SCLK");
                    ISetupTransactionSeqDef transDigSrcRest= paInterface_reset.addTransactionSequenceDef("TS_ReSet"+"_"+meas.getId());

                    List<String> listAddrData = Arrays.asList(meas.RegReset.split("\\s*;\\s*"));
                    for(String perData: listAddrData){
                        List<String> values = Arrays.asList(perData.split("_"));
                        String addr = "<"+values.get(0)+">";
                        String data = "<"+values.get(1)+">";
                        transDigSrcRest.addTransaction("write","<"+USID+">",addr,data);
                    }
                    devSetup.transactionSequenceCall(transDigSrcRest,"");

                }


            }
            devSetup.sequentialEnd();


        }
        devSetup.parallelEnd();


    }



    /**
     * this method used to build up the operation sequencer and test setup for instruments for IDD test
     *
     *
     * @param meas          StepMeas type, read the setup information from testtable
     * @param devSetup      IDeviceSetup interface to build Gain test setup
     * @param setup_stim    ISetupRfStim interface to build Stimulus setup
     * @param setup_meas    ISetupRfMeas interface to build measure setups
     */
    public void IDDSetup(StepMeas meas,IDeviceSetup devSetup,ISetupRfStim setup_stim,ISetupRfMeas setup_meas){

        String stimName = testsuiteName +"_Stim_"+meas.getId();
        String measName = testsuiteName +"_Meas_"+meas.getId();
        String Reg_Key = REGKEY(meas,testsuiteName);
        double wait_time = 5e-3;



        /* Register configure  */
            if(!meas.Reg.equals(""))
            {
                devSetup.parallelBegin();
                {

                    devSetup.sequentialBegin();
                    {
                        devSetup.insertOpSeqComment(meas.getId()+": Register configure");

                        ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("TS_Setup"+"_"+meas.getId(), "setups.mipi.mipi");
                        paInterface.addSignalRole("DATA", "SDATA");
                        paInterface.addSignalRole("CLK", "SCLK");
                        ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup"+"_"+meas.getId());

                        if(!meas.Reg.contains("0x"))
                        {

                            meas.Reg = mRegData.m_RegDataMap.get(Reg_Key);

                        }

                        List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
                        for(String perData: listAddrData){
                            List<String> values = Arrays.asList(perData.split("_"));
                            String addr = "<"+values.get(0)+">";
                            String data = "<"+values.get(1)+">";
                            transDigSrc.addTransaction("write","<"+USID+">",addr,data);

                        }
                        devSetup.transactionSequenceCall(transDigSrc,"");

                    }
                    devSetup.sequentialEnd();
                }
                devSetup.parallelEnd();
            }

        /* RF STIM & MEAS  */
        devSetup.parallelBegin();
        {
            /************STIM ACTION**********/
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": No STIM");
                ISetupRfStim.ICw cwAction = setup_stim.cw(stimName +"_Off");
                cwAction.setFrequency(5.991e9).setPower(-110);
                devSetup.actionCall(stimName  + "_Off");
            }
            devSetup.sequentialEnd();

            /*  MEAS ACTION  */
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": MEAS");


                IImeas DPS_meas = devSetup.addDcVI(VDDPIN).imeas(measName);
                DPS_meas.setWaitTime(wait_time).setIrange(meas.MeasRange*1e-3).setAverages(4)/*.setRestoreIrange(true)*//*.setHighAccuracy(true).setRestoreIrange(true)*/;
//                devSetup.waitCall(1);
                devSetup.actionCall(measName);


                /*Reg Reset*/
                if(!meas.RegReset.equals(""))
                {
                    if(!meas.RegReset.contains("0x"))
                    {
                        meas.RegReset=mRegData.m_RegDataMap.get(meas.RegReset);
                    }

                    ISetupProtocolInterface paInterface_reset = devSetup.addProtocolInterface("TS_ReSet"+"_"+meas.getId(), "setups.mipi.mipi");
                    paInterface_reset.addSignalRole("DATA", "SDATA");
                    paInterface_reset.addSignalRole("CLK", "SCLK");
                    ISetupTransactionSeqDef transDigSrcRest= paInterface_reset.addTransactionSequenceDef("TS_ReSet"+"_"+meas.getId());

                    List<String> listAddrData = Arrays.asList(meas.RegReset.split("\\s*;\\s*"));
                    for(String perData: listAddrData){
                        List<String> values = Arrays.asList(perData.split("_"));
                        String addr = "<"+values.get(0)+">";
                        String data = "<"+values.get(1)+">";
                        transDigSrcRest.addTransaction("write","<"+USID+">",addr,data);
                    }
                    devSetup.transactionSequenceCall(transDigSrcRest,"");

                }


            }
            devSetup.sequentialEnd();


        }
        devSetup.parallelEnd();


    }



    /**
     * this method used to build up the operation sequencer and test setup for instruments for P1dB test
     *
     *
     * @param meas          StepMeas type, read the setup information from testtable
     * @param devSetup      IDeviceSetup interface to build Gain test setup
     * @param setup_stim    ISetupRfStim interface to build Stimulus setup
     * @param setup_meas    ISetupRfMeas interface to build measure setups
     */
    public void P1dB_Setup(StepMeas meas,IDeviceSetup devSetup,ISetupRfStim setup_stim,ISetupRfMeas setup_meas){

        String stimName = testsuiteName +"_Stim_"+meas.getId();
        String measName = testsuiteName +"_Meas_"+meas.getId();
        String Reg_Key = REGKEY(meas,testsuiteName);
        double P1dB_BW = 50e3;
        int K = (int)(P1dB_BW/Fres);
        P1dB_BW = K*Fres;
        double wait_time = 1e-3;


        /* Register configure  */
            if(!meas.Reg.equals(""))
            {
                devSetup.parallelBegin();
                {
                    devSetup.sequentialBegin();
                    {
                        devSetup.insertOpSeqComment(meas.getId()+": Register configure");

                        ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("TS_Setup"+"_"+meas.getId(), "setups.mipi.mipi");
                        paInterface.addSignalRole("DATA", "SDATA");
                        paInterface.addSignalRole("CLK", "SCLK");
                        ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup"+"_"+meas.getId());

                        if(!meas.Reg.contains("0x"))
                        {

                            meas.Reg = mRegData.m_RegDataMap.get(Reg_Key);

                        }

                        List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
                        for(String perData: listAddrData){
                            List<String> values = Arrays.asList(perData.split("_"));
                            String addr = "<"+values.get(0)+">";
                            String data = "<"+values.get(1)+">";
                            transDigSrc.addTransaction("write","<"+USID+">",addr,data);

                        }
                        devSetup.transactionSequenceCall(transDigSrc,"");

                    }
                    devSetup.sequentialEnd();
                }
                devSetup.parallelEnd();
            }


        for(int i =0;i<numPoints;i++)
        {

            /* RF STIM & MEAS  */
            devSetup.parallelBegin();
            {
                /*  STIM ACTION  */
                devSetup.sequentialBegin();
                {
                    devSetup.insertOpSeqComment(meas.getId()+": STIM_"+i);
                    ISetupRfStim.ICw cwAction = setup_stim.cw(stimName+"_Index"+i);
                    cwAction.setFrequency(meas.Freq1).setPower(meas.InPow+i*stepPowerdBm);

                    devSetup.actionCall(stimName+"_Index"+i);
                }
                devSetup.sequentialEnd();

                /*  MEAS ACTION  */
                devSetup.sequentialBegin();
                {
                    devSetup.insertOpSeqComment(meas.getId()+": Meas_"+i);
                    ISetupRfMeas.ICwPower modActionFs_P1dB = setup_meas.cwPower(measName+"_Index"+i);
                    modActionFs_P1dB.setFrequency(meas.Freq1).setIfFrequency(IF_Freq).setExpectedMaxPower( meas.MeasRange-10+Math.round(i*stepPowerdBm) );//.setSamples(SampleSize);
                    modActionFs_P1dB.setBandwidthOfInterest(P1dB_BW);

                    devSetup.waitCall(wait_time);
                    devSetup.actionCall(measName+"_Index"+i);


                    /*Reg Reset*/
                    if(!meas.RegReset.equals(""))
                    {
                        if(!meas.RegReset.contains("0x"))
                        {
                            meas.RegReset=mRegData.m_RegDataMap.get(meas.RegReset);
                        }

                        ISetupProtocolInterface paInterface_reset = devSetup.addProtocolInterface("TS_ReSet"+"_"+meas.getId(), "setups.mipi.mipi");
                        paInterface_reset.addSignalRole("DATA", "SDATA");
                        paInterface_reset.addSignalRole("CLK", "SCLK");
                        ISetupTransactionSeqDef transDigSrcRest= paInterface_reset.addTransactionSequenceDef("TS_ReSet"+"_"+meas.getId());

                        List<String> listAddrData = Arrays.asList(meas.RegReset.split("\\s*;\\s*"));
                        for(String perData: listAddrData){
                            List<String> values = Arrays.asList(perData.split("_"));
                            String addr = "<"+values.get(0)+">";
                            String data = "<"+values.get(1)+">";
                            transDigSrcRest.addTransaction("write","<"+USID+">",addr,data);
                        }
                        devSetup.transactionSequenceCall(transDigSrcRest,"");

                    }
                }
                devSetup.sequentialEnd();

            }
            devSetup.parallelEnd();

        }

    }


    /**
     * this method used to build up the operation sequencer and test setup for instruments for PowerRejection test
     *
     *
     * @param meas          StepMeas type, read the setup information from testtable
     * @param devSetup      IDeviceSetup interface to build Gain test setup
     * @param setup_stim    ISetupRfStim interface to build Stimulus setup
     * @param setup_meas    ISetupRfMeas interface to build measure setups
     */
    public void PowerRejection(StepMeas meas,IDeviceSetup devSetup,ISetupRfStim setup_stim,ISetupRfMeas setup_meas){

        String stimName = testsuiteName +"_Stim_"+meas.getId();
        String measName = testsuiteName +"_Meas_"+meas.getId();
        String Reg_Key = REGKEY(meas,testsuiteName);
        double wait_time = 0.8e-3;
        double REJ_BW = 200e3;
        int K = (int)(REJ_BW/Fres);
        REJ_BW = K*Fres;
        double H3Freq = meas.Freq1;
        if(meas.getId().indexOf("LF_Rej")!=-1)
        {
            H3Freq = meas.Freq1;
        }
        else if(meas.getId().indexOf("H3")!=-1)
        {
            H3Freq = meas.Freq1 * 3;
        }


        /* Register configure  */
            if(!meas.Reg.equals(""))
            {
                devSetup.parallelBegin();
                {

                    devSetup.sequentialBegin();
                    {
                        devSetup.insertOpSeqComment(meas.getId()+": Register configure");

                        ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("TS_Setup"+"_"+meas.getId(), "setups.mipi.mipi");
                        paInterface.addSignalRole("DATA", "SDATA");
                        paInterface.addSignalRole("CLK", "SCLK");
                        ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup"+"_"+meas.getId());

                        if(!meas.Reg.contains("0x"))
                        {

                            meas.Reg = mRegData.m_RegDataMap.get(Reg_Key);

                        }

                        List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
                        for(String perData: listAddrData){
                            List<String> values = Arrays.asList(perData.split("_"));
                            String addr = "<"+values.get(0)+">";
                            String data = "<"+values.get(1)+">";
                            transDigSrc.addTransaction("write","<"+USID+">",addr,data);

                        }
                        devSetup.transactionSequenceCall(transDigSrc,"");

                    }
                    devSetup.sequentialEnd();
                }
                devSetup.parallelEnd();
            }

        /* RF STIM & MEAS  */
        devSetup.parallelBegin();
        {
            /*  STIM ACTION  */
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": STIM");
                ISetupRfStim.ICw cwAction = setup_stim.cw(stimName);
                cwAction.setFrequency(meas.Freq1).setPower(meas.InPow);
                devSetup.actionCall(stimName);

            }
            devSetup.sequentialEnd();

            /*  MEAS ACTION  */
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": MEAS");
                ISetupRfMeas.ICwPower modActionFs_LFRej = setup_meas.cwPower(measName);
                modActionFs_LFRej.setFrequency(H3Freq).setIfFrequency(IF_Freq).setExpectedMaxPower(meas.MeasRange);
                modActionFs_LFRej.setBandwidthOfInterest(REJ_BW);//.setResultAveraging(3);
                devSetup.waitCall(wait_time);
                devSetup.actionCall(measName);

                /*Reg Reset*/
                if(!meas.RegReset.equals(""))
                {
                    if(!meas.RegReset.contains("0x"))
                    {
                        meas.RegReset=mRegData.m_RegDataMap.get(meas.RegReset);
                    }

                    ISetupProtocolInterface paInterface_reset = devSetup.addProtocolInterface("TS_ReSet"+"_"+meas.getId(), "setups.mipi.mipi");
                    paInterface_reset.addSignalRole("DATA", "SDATA");
                    paInterface_reset.addSignalRole("CLK", "SCLK");
                    ISetupTransactionSeqDef transDigSrcRest= paInterface_reset.addTransactionSequenceDef("TS_ReSet"+"_"+meas.getId());

                    List<String> listAddrData = Arrays.asList(meas.RegReset.split("\\s*;\\s*"));
                    for(String perData: listAddrData){
                        List<String> values = Arrays.asList(perData.split("_"));
                        String addr = "<"+values.get(0)+">";
                        String data = "<"+values.get(1)+">";
                        transDigSrcRest.addTransaction("write","<"+USID+">",addr,data);
                    }
                    devSetup.transactionSequenceCall(transDigSrcRest,"");

                }


            }
            devSetup.sequentialEnd();


        }
        devSetup.parallelEnd();

    }


    /**
     * this method used to build up the operation sequencer and test setup for instruments for IIP3 test
     *
     *
     * @param meas          StepMeas type, read the setup information from testtable
     * @param devSetup      IDeviceSetup interface to build Gain test setup
     * @param setup_stim    ISetupRfStim interface to build Stimulus setup
     * @param setup_meas    ISetupRfMeas interface to build measure setups
     */
    public void IIP3Setup(StepMeas meas,IDeviceSetup devSetup,ISetupRfStim setup_stim,ISetupRfMeas setup_meas){

        String stimName = testsuiteName +"_Stim_"+meas.getId();
        String measName = testsuiteName +"_Meas_"+meas.getId();
        String Reg_Key = REGKEY(meas,testsuiteName);
        double wfreq = Math.abs(meas.Freq1-meas.Freq2)/2.0;

        int Avg_IIP = 1;
        double wait_time =1e-3;
        double IIP_BW = 0.2e6;
        int K = (int)(IIP_BW/Fres);
        IIP_BW = K*Fres;

        double MeasuredFreq_I2 = 0.0;
        if(meas.getId().contains("IIP3"))
        {
            MeasuredFreq_I2 =meas.Freq2*2-meas.Freq1;
        }
        else if(meas.getId().contains("IIP2"))
        {
            MeasuredFreq_I2 =meas.Freq2+meas.Freq1;
        }


        /* Register configure  */
            if(!meas.Reg.equals(""))
            {
                devSetup.parallelBegin();
                {

                    devSetup.sequentialBegin();
                    {
                        devSetup.insertOpSeqComment(meas.getId()+": Register configure");

                        ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("TS_Setup"+"_"+meas.getId(), "setups.mipi.mipi");
                        paInterface.addSignalRole("DATA", "SDATA");
                        paInterface.addSignalRole("CLK", "SCLK");
                        ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup"+"_"+meas.getId());

                        if(!meas.Reg.contains("0x"))
                        {

                            meas.Reg = mRegData.m_RegDataMap.get(Reg_Key);

                        }

                        List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
                        for(String perData: listAddrData){
                            List<String> values = Arrays.asList(perData.split("_"));
                            String addr = "<"+values.get(0)+">";
                            String data = "<"+values.get(1)+">";
                            transDigSrc.addTransaction("write","<"+USID+">",addr,data);

                        }
                        devSetup.transactionSequenceCall(transDigSrc,"");

                    }
                    devSetup.sequentialEnd();
                }
                devSetup.parallelEnd();
            }

        /* RF STIM & MEAS  */
        devSetup.parallelBegin();
        {
            /*  STIM ACTION  */
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": STIM");

                ISetupWaveformSine SineWave = devSetup.addWaveformSine(stimName);
                SineWave.setAmplitude(1.0).setFrequency(wfreq);
                SineWave.setSampleRate(204.8e6).setSamples(20480L);
              //ISetupWaveformSampleBased SineWave = devSetup.addWaveformSampleBased(stimName);
                //SineWave.setDataFile("waveforms/WCDMA_TM4.wfm");//.setSampleRate(61.44e6);

                ISetupRfStim.IModulated modStim = setup_stim.modulated(stimName);
                modStim.setFrequency((meas.Freq1+meas.Freq2)/2.0).setPower(meas.InPow).setWaveformI(SineWave).setWaveformQ(SineWave).setOptimization(SetupOptimization.imd);
                modStim.setRepeatInfinite(true);
                modStim.setBasebandAttenuation(3);

                devSetup.actionCall(stimName );

            }
            devSetup.sequentialEnd();

            /*  MEAS ACTION  */
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": MEAS");
//                ISetupRfMeas.ICwPower modActionFs_F2 = setup_meas.cwPower(measName+"_F2");
//                Double measFreq_F2 = meas.Freq2;
//                modActionFs_F2.setFrequency(measFreq_F2).setIfFrequency(IF_Freq).setExpectedMaxPower(meas.MeasRange-15);//.setSamples(SampleSize);
//                modActionFs_F2.setBandwidthOfInterest(IIP_BW);//.setSampleRate(2.048e6);
//                devSetup.waitCall(wait_time);
//                devSetup.actionCall(measName+"_F2");


                ISetupRfMeas.ICwPower modActionFs_I2 = setup_meas.cwPower(measName+"_I2");
                modActionFs_I2.setFrequency(MeasuredFreq_I2).setIfFrequency(IF_Freq).setExpectedMaxPower(meas.MeasRange-15).setResultAveraging(Avg_IIP);//.setSamples(SampleSize);
                modActionFs_I2.setBandwidthOfInterest(IIP_BW);//(Bandwidth/8);//.setSampleRate(2.048e6);
                devSetup.waitCall(wait_time);
                devSetup.actionCall(measName+"_I2");



                /*Reg Reset*/
                if(!meas.RegReset.equals(""))
                {
                    if(!meas.RegReset.contains("0x"))
                    {
                        meas.RegReset=mRegData.m_RegDataMap.get(meas.RegReset);
                    }

                    ISetupProtocolInterface paInterface_reset = devSetup.addProtocolInterface("TS_ReSet"+"_"+meas.getId(), "setups.mipi.mipi");
                    paInterface_reset.addSignalRole("DATA", "SDATA");
                    paInterface_reset.addSignalRole("CLK", "SCLK");
                    ISetupTransactionSeqDef transDigSrcRest= paInterface_reset.addTransactionSequenceDef("TS_ReSet"+"_"+meas.getId());

                    List<String> listAddrData = Arrays.asList(meas.RegReset.split("\\s*;\\s*"));
                    for(String perData: listAddrData){
                        List<String> values = Arrays.asList(perData.split("_"));
                        String addr = "<"+values.get(0)+">";
                        String data = "<"+values.get(1)+">";
                        transDigSrcRest.addTransaction("write","<"+USID+">",addr,data);
                    }
                    devSetup.transactionSequenceCall(transDigSrcRest,"");

                }

            }
            devSetup.sequentialEnd();

        }
        devSetup.parallelEnd();
        /*   STIM OFF  */
        if(meas.getId().contains("IIP"))
        {
            devSetup.parallelBegin();
            {
            ISetupRfStim.ICw cwAction = setup_stim.cw(stimName +"_Off");
            cwAction.setFrequency(5.991e9).setPower(-110);
            devSetup.actionCall(stimName + "_Off");

            }
            devSetup.parallelEnd();
        }




    }



    /**
     * this method used to build up the operation sequencer and test setup for instruments for IIP2 test
     *
     *
     * @param meas          StepMeas type, read the setup information from testtable
     * @param devSetup      IDeviceSetup interface to build Gain test setup
     * @param setup_stim    ISetupRfStim interface to build Stimulus setup
     * @param setup_meas    ISetupRfMeas interface to build measure setups
     */
    public void IIP2Setup(StepMeas meas,IDeviceSetup devSetup,ISetupRfStim setup_stim,ISetupRfMeas setup_meas){

        String stimName = testsuiteName +"_Stim_"+meas.getId();
        String measName = testsuiteName +"_Meas_"+meas.getId();
        String Reg_Key = REGKEY(meas,testsuiteName);

        int Avg_IIP = 1;
        double wait_time =5e-3;
        double IIP_BW = 0.1e6;
        int K = (int)(IIP_BW/Fres);
        IIP_BW = K*Fres;

        double MeasuredFreq_I2 = 0.0;
        if(meas.getId().contains("IIP3"))
        {
            MeasuredFreq_I2 =meas.Freq2*2-meas.Freq1;
        }
        else if(meas.getId().contains("IIP2"))
        {
            MeasuredFreq_I2 =meas.Freq2+meas.Freq1;
        }


        /* Register configure  */
            if(!meas.Reg.equals(""))
            {
                devSetup.parallelBegin();
                {

                    devSetup.sequentialBegin();
                    {
                        devSetup.insertOpSeqComment(meas.getId()+": Register configure");

                        ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("TS_Setup"+"_"+meas.getId(), "setups.mipi.mipi");
                        paInterface.addSignalRole("DATA", "SDATA");
                        paInterface.addSignalRole("CLK", "SCLK");
                        ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup"+"_"+meas.getId());

                        if(!meas.Reg.contains("0x"))
                        {

                            meas.Reg = mRegData.m_RegDataMap.get(Reg_Key);

                        }

                        List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
                        for(String perData: listAddrData){
                            List<String> values = Arrays.asList(perData.split("_"));
                            String addr = "<"+values.get(0)+">";
                            String data = "<"+values.get(1)+">";
                            transDigSrc.addTransaction("write","<"+USID+">",addr,data);

                        }
                        devSetup.transactionSequenceCall(transDigSrc,"");

                    }
                    devSetup.sequentialEnd();
                }
                devSetup.parallelEnd();
            }

        /* RF STIM & MEAS  */
        devSetup.parallelBegin();
        {
            /*  STIM ACTION  */
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": STIM");

              ISetupRfStim.IDualSource cwAction = setup_stim.dualSource(stimName);
              cwAction.source1Cw().setFrequency(meas.Freq1).setPower(meas.InPow);
              cwAction.source2Cw().setFrequency(meas.Freq2).setPower(meas.InPow);

              devSetup.actionCall(stimName );

            }
            devSetup.sequentialEnd();

            /*  MEAS ACTION  */
            devSetup.sequentialBegin();
            {
                devSetup.insertOpSeqComment(meas.getId()+": MEAS");
//                ISetupRfMeas.ICwPower modActionFs_F2 = setup_meas.cwPower(measName+"_F2");
//                Double measFreq_F2 = meas.Freq2;
//                modActionFs_F2.setFrequency(measFreq_F2).setIfFrequency(IF_Freq).setExpectedMaxPower(meas.MeasRange);//.setSamples(SampleSize);
//                modActionFs_F2.setBandwidthOfInterest(IIP_BW);//.setSampleRate(2.048e6);
//                devSetup.waitCall(wait_time);
//                devSetup.actionCall(measName+"_F2");


                ISetupRfMeas.ICwPower modActionFs_I2 = setup_meas.cwPower(measName+"_I2");
                modActionFs_I2.setFrequency(MeasuredFreq_I2).setIfFrequency(IF_Freq).setExpectedMaxPower(meas.MeasRange-20).setResultAveraging(Avg_IIP);//.setSamples(SampleSize);
                modActionFs_I2.setBandwidthOfInterest(IIP_BW);//(Bandwidth/8);//.setSampleRate(2.048e6);
                devSetup.waitCall(wait_time);
                devSetup.actionCall(measName+"_I2");



                /*Reg Reset*/
                if(!meas.RegReset.equals(""))
                {
                    if(!meas.RegReset.contains("0x"))
                    {
                        meas.RegReset=mRegData.m_RegDataMap.get(meas.RegReset);
                    }

                    ISetupProtocolInterface paInterface_reset = devSetup.addProtocolInterface("TS_ReSet"+"_"+meas.getId(), "setups.mipi.mipi");
                    paInterface_reset.addSignalRole("DATA", "SDATA");
                    paInterface_reset.addSignalRole("CLK", "SCLK");
                    ISetupTransactionSeqDef transDigSrcRest= paInterface_reset.addTransactionSequenceDef("TS_ReSet"+"_"+meas.getId());

                    List<String> listAddrData = Arrays.asList(meas.RegReset.split("\\s*;\\s*"));
                    for(String perData: listAddrData){
                        List<String> values = Arrays.asList(perData.split("_"));
                        String addr = "<"+values.get(0)+">";
                        String data = "<"+values.get(1)+">";
                        transDigSrcRest.addTransaction("write","<"+USID+">",addr,data);
                    }
                    devSetup.transactionSequenceCall(transDigSrcRest,"");

                }

            }
            devSetup.sequentialEnd();

        }
        devSetup.parallelEnd();
        /*   STIM OFF  */
        if(meas.getId().contains("IIP2"))
        {
            devSetup.parallelBegin();
            {
              ISetupRfStim.IDualSource cwAction = setup_stim.dualSource(stimName +"_Off");
              cwAction.source1Cw().setFrequency(5.9e9).setPower(-110);
              cwAction.source2Cw().setFrequency(5.9e9).setPower(-110);
            devSetup.actionCall(stimName + "_Off");

            }
            devSetup.parallelEnd();
        }




    }



    /**
     * this method used to build up the operation sequencer and test setup for instruments for NF test
     *
     *
     * @param meas          StepMeas type, read the setup information from testtable
     * @param devSetup      IDeviceSetup interface to build Gain test setup
     * @param setup_stim    ISetupRfStim interface to build Stimulus setup
     * @param setup_meas    ISetupRfMeas interface to build measure setups
     */
    public void NFSetup(StepMeas meas,IDeviceSetup devSetupNF,ISetupRfStim setup_stimNF,ISetupRfMeas setup_measNF)
    {
        String stimName = testsuiteName +"_Stim_"+meas.getId();
        String measName = testsuiteName +"_Meas_"+meas.getId();
        String Reg_Key = REGKEY(meas,testsuiteName);

        int Avg_Cold = 1;
        int Avg_Hot =1;
        int SampleNF = 2048 * 8;
        int SamplesOfCold = SampleNF*4;
        int SamplesOfHot = SampleNF;
        double NF_BW = 5.0e6;   //TODO: 10.0e6
        double wait_time = 1e-3;
        if(testsuiteName.contains("HG"))
        {
            meas.MeasRange = meas.MeasRange-15;
        }
        else if(testsuiteName.contains("LG"))
        {
            meas.MeasRange = meas.MeasRange-20;
        }

//        if(CalMode)   //TODO: use same size between cal and non cal
//        {
            Avg_Cold = 16;
            Avg_Hot = 16;
            SamplesOfCold *= 4;
            SamplesOfHot *= 4;

//        }


        /* Register configure  */
            if(!meas.Reg.equals(""))
            {
                devSetupNF.parallelBegin();
                {

                    devSetupNF.sequentialBegin();
                    {
                        devSetupNF.insertOpSeqComment(meas.getId()+": Register configure");

                        ISetupProtocolInterface paInterface = devSetupNF.addProtocolInterface("TS_Setup"+"_"+meas.getId(), "setups.mipi.mipi");
                        paInterface.addSignalRole("DATA", "SDATA");
                        paInterface.addSignalRole("CLK", "SCLK");
                        ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup"+"_"+meas.getId());

                        if(!meas.Reg.contains("0x"))
                        {

                            meas.Reg = mRegData.m_RegDataMap.get(Reg_Key);

                        }

                        List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
                        for(String perData: listAddrData){
                            List<String> values = Arrays.asList(perData.split("_"));
                            String addr = "<"+values.get(0)+">";
                            String data = "<"+values.get(1)+">";
                            transDigSrc.addTransaction("write","<"+USID+">",addr,data);

                        }
                        devSetupNF.transactionSequenceCall(transDigSrc,"");

                    }
                    devSetupNF.sequentialEnd();
                }
                devSetupNF.parallelEnd();
            }

        /* HOT/COLD STIM & MEAS  */
        devSetupNF.parallelBegin();
        {
            /*Cold Power STIM&MEAS*/
            devSetupNF.insertOpSeqComment(meas.getId()+"_Cold");
            devSetupNF.sequentialBegin();
            {


                ISetupRfStim.ICw cwActionoff = setup_stimNF.cw(stimName +"_Off");
                cwActionoff.setFrequency(5.991e9).setPower(-110);
                devSetupNF.actionCall(stimName + "_Off");

                devSetupNF.waitCall(0.5e-3);

                @SuppressWarnings("unused")
                ISetupRfStim.ILoad cwAction = setup_stimNF.load(stimName+"_Cold");
                devSetupNF.actionCall(stimName+"_Cold");
            }
            devSetupNF.sequentialEnd();


            devSetupNF.sequentialBegin();
            {
                ISetupRfMeas.IModPower modActionFs = setup_measNF.modPower(measName+"_Cold");
                modActionFs.setFrequency(meas.Freq1).setExpectedMaxPower(meas.MeasRange-5).setSamples(SamplesOfCold).setResultAveraging(Avg_Cold);
                modActionFs.setBandwidthOfInterest(NF_BW).setIfFrequency(IF_Freq).setReceiverPath(SetupReceiverPath.preamp3);//.setSampleRate(204.8e6);
                devSetupNF.waitCall(wait_time+0.8e-3);
                devSetupNF.actionCall(measName+"_Cold" );
            }
            devSetupNF.sequentialEnd();

        }
        devSetupNF.parallelEnd();

        devSetupNF.parallelBegin();
        {
            /*Hot Power STIM&MEAS*/
            devSetupNF.insertOpSeqComment(meas.getId()+"_Hot");
            devSetupNF.sequentialBegin();
            {
                ISetupRfStim.INoise cwAction = setup_stimNF.noise(stimName+"_Hot");
                cwAction.setFrequency(meas.Freq1).setEnr(meas.InPow).setIdleCwPower(INoise.SetupIdleCwPower.rms); //15db normally
                devSetupNF.actionCall(stimName+"_Hot");
            }
            devSetupNF.sequentialEnd();


            devSetupNF.sequentialBegin();
            {
                ISetupRfMeas.IModPower modActionFs = setup_measNF.modPower(measName+"_Hot");
                modActionFs.setFrequency(meas.Freq1).setExpectedMaxPower(meas.MeasRange).setSamples(SamplesOfHot).setResultAveraging(Avg_Hot);
                modActionFs.setBandwidthOfInterest(NF_BW).setIfFrequency(IF_Freq).setReceiverPath(SetupReceiverPath.preamp3);

                devSetupNF.waitCall(wait_time);
                devSetupNF.actionCall(measName+"_Hot");
            }
            devSetupNF.sequentialEnd();

        }
        devSetupNF.parallelEnd();


        devSetupNF.parallelBegin();
        {
            devSetupNF.sequentialBegin();
            {
                if(!meas.RegReset.equals(""))
                {
                    if(!meas.RegReset.contains("0x"))
                        {
                           meas.RegReset = mRegData.m_RegDataMap.get(meas.RegReset);
                        }
                       ISetupProtocolInterface paInterface_reset = devSetupNF.addProtocolInterface("TS_ReSet"+"_"+meas.getId(), "setups.mipi.mipi");
                       paInterface_reset.addSignalRole("DATA", "SDATA");
                       paInterface_reset.addSignalRole("CLK", "SCLK");
                       ISetupTransactionSeqDef transDigSrcRest= paInterface_reset.addTransactionSequenceDef("TS_ReSet"+"_"+meas.getId());

                       List<String> listAddrData = Arrays.asList(meas.RegReset.split("\\s*;\\s*"));
                       for(String perData: listAddrData){
                           List<String> values = Arrays.asList(perData.split("_"));
                           String addr = "<"+values.get(0)+">";
                           String data = "<"+values.get(1)+">";
                           transDigSrcRest.addTransaction("write","<"+USID+">",addr,data);
                       }

                       devSetupNF.transactionSequenceCall(transDigSrcRest,"");
                }

            }
            devSetupNF.sequentialEnd();
        }
        devSetupNF.parallelEnd();




    }



    /**
     * this method used to build up the dummy setup for instruments
     *
     *
     * @param meas          StepMeas type, read the setup information from testtable
     * @param devSetup      IDeviceSetup interface to build Gain test setup
     * @param setup_stim    ISetupRfStim interface to build Stimulus setup
     * @param setup_meas    ISetupRfMeas interface to build measure setups
     */
    public void DummySetup(StepMeas meas,IDeviceSetup devSetupNFDummy,ISetupRfStim setup_stimNF)
    {
        String stimName = testsuiteName +"_Stim_"+meas.getId();


        devSetupNFDummy.sequentialBegin();
        {
            ISetupRfStim.INoise cwAction = setup_stimNF.noise(stimName+"_Hotdummy");
            cwAction.setFrequency(meas.Freq1).setEnr(meas.InPow).setIdleCwPower(INoise.SetupIdleCwPower.rms);
            devSetupNFDummy.actionCall(stimName+"_Hotdummy");
        }
        devSetupNFDummy.sequentialEnd();
    }



}
