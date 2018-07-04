
import setups.specs.DFT.Tim_variables_eqn1;
import setups.Common.groups;

spec Tim_eqn_DFT2 {


  set timingSet_1_EQNSET1;
  setup digInOut SCLK {
    set timing timingSet_1_EQNSET1 {
      period = per_9_616_eqn1;
      // Please set "minAllowedPeriod" and "maxAllowedPeriod" manually.
      // minAllowedPeriod = xxx;
      // maxAllowedPeriod = xxx;
      d1 = 0.0 * per_9_616_eqn1;
      d2 = 1.0 / 9.616 * per_9_616_eqn1;
      d3 = 5.77 / 9.616 * per_9_616_eqn1;
    }
  }
  setup digInOut SDATA {
    set timing timingSet_1_EQNSET1 {
      period = per_9_616_eqn1;
      // Please set "minAllowedPeriod" and "maxAllowedPeriod" manually.
      // minAllowedPeriod = xxx;
      // maxAllowedPeriod = xxx;
      d1 = 0.0 * per_9_616_eqn1;
      d2 = 0.278 / (T_per) * per_9_616_eqn1;
      d3 = 0.984 / (T_per) * per_9_616_eqn1;
      d4 = 4.607 / (T_per) * per_9_616_eqn1;
      d5 = 5.771 / (T_per) * per_9_616_eqn1;
      d6 = 7.546 / (T_per) * per_9_616_eqn1;
      d7 = 9.398 / (T_per) * per_9_616_eqn1;
      r1 = 0.0 * per_9_616_eqn1+ roff-1.145*per_9_616_eqn1;  // 0.82
      r2 = 0.278 / (T_per) * per_9_616_eqn1+ roff-1.145*per_9_616_eqn1;
      r3 = 0.984 / (T_per) * per_9_616_eqn1+ roff-1.145*per_9_616_eqn1;
      r4 = 9.398 / (T_per) * per_9_616_eqn1+ roff-1.145*per_9_616_eqn1;
    }
  }
  setup digInOut USID0 +ATEST{
    set timing timingSet_1_EQNSET1 {
      period = per_9_616_eqn1;
      // Please set "minAllowedPeriod" and "maxAllowedPeriod" manually.
      // minAllowedPeriod = xxx;
      // maxAllowedPeriod = xxx;
      d1 = 0.0 * per_9_616_eqn1;
    }
  }


   setup digInOut RF_SWITCH{
            set timing timingSet_1_EQNSET1 { // Timing Set
            period = per_9_616_eqn1;
            d1 = 0 ms;
            r1 = per_9_616_eqn1/2;//need to check!!!!!!!!!!!!!!
        }

   }








}
