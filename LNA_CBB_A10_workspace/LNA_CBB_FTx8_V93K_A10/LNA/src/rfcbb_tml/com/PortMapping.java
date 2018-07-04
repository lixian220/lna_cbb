package src.rfcbb_tml.com;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * This class provide the interface to get the RF ports and control pattern for test path.
 *
 * Firstly the PortMap is initialized by user with collect information for each test path, then user can get the rf ports and pattern in testmethod.
 *
 *
 * @version 1.0.0
 * @author 308770
 *
 */

public class PortMapping {
    private String rfIn;
    private String rfOut;
    private String rfInATE;
    private String rfOutATE;
    private String switchPattern;
    private String switchCtrlPin;
    private String selCode;
    private String ampCtrlPin;
    private static ArrayList<PortMapping> portMap = new ArrayList<>();
    private static ArrayList<String> CSPins = new ArrayList<String>(){{
        add("TC_SW5_9");
        add("TC_SW5_10");
        add("TC_SW5_11");
        add("TC_SW10_8");
        add("TC_SW10_7");
        add("TC_SW11_8");
        add("TC_SW11_7");
        add("TC_SW12_8");
        add("TC_SW12_7");
    }};
    private PortMapping(String _rfIn, String _rfOut, String _rfInATE, String _rfOutATE, String _switchPattern, String _switchCtrlPin, String _selCode)
    {
        rfIn        = _rfIn  ;
        rfOut       = _rfOut  ;
        rfInATE     = _rfInATE ;
        rfOutATE    = _rfOutATE ;
        switchPattern       = _switchPattern ;
        switchCtrlPin   = _switchCtrlPin ;
        selCode     = _selCode ;
    }
    static
    {
        portMap.add(new PortMapping("LB1_IN1", "LB_OUT","aliasRF_Stim1", "aliasRF_Meas1", "LB1IN1_LBOUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("LB1_IN2", "LB_OUT","aliasRF_Stim1", "aliasRF_Meas1", "LB1IN2_LBOUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("LB1_IN3", "LB_OUT","aliasRF_Stim1", "aliasRF_Meas1", "LB1IN3_LBOUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("LB1_IN4", "LB_OUT","aliasRF_Stim1", "aliasRF_Meas1", "LB1IN4_LBOUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("LB1_IN5", "LB_OUT","aliasRF_Stim1", "aliasRF_Meas1", "LB1IN5_LBOUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB1_IN1", "MHB1_OUT","aliasRF_Stim1", "aliasRF_Meas1", "MB1IN1_MHB1OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB1_IN2", "MHB1_OUT","aliasRF_Stim1", "aliasRF_Meas1", "MB1IN2_MHB1OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB1_IN3", "MHB1_OUT","aliasRF_Stim1", "aliasRF_Meas1", "MB1IN3_MHB1OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB2_IN1", "MHB2_OUT","aliasRF_Stim2", "aliasRF_Meas1", "MB2IN1_MHB2OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB2_IN2", "MHB2_OUT","aliasRF_Stim2", "aliasRF_Meas1", "MB2IN2_MHB2OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("HB1_IN1", "MHB1_OUT","aliasRF_Stim2", "aliasRF_Meas1", "HB1IN1_MHB1OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("HB1_IN2", "MHB1_OUT","aliasRF_Stim2", "aliasRF_Meas1", "HB1IN2_MHB1OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("HB2_IN1", "MHB2_OUT","aliasRF_Stim3", "aliasRF_Meas1", "HB2IN1_MHB2OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("HB2_IN2", "MHB2_OUT","aliasRF_Stim3", "aliasRF_Meas1", "HB2IN2_MHB2OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB2_IN3", "MHB2_OUT","aliasRF_Stim3", "aliasRF_Meas1", "MB2IN3_MHB2OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MUX_IN1", "MHB3_OUT","aliasRF_Stim3", "aliasRF_Meas1", "MUXIN1_MHB3OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB1_IN2", "MHB3_OUT","aliasRF_Stim1", "aliasRF_Meas1", "MB1IN2_MHB3OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB2_IN1", "MHB1_OUT","aliasRF_Stim2", "aliasRF_Meas1", "MB2IN1_MHB1OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB2_IN2", "MHB1_OUT","aliasRF_Stim2", "aliasRF_Meas1", "MB2IN2_MHB1OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("MB2_IN2", "MHB3_OUT","aliasRF_Stim2", "aliasRF_Meas1", "MB2IN2_MHB3OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("HB1_IN2", "MHB3_OUT","aliasRF_Stim2", "aliasRF_Meas1", "HB1IN2_MHB3OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
        portMap.add(new PortMapping("HB2_IN2", "MHB3_OUT","aliasRF_Stim3", "aliasRF_Meas1", "HB2IN2_MHB3OUT_SWITCH", "TC_SW1_1,TC_SW1_2,TC_SW1_3,TC_SW2_1,TC_SW2_2,TC_SW3_1,TC_SW3_2,TC_SW4_1,TC_SW4_2", "0,0,1,0,1,0,1"));
    }
    public static String[] getRFPort(String _in, String _out)
    {
        String[] in_out_port= new String[2];
        for (PortMapping _PortMapping : portMap){
            if (_PortMapping.rfIn.contentEquals(_in)
                    && _PortMapping.rfOut.contentEquals(_out) ){
                in_out_port[0] = _PortMapping.rfInATE;
                in_out_port[1] = _PortMapping.rfOutATE;
                return in_out_port;
            }
        }
        System.out.println("ERROR!!! NOT FOUND Port info in the mapping:"+_in+" "+_out);
        return in_out_port;
    }
    public static List<String> getPortList()
    {
        List<String> in_out_port= new ArrayList<String>();
        for (PortMapping _PortMapping : portMap){
            in_out_port.add(_PortMapping.rfIn);
            in_out_port.add(_PortMapping.rfOut);
        }
        return in_out_port;
    }
    public static String getCWPat(String _in,String _out){
        for (PortMapping _PortMapping : portMap){
            if (_PortMapping.rfIn.contentEquals(_in)
                    && _PortMapping.rfOut.contentEquals(_out) ){
                return _PortMapping.switchPattern;
            }
        }
        return "ERROR_SW_PAT";
    }
    public static long getCW(String _in,String _out){
        return getCW(_in, _out,  false);
    }
    @SuppressWarnings("unlikely-arg-type")
    public static long getCW(String _in,String _out, boolean amp){
        long cwBit=0b0;
        List<String> listAmpPin = new ArrayList<String>();
        List<String> listPin = new ArrayList<String>();
        List<Integer> listData = new ArrayList<Integer>();
        for (PortMapping _PortMapping : portMap){
            if (_PortMapping.rfIn.contentEquals(_in)
                    && _PortMapping.rfOut.contentEquals(_out) ){
                listPin = Arrays.asList(_PortMapping.switchCtrlPin.split("\\s*,\\s*"));
                listAmpPin = Arrays.asList(_PortMapping.ampCtrlPin.split("\\s*,\\s*"));
                List<String> listDatatmp = Arrays.asList(_PortMapping.selCode.split("\\s*,\\s*"));
                for(String tmp:listDatatmp){
                    listData.add(Integer.parseInt(tmp));
                }
                for(String cwpin:CSPins){
                    if(Arrays.asList(listPin).contains(cwpin)){
                        cwBit +=(cwBit<<1)+listData.get(Arrays.asList(listPin).indexOf(cwpin));
                    }else if(amp && Arrays.asList(listAmpPin).contains(cwpin)){
                        cwBit +=(cwBit<<1)+listData.get(Arrays.asList(listAmpPin).indexOf(cwpin));
                    }else{
                        cwBit +=(cwBit<<1);
                    }
                }
                break;
            }
        }
        return cwBit;
    }
}
