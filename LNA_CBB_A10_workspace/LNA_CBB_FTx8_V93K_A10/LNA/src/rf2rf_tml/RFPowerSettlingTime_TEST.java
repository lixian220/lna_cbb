package src.rf2rf_tml;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import src.reg_tools.Reg_Read;
import src.rfcbb_tml.com.PortMapping;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupProtocolInterface;
import xoc.dsa.ISetupRfMeas;
import xoc.dsa.ISetupRfMeas.IModPower.SetupReceiverPath;
import xoc.dsa.ISetupRfStim;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.datatypes.dsp.MultiSiteWaveComplex;
import xoc.dta.datatypes.dsp.MultiSiteWaveDouble;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IRfMeasResults;
import xoc.dta.resultaccess.IRfMeasResults.IModPowerResults;
import xoc.dta.testdescriptor.IParametricTestDescriptor;
/**
 *
 * This test method performs RF power settling time test. "Power versus time" method is used in this method to calculate settling time.
 * Test setups are creadted by DSAPI in test method.<br><br>
 * The test is implemented by the following steps:
 * <ol>
 *      <li>The digitizer is trigged at the same time the DUT is programmed to transmit RF power, the power (watt) can be calculated
 *            for the retrieve captured waveform; </li>
 *      <li>Determine the final settled power value using the average of last segments of power versus time array </li>
 *      <li>from the end of power versus time array, <b>search backwards </b> to find the first point out of user defined threshold, e.g, 90%  </li>
 *      <li>calculated power settling time using the found settling point </li>
 * </ol>
 *
 * @param threshold     threshold value for the  power stable judge value
 * @param USID          USID for MIPI protocol interface
 *
 * @since  1.0.0
 * @author 308770
 *
 */
public class RFPowerSettlingTime_TEST extends TestMethod {

    @In    public Double threshold=0.95;
    @In    public String USID = "0xC";

    public IMeasurement cw_measure;
    public IParametricTestDescriptor ptd;
    public IParametricTestDescriptor ptd2;

    public String rfIN = "dummyRFIN";
    public String rfOUT = "dummyRFOUT";

    String RF_IN = "LB1_IN1";
    String RF_OUT = "LB1_OUT";

    String _testSuiteName;
    String testSuiteName;

    String stimName ;
    String measName ;



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
     * @param ptd_Rslt      IParametricTestDescriptor
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
        public String importSpec = "";

