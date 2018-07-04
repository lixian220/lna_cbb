package src.rfcbb_tml.com;
import src.rf2rf_tml.utilities.MapDatalog;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
 * This test method provide the capability to write/read LBID.
 * In write mode ({@code wEEPROM ==true}), test method write user defined ID to EEPRON of LB and update to some global variables; <br>
 * In read mode ({@code wEEPROM ==false}, test method read LBID and update to some global variables
 *
 * @param dutBdId   input variables for testsuite to record the LBID; if in write mode,  this new LB will update to EEPROM of LB, is in read mode, this variable will be ignored.
 * @param wEEPROM   flag to indentify read or write mode. default is false, means read mode.
 *
 *
 *
 * @version 1.0.0
 * @author 308770
 *
 */


public class Loadboard_ID extends TestMethod {
    public IParametricTestDescriptor ptd_ID;
    @In public Integer dutBdId= null;
    @In public boolean wEEPROM= false;
    static int num_run=0;
    public String convertHexToString(String hex){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for( int i=0; i<hex.length()-1; i+=2 ){
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
            temp.append(decimal);
        }
        System.out.println("Decimal : " + temp.toString());
        return sb.toString();
    }
    @Override
    public void update ()
    {
        /*
         *
         */
    }
    @Override
    public void execute ()
    {
        MultiSiteBoolean offline = context.testProgram().variables().getBoolean("SYS.OFFLINE");
        if(offline.equalTo(true))
        {
            dutBdId = 2;
            RF_UserCommon.setDUT_ID(2);
            System.out.println("Offline! Set dut BdId = 2");
            CalData mCalData = CalData.getInstance();
            mCalData.m_currentLB =dutBdId;
            CalData mCalOdsData = CalData.getFixedInstance();
            mCalOdsData.m_currentLB =dutBdId;
            MapDatalog mMapData = MapDatalog.getInstance();
            mMapData.m_PBID =dutBdId;
        }
        else
        {
            int bdId;
            if(wEEPROM){
                context.dutBoard().writeIdString(Integer.toString(dutBdId));
            }
            if(num_run==0)
            {
                bdId = Integer.parseInt(context.dutBoard().readIdString());
                num_run=1;
            }
            else {
                bdId=  RF_UserCommon.getDUT_ID();
            }
            MapDatalog mMapData = MapDatalog.getInstance();
            mMapData.m_PBID =bdId;
            RF_UserCommon.setDUT_ID(bdId);
            CalData mCalData = CalData.getInstance();
            mCalData.m_currentLB =bdId;
            dutBdId = bdId;
        }
        MultiSiteDouble ID_all = new MultiSiteDouble(dutBdId);
        String  _testSuiteName = context.getTestSuiteName();
        String  testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));
        ptd_ID.setTestText(testSuiteName+":LBID");
        ptd_ID.evaluate(ID_all);
        message(1, "LoadBoard ID= "+ID_all);
    }
}
