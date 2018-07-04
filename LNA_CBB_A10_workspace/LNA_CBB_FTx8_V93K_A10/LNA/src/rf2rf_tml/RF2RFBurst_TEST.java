package src.rf2rf_tml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import src.rf2rf_tml.utilities.MapDatalog;
import src.rf2rf_tml.utilities.RF_Meas;
import src.rf2rf_tml.utilities.RF_SETUP;
import src.rf2rf_tml.utilities.SynchronizedVariables.SyncMultiSiteDouble;
import src.rf2rf_tml.utilities.UtMethods;
import src.rfcbb_tml.com.CalData;
import src.rfcbb_tml.com.PortMapping;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupRfMeas;
import xoc.dsa.ISetupRfStim;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.annotations.Out;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteDoubleArray;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDcVIResults;
import xoc.dta.resultaccess.IRfMeasResults;
import xoc.dta.testdescriptor.IParametricTestDescriptor;


/**
 * This test method is the main test method for LNA RF test, it combines IDD, Gain, IIP2,IIP3, P1dB, NF and other test. <br>
 * It can generate the operating sequencer and setups for test instruments according to the setup information from testtable. <br>
 *
 * @param flagSiteInterlacing       Flag indication for SiteInterlaction
 * @param calMode                   Flag indication for calibration or production
 * @param NF_Open                   Flag indication for NF test or none test
 * @param maxCount                  dynamic recording current loop index of testflow
 * @param gainMax_wo_Loss          Measured gain without loss
 * @param in_HG_NF                  NF for High Gain
 * @param in_HG_Gain                Gain for High Gain mode
 * @param in_LG_NF                  NF for Low Gain
 * @param in_LG_Gain                Gain for Low Gain mode
 * @param out_Y                     output variable for Y factor
 * @param in_Y                      Y factor in dB
 * @param out_InLoss                output variable for inLoss
 * @param in_InLoss                 inLoss of test path
 * @param VDDPin                    DPS signal for IDD test
 * @param USID                      USID for MIPI interface
 * @param no_save                   Flag for donot save this device data for calibration
 * @param delta_CAL                 Flag for production calibration
 *
 *
 * @since 1.0.0
 * @author 308770
 *
 */

public class RF2RFBurst_TEST extends TestMethod {


    @In  public boolean flagSiteInterlacing = true;
    @In  public boolean calMode = false;
    @In  public boolean NF_Open = false;
    @In  public Integer maxCount = 3;

    @Out public MultiSiteDouble gainMax_wo_Loss = new MultiSiteDouble();

    @In  public MultiSiteDouble in_HG_NF = new MultiSiteDouble();
    @In  public MultiSiteDouble in_HG_Gain = new MultiSiteDouble();
    @In  public MultiSiteDouble in_LG_NF = new MultiSiteDouble();
    @In  public MultiSiteDouble in_LG_Gain = new MultiSiteDouble();
    @Out public MultiSiteDouble out_Y = new MultiSiteDouble();
    @In  public MultiSiteDouble in_Y = new MultiSiteDouble();
    @Out public MultiSiteDouble out_InLoss = new MultiSiteDouble();
    @In  public MultiSiteDouble in_InLoss = new MultiSiteDouble();

    @In  public String VDDPin = "VDD1P8";
    @In  public String USID = "0xC";
    public int no_save=0;
    public boolean delta_CAL = false;


    /**
     * This Class read the setup parameter from testtable to override init value. if the parameter not exist in
     * testtable, it will use the init value.
     *
     * @param Freq1         Frequency for the primary RF Stimulus
     * @param Freq2         Frequency for the second RF stimulus
     * @param MeasRange     Power range setup for RF measurement
     * @param InPow         input power for RF stimulus
     * @param Reg           Register sequencer for this testitem
     * @param RegReset      Reset register sequencer after test for this testitem
     * @param Order         testitems organization order of this testitem in testsuite
     * @param ptd_Rslt      IParametricTestDescriptor
     * @param ptd_Rslt1     IParametricTestDescriptor
     * @param importSpec    setup specification for test setup <br><br>
     *
     *
     * @author 308770
     *
     */
    public class StepMeas extends ParameterGroup{
        public Double Freq1 = 7.51e8;
        public Double Freq2 = 0.0;
        public double MeasRange = -20;
        public double InPow = -30;
        public String Reg = "";
        public String RegReset = "";
        public int Order = 0;
        public IParametricTestDescriptor ptd_Rslt;
        public IParametricTestDescriptor ptd_Rslt1;
        public String importSpec = "";
    }
    public ParameterGroupCollection<StepMeas> Parameter = new ParameterGroupCollection<>();

