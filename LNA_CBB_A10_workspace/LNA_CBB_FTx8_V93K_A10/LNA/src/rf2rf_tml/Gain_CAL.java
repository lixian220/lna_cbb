package src.rf2rf_tml;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import src.reg_tools.Reg_Read;
import src.rf2rf_tml.utilities.MapDatalog;
import src.rfcbb_tml.com.CalData;
import src.rfcbb_tml.com.PortMapping;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupDcVI.IImeas;
import xoc.dsa.ISetupProtocolInterface;
import xoc.dsa.ISetupRfMeas;
import xoc.dsa.ISetupRfStim;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.annotations.Out;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IRfMeasResults;
import xoc.dta.testdescriptor.IParametricTestDescriptor;



/**
 * This test method is for Gain calibration. it can generate the operating sequencer and setups for test instruments according to the
 * setup information from testtable. <br>
 * The test method read the golden reference Gain value, measure the actual Gain value,  and take the difference between
 * golden reference value and measure value as the <b>total loss</b> of the test path.
 *
 * @param calMode       Flag indication for Calibration
 * @param maxCount      the loop run count of testflow
 * @param out_InLoss    output testsuite variable for inloss
 * @param VDDPin        DPS pin for DCVI measurement
 * @param USID          USID set for MIPI protocol
 * @param delta_CAL     Flag indication for delta Calibration for production
 *
 *
 * @since 1.0.0
 * @author 308770
 *
 */
public class Gain_CAL extends TestMethod {
    @In     public boolean calMode = false;
    @In     public Integer maxCount = 100;
    @Out    public MultiSiteDouble out_InLoss = new MultiSiteDouble();
    @In     public String VDDPin = "VDD1P8";
    @In     public String USID = "0xC";

    public boolean delta_CAL = false;


    /**
     * This Class read the setup parameter from testtable to override init value. if the parameter not exist in
     * testtable, it will use the init value.
     *
     * @param TestType      testitem name.
     * @param Freq1         Frequency for the primary RF Stimulus
     * @param Freq2         Frequency for the second RF stimulus
     * @param MeasRange     Power range setup for RF measurement
     * @param InPow         input power for RF stimulus
     * @param Reg           Register sequencer for this testitem
     * @param RegReset      Reset register sequencer after test for this testitem
     * @param Order         testitems organization order of this testitem in testsuite
     * @param ptd_Rslt      IParametricTestDescriptor
     * @param importSpec    setup specification for test setup <br><br>
     *
     * @author 308770
     *
     */
    public class stepMeas extends ParameterGroup{
        public String TestType = "Gain";
        public Double Freq1 = 7.51e8;
        public Double Freq2 = 0.0;
        public double MeasRange = -20;
        public double InPow = -30;
        public String Reg = "";
        public String RegReset = "";
        public int Order = 0;
        public IParametricTestDescriptor ptd_Rslt;
        public String importSpec = "";
    }
    public ParameterGroupCollection<stepMeas> Parameter = new ParameterGroupCollection<>();

    public  String rfIN = "dummyRFIN";
    public  String rfOUT = "dummyRFOUT";
    public  String opMode = "A_HP";
    public String powermode="HP";
    String _testSuiteName;
    String testSuiteName;
    String stimName ;
    String measName ;
    String RF_IN = "LB1_IN1";
    String RF_OUT = "LB1_OUT";
    int    band = 0;

    double IF_Freq= 27.12311e6;

    public int countAvgNF_LP=1;
    public IMeasurement measurement;
    public IMeasurement measurementLNAOff;

    @Override
    public void setup (){
        String pattPath = "setups.vectors.SWITCH.";
        _testSuiteName = context.getTestSuiteName();
        testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf('.'));
        List<String> ports = Arrays.asList(testSuiteName.split("\\s*_\\s*"));
        RF_IN=ports.get(0)+"_"+ports.get(1);
        RF_OUT=ports.get(2)+"_"+ports.get(3);
        band = Integer.parseInt(ports.get(4).substring(1));
        opMode = ports.get(5)+"_"+ports.get(6);

        String[] atePort = PortMapping.getRFPort(RF_IN,RF_OUT);
        rfIN = atePort[0];
        rfOUT = atePort[1];



        stimName = testSuiteName +"_Stim";
        measName = testSuiteName +"_Meas";

        double wait_time = 5e-3;
        double  Gain_BW  = 50e3;

        Set<String> sortedKey = new LinkedHashSet<String>();
        Set<String> Testitems_str = new LinkedHashSet<String>();
        Para_Sort(sortedKey, Parameter);
        Para_Sort(Testitems_str, Parameter);

        StringBuffer testItemString =new StringBuffer();
        for(Iterator<String> itr = Testitems_str.iterator(); itr.hasNext();)
        {
            String tmp_str= itr.next();
            testItemString.append(tmp_str);
            if(itr.hasNext())
            {
            testItemString.append("_") ;
            }
        }

        IDeviceSetup devSetup = DeviceSetupFactory.createNamedInstance(testItemString.toString());


