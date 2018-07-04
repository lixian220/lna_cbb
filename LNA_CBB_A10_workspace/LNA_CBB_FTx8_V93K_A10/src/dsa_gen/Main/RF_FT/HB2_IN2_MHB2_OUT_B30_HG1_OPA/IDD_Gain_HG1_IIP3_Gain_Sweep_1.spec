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
 *      The name of the test suite is: Main.RF_FT.HB2_IN2_MHB2_OUT_B30_HG1_OPA
 * 
 * This file is not intended to be modified. Do not make changes
 * to this file as your changes may be overwritten when the
 * generating code is executed again!
 *
 * ---------------------------------------------------------------------------*/
import setups.specs.RF.normal;
import setups.specs.RF.normal;
import setups.specs.RF.normal;
import setups.specs.RF.normal;
import setups.specs.RF.normal;
import setups.specs.RF.normal;
import setups.specs.RF.normal;
import setups.specs.RF.normal;

spec IDD_Gain_HG1_IIP3_Gain_Sweep_1
{
    transactSeq _call_TS_Setup_IDD_trSeq_1;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IDD_Off;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_IDD;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_HG1;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_HG1;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IIP3;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_IIP3_I2;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IIP3_Off;
    transactSeq _call_TS_Setup_Gain_Sweep1_trSeq_2;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep1;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep1;
    transactSeq _call_TS_Setup_Gain_Sweep2_trSeq_3;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep2;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep2;
    transactSeq _call_TS_Setup_Gain_Sweep3_trSeq_4;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep3;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep3;
    transactSeq _call_TS_Setup_Gain_Sweep4_trSeq_5;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep4;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep4;
    transactSeq _call_TS_Setup_Gain_Sweep5_trSeq_6;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep5;
    action HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep5;
    transactSeq _call_TS_ReSet_Gain_Sweep5_trSeq_7;
    
    setup rfStim aliasRF_Stim3
    {
        config.port = aliasRF_Stim3;
        config.options = multisiteSplitterOff, siteInterlacingOn;
        config.mode = singleSource;
    
        action cw HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IDD_Off
        {
            frequency = 5.991 GHz;
            power = -110 dBm;
        }
    
        action cw HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_HG1
        {
            frequency = 2.355 GHz;
            power = -50 dBm;
        }
    
        action modulated HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IIP3
        {
            frequency = 2.32125 GHz;
            power = -30 dBm;
            waveformI = HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IIP3;
            waveformQ = HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IIP3;
            optimization = imd;
            repeatInfinite = true;
            basebandAttenuation = 3 dB;
        }
    
        action cw HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IIP3_Off
        {
            frequency = 5.991 GHz;
            power = -110 dBm;
        }
    
        action cw HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep1
        {
            frequency = 2.355 GHz;
            power = -50 dBm;
        }
    
        action cw HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep2
        {
            frequency = 2.355 GHz;
            power = -50 dBm;
        }
    
        action cw HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep3
        {
            frequency = 2.355 GHz;
            power = -50 dBm;
        }
    
        action cw HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep4
        {
            frequency = 2.355 GHz;
            power = -50 dBm;
        }
    
        action cw HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_Gain_Sweep5
        {
            frequency = 2.355 GHz;
            power = -50 dBm;
        }
    }
    
    setup rfMeas aliasRF_Meas1
    {
        config.mode = highResolution;
        config.options = siteInterlacingOn;
    
        action cwPower HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_HG1
        {
            frequency = 2.355 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    
        action cwPower HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_IIP3_I2
        {
            frequency = 2.355 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -15 dBm;
            resultAveraging = 1;
            bandwidthOfInterest = 200 kHz;
        }
    
        action cwPower HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep1
        {
            frequency = 2.355 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    
        action cwPower HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep2
        {
            frequency = 2.355 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    
        action cwPower HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep3
        {
            frequency = 2.355 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    
        action cwPower HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep4
        {
            frequency = 2.355 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    
        action cwPower HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_Gain_Sweep5
        {
            frequency = 2.355 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_IDD
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_IDD
    {
        transactSeq dsa_gen.Main.RF_FT.HB2_IN2_MHB2_OUT_B30_HG1_OPA.IDD_Gain_HG1_IIP3_Gain_Sweep_1.TS_Setup_IDD _call_TS_Setup_IDD_trSeq_1
        {
        }
    }
    
    setup dcVI VDD1P8
    {
        action imeas HB2_IN2_MHB2_OUT_B30_HG1_OPA_Meas_IDD
        {
            waitTime = 5.000 ms;
            irange = 30.00 mA;
            averages = 4;
        }
    }
    
    waveform sine HB2_IN2_MHB2_OUT_B30_HG1_OPA_Stim_IIP3
    {
        amplitude = 1;
        frequency = 11.25 MHz;
        sampleRate = 204.8 MHz;
        samples = 20480;
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_Sweep1
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_Sweep1
    {
        transactSeq dsa_gen.Main.RF_FT.HB2_IN2_MHB2_OUT_B30_HG1_OPA.IDD_Gain_HG1_IIP3_Gain_Sweep_2.TS_Setup_Gain_Sweep1 _call_TS_Setup_Gain_Sweep1_trSeq_2
        {
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_Sweep2
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_Sweep2
    {
        transactSeq dsa_gen.Main.RF_FT.HB2_IN2_MHB2_OUT_B30_HG1_OPA.IDD_Gain_HG1_IIP3_Gain_Sweep_3.TS_Setup_Gain_Sweep2 _call_TS_Setup_Gain_Sweep2_trSeq_3
        {
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_Sweep3
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_Sweep3
    {
        transactSeq dsa_gen.Main.RF_FT.HB2_IN2_MHB2_OUT_B30_HG1_OPA.IDD_Gain_HG1_IIP3_Gain_Sweep_4.TS_Setup_Gain_Sweep3 _call_TS_Setup_Gain_Sweep3_trSeq_4
        {
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_Sweep4
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_Sweep4
    {
        transactSeq dsa_gen.Main.RF_FT.HB2_IN2_MHB2_OUT_B30_HG1_OPA.IDD_Gain_HG1_IIP3_Gain_Sweep_5.TS_Setup_Gain_Sweep4 _call_TS_Setup_Gain_Sweep4_trSeq_5
        {
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_Sweep5
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_Sweep5
    {
        transactSeq dsa_gen.Main.RF_FT.HB2_IN2_MHB2_OUT_B30_HG1_OPA.IDD_Gain_HG1_IIP3_Gain_Sweep_6.TS_Setup_Gain_Sweep5 _call_TS_Setup_Gain_Sweep5_trSeq_6
        {
        }
    }
    
    protocolInterface setups.mipi.mipi TS_ReSet_Gain_Sweep5
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_ReSet_Gain_Sweep5
    {
        transactSeq dsa_gen.Main.RF_FT.LB1_IN4_LB_OUT_B13_HG1_OPA.IDD_Gain_HG1_IIP3_Gain_Sweep_7.TS_ReSet_Gain_Sweep5 _call_TS_ReSet_Gain_Sweep5_trSeq_7
        {
        }
    }
}