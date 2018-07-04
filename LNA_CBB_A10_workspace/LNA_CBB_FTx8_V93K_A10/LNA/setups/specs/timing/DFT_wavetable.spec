import setups.Common.groups;
spec DFT_wavetable {



    setup digInOut RF_SWITCH {
        wavetable wvt1 { // WaveTable
            xModes = 1 {
                0: d1:0;
                1: d1:1;
                L: d1:Z r1:L;
                H: d1:Z r1:H;
                X: d1:Z r1:X;
                C: d1:Z r1:C;
                Z: d1:Z;
                N: d1:N;
            }
        }
    }

  setup digInOut SCLK {
    wavetable timeplate_2 {
      xModes = 1;
      0: d1:FN0 d2:F0N;
      1: d1:FN0 d2:F1N;
      N: d1:FN0;
    }
  }
  setup digInOut SDATA {
//    result.capture.enabled = true;
    result.cyclePassFail.enabled = true;
    wavetable timeplate_2 {
      xModes = 1;
      0: d1:FN0 d2:F0N;
      1: d1:FN0 d2:F1N;
      L: d1:Z r2:L;
      H: d1:Z r2:H;
      N: d1:FN0;
      T: d1:Z r1:WM r2:WC;
      X: d1:Z;
    }
  }
  setup digInOut USID0 {
    wavetable timeplate_2 {
      xModes = 1;
      Q: d1:N;
      0: d1:FN0;
    }
  }
   setup digInOut ATEST {
    wavetable timeplate_2 {
      xModes = 1;
      Q: d1:N;
      0: d1:FN0;
    }
  }

}
