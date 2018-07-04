import setups.Common.groups;

spec Disconnect {

    action ALL_DIG;

    setup dcVI ALL_DPS_PINS
    {
        disconnect = true;
        level.vforce = 0 V;
    }

    setup dcVI ALL_DIG_PINS
    {
        disconnect = true;
        action iforceVmeas ALL_DIG
        {
            forceValue = 0.0001000 uA;
            irange = 1.0000 uA;
            waitTime = 0.100 ms;
            vexpected = 0 V;
        }
    }
}


