package src.rf2rf_tml;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

import src.rf2rf_tml.utilities.MapDatalog;
import src.rf2rf_tml.utilities.UtMethods;
import src.rfcbb_tml.com.CalData;
import src.rfcbb_tml.com.PortMapping;
import src.rfcbb_tml.com.RF_UserCommon;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupRfMeas;
import xoc.dsa.ISetupRfMeas.IModPower.SetupReceiverPath;
import xoc.dsa.ISetupRfStim;
import xoc.dsa.ISetupRfStim.INoise;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.annotations.Out;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.dsp.MultiSiteSpectrum;
import xoc.dta.datatypes.dsp.MultiSiteSpectrumComplex;
import xoc.dta.datatypes.dsp.MultiSiteWaveComplex;
import xoc.dta.datatypes.dsp.SpectrumComplex;
import xoc.dta.datatypes.dsp.SpectrumUnit;
import xoc.dta.datatypes.dsp.WaveComplex;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IRfMeasResults;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
 * This test method is for NF calibration. it can generate the operating sequencer and setups for test instruments according to the
 * setup information from testtable. <br>
 * The test method read the golden reference NF value, measure the actual NF value,  and use calibration algorithm to calculate
 * <b>inLoss</b> of the test path.
 *
 * @param flagSiteInterlacing   Flag indication SiteInterlaction
 * @param hGainH                High Gain in HGain mode
 * @param hGainL                Low Gain in HGain mode
 * @param hGainM                Middle Gain in HGain mode
 * @param freqL                 Frequency for Low Gain
 * @param freqM                 Frequency for Middle Gain
 * @param freqH                 Frequency for High Gain
 * @param lGainM                MLP Gain in Low Gain mode
 * @param gMaxMeas              measured Gain with loss considered
 * @param sysNF                 Noise figure of test receiver
 * @param importSpec            setup specification for test setup
 * @param sampleSize            sample size for measurement
 * @param in_HP_NF              NF in High Gain mode
 * @param in_HP_Gain            Gain in High Gain mode
 * @param in_LP_NF              NF in Low Gain mode
 * @param in_LP_Gain            Gain in Low Gain mode
 * @param in_Y                  Y factor
 * @param out_HP_NF             output variable for NF in high Gain mode
 * @param out_HP_Gain           output variable for Gain in high Gain mode
 * @param out_Y                 output variable for Y factor
 * @param burstNumber           test item number in testsuite
 *
 *
 * @since 1.0.0
 * @author 308770
 *
 */
public class NF_CAL extends TestMethod {
    @In    public boolean flagSiteInterlacing = true;

    @In public MultiSiteDouble hGainH = new MultiSiteDouble();
    @In public MultiSiteDouble hGainL = new MultiSiteDouble();
    @In public MultiSiteDouble hGainM = new MultiSiteDouble();
    @In public Double freqL = 0.0;
    @In public Double freqM = 0.0;
    @In public Double freqH = 0.0;


    @In  public MultiSiteDouble lGainM = new MultiSiteDouble();
    @Out public MultiSiteDouble gMaxMeas = new MultiSiteDouble();

    @In  public MultiSiteDouble sysNF = new MultiSiteDouble();

    @In  public String importSpec = "";

    @In  public int sampleSize = 2048*4;

    @In  public MultiSiteDouble in_HP_NF = new MultiSiteDouble();
    @In  public MultiSiteDouble in_HP_Gain = new MultiSiteDouble();
    @In  public MultiSiteDouble in_LP_NF = new MultiSiteDouble();
    @In  public MultiSiteDouble in_LP_Gain = new MultiSiteDouble();
    @In  public MultiSiteDouble in_Y = new MultiSiteDouble();

    @Out public MultiSiteDouble out_HP_NF = new MultiSiteDouble();
    @Out public MultiSiteDouble out_HP_Gain = new MultiSiteDouble();
    @Out public MultiSiteDouble out_Y = new MultiSiteDouble();

    @In  public Integer burstNumber = 1;

    boolean debug=false;


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
     * @param ptd_Rslt_InLoss     IParametricTestDescriptor for InLoss
     * @param ptd_Rslt_OutLoss    IParametricTestDescriptor for OutLoss
     * @param ptd_Rslt_NFeff      IParametricTestDescriptor for effectiveNF  <br><br>
     *
     * @author 308770
     *
     */

    public class StepMeas extends ParameterGroup{
        public String TestType = "Gain";
        public Double Freq1 = 7.51e8;
        public Double Freq2 = 0.0;
        public double MeasRange = -20;
        public double InPow = -30;
        public String Reg = "";

