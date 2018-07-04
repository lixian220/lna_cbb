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
 *      src/rf2rf_tml/RF2RFBurst_TEST.java:332
 * 
 *   2. The testflow file which contains the used test suite and parameter
 *      settings:
 *      LNA/testflows/RF_FT.flow
 * 
 *      The name of the test suite is: Main.RF_FT.MB2_IN3_MHB2_OUT_B39_HG1_OPA
 * 
 * This file is not intended to be modified. Do not make changes
 * to this file as your changes may be overwritten when the
 * generating code is executed again!
 *
 * ---------------------------------------------------------------------------*/
import setups.specs.RF.normal;

spec LNA_OFF_2
{
    setup rfStim aliasRF_Stim3
    {
        config.port = aliasRF_Stim3;
        config.options = multisiteSplitterOff, siteInterlacingOn;
        config.mode = singleSource;
    }
    
    setup rfMeas aliasRF_Meas1
    {
        config.mode = highResolution;
        config.options = siteInterlacingOn;
    }
}