        ISetupRfStim setup_stim = devSetup.addRfStim(rfIN);
        ISetupRfMeas setup_meas = devSetup.addRfMeas(rfOUT);
        setup_stim.setConfigPortPort(rfIN);
        setup_meas.setConfigModeHighResolution();
        setup_stim.setConfigOptionMultisiteSplitterOff();
        setup_stim.setConfigModeSingleSource();

        setup_stim.setConfigOptionSiteInterlacingOn();
        setup_meas.setConfigOptionSiteInterlacingOn();
        int initFlag=0;

        Reg_Read mRegData = Reg_Read.getInstance();


        devSetup.sequentialBegin();
        {
            for (String _sortedKey : sortedKey){
                stepMeas meas = Parameter.get(_sortedKey);
                if(!meas.importSpec.isEmpty()) {
                    devSetup.importSpec(meas.importSpec);

                }
                String Reg_Key = "";
                if(!meas.Reg.contains("0x")&&!meas.Reg.equals(""))
                {
                    Reg_Key = testSuiteName+'_'+meas.Reg;
                }

                if(meas.getId().indexOf("Gain")!=-1 ){
                    if(initFlag==0){

                        initFlag++;
                        String RF_SW = PortMapping.getCWPat(RF_IN, RF_OUT);
                        devSetup.patternCall(pattPath+RF_SW);

                    }
                }
                else if(meas.getId().indexOf("IDD")!=-1)
                {
                    continue;
                }
                devSetup.parallelBegin();
                {

                    devSetup.sequentialBegin();{
                        if( !meas.Reg.equals("") )
                        {

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



                    }
                    devSetup.sequentialEnd();
                }devSetup.parallelEnd();



                devSetup.parallelBegin();
                {
                  //stim
                    devSetup.sequentialBegin();{

                        if(meas.getId().indexOf("IDD")!=-1)
                        {
                            // NO Stim for IDD test
                            devSetup.insertOpSeqComment(meas.getId()+": Stim Off");
                            ISetupRfStim.ICw cwAction = setup_stim.cw(stimName+meas.getId() +"_Off");
                            cwAction.setFrequency(5.991e9).setPower(-110);
                            devSetup.actionCall(stimName+meas.getId() +"_Off");
                        }
                        else
                        {
                            devSetup.insertOpSeqComment(meas.getId()+": Stim");
                            ISetupRfStim.ICw cwAction = setup_stim.cw(stimName +"_"+meas.getId());
                            cwAction.setFrequency(meas.Freq1).setPower(meas.InPow);
                            devSetup.actionCall(stimName +"_"+meas.getId() );
                        }


                      } devSetup.sequentialEnd();

                      //meas
                      devSetup.sequentialBegin();{

                          if(meas.getId().indexOf("IDD")!=-1)
                          {
                              devSetup.insertOpSeqComment(meas.getId()+": MEAS");


                              IImeas DPS_meas = devSetup.addDcVI(VDDPin).imeas(measName+"_"+meas.getId());
                              DPS_meas.setWaitTime(wait_time).setIrange(meas.MeasRange*1e-3).setAverages(4)/*.setRestoreIrange(true)*//*.setHighAccuracy(true).setRestoreIrange(true)*/;
//                              devSetup.waitCall(1);
                              devSetup.actionCall(measName+"_"+meas.getId());
                          }
                          else if(meas.getId().indexOf("Gain")!=-1)
                          {
                              devSetup.insertOpSeqComment(meas.getId()+": MEAS");
                              ISetupRfMeas.ICwPower CwActionFs = setup_meas.cwPower(measName+"_"+meas.getId());
                              CwActionFs.setFrequency(meas.Freq1).setIfFrequency(IF_Freq).setExpectedMaxPower(meas.MeasRange+10);//.setSamples(SampleSIze);
                              CwActionFs.setBandwidthOfInterest(Gain_BW)/*.setIfFilterBandwidth(40.0e6)*/.setResultAveraging(1);//.setResultAveraging(CW_avg);
                              devSetup.waitCall(wait_time);
                              devSetup.actionCall(measName+"_"+meas.getId());
                          }


                        if(!meas.RegReset.equals(""))
                        {
                            if(!meas.RegReset.contains("0x"))
                            {

                              meas.RegReset = mRegData.m_RegDataMap.get(meas.RegReset);

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
                        }//end of measure setup
                      }devSetup.sequentialEnd();

                }devSetup.parallelEnd();


                }
                    devSetup.sequentialEnd();

            }
        measurement.setSetups(devSetup);

        IDeviceSetup devSetupLNAOFF = DeviceSetupFactory.createNamedInstance("LNA_OFF");
        ISetupRfStim setup_stim_LNAOFF = devSetupLNAOFF.addRfStim(rfIN);
        ISetupRfMeas setup_meas_LNAOFF = devSetupLNAOFF.addRfMeas(rfOUT);
        setup_stim_LNAOFF.setConfigPortPort(rfIN);
        setup_stim_LNAOFF.setConfigOptionMultisiteSplitterOff();//WP1012
        setup_stim_LNAOFF.setConfigModeSingleSource();
        setup_meas_LNAOFF.setConfigModeHighResolution();

            setup_stim_LNAOFF.setConfigOptionSiteInterlacingOn();
            setup_meas_LNAOFF.setConfigOptionSiteInterlacingOn();


            devSetupLNAOFF.importSpec("setups.specs.RF.normal");
        devSetupLNAOFF.sequentialBegin();
        {
            devSetupLNAOFF.patternCall("dsa_gen.Main.LNA_OFF.LNA_OFF_Write");//LNA_OFF

        }
        devSetupLNAOFF.sequentialEnd();


        measurementLNAOff.setSetups(devSetupLNAOFF);
        message(1, testSuiteName + " device setup done!" );


    }



    @Override
    public void update(){ //This is the area to modify something that already exists..

    }

    @Override
    public void execute() {
        measurement.execute();

        IRfMeasResults rfResult = measurement.rfMeas(rfOUT).preserveResults();
        measurementLNAOff.execute();

        releaseTester();
        delta_CAL = context.testProgram().variables().getBoolean("CAL_Delta").get();
        CalData mCalData = CalData.getInstance();
        CalData mCalOdsData = CalData.getFixedInstance();
        MapDatalog mDataMap = MapDatalog.getInstance();

        String keyName= RF_IN+"_"+RF_OUT+"_B"+band;
        MultiSiteDouble g_GainH =mCalData.getValue(0,keyName,"golden_Gain_HG1",0.0);

        MultiSiteDouble outLoss;
        MultiSiteDouble inLoss;
        MultiSiteDouble inLoss_Init = new MultiSiteDouble(0);
        MultiSiteDouble prodInLoss = new MultiSiteDouble(0);
        MultiSiteDouble OutLoss_Init;
        MultiSiteDouble prodOutLoss = new MultiSiteDouble(0);




        for(stepMeas meas: Parameter.values() ){

            if(delta_CAL)
            {
                prodInLoss = mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0);
                prodOutLoss = mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss", meas.Freq1/1.0e6);
                inLoss = mCalOdsData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0).add(prodInLoss);
                outLoss=mCalOdsData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss", meas.Freq1/1.0e6).add(prodOutLoss);
            }
            else
            {
                inLoss = mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0);
                outLoss=mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss", meas.Freq1/1.0e6);
            }



