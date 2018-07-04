
spec sPara {

    action twoPort;
    var Frequency freq =  0.60 GHz;
    var PowerLevel pow =  -10 dBm;

    setup rfVna RF_IN1
    {
        config.port2 =  RF_OUT;

        action sParameter twoPort
        {
            frequency = freq;
            stimPower = pow;
            expectedMaxPower = pow;
            bandwidthOfInterest = 10 kHz;
            resultAveraging = 1;
            receiverPath =  direct;
            receiverAttenuation =  0 dB;
        }
    }

}
