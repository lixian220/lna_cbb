


//**************for X8 loadbaord **************//
spec groups {

  group OS_VSS = SDATA+SCLK+USID0+ATEST;
  group OS_VDD = OS_VSS;
  group RF_SWITCH = TC_SW1_1+TC_SW1_2+TC_SW1_3+TC_SW2_1+TC_SW2_2+TC_SW3_1+TC_SW3_2+TC_SW4_1+TC_SW4_2;
  group MIPIGRP =  SDATA+SCLK+USID0;
  group ALL_DIG_PINS = SDATA+SCLK+USID0+RF_SWITCH;
  group ALL_DPS_PINS = VDD1P8+VDDIO;


  signal aliasRF_Stim1 =  RF_IN1;
  signal aliasRF_Stim2 =  RF_IN2;
  signal aliasRF_Stim3 =  RF_IN3;
  signal aliasRF_Meas1 =  RF_OUT;

  signal aliasDPS1 =  VDDIO;
  signal alisdDPS2 =  VDD1P8;



}
