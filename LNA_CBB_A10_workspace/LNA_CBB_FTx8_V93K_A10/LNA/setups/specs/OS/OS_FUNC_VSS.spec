import setups.Common.groups;
import setups.specs.timing.os_timing;
// Uncomment the following line to swith on cycelPassFail result configuration;
//import levels.lev_eqn1_spec1_set1;
//import timing.EQNSET1_TRXSSI.tim_eqn1_spec1_set1;
//import wavetables.wvt_trxssi;

spec OS_FUNC_VSS {

  set levelSet_1;
  setup dcVI VDD1P8{
    connect = true;
    level.vrange = 3.0 V;
    level.vforce = 0V;
    level.iclamp = 500 mA;
    disconnectMode = loz;
  }
  setup dcVI VDDIO{
    connect = true;
    level.vrange = 3.0 V;
    level.vforce = 0V;
    level.iclamp = 500 mA;
    disconnectMode = loz;
  }
  setup digInOut ALL_DIG_PINS +ATEST {
    connect = true;
    set level levelSet_1 {
      vil = 0.0 V;
      vih = 1.8 V;
      vol = -0.8 V;
      voh = -0.2 V;
      iol = 100 uA;
      ioh = 100 uA;
      vt = -1 V;
      term = load;
  }
  }


}
