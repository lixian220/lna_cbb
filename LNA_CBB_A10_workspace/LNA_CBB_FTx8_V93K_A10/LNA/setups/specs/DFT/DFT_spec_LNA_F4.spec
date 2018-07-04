import setups.Common.groups;
// -------- For configuration --------;
// Uncomment the following line to swith on cycelPassFail result configuration;
// import configuration.tc_usid_prg_test_001_224119995_pins_46_cha.Instrument_tc_usid_prg_test_001_224119995_pins_46_cha;
// -------- For Level --------;

import setups.specs.levels.lev_normal;
// -------- For Timing --------;
import setups.specs.DFT.Tim_eqn_spec_DFT4;
import setups.specs.DFT.Tim_eqn_DFT4;
// -------- For Wavetable --------;
import setups.specs.DFT.Wvt_LNA_F4;
spec DFT_spec_LNA_F4 {

  setup digInOut SDATA{
//        result.capture.enabled = true;
result.cyclePassFail.enabled =  true;
    }
}
