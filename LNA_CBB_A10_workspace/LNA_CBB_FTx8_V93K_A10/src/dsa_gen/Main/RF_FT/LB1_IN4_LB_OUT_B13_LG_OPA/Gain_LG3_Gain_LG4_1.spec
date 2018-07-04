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
 *      The name of the test suite is: Main.RF_FT.LB1_IN4_LB_OUT_B13_LG_OPA
 * 
 * This file is not intended to be modified. Do not make changes
 * to this file as your changes may be overwritten when the
 * generating code is executed again!
 *
 * ---------------------------------------------------------------------------*/
import setups.specs.RF.normal;
import setups.specs.RF.normal;

spec Gain_LG3_Gain_LG4_1
{
    transactSeq _call_TS_Setup_Gain_LG3_trSeq_1;
    action LB1_IN4_LB_OUT_B13_LG_OPA_Stim_Gain_LG3;
    action LB1_IN4_LB_OUT_B13_LG_OPA_Meas_Gain_LG3;
    transactSeq _call_TS_Setup_Gain_LG4_trSeq_2;
    action LB1_IN4_LB_OUT_B13_LG_OPA_Stim_Gain_LG4;
    action LB1_IN4_LB_OUT_B13_LG_OPA_Meas_Gain_LG4;
    
    setup rfStim aliasRF_Stim1
    {
        config.port = aliasRF_Stim1;
        config.options = multisiteSplitterOff, siteInterlacingOn;
        config.mode = singleSource;
    
        action cw LB1_IN4_LB_OUT_B13_LG_OPA_Stim_Gain_LG3
        {
            frequency = 751 MHz;
            power = -30 dBm;
        }
    
        action cw LB1_IN4_LB_OUT_B13_LG_OPA_Stim_Gain_LG4
        {
            frequency = 751 MHz;
            power = -30 dBm;
        }
    }
    
    setup rfMeas aliasRF_Meas1
    {
        config.mode = highResolution;
        config.options = siteInterlacingOn;
    
        action cwPower LB1_IN4_LB_OUT_B13_LG_OPA_Meas_Gain_LG3
        {
            frequency = 751 MHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    
        action cwPower LB1_IN4_LB_OUT_B13_LG_OPA_Meas_Gain_LG4
        {
            frequency = 751 MHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_LG3
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_LG3
    {
        transactSeq dsa_gen.Main.RF_FT.LB1_IN4_LB_OUT_B13_LG_OPA.Gain_LG3_Gain_LG4_1.TS_Setup_Gain_LG3 _call_TS_Setup_Gain_LG3_trSeq_1
        {
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_LG4
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_LG4
    {
        transactSeq dsa_gen.Main.RF_FT.LB1_IN4_LB_OUT_B13_LG_OPA.Gain_LG3_Gain_LG4_2.TS_Setup_Gain_LG4 _call_TS_Setup_Gain_LG4_trSeq_2
        {
        }
    }
}