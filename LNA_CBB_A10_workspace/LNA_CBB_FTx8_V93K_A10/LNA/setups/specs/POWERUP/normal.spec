
// Uncomment the following line to swith on cycelPassFail result configuration;
// import configuration.pin.instrument_hi6362v100_8site_46_pin;
import setups.specs.levels.lev_normal;
import setups.specs.timing.mipi_timing;
import setups.mipi.specPA_MIPI;
spec normal {
    setup digInOut SDATA{
        result.capture.enabled = true;
    }
}
