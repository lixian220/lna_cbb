import setups.Common.groups;
spec Wvt_LNA_F4 {
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
      0: d6:0;
      1: d5:1;
      L: d3:Z r3:L;
      H: d4:Z r4:H;
      l: d2:Z r2:L;
      X: d1:Z r1:X;
      D: d5:0;
    }
  }
  setup digInOut USID0 {
    wavetable LNA {
      xModes = 1;
      1: d1:1;

    }
  }
}