    public  String rfIN = "dummyRFIN";
    public  String rfOUT = "dummyRFOUT";
    public  String opMode = "LG4_OPA";
    public  String powermode="HG";
    String _testSuiteName;
    String testSuiteName;
    String measName ;
    String RF_IN = "LB1_IN1";
    String RF_OUT = "LB1_OUT";
    int    band = 0;
    double IF_Freq= 27.12311e6;

    MultiSiteDouble act_enr = new MultiSiteDouble();

    public IMeasurement measurement;
    public IMeasurement measurementLNAOff;

    @Override
    public void setup (){

        String pattPath = "setups.vectors.SWITCH.";
        _testSuiteName = context.getTestSuiteName();
        testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));
        NF_Open=context.testProgram().variables().getBoolean("NF_FLAG").get();

        List<String> ports = Arrays.asList(testSuiteName.split("\\s*_\\s*"));
        RF_IN=ports.get(0)+"_"+ports.get(1);
        RF_OUT=ports.get(2)+"_"+ports.get(3);
        band = Integer.parseInt(ports.get(4).substring(1));
        opMode = ports.get(5)+"_"+ports.get(6);

        String[] atePort = PortMapping.getRFPort(RF_IN,RF_OUT);
        rfIN = atePort[0];
        rfOUT = atePort[1];

        Set<String> sortedKey = new LinkedHashSet<String>();
        Set<String> Testitems_str = new LinkedHashSet<String>();
        Para_Sort(sortedKey, Parameter);
        Para_Sort(Testitems_str, Parameter);

        int initFlag=0;

        if(!calMode)
        {
            sortedKey.remove("P1dB");
            Testitems_str.remove("P1dB");
        }

        if(!NF_Open)
        {
            sortedKey.remove("NF");
            Testitems_str.remove("NF");
        }

        String testItemString="";
        for(Iterator<String> itr = Testitems_str.iterator(); itr.hasNext();)
        {
            String tmp_str= itr.next();
            testItemString +=tmp_str;
            if(itr.hasNext())
            {
            testItemString +="_";
            }
        }


        if(testItemString.contains("Gain_Sweep1_Gain_Sweep2_Gain_Sweep3_Gain_Sweep4_Gain_Sweep5"))
        {
            testItemString=testItemString.replaceAll("Gain_Sweep1_Gain_Sweep2_Gain_Sweep3_Gain_Sweep4_Gain_Sweep5", "Gain_Sweep");
        }


        IDeviceSetup devSetup = DeviceSetupFactory.createNamedInstance(testItemString);


        ISetupRfStim setup_stim = devSetup.addRfStim(rfIN);
        ISetupRfMeas setup_meas = devSetup.addRfMeas(rfOUT);
        setup_stim.setConfigPortPort(rfIN);
        setup_stim.setConfigOptionMultisiteSplitterOff();
        setup_stim.setConfigModeSingleSource();
        setup_meas.setConfigModeHighResolution();

        if(flagSiteInterlacing) {
            setup_stim.setConfigOptionSiteInterlacingOn();
            setup_meas.setConfigOptionSiteInterlacingOn();
        }

