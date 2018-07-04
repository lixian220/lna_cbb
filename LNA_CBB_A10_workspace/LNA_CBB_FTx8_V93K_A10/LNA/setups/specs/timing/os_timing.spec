import setups.Common.groups;
spec os_timing {

  set timingSet;
  var Time period;
  period = 1000 ns;


  setup digInOut  ALL_DIG_PINS+ATEST {
    set timing timingSet {
      period = period;
      d1 = 0 * period;
      r1 = 0.5 * period;
    }
    result.cyclePassFail.enabled = true;

    wavetable MIPI {
      xModes = 1;
      0: d1:0 r1:X;
      1: d1:1 r1:X;
      Z: d1:Z r1:X;
      M: d1:Z r1:M;

    }
  }
}
