package src.rfcbb_tml.com;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;

import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteComplex;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.datatypes.MultiSiteLongArray;
import xoc.dta.datatypes.dsp.MultiSiteSpectrum;
import xoc.dta.datatypes.dsp.MultiSiteWaveComplex;
import xoc.dta.datatypes.dsp.MultiSiteWaveDouble;
import xoc.dta.datatypes.dsp.MultiSiteWaveDoubleArray;
import xoc.dta.datatypes.dsp.SpectrumUnit;
import xoc.dta.datatypes.dsp.WaveComplex;
import xoc.dta.datatypes.dsp.WaveDouble;
import xoc.dta.datatypes.dsp.WaveformFileType;
import xoc.dta.datatypes.dsp.WindowFunction;
import xoc.dta.datatypes.dsp.WindowScaling;
import xoc.dta.resultaccess.datatypes.BitSequence;
import xoc.dta.resultaccess.datatypes.MultiSiteBitSequence;


/**
 *
 * this common class provide lots of methods for data processing after measurement execution.
 *
 *
 * @author 308770
 *
 */


public class RF_UserCommon {
    private static final boolean hiddenUpload = true;
    public static boolean hiddenupload() {
        return hiddenUpload;
    }


    private static int DUT_ID = 0;

    /**
     * this method gets LBID
     *
     * @return
     */
    public static int getDUT_ID() {
        return DUT_ID;
    }

    /**
     * this method link the value of DUT_ID to LBID
     * @param id
     */
    public static void setDUT_ID(int id) {
        DUT_ID = id;
    }


    public static int Caculate_DeltaGain (int gain_reg)
    {
        int Gain_delta;
        int[] RX_LNA_mapping = {48,45,42,36,30,24,18,6};
        int[] RX_VGA_mapping = {13,11,9,7,5,3,1,-1,-3,-5,-7,-9,-11,-13,-13,-13};
        int LNA_gain_reg = (gain_reg>>12)&0x7;
        int VGA_gain_reg = (gain_reg>>8)&0xf;
        Gain_delta = RX_LNA_mapping[0] + RX_VGA_mapping[0] - RX_LNA_mapping[LNA_gain_reg] - RX_VGA_mapping[VGA_gain_reg];
        return(Gain_delta);
    }


    /**
     * This method calculates RX output power on target frequency
     *
     * @param capWave    input waveform for calculate power
     * @param freqOut    target frequency
     * @param debugLevel level control for output
     *
     *
     * @return  double power value with unit in dBm for multisites
     *
     *
     * @since 1.0.0
     */
    public static MultiSiteDouble RX_IQ_PowerDsp (MultiSiteWaveComplex capWave,Double freqOut, int debugLevel )
    {
        MultiSiteDouble iqPower = new MultiSiteDouble();
        long sampleSize = capWave.getSize().get();
        double sampleRate = capWave.getSampleRate().get();
        int stdBin = (int)(sampleSize* freqOut/sampleRate + sampleSize/2 -1) ;
        MultiSiteSpectrum spect = capWave .spectrum(SpectrumUnit.dBm);
        iqPower = spect.getValue(stdBin);
        if(debugLevel>=2)
        {
            spect.plot("spectrum","rxIIP_Cal");
            System.out.println("rxIIP_Cal sample size = "+ sampleSize);
            System.out.println("rxIIP_Cal sample rate = "+ sampleRate);
            System.out.println("rxIIP_Cal std bin = "+ stdBin);
            System.out.println("rxIIP_Cal iq power = "+ iqPower);
        }
        return(iqPower);
    }