//      setup_stim.setConfigOptionInternalCrossConnect();
//      setup_meas.setConfigOptionInternalCrossConnect();
        RF_SETUP CommonSetup = new RF_SETUP();


        CommonSetup.USID = USID;
        CommonSetup.IF_Freq = IF_Freq;
        CommonSetup.testsuiteName = testSuiteName;
        CommonSetup.CalMode = calMode;
        CommonSetup.VDDPIN = VDDPin;

  /*
   * *******************************************Others TestItem Setup START**************************************************
   */
        devSetup.sequentialBegin();
        {

            for (String _sortedKey : sortedKey){
                StepMeas meas = Parameter.get(_sortedKey);
                if(!meas.importSpec.isEmpty()) {
                    devSetup.importSpec(meas.importSpec);
                }


                if(meas.getId().indexOf("Gain")!=-1 ){
                    if(initFlag==0){
                        initFlag++;

                        String RF_SW = PortMapping.getCWPat(RF_IN, RF_OUT);
                        devSetup.patternCall(pattPath+RF_SW);

                    }
                }
                else if(meas.getId().contains("IIP2")){
                    continue;
                }
                if(meas.getId().contains("IDD"))
                {
                    CommonSetup.IDDSetup(meas, devSetup, setup_stim, setup_meas);
                }
                else if(meas.getId().contains("Gain"))
                {
                    CommonSetup.GainSetup( meas, devSetup, setup_stim, setup_meas);
                }
                else if(meas.getId().contains("IIP3"))
                {

                    CommonSetup.IIP3Setup(meas, devSetup, setup_stim, setup_meas);
                }
                else if(meas.getId().contains("H3")||meas.getId().contains("LF_Rej"))
                {
                    CommonSetup.PowerRejection(meas, devSetup, setup_stim, setup_meas);
                }
                else if(meas.getId().contains("P1dB"))
                {
                    CommonSetup.P1dB_Setup(meas, devSetup, setup_stim, setup_meas);

                }

            }//for
        }
        devSetup.sequentialEnd();
        measurement.setSetups(devSetup);
  /*
   * *******************************************Others TestItem Setup END**************************************************
   */

/*
 *******************************************IIP2 SETUP START********************************************************
 */
//        for (String _sortedKey : sortedKey){
//                StepMeas meas = Parameter.get(_sortedKey);
//                  if(meas.getId().contains("IIP2"))
//                  {
//                      ISetupRfStim setup_stimIIP2 = devSetupIIP2.addRfStim(rfIN);
//                      ISetupRfMeas setup_measIIP2 = devSetupIIP2.addRfMeas(rfOUT);
//                      setup_stimIIP2.setConfigPortPort(rfIN);
//                      setup_measIIP2.setConfigPortPort(rfOUT);
//                      setup_stimIIP2.setConfigOptionMultisiteSplitterOff();//WP1012
//                      setup_measIIP2.setConfigModeHighResolution();
//                      setup_stimIIP2.setConfigModeDualSource();
//                      devSetupIIP2.sequentialBegin();
//                      {
//                          CommonSetup.IIP2Setup(meas, devSetupIIP2, setup_stimIIP2, setup_measIIP2);
//
//                      }
//                      devSetupIIP2.sequentialEnd();
//                  }
//
//        }
//        measurementIIP2.setSetups(devSetupIIP2);
 /* *******************************************IIP2 SETUP END********************************************************
  */

/*
 * ******************************************ENR SETUP START********************************************************
 */

//        for (String _sortedKey : sortedKey){
//            StepMeas meas = Parameter.get(_sortedKey);
//            if(meas.getId().equals("NF")&&NF_Open ){
//                ISetupRfStim setup_stimDummy = devSetupDummy.addRfStim(rfIN);
//                ISetupRfMeas setup_measDummy = devSetupDummy.addRfMeas(rfOUT);
//                setup_stimDummy.setConfigPortPort(rfIN);
//                setup_measDummy.setConfigPortPort(rfOUT);
//                setup_stimDummy.setConfigOptionMultisiteSplitterOff();//WP1012
//                setup_measDummy.setConfigModeHighResolution();
//                setup_stimDummy.setConfigModeSingleSource();
//                CommonSetup.DummySetup(meas, devSetupDummy, setup_stimDummy);
//
//            }
//
//        }
//
//
//        measurementDummy.setSetups(devSetupDummy);
 /*
  *******************************************ENR SETUP END********************************************************
*/


