import setups.Common.groups;
spec lev_eqn1_set1 {

  set levelSet_1;


  var Double d_VDD1P8;
  var Double d_VDDIO;
  var Double Vcoef1;
  var Double Vcoef2;
  var Double Vcoef;


  setup digInOut OS_VSS {
    connect = true;
    set level levelSet_1 {

      vil = (0.1*1.8 V);
      vih = (0.9*1.8 V) ; //0.92*1.8 V;
      vol = (0.5*Vcoef*1.8 V)/Vcoef2;
      voh = (0.5*Vcoef*1.8 V)*Vcoef2;


//      vil = 0.0 V;
//      vih = 1.8 V;
//      vol = 0.9 V;
//      voh = 0.9 V;

//      iol = 0.1 mA;
//      ioh = 0.1 mA;
//      vt = 1.8 V;
//      term = load;
    }
  }
  setup digInOut RF_SWITCH {
    connect = true;
//    disconnect =  true;
    set level levelSet_1 {
      vil = 0.0 V;
      vih = 1.8 V;
      vol = 0.9 V;
      voh = 0.9 V;
//      iol = 0.1 mA;
//      ioh = 0.1 mA;
//      vt = 0.9 V;
//      term = load;
    }
  }
  setup digInOut ATEST{
//      disconnect = true; // Disconnect after test?
       connect = true;  // Connect before test?
       disconnectMode = hiz; // in HIZ case, some site can be High, some site can be Low voltage

    set level levelSet_1 {

      vil = (0.1*1.8 V);
      vih = (0.9*1.8 V) ; //0.92*1.8 V;
      vol = (0.3*1.8 V)/Vcoef2;
      voh = (0.7*1.8 V)*Vcoef2;


    }
  }

  setup dcVI VDD1P8 {
    connect = true;

    level.vrange = 3.0 V;
    level.vforce = (d_VDD1P8 * Vcoef1*Vcoef) * 1 V;

    level.iclamp = 30 mA;
    level.irange = 30 mA;

    disconnectMode = loz;

  }
    setup dcVI VDDIO {
    connect = true;

    level.vrange = 3.0 V;
    level.vforce = (d_VDDIO * Vcoef2*Vcoef) * 1 V;

    level.iclamp = 30 mA;
    level.irange = 30 mA;

    disconnectMode = loz;

  }
}