        public IParametricTestDescriptor ptd_Rslt;
    }
    public ParameterGroupCollection<StepMeas> Parameter = new ParameterGroupCollection<>();

    int measLoopCpunt = 50;

    @Override
    public void setup() {

        _testSuiteName = context.getTestSuiteName();
        testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));
        List<String> ports = Arrays.asList(testSuiteName.split("\\s*_\\s*"));
        RF_IN=ports.get(0)+"_"+ports.get(1);
        RF_OUT=ports.get(2)+"_"+ports.get(3);

        String[] atePort = PortMapping.getRFPort(RF_IN,RF_OUT);
        rfIN = atePort[0];
        rfOUT = atePort[1];

        stimName = testSuiteName +"_Stim";
        measName = testSuiteName +"_Meas";


        IDeviceSetup devSetup = DeviceSetupFactory.createInstance();


        ISetupRfStim setup_stim = devSetup.addRfStim(rfIN);
        ISetupRfMeas setup_meas = devSetup.addRfMeas(rfOUT);
        setup_stim.setConfigPortPort(rfIN);
        setup_meas.setConfigModeHighResolution();
        setup_stim.setConfigOptionMultisiteSplitterOff();
        setup_stim.setConfigOptionSiteInterlacingOn();
        setup_stim.setConfigModeSingleSource();

        setup_meas.setConfigOptionSiteInterlacingOn();

        devSetup.sequentialBegin();{


            //For loop code
            Set<String> key = Parameter.keySet();
            Set<String> sortedKey = new TreeSet<String>();
            Reg_Read mRegData = Reg_Read.getInstance();
            for (String _key : key) { sortedKey.add(_key); }

            for (String _sortedKey : sortedKey)
            {
                StepMeas meas = Parameter.get(_sortedKey);

                if(!meas.importSpec.isEmpty()) {
                    devSetup.importSpec(meas.importSpec);
                }

                String Reg_Key = "";
                if(!meas.Reg.contains("0x")&&!meas.Reg.equals(""))
                {
                    Reg_Key = testSuiteName+'_'+meas.Reg;
                }


                String RegReset_Key = "";
                if(!meas.RegReset.contains("0x")&&!meas.RegReset.equals(""))
                {
                    RegReset_Key = testSuiteName+'_'+meas.RegReset;
                }



                if(meas.getId().indexOf("Gain")!=-1)
                {
                    String RF_SW = PortMapping.getCWPat(RF_IN, RF_OUT);
                    devSetup.patternCall("setups.vectors.SWITCH."+RF_SW);

                }

                devSetup.parallelBegin();{

                    devSetup.sequentialBegin();{

                        if(meas.getId().contains("Gain"))
                        {

//                            devSetup.insertOpSeqComment("LNA_OFF");
//                            ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("rf_pwr_off"+meas.getId(), "MIPI.mipi");
//                            paInterface.addSignalRole("DATA", "SDATA");
//                            paInterface.addSignalRole("CLK", "SCLK");
//                            ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("rf_pwr_off"+meas.getId());
//
//                          List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
////                          println("INFO: TS ==== "+testSuiteName+"*********"+listAddrData);
//                          for(String perData: listAddrData){
//                              List<String> values = Arrays.asList(perData.split("_"));
//                              String addr = "<"+values.get(0)+">";
//                              String data = "<"+values.get(1)+">";
//                              transDigSrc.addTransaction("write","<"+USID+">",addr,data);
//
//                          }
//
//                            transDigSrc.addWait(10e-6);
//                            devSetup.transactionSequenceCall(transDigSrc,"");

                          devSetup.patternCall("dsa_gen.Main.LNA_OFF.LNA_OFF_Write");//LNA_OFF

                          devSetup.insertOpSeqComment("BandSelect");
                          ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("band_sel_out_sel"+meas.getId(), "setups.mipi.mipi");
                          paInterface.addSignalRole("DATA", "SDATA");
                          paInterface.addSignalRole("CLK", "SCLK");
                          ISetupTransactionSeqDef transDigSrc1= paInterface.addTransactionSequenceDef("band_sel_out_sel"+meas.getId());

                          if(!meas.Reg.contains("0x"))
                          {
                              meas.Reg = mRegData.m_RegDataMap.get(Reg_Key);
                          }
                          List<String> listAddrData = Arrays.asList(meas.Reg.split("\\s*;\\s*"));
                          for(String perData: listAddrData){
                              List<String> values = Arrays.asList(perData.split("_"));
                              String addr = "<"+values.get(0)+">";
                              String data = "<"+values.get(1)+">";
                              transDigSrc1.addTransaction("write","<"+USID+">",addr,data);
                          }

                          devSetup.transactionSequenceCall(transDigSrc1,"");


                        }

                    }devSetup.sequentialEnd();


                }devSetup.parallelEnd();



                devSetup.parallelBegin();{

                    devSetup.sequentialBegin();{
                        if(meas.getId().contains("Gain")&&!meas.RegReset.equals(""))
                        {
                            devSetup.insertOpSeqComment("LNA_ON");
                            ISetupProtocolInterface paInterface = devSetup.addProtocolInterface("rf_pwr_on"+meas.getId(), "setups.mipi.mipi");
                            paInterface.addSignalRole("DATA", "SDATA");
                            paInterface.addSignalRole("CLK", "SCLK");
                            ISetupTransactionSeqDef transDigSrc2= paInterface.addTransactionSequenceDef("rf_pwr_on"+meas.getId());

                            transDigSrc2.addWait(1.0e-3);

                            if(!meas.RegReset.contains("0x"))
                            {
                                meas.RegReset = mRegData.m_RegDataMap.get(RegReset_Key);
                            }
                            List<String> listAddrData = Arrays.asList(meas.RegReset.split("\\s*;\\s*"));
                            for(String perData: listAddrData){
                                List<String> values = Arrays.asList(perData.split("_"));
                                String addr = "<"+values.get(0)+">";
                                String data = "<"+values.get(1)+">";
                                transDigSrc2.addTransaction("write","<"+USID+">",addr,data);
                            }

                            devSetup.transactionSequenceCall(transDigSrc2,"");
                        }

                    }devSetup.sequentialEnd();

                    devSetup.sequentialBegin();{
                        if(meas.getId().contains("Gain"))
                        {
                            devSetup.insertOpSeqComment(stimName);
                            ISetupRfStim.ICw cwAction = setup_stim.cw(stimName+ meas.getId());
                            cwAction.setFrequency(meas.Freq1).setPower(meas.InPow+20);
                            devSetup.actionCall(stimName+ meas.getId() );
                        }


                      }devSetup.sequentialEnd();


                    devSetup.sequentialBegin();{
                        devSetup.waitCall(1.0e-3);
                        if(meas.getId().contains("Gain"))
                        {
                            devSetup.insertOpSeqComment(measName);
                            ISetupRfMeas.IModPower modActionFs = setup_meas.modPower(measName);
                            modActionFs.setFrequency(meas.Freq1-0.5e6).setExpectedMaxPower(meas.MeasRange+20).setSamples(1024*16);
                            modActionFs.setBandwidthOfInterest(10.0e6).setIfFrequency(27.12311e6).setReceiverPath(SetupReceiverPath.preamp3);//.setSampleRate(204.8e6);
                            devSetup.actionCall(measName );
                        }
                    }devSetup.sequentialEnd();
                    setup_stim.setDisconnect(true);
                    setup_meas.setDisconnect(true);

                }devSetup.parallelEnd();

            }


        }devSetup.sequentialEnd();

        cw_measure.setSetups(devSetup);

    }

    @Override
    public void update() {
        // TODO Auto-generated method stub

    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub

        cw_measure.execute();


        IRfMeasResults rfResult = cw_measure.rfMeas(rfOUT).preserveResults();

        releaseTester();

        IModPowerResults Modresult = rfResult.modPower(measName);
        MultiSiteWaveComplex wave_cmplx = Modresult.getComplexWaveform(rfOUT).getElement(0);


        MultiSiteDouble power_settling_time = Power_Server_timeDomain(wave_cmplx);

        double Reg_time = ((38*26)/40)*40*1e-9;  // 38 valid cycles for reg, period = 26ns
        power_settling_time = power_settling_time.subtract( Reg_time);


        ptd.setTestText(testSuiteName+":ST");
        ptd.evaluate(power_settling_time);


    }

    @SuppressWarnings("unused")
    /**
     * This method is used to calculated RF power settling time using "Power versus time" method.
     *
     * @param   wave_src  captured waveform
     * @return  value of settling time
     */
    private MultiSiteDouble Power_Server_timeDomain(MultiSiteWaveComplex wave_src ){

        double Threshold_delta = 0.5;
        int totalPoints = 1024*2;
        int Averagelength = 2;

        MultiSiteDouble tmpValue;

        MultiSiteLong OverShootdetect = new MultiSiteLong(0L);

        MultiSiteDouble Frequency =wave_src.getSampleRate();

        MultiSiteDouble ones_Array = new MultiSiteDouble(1);
        MultiSiteDouble sampleperiod = ones_Array.divide(Frequency);

        MultiSiteWaveComplex wave_cmplx;
        wave_cmplx = wave_src.extractValues(0, totalPoints+Averagelength-1);

        MultiSiteWaveDouble wave_real = wave_cmplx.getReal();
        MultiSiteWaveDouble wave_imag = wave_cmplx.getImaginary();

        MultiSiteWaveDouble power_timedomain_ori = wave_imag;
        MultiSiteWaveDouble power_timedomain = wave_real.extractValues(0, totalPoints);

        int[] activeSites = context.getActiveSites();
        for(int site:activeSites)
        {
            for(int itr = 0; itr<(wave_real.getSize(site)); itr++)
            {
                double pwr_i_dbm =wave_real.getValue(site, itr)*wave_real.getValue(site, itr) ;
                double pwr_q_dbm =wave_imag.getValue(site, itr)*wave_imag.getValue(site, itr) ;
                double pwr_iq_dbm = 10*Math.log10(pwr_i_dbm + pwr_q_dbm)+13;
                power_timedomain_ori.setValue(site,itr, pwr_iq_dbm);
            }
        }

        power_timedomain = power_timedomain_ori.movingAverage(Averagelength);


//        power_timedomain.plot("power_timedomain", testSuiteName);

          int settled_power_T2_int = totalPoints/2;
          int settled_power_T3_int = totalPoints-1;

        MultiSiteWaveDouble power_timedomain_settled_short = power_timedomain.extractValues(settled_power_T2_int, settled_power_T3_int-settled_power_T2_int+1);
        MultiSiteDouble stable_power = new MultiSiteDouble(-100.0);


        MultiSiteDouble sum= new MultiSiteDouble(0.0);

        for(int site:activeSites)
        {
            double  tmp_sum =0.0;

            for(int index = 0; index<(power_timedomain_settled_short.getSize(site)); index++)
            {
                double tmpValue1 = power_timedomain_settled_short.getValue(site, index);
                tmp_sum = tmp_sum +tmpValue1;
            }

            sum.set(site, tmp_sum);
        }

        sum=sum.divide(power_timedomain_settled_short.getSize());
        stable_power.set(sum);

        MultiSiteDouble Start_power = stable_power.add(20*Math.log10(1-threshold));

        double Threshold_Hi = -10*Math.log10(threshold*threshold);  //0.9 times amplitude 0.81 =0.9*0.9   0.4dB
        double Threshold_Lo = 10*Math.log10(threshold*threshold);

        double ThrOverShoot_Lo = 10*Math.log10(1.03*1.03);
        double ThrOverShoot_Hi = 10*Math.log10(1.8*1.8);


        MultiSiteLong first_settled_point = new MultiSiteLong(1L);
        MultiSiteLong OverShoot_point = new MultiSiteLong(0L);
        MultiSiteLong first_start_point = new MultiSiteLong(0L);


        for(int site:activeSites)
        {

            for(int index = power_timedomain.getSize(site)-100; index>2; index--)
            {

                double tmp = power_timedomain.getValue(site, index)-stable_power.get(site);

                if ( Threshold_Hi<tmp  || Threshold_Lo >tmp)
                {

//                    println("WARNING: 1st touch point @"+index+" time_power ="+ power_timedomain.getValue(site, index));

                    int second_search_point = index-1;

                    double tmp2 = power_timedomain.getValue(site, second_search_point)-stable_power.get(site);
                    if(Threshold_Hi<tmp2  || Threshold_Lo >tmp2)
                    {
//                        println("ERROR: 2nd touch point @"+second_search_point+" time_power ="+ power_timedomain.getValue(site, second_search_point));

                        int third_search_point = second_search_point-1;
                        double tmp3 = power_timedomain.getValue(site, third_search_point)-stable_power.get(site);
                        {
                            if(Threshold_Hi<tmp3  || Threshold_Lo >tmp3)
                            {
                                first_settled_point.set(site, third_search_point+3);  //todo: third_search_point+2 unstable, third_search_point+3 stable
//                                println("INFO: fisrt stable touch point @"+third_search_point+" time_power ="+ power_timedomain.getValue(site, third_search_point));

                              break;
                            }
                        }

                    }
                }//TODO: end of continue 3 search point method

            }



            /*
             * Over Shoot Judge
             */
            if(testSuiteName.contains("Power_Servo")  ||testSuiteName.contains("LG3_HG1") ||testSuiteName.contains("OFF_LG3") ||testSuiteName.contains("OFF_LG4") || testSuiteName.contains("LG3_LG4"))
            {
                for(int index = first_settled_point.getAsInt(site)+50; index>2; index--)
                {

                    double tmp = power_timedomain.getValue(site, index)-stable_power.get(site);

//                    if(testSuiteName.contains("LB1_IN1_LB_OUT_B5_HG1_OPA_PS_LG3_HG1"))
//                    {
//                        println("INFO: index "+index + "Power: "+ power_timedomain.getValue(site, index)+" diffreence: "+tmp);
//                    }

                    if(ThrOverShoot_Lo<tmp  && ThrOverShoot_Hi >tmp)
                    {
                      OverShoot_point.set(site, index);
                      OverShootdetect.set(site, 1L );
//                      println("INFO: Last overshoot point @"+index+" time_power ="+ power_timedomain.getValue(site, index)+" , settled power :"+stable_power.get(site));

                        break;
                    }//TODO:  1 touch-end search method

                }

//                power_timedomain.plot("power_timedomain", testSuiteName);

            }

        }


        tmpValue = sampleperiod.multiply(first_settled_point.subtract(first_start_point));

        return tmpValue;


    }



}