/*
 * *******************************************LNAOFF Setup START**************************************************
 */
               IDeviceSetup devSetupLNAOFF = DeviceSetupFactory.createNamedInstance("LNA_OFF");
               ISetupRfStim setup_stim_LNAOFF = devSetupLNAOFF.addRfStim(rfIN);
               ISetupRfMeas setup_meas_LNAOFF = devSetupLNAOFF.addRfMeas(rfOUT);
               setup_stim_LNAOFF.setConfigPortPort(rfIN);
               setup_stim_LNAOFF.setConfigOptionMultisiteSplitterOff();
               setup_stim_LNAOFF.setConfigModeSingleSource();
               setup_meas_LNAOFF.setConfigModeHighResolution();
               if(flagSiteInterlacing) {
                   setup_stim_LNAOFF.setConfigOptionSiteInterlacingOn();
                   setup_meas_LNAOFF.setConfigOptionSiteInterlacingOn();
               }

               devSetupLNAOFF.importSpec("setups.specs.RF.normal");

               devSetupLNAOFF.sequentialBegin();
               {
                   devSetupLNAOFF.patternCall("dsa_gen.Main.LNA_OFF.LNA_OFF_Write");//LNA_OFF
//                   devSetupLNAOFF.patternCall("vectors.patterns.dummy");
//                   if(CalMode)
//                   {
//                       devSetupLNAOFF.waitCall(3);
//                   }
               }
               devSetupLNAOFF.sequentialEnd();


               measurementLNAOff.setSetups(devSetupLNAOFF);
/*
 * ******************************************LNA_OFF SETUP END********************************************************
 */

        message(1, testSuiteName + " device setup done!" );
    }
    @Override
    public void execute() {

        delta_CAL = context.testProgram().variables().getBoolean("CAL_Delta").get();
        _testSuiteName = context.getTestSuiteName();
        testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));
        measurement.execute();

        IRfMeasResults rfResult = measurement.rfMeas(rfOUT).preserveResults();
        IDcVIResults DPSResult = measurement.dcVI(VDDPin).preserveResults();
        IRfMeasResults rfResultIIP2=rfResult;

        no_save=0;
        Set<String> sortedKey = new LinkedHashSet<String>();
        Para_Sort(sortedKey, Parameter);

        if(!calMode)
        {
            sortedKey.remove("P1dB");
        }
        if(!NF_Open)
        {
            sortedKey.remove("NF");
        }
//        for (String _sortedKey : sortedKey){
//
//            StepMeas meas = Parameter.get(_sortedKey);
//            if(meas.getId().equals("NF")){
//                if(act_enr.equalTo(0.0))
//                {
//
//                    act_enr = measurementDummy.rfStim(rfIN).noise(testSuiteName +"_Stim_"+meas.getId()+"_Hotdummy").getEnr10MHz();
//                }
//
//            }
//
//            else if(meas.getId().contains("IIP2"))
//            {
//               measurementIIP2.execute();
//               rfResultIIP2 = measurementIIP2.rfMeas(rfOUT).preserveResults();
//            }
//
//          }
        measurementLNAOff.execute();

        SyncMultiSiteDouble hGain = SyncMultiSiteDouble.create(RF_IN+"_"+RF_OUT+"_B"+band+"_HG");//Get Gain_HG1 value
        if(opMode.contains("HG1_OPA")){//A_HP ---> HG1_OPA
            hGain.set(new MultiSiteDouble(0.0));
            hGain.reserve();
        }

        releaseTester();

        CalData mCalData = CalData.getInstance();
        CalData mCalOdsData = CalData.getFixedInstance();

        MultiSiteDouble gainNF = new MultiSiteDouble(0.0);
        MultiSiteDouble gainP1dB = new MultiSiteDouble(0.0);
        MultiSiteDouble gainIIP = new MultiSiteDouble(0.0);
        MultiSiteDouble inLoss = new MultiSiteDouble(0.0);
        MultiSiteDouble outLoss = new MultiSiteDouble(0.0);


        double gainFreq = 500e6;
        double getLossFreq = 500e6;

        for (String _sortedKey : sortedKey){

            StepMeas meas = Parameter.get(_sortedKey);
            measName=testSuiteName +"_Meas_"+meas.getId();


            String stepText =meas.getId();
            Double tmpFreq1= (meas.Freq1/1.0e6);
            stepText =meas.getId();
            if(meas.getId().contains("Gain"))
            {
               stepText = Integer.toString(tmpFreq1.intValue())+"MHz_"+stepText;
            }



            MultiSiteDouble rslt;
            MultiSiteDouble rslt2;
//            MultiSiteDouble nfEff=new MultiSiteDouble(15);



            getLossFreq = meas.Freq1;
            if(meas.getId().contains("Gain"))
            {
                gainFreq=meas.Freq1;
            }
            if(meas.getId().contains("IIP") ||meas.getId().contains("LF_Rej") )
            {
                getLossFreq =  gainFreq;
            }

            if(delta_CAL)
            {

                if(opMode.contains("HG")||opMode.contains("LP")||meas.getId().contains("Gain_LG4")){

                    inLoss = mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0).add(mCalOdsData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0));
                    outLoss=mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss", getLossFreq/1.0e6).add(mCalOdsData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss", getLossFreq/1.0e6));//for HG0,HG1,HG2,ML
                }
                else{//LG3
                    inLoss = mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0).add(mCalOdsData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0));
                    outLoss=mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLossL", getLossFreq/1.0e6).add(mCalOdsData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLossL", getLossFreq/1.0e6));//for LG3,LG4
                }
            }
            else
            {
                if(opMode.contains("HG")||opMode.contains("LP")||meas.getId().contains("Gain_LG4")){

                    inLoss = mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0);
                    outLoss=mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss", getLossFreq/1.0e6);//for HG0,HG1,HG2,ML
                }
                else{//LG3
                    inLoss = mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band,"cal_InLoss",0.0);
                    outLoss=mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLossL", getLossFreq/1.0e6);//for LG3,LG4
                }
            }



