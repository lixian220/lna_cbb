import setups.Common.groups;
spec mipi_timing {

  set timingSet;
  var Time per_50;

  per_50 = 26 ns;//
//    per_50 =  6.3 ns;

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
        set timing timingSet { // Timing Set
            period = per_50;
            d1 = 0 ms;
            r1 = per_50/2;
        }

    }

  setup digInOut SDATA+USID0+ATEST{
    set timing timingSet {
      period = per_50;
      d1 = 0.25* per_50;
      r1 = 0.25 * per_50; //Not used
    }
  }
  setup digInOut SCLK{
    set timing timingSet {
      period = per_50;
      d1 = 0.0 * per_50;
      d2 = 0.5 * per_50;
      r1 = 1.4 * per_50;
    }
  }
  setup digInOut SDATA+USID0+ATEST{
    wavetable DCAP {
      xModes = 1;
      0: d1:0;
      1: d1:1;
      Z: d1:Z;
      H: d1:Z r1:H;
      L: d1:Z r1:L;
      X: d1:Z r1:X;
      C: d1:Z r1:C;
      N: d1:N;
    }
  }
  setup digInOut SCLK{
    wavetable DCAP {
      xModes = 1;
      0: d1:0;
      1: d1:1;
      P: d1:1 d2:0;
      Z: d1:Z;
      H: d1:Z r1:H;
      L: d1:Z r1:L;
      X: d1:Z r1:X;
      C: d1:Z r1:C;
      N: d1:N;
    }
  }
}
