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
 *      The name of the test suite is: Main.RF_FT.HB1_IN2_MHB3_OUT_B40_HG1_OPA
 * 
 * This file is not intended to be modified. Do not make changes
 * to this file as your changes may be overwritten when the
 * generating code is executed again!
 *
 * ---------------------------------------------------------------------------*/
import setups.specs.RF.normal;

spec Gain_MUX_1
{
    transactSeq _call_TS_Setup_Gain_MUX_trSeq_1;
    action HB1_IN2_MHB3_OUT_B40_HG1_OPA_Stim_Gain_MUX;
    action HB1_IN2_MHB3_OUT_B40_HG1_OPA_Meas_Gain_MUX;
    
    setup rfStim aliasRF_Stim2
    {
        config.port = aliasRF_Stim2;
        config.options = multisiteSplitterOff, siteInterlacingOn;
        config.mode = singleSource;
    
        action cw HB1_IN2_MHB3_OUT_B40_HG1_OPA_Stim_Gain_MUX
        {
            frequency = 2.35 GHz;
            power = -50 dBm;
        }
    }
    
    setup rfMeas aliasRF_Meas1
    {
        config.mode = highResolution;
        config.options = siteInterlacingOn;
    
        action cwPower HB1_IN2_MHB3_OUT_B40_HG1_OPA_Meas_Gain_MUX
        {
            frequency = 2.35 GHz;
            ifFrequency = 27.12311 MHz;
            expectedMaxPower = -20 dBm;
            bandwidthOfInterest = 50 kHz;
            resultAveraging = 1;
        }
    }
    
    protocolInterface setups.mipi.mipi TS_Setup_Gain_MUX
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup_Gain_MUX
    {
        transactSeq dsa_gen.Main.RF_FT.HB1_IN2_MHB3_OUT_B40_HG1_OPA.Gain_MUX_1.TS_Setup_Gain_MUX _call_TS_Setup_Gain_MUX_trSeq_1
        {
        }
    }
}