    /**
     * This method calculates RX IQ gain on target frequency
     *
     * @param capWave       input waveform for calculate power
     * @param freqOut       target frequency
     * @param inputPower    input power of RF Stimulus for test
     * @param getPowerFlag  Flag indicate to return power or Gain for I Q
     * @param debugLevel    level control for output
     *
     *
     * @return  double value for power/Gain for I and Q path
     *
     *
     * @since 1.0.0
     */
    public static MultiSiteDouble[] RX_IQ_GainDsp (MultiSiteWaveComplex capWave,Double freqOut,  Double inputPower, int getPowerFlag, int debugLevel )
    {
        MultiSiteDouble idBm = new MultiSiteDouble();
        MultiSiteDouble qdBm = new MultiSiteDouble();
        MultiSiteDouble[] iqdBm = new MultiSiteDouble[2];
        long sampleSize = capWave.getSize().get();
        double sampleRate = capWave.getSampleRate().get();
        int stdBin = (int)(sampleSize *  freqOut /sampleRate +0.5);
        MultiSiteSpectrum iABS   = capWave.getReal().setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).spectrum(SpectrumUnit.V);
        MultiSiteSpectrum qABS   = capWave.getImaginary().setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).spectrum(SpectrumUnit.V);
        if(stdBin < (sampleSize/2))
        {
            MultiSiteDouble idBm_S = iABS.getValue(stdBin).pow(2);
            MultiSiteDouble idBm_L = iABS.getValue(stdBin-1).pow(2);
            MultiSiteDouble idBm_H = iABS.getValue(stdBin+1).pow(2);
            idBm = idBm_S.add(idBm_L).add(idBm_H).log10().multiply(10).add(10);
            MultiSiteDouble qdBm_S = qABS.getValue(stdBin).pow(2);
            MultiSiteDouble qdBm_L = qABS.getValue(stdBin-1).pow(2);
            MultiSiteDouble qdBm_H = qABS.getValue(stdBin+1).pow(2);
            qdBm = qdBm_S.add(qdBm_L).add(qdBm_H).log10().multiply(10).add(10);
        }
        else
        {
            idBm.set(999);
            qdBm.set(999);
        }
        if(getPowerFlag ==0)
        {
            idBm   = idBm.subtract(inputPower);
            qdBm   = qdBm.subtract(inputPower);
        }
        iqdBm[0]= idBm;
        iqdBm[1]= qdBm;
        return(iqdBm);
    }



    /**
     * This method calculates RX IQ gain on target frequency
     *
     * @param capWave       input waveform for calculate power
     * @param freqOut       target frequency
     * @param inputPower    input power of RF Stimulus for test
     * @param getPowerFlag  Flag indicate to return power or Gain for I Q
     * @param debugLevel    level control for output
     *
     *
     * @return  double value for Power/Gain/cpxGain for I and Q path
     *
     *
     * @since 1.0.0
     */
    public static MultiSiteDouble[] RX_IQ_Complex_GainDsp (MultiSiteWaveComplex capWave,Double freqOut,  Double inputPower, int getPowerFlag, int debugLevel )
    {
        MultiSiteDouble[] iqdBm = new MultiSiteDouble[3];
        MultiSiteDouble cpxDBm = new MultiSiteDouble();
        MultiSiteDouble idBm = new MultiSiteDouble();
        MultiSiteDouble qdBm = new MultiSiteDouble();
        long sampleSize = capWave.getSize().get();
        double sampleRate = capWave.getSampleRate().get();
        int stdBin = (int)(sampleSize -(sampleSize*  freqOut /sampleRate) +0.5 );
        int stdBinIQ = (int)(sampleSize *  freqOut /sampleRate);
        if(debugLevel >=2){
            System.out.println("input power is "+inputPower);
            System.out.println("freqOut is "+ freqOut);
            System.out.println("sampleRate is "+sampleRate);
            System.out.println("sampleSize is "+sampleSize);
            System.out.println("stdBin is "+stdBin);
            System.out.println("stdBinIQ is "+stdBinIQ);
        }
        MultiSiteSpectrum cpxABS = capWave.setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).fft().abs();
        MultiSiteDouble cpxDBm_S = cpxABS.getValue(stdBin).pow(2);
        MultiSiteDouble cpxDBm_L = cpxABS.getValue(stdBin-1).pow(2);
        MultiSiteDouble cpxDBm_H = cpxABS.getValue(stdBin+1).pow(2);
        cpxDBm = cpxDBm_S.add(cpxDBm_L).add(cpxDBm_H).log10().multiply(10).add(10);
        if(stdBinIQ < (sampleSize/2))
        {
            MultiSiteSpectrum iABS   = capWave.getReal().setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).spectrum(SpectrumUnit.V);
            MultiSiteSpectrum qABS   = capWave.getImaginary().setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).spectrum(SpectrumUnit.V);
            MultiSiteDouble idBm_S = iABS.getValue(stdBinIQ).pow(2);
            MultiSiteDouble idBm_L = iABS.getValue(stdBinIQ-1).pow(2);
            MultiSiteDouble idBm_H = iABS.getValue(stdBinIQ+1).pow(2);
            idBm = idBm_S.add(idBm_L).add(idBm_H).log10().multiply(10).add(10);
            MultiSiteDouble qdBm_S = qABS.getValue(stdBinIQ).pow(2);
            MultiSiteDouble qdBm_L = qABS.getValue(stdBinIQ-1).pow(2);
            MultiSiteDouble qdBm_H = qABS.getValue(stdBinIQ+1).pow(2);
            qdBm = qdBm_S.add(qdBm_L).add(qdBm_H).log10().multiply(10).add(10);
        }else
        {
            idBm.set(999);
            qdBm.set(999);
        }
        if(getPowerFlag ==0)    //Gain and not only Power
        {
            cpxDBm = cpxDBm.subtract(inputPower);
            idBm   = idBm.subtract(inputPower);
            qdBm   = qdBm.subtract(inputPower);
        }
        iqdBm[0]= cpxDBm;
        iqdBm[1]= idBm;
        iqdBm[2]= qdBm;
        if(debugLevel >=2)
        {
            System.out.println("idqBm_cpx is "+cpxDBm);
            System.out.println("idqBm_i is "+idBm);
            System.out.println("idqBm_q is "+qdBm);
        }
        return(iqdBm);
    }


    /**
     * this method perfoms remode DC function for a MultiSiteWaveComplex waveform
     *
     * @param inData   MultiSiteWaveComplex waveform
     *
     * @return MultiSiteWaveComplex waveform with DC removed
     */
    public static MultiSiteWaveComplex removeingDC_RF ( MultiSiteWaveComplex inData)
    {
        MultiSiteWaveDouble temp_data_I = new MultiSiteWaveDouble();
        MultiSiteWaveDouble temp_data_Q = new MultiSiteWaveDouble();
        temp_data_I = inData.getReal().add(inData.getReal().mean().multiply(-1));
        temp_data_Q = inData.getImaginary().add(inData.getImaginary().mean().multiply(-1));
        inData.setArray(new MultiSiteWaveComplex(temp_data_I, temp_data_Q).getArray());
        return inData;
    }

    /**
     * this method perfoms remode DC function for a MultiSiteWaveComplex waveform
     *
     * @param inData   MultiSiteWaveComplex waveform
     *
     * @return MultiSiteWaveComplex waveform with DC removed
     */
    public static MultiSiteWaveComplex removeDC ( MultiSiteWaveComplex inData)
    {
        MultiSiteDouble real_mean = new MultiSiteDouble(0.0);
        MultiSiteDouble image_mean = new MultiSiteDouble(0.0);
        real_mean = inData.getReal().mean().multiply(-1.0);
        image_mean = inData.getImaginary().mean().multiply(-1.0);
        MultiSiteComplex temp_cplx= new MultiSiteComplex();
        for(int site : inData.getActiveSites())
        {
            temp_cplx.set(site, new Complex(real_mean.get(site), image_mean.get(site)));
        }
        inData=inData.add(temp_cplx);
        return inData;
    }


    /**
     *
     * this method performs NF calculation for RX with one captured waveform
     *
     * @param testSuiteName         testsuitename string
     * @param debugLogLevel         level control for output
     * @param Prj_path              project path
     * @param offline_flag          Flag to indicate offline or online
     * @param capWaveI              input waveform for I path
     * @param capWaveQ              input waveform for Q path
     * @param freqOut               Target frequency for signal
     * @param mNoiseFreqL           start frequency to be taken account
     * @param mNoiseFreqH           stop frequency to be taken account
     * @param inputPower            stimulus power for NF test
     *
     *
     * @return  Noise Figure fr I and Q path
     *
     * @since 1.0.0
     */
    public static MultiSiteDouble[] RX_IQ_NFDsp(String testSuiteName, int debugLogLevel, String Prj_path, MultiSiteBoolean offline_flag, MultiSiteWaveDoubleArray capWaveI, MultiSiteWaveDoubleArray capWaveQ, double freqOut,double mNoiseFreqL,double mNoiseFreqH,double inputPower)
    {
        MultiSiteDouble[] iqNF = new MultiSiteDouble[2];
        MultiSiteDouble idBm, qdBm, iGain, qGain, iNF, qNF;
        int stdBin;
        int noiseBinL, noiseBinH;
        long sampleSize = 0;
        double sampleFreq;
        MultiSiteWaveComplex  capWave  = new MultiSiteWaveComplex();
        MultiSiteWaveComplex  capWave2  = new MultiSiteWaveComplex();
        if(offline_flag.equalTo(true))
        {
            String deviceDir_I  = Prj_path + "/waste/smt7_online_waveform/RX_NF_B26_MRX_LB1.0.RX_NF.RX_I1.5.1.raw";
            String deviceDir_Q  = Prj_path + "/waste/smt7_online_waveform/RX_NF_B26_MRX_LB1.0.RX_NF.RX_Q1.5.1.raw";
            if(debugLogLevel>=2) {
                System.out.println("device path is:  " + Prj_path);
                System.out.println("wave path is:  " + deviceDir_I);
            }
            MultiSiteWaveDouble raw_waveforms_I = new MultiSiteWaveDouble();
            MultiSiteWaveDouble raw_waveforms_Q = new MultiSiteWaveDouble();
            try
            {
                raw_waveforms_I.setArray(AnalogMath.readAsciiFileDoubleFromWasteDir(deviceDir_I));
                raw_waveforms_Q.setArray(AnalogMath.readAsciiFileDoubleFromWasteDir(deviceDir_Q));
                if(debugLogLevel>=2) {
                    raw_waveforms_I.plot("rawWave_I", "RX_NF");
                    raw_waveforms_Q.plot("rawWave_Q", "RX_NF");
                }
            }
            catch (IOException e)
            {
                System.out.println("can't get file waveform");
            }
            capWave = new MultiSiteWaveComplex(raw_waveforms_I,raw_waveforms_Q);
            capWave = capWave.setSampleRate(5.979562e6);
            sampleFreq = 5.979562e6;
            sampleSize = capWave.getSize(1);
        }
        else
        {
            sampleSize = capWaveI.getElement(0).getSize().get();
            sampleFreq = capWaveI.getElement(0).getSampleRate().get();
            capWave = new MultiSiteWaveComplex(capWaveI.getElement(0),capWaveQ.getElement(0));
            if(debugLogLevel>=2) {
                capWave.plot("first_wave", testSuiteName);
            }
        }
        stdBin      = (int) (sampleSize*(freqOut/sampleFreq) + 0.5);
        noiseBinL   = (int) (sampleSize*((mNoiseFreqL * 1e6)/sampleFreq) + 0.5);
        noiseBinH   = (int) (sampleSize*((mNoiseFreqH * 1e6)/sampleFreq) + 0.5);
        if(debugLogLevel>=1)
        {
            System.out.println("mNoiseFreqL is: \t" + mNoiseFreqL);
            System.out.println("mNoiseFreqH is: \t" + mNoiseFreqH);
            System.out.println("sampleFreq is: \t" + sampleFreq);
            System.out.println("sampleSize is: \t" + sampleSize);
            System.out.println("stdBin is: \t" + stdBin);
            System.out.println("noiseBinL is: \t" + noiseBinL);
            System.out.println("noiseBinH is: \t" + noiseBinH);
        }
        MultiSiteSpectrum iABS = capWave.getReal().setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).fft().abs();
        MultiSiteSpectrum qABS = capWave.getImaginary().setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).fft().abs();
        MultiSiteDouble iDBm_S = iABS.getValue(stdBin).pow(2);
        MultiSiteDouble iDBm_L = iABS.getValue(stdBin -1 ).pow(2);
        MultiSiteDouble iDBm_H = iABS.getValue(stdBin + 1).pow(2);
        idBm = (iDBm_S.add(iDBm_L).add(iDBm_H)).log10().multiply(10).add(10);
        MultiSiteDouble qDBm_S = qABS.getValue(stdBin).pow(2);
        MultiSiteDouble qDBm_L = qABS.getValue(stdBin -1 ).pow(2);
        MultiSiteDouble qDBm_H = qABS.getValue(stdBin + 1).pow(2);
        qdBm = (qDBm_S.add(qDBm_L).add(qDBm_H)).log10().multiply(10).add(10);
        iGain = idBm.subtract(inputPower);
        qGain = qdBm.subtract(inputPower);
        if(debugLogLevel>=1)
        {
            System.out.println("idBm of first capture is: \t" + idBm);
            System.out.println("qdBm of first capture is: \t" + qdBm);
            System.out.println("iGain of first capture is: \t" + iGain);
            System.out.println("qGain of first capture is: \t" + qGain);
        }
        if(offline_flag.equalTo(true))
        {
            String deviceDir_I  = Prj_path + "/waste/smt7_online_waveform/RX_NF_B26_MRX_LB1.0.RX_NF.RX_I2.5.1.raw";
            String deviceDir_Q  = Prj_path + "/waste/smt7_online_waveform/RX_NF_B26_MRX_LB1.0.RX_NF.RX_Q2.5.1.raw";
            if(debugLogLevel>=2) {
                System.out.println("device path is:  " + Prj_path);
                System.out.println("wave path is:  " + deviceDir_I);
            }
            MultiSiteWaveDouble raw_waveforms_I = new MultiSiteWaveDouble();
            MultiSiteWaveDouble raw_waveforms_Q = new MultiSiteWaveDouble();
            try
            {
                raw_waveforms_I.setArray(AnalogMath.readAsciiFileDoubleFromWasteDir(deviceDir_I));
                raw_waveforms_Q.setArray(AnalogMath.readAsciiFileDoubleFromWasteDir(deviceDir_Q));
                if(debugLogLevel>=2) {
                    raw_waveforms_I.plot("rawWave_I", "RX_NF");
                    raw_waveforms_Q.plot("rawWave_Q", "RX_NF");
                }
            }
            catch (IOException e)
            {
                System.out.println("can't get file waveform");
            }
            capWave2 = new MultiSiteWaveComplex(raw_waveforms_I,raw_waveforms_Q);
            capWave2 = capWave2.setSampleRate(5.979562e6);
            sampleFreq = 5.979562e6;
        }
        else
        {
            capWave2 = new MultiSiteWaveComplex(capWaveI.getElement(1),capWaveQ.getElement(1));
            if(debugLogLevel>=2)
            {
                capWave2.plot("second_wave", testSuiteName);
            }
        }
        MultiSiteSpectrum iABS2 = capWave2.getReal().setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).fft().abs();
        MultiSiteSpectrum qABS2 = capWave2.getImaginary().setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).fft().abs();
        idBm = iABS2.extractValues(noiseBinL, noiseBinH-noiseBinL+1).pow(2).sum();
        qdBm = qABS2.extractValues(noiseBinL, noiseBinH-noiseBinL+1).pow(2).sum();
        idBm =idBm.divide((mNoiseFreqH - mNoiseFreqL) * 1e6).log10().multiply(10).add(10);
        qdBm =qdBm.divide((mNoiseFreqH - mNoiseFreqL) * 1e6).log10().multiply(10).add(10);
        if(debugLogLevel>=1)
        {
            System.out.println("idBm of second capture is: \t" + idBm);  //3.5
            System.out.println("qdBm of second capture is: \t" + qdBm);  //3.47
        }
        iNF =idBm.subtract(iGain).add(174);
        qNF =qdBm.subtract(qGain).add(174);
        iqNF[0]=iNF;
        iqNF[1]=qNF;
        if(debugLogLevel>=1)
        {
            System.out.println("iNF is: \t" + iNF);  //3.5
            System.out.println("qNF is: \t" + qNF);  //3.47
        }
        return (iqNF);
    }

    /**
     *
     * this method performs NF calculation for RX with 2 captured wavefroms
     *
     * @param testSuiteName         testsuitename string
     * @param debugLogLevel         level control for output
     * @param Prj_path              project path
     * @param offline_flag          Flag to indicate offline or online
     * @param capWaveI              input waveform for I path
     * @param capWaveQ              input waveform for Q path
     * @param capWaveI2             input 2nd waveform for I path
     * @param capWaveQ2             input 2nd waveform for Q path
     * @param freqOut               Target frequency for signal
     * @param mNoiseFreqL           start frequency to be taken account
     * @param mNoiseFreqH           stop frequency to be taken account
     * @param inputPower            stimulus power for NF test
     *
     *
     * @return  Noise Figure fr I and Q path
     *
     * @since 1.0.0
     */
    public static MultiSiteDouble[] RX_IQ_NF2Dsp(String testSuiteName, int debugLogLevel, String Prj_path, MultiSiteBoolean offline_flag, MultiSiteWaveDoubleArray capWaveI, MultiSiteWaveDoubleArray capWaveQ, MultiSiteWaveDoubleArray capWaveI2, MultiSiteWaveDoubleArray capWaveQ2,double freqOut,double mNoiseFreqL,double mNoiseFreqH,double inputPower)
    {
        MultiSiteDouble[] iqNF = new MultiSiteDouble[2];
        MultiSiteDouble idBm, qdBm, iGain, qGain, iNF, qNF;
        int stdBin;
        int noiseBinL, noiseBinH;
        long sampleSize = 0;
        double sampleFreq;
        MultiSiteWaveComplex  capWave  = new MultiSiteWaveComplex();
        MultiSiteWaveComplex  capWave2  = new MultiSiteWaveComplex();
        if(offline_flag.equalTo(true))
        {
            String deviceDir_I  = Prj_path + "/waste/smt7_online_waveform/RX_NF_B26_MRX_LB1.0.RX_NF.RX_I1.5.1.raw";
            String deviceDir_Q  = Prj_path + "/waste/smt7_online_waveform/RX_NF_B26_MRX_LB1.0.RX_NF.RX_Q1.5.1.raw";
            if(debugLogLevel>=2) {
                System.out.println("device path is:  " + Prj_path);
                System.out.println("wave path is:  " + deviceDir_I);
            }
            MultiSiteWaveDouble raw_waveforms_I = new MultiSiteWaveDouble();
            MultiSiteWaveDouble raw_waveforms_Q = new MultiSiteWaveDouble();
            try
            {
                raw_waveforms_I.setArray(AnalogMath.readAsciiFileDoubleFromWasteDir(deviceDir_I));
                raw_waveforms_Q.setArray(AnalogMath.readAsciiFileDoubleFromWasteDir(deviceDir_Q));
                if(debugLogLevel>=2) {
                    raw_waveforms_I.plot("rawWave_I", "RX_NF");
                    raw_waveforms_Q.plot("rawWave_Q", "RX_NF");
                }
            }
            catch (IOException e)
            {
                System.out.println("can't get file waveform");
            }
            capWave = new MultiSiteWaveComplex(raw_waveforms_I,raw_waveforms_Q);
            capWave = capWave.setSampleRate(5.979562e6);
            sampleFreq = 5.979562e6;
            sampleSize = capWave.getSize(1);
        }
        else
        {
            capWave = new MultiSiteWaveComplex(capWaveI.getElement(0),capWaveQ.getElement(0));
            if(debugLogLevel>=2) {
                capWave.plot("first_wave", testSuiteName);
            }
        }
        sampleSize = capWaveI.getElement(0).getSize().get();
        sampleFreq = capWaveI.getElement(0).getSampleRate().get();
        stdBin      = (int) (sampleSize*(freqOut/sampleFreq) + 0.5);
        noiseBinL   = (int) (sampleSize*((mNoiseFreqL * 1e6)/sampleFreq) + 0.5);
        noiseBinH   = (int) (sampleSize*((mNoiseFreqH * 1e6)/sampleFreq) + 0.5);
        if(debugLogLevel>=1)
        {
            System.out.println("mNoiseFreqL is: \t" + mNoiseFreqL);
            System.out.println("mNoiseFreqH is: \t" + mNoiseFreqH);
            System.out.println("sampleFreq is: \t" + sampleFreq);
            System.out.println("sampleSize is: \t" + sampleSize);
            System.out.println("stdBin is: \t" + stdBin);
            System.out.println("noiseBinL is: \t" + noiseBinL);
            System.out.println("noiseBinH is: \t" + noiseBinH);
        }
        MultiSiteSpectrum iABS   = capWave.getReal().spectrum(SpectrumUnit.V);
        MultiSiteSpectrum qABS   = capWave.getImaginary().spectrum(SpectrumUnit.V);
        MultiSiteDouble iDBm_S = iABS.getValue(stdBin).pow(2);
        MultiSiteDouble iDBm_L = iABS.getValue(stdBin -1 ).pow(2);
        MultiSiteDouble iDBm_H = iABS.getValue(stdBin + 1).pow(2);
        idBm = (iDBm_S.add(iDBm_L).add(iDBm_H)).log10().multiply(10).add(10);
        MultiSiteDouble qDBm_S = qABS.getValue(stdBin).pow(2);
        MultiSiteDouble qDBm_L = qABS.getValue(stdBin -1 ).pow(2);
        MultiSiteDouble qDBm_H = qABS.getValue(stdBin + 1).pow(2);
        qdBm = (qDBm_S.add(qDBm_L).add(qDBm_H)).log10().multiply(10).add(10);
        iGain = idBm.subtract(inputPower);
        qGain = qdBm.subtract(inputPower);
        if(debugLogLevel>=1)
        {
            System.out.println("idBm of first capture is: \t" + idBm);
            System.out.println("qdBm of first capture is: \t" + qdBm);
            System.out.println("iGain of first capture is: \t" + iGain);
            System.out.println("qGain of first capture is: \t" + qGain);
        }
        if(offline_flag.equalTo(true))
        {
            String deviceDir_I  = Prj_path + "/waste/smt7_online_waveform/RX_NF_B26_MRX_LB1.0.RX_NF.RX_I2.5.1.raw";
            String deviceDir_Q  = Prj_path + "/waste/smt7_online_waveform/RX_NF_B26_MRX_LB1.0.RX_NF.RX_Q2.5.1.raw";
            if(debugLogLevel>=2) {
                System.out.println("device path is:  " + Prj_path);
                System.out.println("wave path is:  " + deviceDir_I);
            }
            MultiSiteWaveDouble raw_waveforms_I = new MultiSiteWaveDouble();
            MultiSiteWaveDouble raw_waveforms_Q = new MultiSiteWaveDouble();
            try
            {
                raw_waveforms_I.setArray(AnalogMath.readAsciiFileDoubleFromWasteDir(deviceDir_I));
                raw_waveforms_Q.setArray(AnalogMath.readAsciiFileDoubleFromWasteDir(deviceDir_Q));
                if(debugLogLevel>=2) {
                    raw_waveforms_I.plot("rawWave_I", "RX_NF");
                    raw_waveforms_Q.plot("rawWave_Q", "RX_NF");
                }
            }
            catch (IOException e)
            {
                System.out.println("can't get file waveform");
            }
            capWave2 = new MultiSiteWaveComplex(raw_waveforms_I,raw_waveforms_Q);
            capWave2 = capWave2.setSampleRate(5.979562e6);
            sampleFreq = 5.979562e6;
        }
        else
        {
            capWave2 = new MultiSiteWaveComplex(capWaveI2.getElement(0),capWaveQ2.getElement(0));
            if(debugLogLevel>=2)
            {
                capWave2.plot("second_wave", testSuiteName);
            }
        }
        sampleSize = capWaveI2.getElement(0).getSize().get();
        sampleFreq = capWaveI2.getElement(0).getSampleRate().get();
        stdBin      = (int) (sampleSize*(freqOut/sampleFreq) + 0.5);
        noiseBinL   = (int) (sampleSize*((mNoiseFreqL * 1e6)/sampleFreq) + 0.5);
        noiseBinH   = (int) (sampleSize*((mNoiseFreqH * 1e6)/sampleFreq) + 0.5);
        MultiSiteSpectrum iABS2   = capWave2.getReal().spectrum(SpectrumUnit.V);
        MultiSiteSpectrum qABS2   = capWave2.getImaginary().spectrum(SpectrumUnit.V);
        idBm = iABS2.extractValues(noiseBinL, noiseBinH-noiseBinL+1).pow(2).sum();
        qdBm = qABS2.extractValues(noiseBinL, noiseBinH-noiseBinL+1).pow(2).sum();
        idBm =idBm.divide((mNoiseFreqH - mNoiseFreqL) * 1e6).log10().multiply(10).add(10);
        qdBm =qdBm.divide((mNoiseFreqH - mNoiseFreqL) * 1e6).log10().multiply(10).add(10);
        if(debugLogLevel>=1)
        {
            System.out.println("idBm of second capture is: \t" + idBm);  //3.5
            System.out.println("qdBm of second capture is: \t" + qdBm);  //3.47
        }
        iNF =idBm.subtract(iGain).add(174);
        qNF =qdBm.subtract(qGain).add(174);
        iqNF[0]=iNF;
        iqNF[1]=qNF;
        if(debugLogLevel>=1)
        {
            System.out.println("iNF is: \t" + iNF);  //3.5
            System.out.println("qNF is: \t" + qNF);  //3.47
        }
        return (iqNF);
    }


    public static MultiSiteDouble[] RX_IQ_Gain_FiltDsp(String testSuiteName, int debugLogLevel, MultiSiteWaveDouble capWaveI, MultiSiteWaveDouble capWaveQ, double sampleFreq, double freqOut, double inputPw, int getPowerFlag)
    {
        MultiSiteDouble[] iqPower = new MultiSiteDouble[2];
        MultiSiteDouble idBm = new MultiSiteDouble();
        MultiSiteDouble qdBm = new MultiSiteDouble();
        int sampleSize = capWaveI.getSize().get().intValue();
        int stdBin      = (int) (sampleSize*(freqOut/sampleFreq) + 0.5);
        MultiSiteSpectrum iABS = capWaveI.setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).spectrum(SpectrumUnit.V);
        MultiSiteSpectrum qABS = capWaveQ.setWindowFunction(WindowFunction.HANNING).setWindowScaling(WindowScaling.SCALE_FOR_AMPLITUDE).spectrum(SpectrumUnit.V);
        if(debugLogLevel>=1)
        {
            System.out.println("sr is: " + sampleFreq);
            System.out.println("stdBin is: " + stdBin + " frq_out " + freqOut);
        }
        if(debugLogLevel>=2)
        {
            MultiSiteSpectrum spec_Idbm = capWaveI.spectrum(SpectrumUnit.dBm);
            MultiSiteSpectrum spec_Qdbm = capWaveQ.spectrum(SpectrumUnit.dBm);
            spec_Idbm.plot("spec_I of " + freqOut, testSuiteName);
            spec_Qdbm.plot("spec_Q of " + freqOut, testSuiteName );
        }
        if(stdBin < sampleSize/2)
        {
            MultiSiteDouble iDBm_S = iABS.getValue(stdBin).pow(2);
            MultiSiteDouble iDBm_L = iABS.getValue(stdBin -1 ).pow(2);
            MultiSiteDouble iDBm_H = iABS.getValue(stdBin + 1).pow(2);
            idBm = (iDBm_S.add(iDBm_L).add(iDBm_H)).log10().multiply(10).add(10);
            MultiSiteDouble qDBm_S = qABS.getValue(stdBin).pow(2);
            MultiSiteDouble qDBm_L = qABS.getValue(stdBin -1 ).pow(2);
            MultiSiteDouble qDBm_H = qABS.getValue(stdBin + 1).pow(2);
            qdBm = (qDBm_S.add(qDBm_L).add(qDBm_H)).log10().multiply(10).add(10);
        }
        else
        {
            idBm.set(999);
            qdBm.set(999);
        }
        if(debugLogLevel>=1)
        {
            System.out.println("idBm is: \t" + idBm);
            System.out.println("qdBm is: \t" + qdBm);
        }
        if(getPowerFlag==0){
            idBm = idBm.subtract(inputPw);
            qdBm = qdBm.subtract(inputPw);
        }
        if(debugLogLevel>=1)
        {
            System.out.println("iGain is: \t" + idBm);
            System.out.println("qGain is: \t" + qdBm);
        }
        iqPower[0]=idBm;
        iqPower[1]=qdBm;
        return (iqPower);
    }


    public static MultiSiteLong GetCapInt( Map<String, MultiSiteBitSequence> bitsOfAllSignals,
            int[] activeSites,
            /** Serial Bus PinName (1 pin only) */
            String mPinName,
            /** Sample length in Bits */
            int mSampleLengthInBits,
            int debugMode)
    {
        /** Initial Skip Bits */
        int mInitSkipBits=0;
        /** Sample Count */
        int mSampleCount=1;
        /** Skip Bits */
        int mSkipBits=0;
        /** Inter-Sample Skip Bits */
        int mInterSampleSkipBits=0;
        /** Functional test descriptor for datalog */
        MultiSiteLong Capt_int= new MultiSiteLong();
        int numOfPoints = mSampleCount * mSampleLengthInBits;
        for (int iSite : activeSites) {
            BitSequence tmpBitStream = new BitSequence(numOfPoints);
            if (debugMode>1) {
                System.out.print("Site " + iSite);
                System.out.print(" Bit Array::  \n");
                System.out.println("===================");
                System.out.println("Number of extracted bits = " + numOfPoints);
                System.out.println("Bit Index : Raw Data Index : Capture Data (Bool)");
            }
            for (int sampleIndex = 0; sampleIndex < mSampleCount; sampleIndex++) {
                for (int bitPoint = 0; bitPoint < mSampleLengthInBits; bitPoint++) {
                    int bitIndex = mInitSkipBits + (sampleIndex * (mSampleLengthInBits * (mSkipBits + 1) + mInterSampleSkipBits)) + (bitPoint * (mSkipBits + 1));
                    boolean tmpDataBool = bitsOfAllSignals.get(mPinName).get(iSite).get(bitIndex);
                    int bitLocation = bitPoint + (sampleIndex * mSampleLengthInBits);
                    if(tmpDataBool)
                    {
                        tmpBitStream.set(bitLocation);
                    }
                    if (debugMode>1) {
                        System.out.println(String.format("%06d",
                                (bitLocation))
                                + " : "
                                + String.format("%06d", bitIndex) + " : " + (tmpDataBool ? 1 : 0));
                    }
                } //end of 3rd-for() loop -> bitPoint
                double tmp_int=0;
                for (int bitPoint = 0; bitPoint < mSampleLengthInBits; bitPoint++) {
                    int bitIndex = mInitSkipBits + (sampleIndex * (mSampleLengthInBits * (mSkipBits + 1) + mInterSampleSkipBits)) + (bitPoint * (mSkipBits + 1));
                    boolean tmpDataBool = bitsOfAllSignals.get(mPinName).get(iSite).get(bitIndex);
                    if(tmpDataBool)
                    {
                        tmp_int= tmp_int + Math.pow(2,15-bitPoint);
                    }
                } //end of 3rd-for() loop -> bitPoint
                if (debugMode>1) {
                    System.out.println("Site "+iSite+"  Val="+ tmp_int);
                }
                Capt_int.set(iSite, (long) tmp_int);
            } //end of 2nd-for() loop -> sample
            if (debugMode>1) {
                System.out.print("Bitset Data -> ");
                System.out.println(tmpBitStream);
                System.out.println("====================\n\n");
            }
        } //end of 1st-for() loop -> Site
        return Capt_int;
    }
    public static MultiSiteLongArray GetCapInt( Map<String, MultiSiteBitSequence> bitsOfAllSignals,
            int[] activeSites,
            /** Serial Bus PinName (1 pin only) */
            String mPinName,
            /** Sample length in Bits */
            int mSampleLengthInBits,
            int mSampleCount,
            int debugMode)
    {
        MultiSiteLongArray tmpResult = new MultiSiteLongArray();
        /** Initial Skip Bits */
        int mInitSkipBits=0;
        /** Sample Count */
        /** Skip Bits */
        int mSkipBits=0;
        /** Inter-Sample Skip Bits */
        int mInterSampleSkipBits=0;
        /** Functional test descriptor for datalog */
        long[] Capt_int = new long[mSampleCount];
        int numOfPoints = mSampleCount * mSampleLengthInBits;
        for (int iSite : activeSites) {
            BitSequence tmpBitStream = new BitSequence(numOfPoints);
            if (debugMode>=2) {
                System.out.print("Site " + iSite);
                System.out.print(" Bit Array::  \n");
                System.out.println("===================");
                System.out.println("Number of extracted bits = " + numOfPoints);
                System.out.println("Bit Index : Raw Data Index : Capture Data (Bool)");
            }
            for (int sampleIndex = 0; sampleIndex < mSampleCount; sampleIndex++) {
                for (int bitPoint = 0; bitPoint < mSampleLengthInBits; bitPoint++) {
                    int bitIndex = mInitSkipBits + (sampleIndex * (mSampleLengthInBits * (mSkipBits + 1) + mInterSampleSkipBits)) + (bitPoint * (mSkipBits + 1));
                    boolean tmpDataBool = bitsOfAllSignals.get(mPinName).get(iSite).get(bitIndex);
                    int bitLocation = bitPoint + (sampleIndex * mSampleLengthInBits);
                    if(tmpDataBool)
                    {
                        tmpBitStream.set(bitLocation);
                    }
                    if (debugMode>=2) {
                        System.out.println(String.format("%06d",
                                (bitLocation))
                                + " : "
                                + String.format("%06d", bitIndex) + " : " + (tmpDataBool ? 1 : 0));
                    }
                } //end of 3rd-for() loop -> bitPoint
                double tmp_int=0;
                for (int bitPoint = 0; bitPoint < mSampleLengthInBits; bitPoint++) {
                    int bitIndex = mInitSkipBits + (sampleIndex * (mSampleLengthInBits * (mSkipBits + 1) + mInterSampleSkipBits)) + (bitPoint * (mSkipBits + 1));
                    boolean tmpDataBool = bitsOfAllSignals.get(mPinName).get(iSite).get(bitIndex);
                    if(tmpDataBool)
                    {
                        tmp_int= tmp_int + Math.pow(2,mSampleLengthInBits-bitPoint-1);
                    }
                } //end of 3rd-for() loop -> bitPoint
                if (debugMode>=2) {
                    System.out.println("Site "+iSite+"  Val="+ tmp_int);
                }
                Capt_int[sampleIndex]=(int)tmp_int;
            } //end of 2nd-for() loop -> sample
            if (debugMode>=2) {
                System.out.print("Bitset Data -> ");
                System.out.println(tmpBitStream);
                System.out.println("====================\n\n");
            }
            tmpResult.set(iSite, Capt_int);
        } //end of 1st-for() loop -> Site
        return tmpResult;
    }
    public static MultiSiteLongArray GetCapbit( Map<String, MultiSiteBitSequence> bitsOfAllSignals,
            int[] activeSites,
            /** Serial Bus PinName (1 pin only) */
            String mPinName,
            /** Sample length in Bits */
            int mSampleCount,
            int debugMode)
    {
        int mSampleLengthInBits=1;
        MultiSiteLongArray tmpResult = new MultiSiteLongArray();
        /** Initial Skip Bits */
        int mInitSkipBits=0;
        /** Sample Count */
        /** Skip Bits */
        int mSkipBits=0;
        /** Inter-Sample Skip Bits */
        int mInterSampleSkipBits=0;
        /** Functional test descriptor for datalog */
        long[] Capt_int = new long[mSampleCount];
        int numOfPoints = mSampleCount * mSampleLengthInBits;
        for (int iSite : activeSites) {
            BitSequence tmpBitStream = new BitSequence(numOfPoints);
            if (debugMode>=2) {
                System.out.print("Site " + iSite);
                System.out.print(" Bit Array::  \n");
                System.out.println("===================");
                System.out.println("Number of extracted bits = " + numOfPoints);
                System.out.println("Bit Index : Raw Data Index : Capture Data (Bool)");
            }
            for (int sampleIndex = 0; sampleIndex < mSampleCount; sampleIndex++) {
                for (int bitPoint = 0; bitPoint < mSampleLengthInBits; bitPoint++) {
                    int bitIndex = mInitSkipBits + (sampleIndex * (mSampleLengthInBits * (mSkipBits + 1) + mInterSampleSkipBits)) + (bitPoint * (mSkipBits + 1));
                    boolean tmpDataBool = bitsOfAllSignals.get(mPinName).get(iSite).get(bitIndex);
                    int bitLocation = bitPoint + (sampleIndex * mSampleLengthInBits);
                    if(tmpDataBool)
                    {
                        tmpBitStream.set(bitLocation);
                    }
                    if (debugMode>=2) {
                        System.out.println(String.format("%06d",
                                (bitLocation))
                                + " : "
                                + String.format("%06d", bitIndex) + " : " + (tmpDataBool ? 1 : 0));
                    }
                } //end of 3rd-for() loop -> bitPoint
                double tmp_int=0;
                for (int bitPoint = 0; bitPoint < mSampleLengthInBits; bitPoint++) {
                    int bitIndex = mInitSkipBits + (sampleIndex * (mSampleLengthInBits * (mSkipBits + 1) + mInterSampleSkipBits)) + (bitPoint * (mSkipBits + 1));
                    boolean tmpDataBool = bitsOfAllSignals.get(mPinName).get(iSite).get(bitIndex);
                    tmp_int=0;
                    if(tmpDataBool)
                    {
                        tmp_int= 1;// tmp_int + Math.pow(2,15-bitPoint);
                    }
                } //end of 3rd-for() loop -> bitPoint
                if (debugMode>=2) {
                    System.out.println("Site "+iSite+"  Val="+ tmp_int);
                }
                Capt_int[sampleIndex]=(int)tmp_int;
            } //end of 2nd-for() loop -> sample
            if (debugMode>=2) {
                System.out.print("Bitset Data -> ");
                System.out.println(tmpBitStream);
                System.out.println("====================\n\n");
            }
            tmpResult.set(iSite, Capt_int);
        } //end of 1st-for() loop -> Site
        return tmpResult;
    }
    public static void  ShiftWaveform(String path){
        WaveDouble wave_i = new WaveDouble();
        wave_i =  WaveDouble.importFromFile(path+"/setups/waveforms/i.cwf", WaveformFileType.CUSTOM_WAVEFORM);
        WaveDouble wave_q = new WaveDouble();
        wave_q = WaveDouble.importFromFile(path+"/setups/waveforms/q.cwf", WaveformFileType.CUSTOM_WAVEFORM);
        wave_i.plot("i_plot");
        wave_q.plot("q_plot");
        double samplingRate =  2.4576e8;//160e6;
        double FreqShift = 10e6;//samplingRate / 2;
        double deltat;
        WaveComplex shiftedWave = new WaveComplex(wave_i.getSize());
        deltat = 1 / samplingRate;

        for (int i = 0; i < wave_i.getSize(); i++){
            float time = (float) (deltat * i);
            float iNum1  = (float) (wave_i.getValue(i)*Math.cos(2*Math.PI* FreqShift * time)- wave_q.getValue(i)*Math.sin(2*Math.PI* FreqShift * time));
            float qNum1 = (float) (wave_i.getValue(i)*Math.sin(2*Math.PI* FreqShift * time)+wave_q.getValue(i)*Math.cos(2*Math.PI* FreqShift * time));
            shiftedWave.setValue(i, new Complex(iNum1, qNum1));
        }
        shiftedWave.plot("shifted_plot");
        shiftedWave.exportToFile(path+"/setups/waveforms/WCDMA_TM4_shifted.wfm", WaveformFileType.SIGNAL_STUDIO);
    }
}