            String stepText =testSuiteName+":"+meas.getId();//+"_"+opMode;
            Double tmpFreq1= (meas.Freq1/1.0e6);
            stepText = Integer.toString(tmpFreq1.intValue())+"MHz_"+stepText;

            MultiSiteDouble rslt;


            if(meas.getId().indexOf("Gain")>=0){
                MultiSiteDouble totalLoss = new MultiSiteDouble(0.0);

                MultiSiteDouble power = rfResult.cwPower(measName +"_"+meas.getId()).getPower(rfOUT).getElement(0);

                rslt=power.subtract(meas.InPow);
                if(opMode.contains("HG1")){
                    totalLoss = g_GainH.subtract(rslt);

                }


                if(meas.getId().equals("Gain_HG1") ||meas.getId().equals("Gain_MUX")){
                    if(opMode.contains("HG1")){
                        inLoss_Init = totalLoss.multiply(1.0/3.0);
                        OutLoss_Init = totalLoss.subtract(inLoss_Init);
                        if(delta_CAL)
                        {
                            mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_InLoss",inLoss_Init.subtract(inLoss).add(prodInLoss));
                            mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss",meas.Freq1/1.0e6,OutLoss_Init.subtract(outLoss).add(prodOutLoss));
                            inLoss_Init = inLoss_Init.subtract(inLoss).add(prodInLoss);
                            OutLoss_Init = OutLoss_Init.subtract(outLoss).add(prodOutLoss);
                        }
                        else
                        {
                            mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_InLoss",inLoss_Init);
                            mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss",meas.Freq1/1.0e6,OutLoss_Init);
                        }
                        out_InLoss=inLoss;
                    }
                }
                else{
                    OutLoss_Init = totalLoss.subtract(inLoss_Init);
                    mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss",meas.Freq1/1.0e6,OutLoss_Init);
                }
            }else{
                continue;
            }
            meas.ptd_Rslt.setTestText(stepText);
            meas.ptd_Rslt.evaluate(rslt.add(inLoss).add(outLoss));

        }
    }



    private Set<String> Para_Sort(Set<String> sortedKey,ParameterGroupCollection<stepMeas> parameter)
    {
        Set<String> key = Parameter.keySet();
        for(int i = 0;i<key.size();i++)
        {
            for(String _key:key)
            {
                stepMeas meas = parameter.get(_key);
                if(meas.Order == i)
                {
                    sortedKey.add(meas.getId());
                }
            }
        }
        return sortedKey;
    }


}
