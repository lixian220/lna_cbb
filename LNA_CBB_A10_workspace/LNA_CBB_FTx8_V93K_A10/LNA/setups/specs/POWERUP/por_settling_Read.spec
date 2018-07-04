
// Uncomment the following line to swith on cycelPassFail result configuration;
// import configuration.pin.instrument_hi6362v100_8site_46_pin;
import setups.specs.levels.lev_por_settling_Read;
import setups.specs.timing.mipi_Read_timing;
import setups.mipi.specPA_MIPI;
spec por_settling_Read {
    setup digInOut SDATA{
        result.capture.enabled = true;
    }

}
