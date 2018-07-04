import setups.Common.groups;
spec Wvt_LNA_F2 {
  setup digInOut SCLK {
    wavetable LNA {
      xModes = 1;
      0: d3:0;
      1: d3:1;
      N: d1:FN0 d2:F0N;
    }
  }
  setup digInOut SDATA {
    wavetable LNA {
      xModes = 1;
      0: d3:0;
      1: d2:1;
      X: d1:Z r1:X;
      D: d2:0;
    }
  }
  setup digInOut USID0 {
    wavetable LNA {
      xModes = 1;
      0: d1:0;
    }
  }
}