/* *********************************************IDD TEST START************************************************ */
            if(meas.getId().contains("IDD")&& !calMode){
                MultiSiteDoubleArray DPSvoltages = DPSResult.imeas(measName).getCurrent(VDDPin);
                rslt = DPSvoltages.getElement(0);//converted to mA
            }
/* *********************************************IDD TEST END*************************************************/

/* *********************************************Gain TEST START************************************************ */

            else if(meas.getId().indexOf("Gain")>=0){

                MultiSiteDouble power = rfResult.cwPower(measName).getPower(rfOUT).getElement(0);
                rslt=power.subtract(meas.InPow).add(outLoss).add(inLoss);
                rslt2=power.subtract(meas.InPow);//without Loss

                if(meas.getId().contains("Gain_HG")||meas.getId().contains("Gain_LG3")){//change it@20180118
                    gainMax_wo_Loss =rslt2; //for harmonic 3 test, LF_Rej
                    gainNF = rslt;//for Calc NF,need Gain value, Gain_HG0,Gain_HG1,Gain_HG2,Gain_LG3,Gain_LG4,Gain_MLP;
//                    gainP1dB = rslt;//for A10/Cal prog , gain P1dB = Gain_HG1,Gain_LG3
                }
                /*
                 * if you use Char prog, gainP1dB = Gain_HG0,Gain_HG1,Gain_HG2,Gain_MLP,Gain_LG3,Gain_LG4;
                 * if you use A10/Cal prog, gain P1dB = Gain_HG1,Gain_LG3
                 */

                if(testSuiteName.contains("LG3")||testSuiteName.contains("LG4"))//for char
                {

                    if(meas.getId().contains("Gain_LG"))
                    {
                        gainP1dB = rslt;//just for char
                        gainIIP = rslt;
                    }
                }
                else//char & A10 & CAL
                {
                    if(meas.getId().contains("Gain_HG")||meas.getId().contains("Gain_LG3")||meas.getId().contains("Gain_MLP")){//change it@20180118
                        gainP1dB = rslt;//for A10/Cal prog , gain P1dB = Gain_HG1,Gain_LG3
                        gainIIP = rslt;
                    }
                }


                if(meas.getId().contains("HG1")&&opMode.contains("HG1_OPA")){
                    hGain.set(rslt);
                }
                else if(opMode.contains("LG")&&meas.getId().contains("LG")){

                    MultiSiteDouble hp_gain = hGain.get();
                    meas.ptd_Rslt1.setTestText(testSuiteName+":GSTEP_"+meas.getId());
                    meas.ptd_Rslt1.evaluate(hp_gain.subtract(rslt));
                }

            }

/* *********************************************Gain TEST END*************************************************/