        public IParametricTestDescriptor ptd_Rslt_InLoss;
        public IParametricTestDescriptor ptd_Rslt_OutLoss;
        public IParametricTestDescriptor ptd_Rslt_NFeff;
    }
    public ParameterGroupCollection<StepMeas> Parameter = new ParameterGroupCollection<>();

    public  String rfIN = "dummyRFIN";
    public  String rfOUT = "dummyRFOUT";
    public  String opMode = "A_HP";
    public String Powermode="HP";
    String _testSuiteName;
    String testSuiteName;
    String stimName ;
    String measName ;
    String RF_IN = "HB1_IN1";
    String RF_OUT = "HB1_OUT";
    int    band = 0;

    double IF_Freq= 20.12311e6;

    @Override
    public void setup (){
        String pattPath = "vectors.patterns."; //input parameter
        _testSuiteName = context.getTestSuiteName();
        testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));
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


        IDeviceSetup devSetup = DeviceSetupFactory.createInstance();
        if(!importSpec.isEmpty()) {
            devSetup.importSpec(importSpec);
        }


        ISetupRfStim setup_stim = devSetup.addRfStim(rfIN);
        ISetupRfMeas setup_meas = devSetup.addRfMeas(rfOUT);
        setup_stim.setConfigPortPort(rfIN);
        setup_meas.setConfigModeHighResolution();
        if(flagSiteInterlacing) {
            setup_meas.setConfigOptionSiteInterlacingOn();
        }

        devSetup.sequentialBegin();
        {
            for(StepMeas meas: Parameter.values() ){
                devSetup.parallelBegin();
                {
                    devSetup.patternCall(pattPath+"AMP_SWITCH");
                }
                devSetup.parallelEnd();
                devSetup.parallelBegin();
                {
                    devSetup.sequentialBegin();{

                        setup_stim.setConfigModeSingleSource();
                        ISetupRfStim.INoise cwAction = setup_stim.noise(stimName+"_Hot" +meas.getId());
                        cwAction.setFrequency(meas.Freq1).setEnr(meas.InPow).setIdleCwPower(INoise.SetupIdleCwPower.rms); //15db normally
                        devSetup.actionCall(stimName+"_Hot" + meas.getId() );
                    }
                    devSetup.sequentialEnd();
                    devSetup.sequentialBegin();{
                        String actMeasName = measName + meas.getId();
                        actMeasName = measName+"_Hot" + meas.getId();
                        ISetupRfMeas.IModPower modActionFs = setup_meas.modPower(actMeasName);
                        modActionFs.setFrequency(meas.Freq1).setExpectedMaxPower(meas.MeasRange).setSamples(2048*8*2).setResultAveraging(10); //2048*8*8
                        modActionFs.setBandwidthOfInterest(10.0e6).setIfFrequency(IF_Freq).setReceiverPath(SetupReceiverPath.preamp3);
                        devSetup.waitCall(10e-3);
                        devSetup.actionCall(actMeasName);
                    }
                    devSetup.sequentialEnd();
                }
                devSetup.parallelEnd();

                devSetup.parallelBegin();
                {
                    devSetup.sequentialBegin();{
                        @SuppressWarnings("unused")
                        ISetupRfStim.ILoad cwAction = setup_stim.load(stimName+"_Cold" +meas.getId());
                        devSetup.actionCall(stimName+"_Cold" + meas.getId() );
                    }
                    devSetup.sequentialEnd();
                    devSetup.sequentialBegin();{

                        if(opMode.contains("LP")){
                            sampleSize = 2048*8*2*4;
                        }

                        ISetupRfMeas.IModPower modActionFs = setup_meas.modPower(measName+"_Cold" + meas.getId());
                        modActionFs.setFrequency(meas.Freq1).setExpectedMaxPower(meas.MeasRange).setSamples(sampleSize).setResultAveraging(20);
                        modActionFs.setBandwidthOfInterest(10.0e6).setIfFrequency(IF_Freq).setReceiverPath(SetupReceiverPath.preamp3);
                        devSetup.waitCall(10e-3);
                        devSetup.actionCall(measName+"_Cold" + meas.getId() );
                    }
                    devSetup.sequentialEnd();
                }
                devSetup.parallelEnd();

                if(burstNumber==1){
                    break;
                }
            }//for
        }//seq def End
        devSetup.sequentialEnd();
        measurement.setSetups(devSetup);
        message(1, testSuiteName + " device setup done!" );


    }


    public IMeasurement measurement;


    private List<MultiSiteDouble> Calc_inloss_Fr( MultiSiteDouble F1, MultiSiteDouble F2, MultiSiteDouble Y1, MultiSiteDouble Y2,
            MultiSiteDouble G1, MultiSiteDouble G2, MultiSiteDouble ENR)
            {
        /// Xdb = ENRdb - Input Loss
        /// Z = Freceiver -1

        // F = (Xlinear)/(Y-1) - (Freceiver-1)/Gain

        // F = X/(Y-1) - Z/G ;


        MultiSiteDouble tmp1 = G1.divide(Y1.subtract(1));
        MultiSiteDouble tmp2 = G2.divide(Y2.subtract(1));


        MultiSiteDouble X = (F1.multiply(G1).subtract(F2.multiply(G2))).divide(tmp1.subtract(tmp2));
        MultiSiteDouble Z = X.multiply(tmp1).subtract(F1.multiply(G1));


        MultiSiteDouble In_loss_db = UtMethods.linear2db(ENR).subtract(UtMethods.linear2db(X));
        MultiSiteDouble Freceiver = Z.add(1);

        if(debug){


            System.out.println("In_loss  = "+UtMethods.db2linear(In_loss_db));
            System.out.println("F Receiver  = "+Freceiver);
            System.out.println("ENR  = "+ENR);
            System.out.println("G1 = "+G1);
            System.out.println("G2 = "+G2);
            System.out.println("F1 = "+F1);
            System.out.println("F2 = "+F2);
            System.out.println("Y1 = "+Y1);
            System.out.println("Y2 = "+Y2);


            System.out.println("In_loss  = "+In_loss_db);
            System.out.println("F Receiver  = "+UtMethods.linear2db(Freceiver));
            System.out.println("ENR  = "+UtMethods.linear2db(ENR));
            System.out.println("G1 = "+UtMethods.linear2db(G1));
            System.out.println("G2 = "+UtMethods.linear2db(G2));
            System.out.println("F1 = "+UtMethods.linear2db(F1));
            System.out.println("F2 = "+UtMethods.linear2db(F2));
            System.out.println("Y1 = "+UtMethods.linear2db(Y1));
            System.out.println("Y2 = "+UtMethods.linear2db(Y2));
        }


        return Arrays.asList(In_loss_db,UtMethods.linear2db(Freceiver));

            }


    @SuppressWarnings("unused")
    private MultiSiteDouble remove_spur( MultiSiteWaveComplex wave){

        MultiSiteDouble spur_max=new MultiSiteDouble();
        spur_max.set(30);


        return spur_max;
    }





    @SuppressWarnings("unused")
    private MultiSiteDouble Get_sysNF( MultiSiteWaveComplex waveform_hot){

        MultiSiteDouble tmpNF=new MultiSiteDouble();

        for(int site: context.getActiveSites() ){

            WaveComplex waveform_hot1 = waveform_hot.get(site);

            double hot_power = waveform_hot.modPower(SpectrumUnit.mW).divide(1e3).get(site)*2;
            double DUT_NF = waveform_hot1.noiseFigureYFactorMethod(hot_power, 19, 1, false); //not work in 805
            tmpNF.set(site,DUT_NF);
        }

        tmpNF = UtMethods.linear2db(UtMethods.db2linear(tmpNF).multiply(-1).add(20));
        System.out.println("System NF[db]  = "+ tmpNF );

        return tmpNF;
    }


    @SuppressWarnings("unused")
    private MultiSiteWaveComplex Spur_filter( MultiSiteWaveComplex wave,String waveName)
    {
        MultiSiteWaveComplex wave_filtered;
        MultiSiteSpectrum spect;
        MultiSiteSpectrumComplex wfft;

        MultiSiteDouble BW= wave.getBandwidthOfInterest();

        spect=wave.spectrum(SpectrumUnit.dBm).setBandwidthOfInterest(BW);
        wfft=wave.fft();

        long fsize= spect.getSize(context.getActiveSites()[0]);
        long startBinFull = spect.getSpectrumStartBin().get(context.getActiveSites()[0]);
        long stopBinFull = spect.getSpectrumStopBin().get(context.getActiveSites()[0]);

        long startbin = spect.getBandwidthStartBin().get(context.getActiveSites()[0]);
        long stopbin = spect.getBandwidthStopBin().get(context.getActiveSites()[0]);
        MultiSiteDouble mean_spect = spect.extractValues((int)startbin, (int)(stopbin-startbin)).mean();
        long fft_startbin= startbin + fsize/2 ;
        long fft_stopbin=  stopbin - fsize/2;

        if(debug){
            System.out.println("======= Start  "+context.getTestSuiteName());

            System.out.println("size  = "+fsize);
            System.out.println("Spect start  = "+startbin);
            System.out.println("Spect stop  = "+stopbin);
            System.out.println("FFT start  = "+fft_startbin);
            System.out.println("FFT stop  = "+fft_stopbin);
        }
        MultiSiteDouble mean_fft_r = wfft.extractValues((int)fft_startbin, (int)(fsize-fft_startbin)).getReal().pow(2).mean().sqrt();
        MultiSiteDouble mean_fft_i = wfft.extractValues((int)fft_startbin, (int)(fsize-fft_startbin)).getImaginary().pow(2).mean().sqrt();
        if(debug){
            System.out.println("mean real  = "+mean_fft_r);
            System.out.println("mean imag  = "+mean_fft_i);
        }

        int[] activeSites = context.getActiveSites();

        for(int site : activeSites)
        {

            double[] tmp = spect.getArray(site);
            double tmp_mean_spect= mean_spect.get(site);
            double tmp_mean_fft_i= mean_fft_i.get(site);
            double tmp_mean_fft_r= mean_fft_r.get(site);
            SpectrumComplex sfft = wfft.get(site);
            Complex mean_cmplx= new Complex(tmp_mean_fft_r, tmp_mean_fft_i);

            for (int i=(int)startbin; i <=(int)stopbin; i++)
            {

                if(tmp[i]> tmp_mean_spect+25.0)
                {
                    int fftBin=i;
                    if(i<fsize/2){ fftBin = i+(int)(fsize/2);
                    }else{
                        fftBin = i-(int)(fsize/2);
                    }
                    sfft.setValue(fftBin, mean_cmplx);

                }
            }
            sfft.setValue(0, mean_cmplx);
            sfft.setValue(1, mean_cmplx);
            sfft.setValue((int)(fsize-1), mean_cmplx);
            sfft.setValue((int)(fsize-2), mean_cmplx);
            wfft.set(site, sfft);


        }

        if(debug){
            System.out.println("BW  = "+BW);
            System.out.println("Mean Spect  = "+mean_spect);
            System.out.println("Mean fft I  = "+mean_fft_i);
            System.out.println("Mean fft R  = "+mean_fft_r);
            System.out.println("Start  = "+startbin);
            System.out.println("Stop  = "+stopbin);
        }


        wave_filtered=wfft.ifft();
        if(debug){
            System.out.println("Mean Spect  = "+mean_spect);
            System.out.println("Mean fft I  = "+mean_fft_i);
            System.out.println("Mean fft R  = "+mean_fft_r);
            wave_filtered.spectrum(SpectrumUnit.dBm).plot("Filtered_spect_"+waveName, context.getTestSuiteName());
        }




        return wave_filtered;
    }




    @Override
    public void update(){ //This is the area to modify something that already exists..


    }

    @Override
    public void execute() {
        measurement.execute();
        if(debug){
            System.out.println("----------------------------------");
        }
        IRfMeasResults rfResult = measurement.rfMeas(rfOUT).preserveResults();

        for(StepMeas meas: Parameter.values() ){

            String stepText =meas.TestType;
            Double tmpFreq1= (meas.Freq1/1.0e6);
            stepText = Integer.toString(tmpFreq1.intValue())+"MHz_"+stepText;

            MultiSiteWaveComplex waveform_hot = rfResult.modPower(measName+"_Hot"+meas.getId()).getComplexWaveform(rfOUT).getElement(0);
            MultiSiteWaveComplex waveform_cold = rfResult.modPower(measName+"_Cold"+meas.getId()).getComplexWaveform(rfOUT).getElement(0);
            waveform_hot = RF_UserCommon.removeingDC_RF(waveform_hot);
            waveform_cold = RF_UserCommon.removeingDC_RF(waveform_cold);

            MultiSiteDouble power_hot = waveform_hot.modPower(SpectrumUnit.dBm);
            MultiSiteDouble power_cold = waveform_cold.modPower(SpectrumUnit.dBm);

            MultiSiteDouble act_enr = measurement.rfStim(rfIN).noise(stimName+"_Hot"+meas.getId()).getEnr10MHz();

            MultiSiteDouble Y= UtMethods.db2linear(power_hot).divide(UtMethods.db2linear(power_cold));

            if(debug){
                System.out.println(measName+"_Hot"+meas.getId()+" db: "+ power_hot);
                System.out.println(measName+"_Cold"+meas.getId()+" db: "+ power_cold);
                System.out.println(measName+":power_hot walts: "+ UtMethods.db2linear(power_hot));
                System.out.println(measName+":power_cold walts: "+ UtMethods.db2linear(power_cold));
                System.out.println("Y : "+ Y);
                System.out.println("Ydb : "+ UtMethods.linear2db(Y));
                System.out.println(" ");
            }


            out_Y = Y;
            if(debug){
                Double currFreq =  measurement.rfMeas(rfOUT).modPower(measName+"_Hot"+meas.getId()).getFrequency().get();
                System.out.println("Freq = "+currFreq);
            }
            if(context.getTestSuiteName().contains("_LP_NF")){

                CalData mCalData = CalData.getInstance();
                String keyName= RF_IN+"_"+RF_OUT+"_B"+band;

                MultiSiteDouble g_GainH =mCalData.getValue(0,keyName,"golden_Gain_HP",0.0);
                MultiSiteDouble g_GainL =mCalData.getValue(0,keyName,"golden_Gain_LP",0.0);
                MultiSiteDouble g_NfH =mCalData.getValue(0,keyName,"golden_NF_HP",0.0);
                MultiSiteDouble g_NfL =mCalData.getValue(0,keyName,"golden_NF_LP",0.0);


                in_HP_Gain.set(g_GainH);
                in_LP_Gain.set(g_GainL);
                in_HP_NF.set(g_NfH);
                in_LP_NF.set(g_NfL);

                MultiSiteDouble F1 = UtMethods.db2linear(in_HP_NF);
                MultiSiteDouble F2 = UtMethods.db2linear(in_LP_NF);
                MultiSiteDouble G1 = UtMethods.db2linear(in_HP_Gain);
                MultiSiteDouble G2 = UtMethods.db2linear(in_LP_Gain);
                MultiSiteDouble Y1 = in_Y;
                MultiSiteDouble Y2 = Y;
                MultiSiteDouble ENR = UtMethods.db2linear(act_enr);
                List<MultiSiteDouble>tmpList= Calc_inloss_Fr( F1, F2, Y1, Y2, G1, G2, ENR);

                MultiSiteDouble inLoss= new MultiSiteDouble(-999);

                MultiSiteDouble nfEff= new MultiSiteDouble(-999);
                inLoss = tmpList.get(0);
                nfEff = tmpList.get(1);
                MultiSiteDouble totalLossH = g_GainH.subtract(hGainH);
                MultiSiteDouble totalLossM = g_GainH.subtract(hGainM);
                MultiSiteDouble totalLossL = g_GainH.subtract(hGainL);
                MultiSiteDouble outLossH = totalLossH.subtract(inLoss);
                MultiSiteDouble outLossM = totalLossM.subtract(inLoss);
                MultiSiteDouble outLossL = totalLossL.subtract(inLoss);
                MultiSiteDouble totalLossM_LP = g_GainL.subtract(lGainM);
                MultiSiteDouble outLossM_LP = totalLossM_LP.subtract(inLoss);

                meas.ptd_Rslt_InLoss.setTestText("InLoss");
                meas.ptd_Rslt_InLoss.evaluate(inLoss);

                meas.ptd_Rslt_OutLoss.setTestText("OutLoss_M");
                meas.ptd_Rslt_OutLoss.evaluate(outLossM);
                meas.ptd_Rslt_OutLoss.setTestText("OutLoss_M_LP");
                meas.ptd_Rslt_OutLoss.evaluate(outLossM_LP);

                meas.ptd_Rslt_NFeff.setTestText("NFeff");
                meas.ptd_Rslt_NFeff.evaluate(nfEff);

                MapDatalog mDataMap = MapDatalog.getInstance();
                mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_InLoss",inLoss);
                mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLoss",freqM/1.0e6,outLossM);
                mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_OutLossL",freqM/1.0e6,outLossM_LP);
                mDataMap.setValue(RF_IN+"_"+RF_OUT+"_B"+band, "cal_NFeff",nfEff);

                System.out.println(RF_IN+"_"+RF_OUT+"_B"+band+":In_loss  = "+inLoss);
                System.out.println(RF_IN+"_"+RF_OUT+"_B"+band+":cal_OutLossH  = "+outLossH);
                System.out.println(RF_IN+"_"+RF_OUT+"_B"+band+":cal_OutLossM  = "+outLossM);
                System.out.println(RF_IN+"_"+RF_OUT+"_B"+band+":cal_OutLossL  = "+outLossL);
                System.out.println(RF_IN+"_"+RF_OUT+"_B"+band+":cal_OutLossM_LP  = "+outLossM_LP);
                System.out.println(RF_IN+"_"+RF_OUT+"_B"+band+":F Receiver  = "+nfEff);

            }
            if(burstNumber==1){
                break;
            }
        }
    }
}
