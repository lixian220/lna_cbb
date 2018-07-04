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
 *      The name of the test suite is: Main.RF_FT.MUX_IN1_MHB3_OUT_B23_HG1_OPA
 * 
 * This file is not intended to be modified. Do not make changes
 * to this file as your changes may be overwritten when the
 * generating code is executed again!
 *
 * ---------------------------------------------------------------------------*/
import setups.specs.RF.normal;
import setups.specs.RF.normal;

spec IDD_Gain_HG1_1
{
    transactSeq _call_TS_Setup_IDD_trSeq_1;
    action MUX_IN1_MHB3_OUT_B23_HG1_OPA_Stim_IDD_Off;
    action MUX_IN1_MHB3_OUT_B23_HG1_OPA_Meas_IDD;
    transactSeq _call_TS_Setup_Gain_HG1_trSeq_2;
    action MUX_IN1_MHB3_OUT_B23_HG1_OPA_Stim_Gain_HG1;
    action MUX_IN1_MHB3_OUT_B23_HG1_OPA_Meas_Gain_HG1;
    
    setup rfStim aliasRF_Stim3
    {
        config.port = aliasRF_Stim3;
        config.options = multisiteSplitterOff, siteInterlacingOn;
        config.mode = singleSource;
    
        action cw MUX_IN1_MHB3_OUT_B23_HG1_OPA_Stim_IDD_Off
        {
            frequency = 5.991 GHz;
            power = -110 dBm;
        }
    
        action cw MUX_IN1_MHB3_OUT_B23_HG1_OPA_Stim_Gain_HG1
        {
            frequency = 2.19 GHz;
            power = -30 dBm;
        }
    }
    
    setup rfMeas aliasRF_Meas1
    {
        config.mode = highResolution;
        config.options = siteInterlacingOn;
    
        action cwPower MUX_IN1_MHB3_OUT_B23_HG1_OPA_Meas_Gain_HG1
        {
            frequency = 2.19 GHz;
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
        transactSeq dsa_gen.Main.RF_FT.MUX_IN1_MHB3_OUT_B23_HG1_OPA.IDD_Gain_HG1_1.TS_Setup_IDD _call_TS_Setup_IDD_trSeq_1
        {
        }
    }
    
    setup dcVI VDD1P8
    {
        action imeas MUX_IN1_MHB3_OUT_B23_HG1_OPA_Meas_IDD
        {
            waitTime = 5.000 ms;
            irange = 30.00 mA;
            averages = 4;
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_HG1
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_HG1
    {
        transactSeq dsa_gen.Main.RF_FT.MUX_IN1_MHB3_OUT_B23_HG1_OPA.IDD_Gain_HG1_2.TS_Setup_Gain_HG1 _call_TS_Setup_Gain_HG1_trSeq_2
        {
        }
    }
}