/* *********************************************P1dB TEST Start*************************************************/
            else if(meas.getId().indexOf("P1dB")>=0)
            {
                RF_Meas P1dB_MEAS = new RF_Meas();
                P1dB_MEAS.testName = testSuiteName;
                P1dB_MEAS.InLoss = inLoss;
                P1dB_MEAS.OutLoss = outLoss;
                P1dB_MEAS.activeSites = context.getActiveSites();
                P1dB_MEAS.gainP1dB = gainP1dB;
                List<MultiSiteDouble> Rslt_P1dB = new ArrayList<>();
                Rslt_P1dB =P1dB_MEAS.P1dB_Calc(meas, rfResult, rfOUT);
                rslt=Rslt_P1dB.get(0);
                rslt2=Rslt_P1dB.get(1);
//                meas.ptd_Rslt.setTestText(testSuiteName+":OutP1dB");
//                meas.ptd_Rslt.evaluate(rslt2);
                if(calMode)
                {
                    MapDatalog mDataMap = MapDatalog.getInstance();

                    String keyName = RF_IN+"_"+RF_OUT+"_B"+band;
                    if(testSuiteName.contains("HG1"))
                    {
                        MultiSiteDouble g_GainH =mCalData.getValue(0,keyName,"golden_Gain_HG1",0.0);
                        MultiSiteDouble g_P1dBH =mCalData.getValue(0, keyName, "golden_P1dB_HG1",0.0);
                        MultiSiteDouble InLoss_Com = mCalData.getValue(keyName, "cal_InLoss",0.0);
                        MultiSiteDouble OutLoss_HG = mCalData.getValue( keyName, "cal_OutLoss",meas.Freq1/1.0e6);


                        MultiSiteDouble TotalLoss_HG;

                        TotalLoss_HG = InLoss_Com.add(OutLoss_HG);

                        MultiSiteDouble P1dB_InLossH = (Rslt_P1dB.get(0).add(inLoss)).subtract(g_P1dBH);//TODO: Get InLoss with P1dB CAL
                        out_InLoss = P1dB_InLossH;

//                        MultiSiteDouble totalLossM = g_GainH.subtract( hGain.get()).add(TotalLoss_HG);
                        MultiSiteDouble totalLossM = g_GainH.subtract( hGain.get().subtract(inLoss).subtract(outLoss));
                        MultiSiteDouble outLossM = totalLossM.subtract(P1dB_InLossH);


                        if(delta_CAL)
                        {
                            mDataMap.setValue(keyName, "cal_InLoss",P1dB_InLossH.subtract(inLoss));
                            mDataMap.setValue(keyName, "cal_OutLoss",meas.Freq1/1.0e6,outLossM.subtract(outLoss));
                        }
                        else
                        {
                            mDataMap.setValue(keyName, "cal_InLoss",P1dB_InLossH);
                            mDataMap.setValue(keyName, "cal_OutLoss",meas.Freq1/1.0e6,outLossM);
                        }

                        System.out.println(keyName+":In_loss  = "+P1dB_InLossH);
                        System.out.println(keyName+":cal_OutLossM  = "+outLossM);




                    }
                    else if(testSuiteName.contains("LG"))
                    {
                        MultiSiteDouble g_GainL =mCalData.getValue(0,keyName,"golden_Gain_LG",0.0);
                        MultiSiteDouble InLoss_Com = mCalData.getValue(keyName, "cal_InLoss",0.0);
                        MultiSiteDouble OutLoss_LG = mCalData.getValue( keyName, "cal_OutLossL",meas.Freq1/1.0e6);


                        MultiSiteDouble TotalLoss_LG;
                        TotalLoss_LG = InLoss_Com.add(OutLoss_LG);


//                        MultiSiteDouble totalLossM_LG = g_GainL.subtract(gainNF).add(TotalLoss_LG);
                        MultiSiteDouble totalLossM_LG = g_GainL.subtract(gainNF.subtract(inLoss).subtract(outLoss));
                        MultiSiteDouble outLossM_LG = totalLossM_LG.subtract(in_InLoss);


                        if(delta_CAL)
                        {

                            mDataMap.setValue(keyName, "cal_OutLossL",meas.Freq1/1.0e6,outLossM_LG.subtract(outLoss));
                        }
                        else
                        {
                            mDataMap.setValue(keyName, "cal_OutLossL",meas.Freq1/1.0e6,outLossM_LG);
                        }


                        System.out.println(keyName+":In_loss  = "+in_InLoss);
                        System.out.println(keyName+":cal_OutLossM_LG  = "+outLossM_LG);

                    }


                }

            }
/* *********************************************P1dB TEST END*************************************************/

