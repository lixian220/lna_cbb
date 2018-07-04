/* ---------------------------------------------------------------------------*
 * 
 * This file was automatically generated by the SmarTest Device Setup API.
 * 
 * The contents of this file are generated by a combination of the 
 * test method code as well as the testflow file being used. In the 
 * SmarTest Work Center, you can navigate to the generating source 
 * by holding down the CTRL+ALT+R keys when your cursor is positioned 
 * over either of these two file paths listed below:
 * 
 *   1. The test method location which resulted in the creation of this file:
 *      src/rf2rf_tml/RF2RFBurst_TEST.java:193
 * 
 *   2. The testflow file which contains the used test suite and parameter
 *      settings:
 *      LNA/testflows/RF_FT.flow
 * 
 *      The name of the test suite is: Main.RF_FT.MB2_IN3_MHB2_OUT_B39_LP_OPA
 * 
 * This file is not intended to be modified. Do not make changes
 * to this file as your changes may be overwritten when the
 * generating code is executed again!
 *
 * ---------------------------------------------------------------------------*/
import setups.specs.RF.normal;
import setups.specs.RF.normal;
import setups.specs.RF.normal;

spec IDD_Gain_MLP_IIP3_1
{
    transactSeq _call_TS_Setup_IDD_trSeq_1;
    action MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IDD_Off;
    action MB2_IN3_MHB2_OUT_B39_LP_OPA_Meas_IDD;
    action MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_Gain_MLP;
    action MB2_IN3_MHB2_OUT_B39_LP_OPA_Meas_Gain_MLP;
    action MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IIP3;
    action MB2_IN3_MHB2_OUT_B39_LP_OPA_Meas_IIP3_I2;
    transactSeq _call_TS_ReSet_IIP3_trSeq_2;
    action MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IIP3_Off;
    
    setup rfStim aliasRF_Stim3
    {
        config.port = aliasRF_Stim3;
        config.options = multisiteSplitterOff, siteInterlacingOn;
        config.mode = singleSource;
    
        action cw MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IDD_Off
        {
            frequency = 5.991 GHz;
            power = -110 dBm;
        }
    
        action cw MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_Gain_MLP
        {
            frequency = 1.9 GHz;
            power = -50 dBm;
        }
    
        action modulated MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IIP3
        {
            frequency = 1.902 GHz;
            power = -30 dBm;
            waveformI = MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IIP3;
            waveformQ = MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IIP3;
            optimization = imd;
            repeatInfinite = true;
            basebandAttenuation = 3 dB;
        }
    
        action cw MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IIP3_Off
        {
            frequency = 5.991 GHz;
            power = -110 dBm;
        }
    }
    
    setup rfMeas aliasRF_Meas1
    {
        config.mode = highResolution;
        config.options = siteInterlacingOn;
    
        action cwPower MB2_IN3_MHB2_OUT_B39_LP_OPA_Meas_Gain_MLP
        {
            frequency = 1.9 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    
        action cwPower MB2_IN3_MHB2_OUT_B39_LP_OPA_Meas_IIP3_I2
        {
            frequency = 1.917 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -15 dBm;
            resultAveraging = 1;
            bandwidthOfInterest = 200 kHz;
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_IDD
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_IDD
    {
        transactSeq dsa_gen.Main.RF_FT.MB2_IN3_MHB2_OUT_B39_LP_OPA.IDD_Gain_MLP_IIP3_1.TS_Setup_IDD _call_TS_Setup_IDD_trSeq_1
        {
        }
    }
    
    setup dcVI VDD1P8
    {
        action imeas MB2_IN3_MHB2_OUT_B39_LP_OPA_Meas_IDD
        {
            waitTime = 5.000 ms;
            irange = 30.00 mA;
            averages = 4;
        }
    }
    
    waveform sine MB2_IN3_MHB2_OUT_B39_LP_OPA_Stim_IIP3
    {
        amplitude = 1;
        frequency = 5 MHz;
        sampleRate = 204.8 MHz;
        samples = 20480;
    }
    
    protocolInterface setups.mipi.mipi TS_ReSet_IIP3
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_ReSet_IIP3
    {
        transactSeq dsa_gen.Main.RF_FT.MB2_IN1_MHB2_OUT_B2_LP_OPA.IDD_Gain_MLP_IIP3_2.TS_ReSet_IIP3 _call_TS_ReSet_IIP3_trSeq_2
        {
        }
    }
}
