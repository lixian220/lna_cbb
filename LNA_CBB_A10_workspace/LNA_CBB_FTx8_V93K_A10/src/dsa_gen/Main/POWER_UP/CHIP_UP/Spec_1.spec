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
 *      src/mipi_tml/MIPIWrite_TEST.java:69
 * 
 *   2. The testflow file which contains the used test suite and parameter
 *      settings:
 *      LNA/testflows/POWER_UP.flow
 * 
 *      The name of the test suite is: Main.POWER_UP.CHIP_UP
 * 
 * This file is not intended to be modified. Do not make changes
 * to this file as your changes may be overwritten when the
 * generating code is executed again!
 *
 * ---------------------------------------------------------------------------*/
import setups.specs.POWERUP.normal;
import setups.specs.POWERUP.normal;
import setups.specs.POWERUP.normal;
import setups.specs.POWERUP.normal;

spec Spec_1
{
    transactSeq _call_TS_Setup1_trSeq_1;
    
    protocolInterface setups.mipi.mipi TS_Setup1
    {
        DATA = SDATA;
        CLK = SCLK;
    }
    
    setup protocolInterface TS_Setup1
    {
        transactSeq dsa_gen.Main.POWER_UP.CHIP_UP.TrSeq_1.TS_Setup1 _call_TS_Setup1_trSeq_1
        {
        }
    }
}
