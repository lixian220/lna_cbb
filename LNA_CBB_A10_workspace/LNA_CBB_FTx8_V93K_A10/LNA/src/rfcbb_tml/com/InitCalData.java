package src.rfcbb_tml.com;

import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
 * load calibration ODS file and read data into data container
 *
 * @param CalDataFilePath       production calibration ODS file
 * @param FixedCalDataFilePath  Init calibration ODS file
 * @param sheetName             all the sheets in above two ODS file need to be parsed
 *
 * @since  1.0.0
 * @author 308770
 *
 *
 */
public class InitCalData extends TestMethod{

    @In public String CalDataFilePath="calibration/LNA_CAL_prod.ods";
    @In public String FixedCalDataFilePath="calibration/LNA_CAL.ods";

    @In public String sheetName = "golden_Gain;golden_NF;golden_P1dB;golden_IIP;golden_H3;cal_NFeff;cal_InLoss;cal_OutLoss;cal_OutLossL"; //"pathloss;AmpGain;AmpNF";


    public IParametricTestDescriptor ptd_Rslt;
    @SuppressWarnings("static-access")
    @Override
    public void execute(){




        String prjDir = context.workspace().getActiveProjectPath()+"/";
        String pathLossFile = prjDir + CalDataFilePath;
        String FixedPathLossFile = prjDir + FixedCalDataFilePath;

        int[] activeSites = context.getActiveSites();
        MultiSiteBoolean offline = context.testProgram().variables().getBoolean("SYS.OFFLINE");
        CalData.getInstance().init(pathLossFile, sheetName, activeSites, offline);
        CalData.getFixedInstance().init(FixedPathLossFile, sheetName, activeSites, offline);
        println("Reading pathLoss value from file \'"+pathLossFile + "\'... Done");
        println("Reading pathLoss value from file \'"+FixedPathLossFile + "\'... Done");
//        for(Key_Port key_port:CalData.getInstance().m_CalDataMap.keySet()){
//
//            for(rfcbb_tml.COM.Key key:CalData.getInstance().m_CalDataMap.get(key_port).calMap.keySet()){
//
////            println("WARNING:PB"+key_port.lbid+":"+key_port.test+":"+key.item+":"+key.freq+":"+CalData.getInstance().m_CalDataMap.get(key_port).calMap.get(key));
//
//            }
//        }



    }
}