/* *********************************************LF_Rej/H3 TEST START*************************************************/

            else if(meas.getId().indexOf("LF_Rej")>=0||meas.getId().indexOf("H3")>=0)
            {
                MultiSiteDouble gain_M = gainMax_wo_Loss;
                MultiSiteDouble power_Rej = rfResult.cwPower(measName).getPower(rfOUT).getElement(0);
                rslt = gain_M.subtract(power_Rej.subtract(meas.InPow));//Gain(f1)-Gain(3f1) && Gain(f1)-Gain(Lf)
            }
/* *********************************************LF_Rej/H3 TEST END*************************************************/

/* *********************************************IIP2/IIP3 TEST START*************************************************/
            else if(meas.getId().contains("IIP")){
                   RF_Meas IIP_MEAS = new RF_Meas();
                   IIP_MEAS.testName = testSuiteName;
                   IIP_MEAS.gainIIP = gainIIP;
                   IIP_MEAS.InLoss = inLoss;
                   rslt = IIP_MEAS.IIP_Calc(meas,rfResult,rfResultIIP2,rfOUT);

            }
/* *********************************************IIP2/IIP3 TEST END*************************************************/

/**********************************************NF TEST START*************************************************/
//            else if(meas.getId().indexOf("NF")>=0){
//
//                nfEff=mCalData.getValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_NFeff", 0.0);
//
//                MultiSiteDouble power_hot = rfResult.modPower(measName+"_Hot").getPower(rfOUT).getElement(0);
//                MultiSiteDouble power_cold = rfResult.modPower(measName+"_Cold").getPower(rfOUT).getElement(0);
//
//
//
//                /* for debug purpose */
////                meas.ptd_Rslt.setTestText("power_hot_ori");
////                meas.ptd_Rslt.evaluate(power_hot);
////
////                meas.ptd_Rslt.setTestText("power_Cold_ori");
////                meas.ptd_Rslt.evaluate(power_cold);
//                /* for debug purpose end */
//
//
//                if(!CalMode) {
//                    /*********************** NF Calc ***************************************/
////                    MultiSiteDouble Fdgt_Hot_dB = wave_cmplx_Hot.getSystemNoiseFigure();
////                    nfEff = Fdgt_Hot_dB;
//
//                    MultiSiteDouble NF = new MultiSiteDouble(9999.0);
//
//                    System.out.println("INFO:  _testSuiteName= "+_testSuiteName);
//
//
//                    System.out.println(_testSuiteName+":In_loss  = "+inLoss);
//                    System.out.println(_testSuiteName+":cal_OutLossM  = "+outLoss);
//                    System.out.println(_testSuiteName+":F Receiver  = "+nfEff);
//
//
//
//                    NF = NF_direct( power_hot, power_cold, inLoss, outLoss, nfEff, act_enr,gainNF );
////                    NF = NF_direct_New( power_hot, power_cold, inLoss, outLoss, nfEff, act_enr,gainNF);
//                    rslt = NF;
//
//                    //TODO: for power_cold>power_hot ////////////
//                    int[] activeSites = context.getActiveSites();
//                    for(int site:activeSites){
//                        if(!(power_cold.get(site)<power_hot.get(site)))
//                        {
//                            double tmp = 99.0;
//                            rslt.set(site,tmp);
//                        }
//
//                    }
//
//                }else{
///* ***********************************calibration***********************************************************************************/
//                    MultiSiteDouble Y= UtMethods.db2linear(power_hot).divide(UtMethods.db2linear(power_cold));
//                    out_Y = Y;//HG Y transfer to LG execution.
//
//                    /*
//                     * FT: just use LG;
//                     * CHAR: there are LG3 and LG4 test suite;
//                     */
//                    if(context.getTestSuiteName().contains("_LG")){
//
//                         int[] activeSites = context.getActiveSites();
//                          RF_NF_CAL RFCAL = new RF_NF_CAL();
//                          RFCAL.keyName = RF_IN+"_"+RF_OUT+"_B"+band;
//                          RFCAL.testsuiteName = testSuiteName;
//                          RFCAL.activeSites = activeSites;
//                          RFCAL.act_enr = act_enr;
//                          RFCAL.in_Y = in_Y;
//                          RFCAL.Y = Y;
//                          RFCAL.inLoss = inLoss;
//                          RFCAL.nfEff = nfEff;
//                          RFCAL.mGain_HG1 = hGain.get();
//                          RFCAL.mGain_LG3 = gainNF;
//                          RFCAL.NF_CAL(meas, mCalData);
//
//                    }//LG
///* ***********************************calibration end***********************************************************************************/
//
//
//                }//cal
//
//            }
/* *********************************************NF TEST END*************************************************/
            else
            {
                continue;
            }
            if(!calMode )
            {
                meas.ptd_Rslt.setTestText(testSuiteName+":"+stepText);
                meas.ptd_Rslt.evaluate(rslt);

            }
            else  if(calMode && (meas.getId().contains("Gain")) && !(meas.getId().contains("Gain_Sweep"))   )

            {

                meas.ptd_Rslt.setTestText(testSuiteName+":"+stepText);
                meas.ptd_Rslt.evaluate(rslt);
            }

        }//for
    }//execute

    @Override
    public void update(){ //This is the area to modify something that already exists..

    }


    /**
     * this method is used to sort testitems according to user defined orders for items in profile of testtable.
     *
     * @param sortedKey  set of all selected testitems
     * @param parameter  setups information
     *
     * @return new sorted testitmes set
     *
     * @since  1.0.0
     */
    public Set<String> Para_Sort(Set<String> sortedKey,ParameterGroupCollection<StepMeas> parameter)
    {
        Set<String> key = Parameter.keySet();
        for(int i = 0;i<key.size();i++)
        {
            for(String _key:key)
            {
                StepMeas meas = parameter.get(_key);
                if(meas.Order == i)
                {
                    sortedKey.add(meas.getId());
                }
            }
        }
        return sortedKey;
    }


    @SuppressWarnings("unused")
    private MultiSiteDouble NF_direct( MultiSiteDouble power_hot, MultiSiteDouble power_cold, MultiSiteDouble In_loss,
            MultiSiteDouble Out_loss, MultiSiteDouble _Sys_NF, MultiSiteDouble _act_enr,MultiSiteDouble _gain )
    {
        MultiSiteBoolean offlineFlag = context.testProgram().variables().getBoolean("SYS.OFFLINE");
        if(offlineFlag.equalTo(true)){
            return new MultiSiteDouble(999.9);
        }
        try{
            MultiSiteDouble Y_linear = UtMethods.db2linear(power_hot.subtract(power_cold));//power is db
            MultiSiteDouble eff_enr = UtMethods.db2linear(_act_enr.subtract(In_loss));
            MultiSiteDouble F_total = eff_enr.divide( Y_linear.subtract(1));
            MultiSiteDouble effSys_NF = UtMethods.db2linear(_Sys_NF);//.add(Out_loss);

            // Friss Noise EQN : F_total = F1 + F2/G1-1 + F3/G1G2-1 + ....
            // Friss Fdut = F_total-(Fsys_eff-1)/Gdut
            //            MultiSiteDouble Fdut = F_total.subtract( (UtMethods.db2linear(effSys_NF).subtract(1)).divide(UtMethods.db2linear(gain)));
            //            MultiSiteDouble NF = UtMethods.linear2db(Fdut);
            //Friss Fdut = (ENFeff)/(Ymeas-1)-(effNF-1)/Gdut
            MultiSiteDouble Fdut = F_total.subtract((effSys_NF.subtract(1)).divide(UtMethods.db2linear(_gain)));
            MultiSiteDouble NF = UtMethods.linear2db(Fdut);


            if(messageLogLevel>=1)
            {
                System.out.println("INFO  = "+_testSuiteName);
                System.out.println("Recv NF  = "+_Sys_NF);
                System.out.println("In_loss  = "+In_loss);
                System.out.println("Out_loss  = "+Out_loss);
                System.out.println("Rcv Enr = "+ act_enr);
                System.out.println("Eff_enr_db = "+UtMethods.linear2db(eff_enr));
                System.out.println("eff_enr_linear  = "+eff_enr);
                System.out.println("Hot_db  = "+power_hot);
                System.out.println("Cold_db  = "+power_cold);
                System.out.println("Gain_db  = "+_gain);
                System.out.println("Y_linear  = "+Y_linear);


                System.out.println("------------------------ ");
                System.out.println("F_total  = "+UtMethods.linear2db(F_total));
                System.out.println("1ENR==== Calc NF  = "+NF);
                System.out.println("------------------------ ");
            }
            return NF;
        }catch(ArithmeticException e){
            return new MultiSiteDouble(999.9);
        }
    }


}
