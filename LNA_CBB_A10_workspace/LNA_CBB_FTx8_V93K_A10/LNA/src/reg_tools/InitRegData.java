package src.reg_tools;

import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
*
* This class provide Init method to read register sheets  .
*
*
* @param RegDataFilePath reference to the path (relative to project folder)
* @param sheetName       reference to the sheet names to be read.
*
*
* @author  308770
* @since   1.0.0
*
*
*/



public class InitRegData extends TestMethod {


    @In public String RegDataFilePath = "LNA/setups/testtable/LNA_RF_FT.ods";
    @In public String sheetName = "POR15dB;POR13dB;POR18dB;Common_REG";

    public IParametricTestDescriptor ptd_Rslt;

    @SuppressWarnings("static-access")
    @Override
    public void execute() {
        // TODO Auto-generated method stub
        String prjDir = context.workspace().getActiveProjectPath()+"/";
        String RegFilePath = prjDir + RegDataFilePath;

        int[] activeSites = context.getActiveSites();
        MultiSiteBoolean offline = context.testProgram().variables().getBoolean("SYS.OFFLINE");
        Reg_Read.getInstance().init(RegFilePath, sheetName, activeSites, offline);
        println("Reading Reg value from file \'"+RegFilePath + "\'... Done");


    }

}
