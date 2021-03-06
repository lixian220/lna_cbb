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
 *      xoc/dsa/DeviceSetupUtils.java:120
 * 
 *   2. The testflow file which contains the used test suite and parameter
 *      settings:
 *      LNA/testflows/OS.flow
 * 
 *      The name of the test suite is: Main.OS.OS_VSS_Open_PPMU
 * 
 * This file is not intended to be modified. Do not make changes
 * to this file as your changes may be overwritten when the
 * generating code is executed again!
 *
 * ---------------------------------------------------------------------------*/
import setups.Common.groups;

spec Spec_1
{
    action OS_VSS_serial_SDATA;
    action OS_VSS_serial_reset_SDATA;
    action OS_VSS_serial_SCLK;
    action OS_VSS_serial_reset_SCLK;
    action OS_VSS_serial_USID0;
    action OS_VSS_serial_reset_USID0;
    action OS_VSS_serial_ATEST;
    action OS_VSS_serial_reset_ATEST;
    
    setup dcVI ALL_DPS_PINS
    {
        connect = true;
        disconnect = true;
        level.vforce = 0 V;
        level.ungangMode = never;
    }
    
    setup dcVI OS_VSS
    {
        connect = true;
        disconnect = true;
    }
    
    setup dcVI SDATA
    {
        action iforceVmeas OS_VSS_serial_SDATA
        {
            forceValue = -10.00000 uA;
            irange = 10.00000 uA;
            waitTime = 5.000 ms;
            vexpected = 0 V;
            highAccuracy = false;
        }
    
        action vforce OS_VSS_serial_reset_SDATA
        {
            forceValue = 0 V;
        }
    }
    
    setup dcVI SCLK
    {
        action iforceVmeas OS_VSS_serial_SCLK
        {
            forceValue = -10.00000 uA;
            irange = 10.00000 uA;
            waitTime = 5.000 ms;
            vexpected = 0 V;
            highAccuracy = false;
        }
    
        action vforce OS_VSS_serial_reset_SCLK
        {
            forceValue = 0 V;
        }
    }
    
    setup dcVI USID0
    {
        action iforceVmeas OS_VSS_serial_USID0
        {
            forceValue = -10.00000 uA;
            irange = 10.00000 uA;
            waitTime = 5.000 ms;
            vexpected = 0 V;
            highAccuracy = false;
        }
    
        action vforce OS_VSS_serial_reset_USID0
        {
            forceValue = 0 V;
        }
    }
    
    setup dcVI ATEST
    {
        action iforceVmeas OS_VSS_serial_ATEST
        {
            forceValue = -10.00000 uA;
            irange = 10.00000 uA;
            waitTime = 5.000 ms;
            vexpected = 0 V;
            highAccuracy = false;
        }
    
        action vforce OS_VSS_serial_reset_ATEST
        {
            forceValue = 0 V;
        }
    }
}
