
spec specPA_MIPI {

    transactSeq TSname_patsetup1;

    // Change value of this parameter to show masking
    signal data  = SDATA;
    signal clk   = SCLK ;

    protocolInterface setups.mipi.mipi MIPI {
        // assign signal to signalRoles of the protocol (aka protocol assignment)
        // xMode = 1;
        DATA   = SDATA;
        CLK    = SCLK;
    }


}


