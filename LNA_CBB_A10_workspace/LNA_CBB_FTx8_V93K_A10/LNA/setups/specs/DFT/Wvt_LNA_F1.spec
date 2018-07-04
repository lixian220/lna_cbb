import setups.Common.groups;
spec Wvt_LNA_F1 {
  setup digInOut SCLK {
    wavetable LNA {
      xModes = 1;
      0: d3:0;
      1: d3:1;
      N: d1:FN0 d2:F0N;
    }
  }

     setup digInOut ATEST {
    wavetable timeplate_2 {
      xModes = 1;
      Q: d1:N;
      0: d1:FN0;
    }
  }
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








  setup digInOut SDATA {
    wavetable LNA {
      xModes = 1;
      0: d6:0;
      1: d5:1;
      L: d7:Z r4:L;
      H: d3:Z r3:H;
      l: d2:Z r2:L;
      X: d1:Z r1:X;
      D: d5:0;
      2: d2:Z d4:0 r2:L;
    }
  }
  setup digInOut USID0 {
    wavetable LNA {
      xModes = 1;
      0: d1:0;
    }
  }
}
