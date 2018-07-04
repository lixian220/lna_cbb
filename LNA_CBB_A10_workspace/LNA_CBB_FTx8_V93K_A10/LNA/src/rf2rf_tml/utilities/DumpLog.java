package src.rf2rf_tml.utilities;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.testdescriptor.IParametricTestDescriptor;


/**
 * This class write data of DataMap to specified ODS sheet at the {@link maxCount} run loop of testflow.
 *
 *
 *
 * @param maxCount              the loop run count to dump data into ODS sheet
 * @param initFlag              flag to create the data container object and init it
 * @param calFlag               string to identify to CAL mode: "PROD" or "INIT"
 * @param importSpec            specification for this test
 * @param calDataFilePath       filepath for ****CAL.ODS
 * @param calDataFilePath_prod  filepath for ****CAL_delta.ODS
 * @param sheetName             detail sheet name array which need to be update its content in calibration
 * @param flowCount             variable to accumulate the current loop index in the loop run of testflow
 *
 * @param measurement           measurement server interface
 *
 * @author 308770
 *
 */


public class DumpLog extends TestMethod {

    @In    public Integer maxCount = 100;
    @In     public boolean initFlag = false;
    @In     public String calFlag=""; //PROD  INIT
    @In    public String importSpec = "";
    @In     public String calDataFilePath="calibration/Hi6H02TV100_CAL.ods";
    @In     public String calDataFilePath_prod="calibration/Hi6H02TV100_CAL_prod.ods";

    @In     public String sheetName = "golden_Gain;golden_NF;cal_NFeff;cal_InLoss;cal_OutLoss;cal_OutLossL;cal_H3InLoss";

    @In    public MultiSiteDouble LastNFDone= new MultiSiteDouble();

    public IParametricTestDescriptor ptd_Rslt1;
    public IParametricTestDescriptor ptd_Rslt2;
    public IParametricTestDescriptor ptd_Rslt3;
    public IParametricTestDescriptor ptd_Rslt4;
    public IParametricTestDescriptor ptd_Rslt5;
    public IParametricTestDescriptor ptd_Rslt6;
    public IParametricTestDescriptor ptd_Rslt7;

    private static Integer flowCount = 0;


    public IMeasurement measurement;
    @Override
    public void setup (){
        IDeviceSetup devSetup = DeviceSetupFactory.createInstance();
        if(!importSpec.isEmpty()) {
            devSetup.importSpec(importSpec);
        }
        measurement.setSetups(devSetup);
    }

    @Override
    public void update(){ //This is the area to modify something that already exists..
    }

    @SuppressWarnings({ "static-access", "unused" })
    @Override
    public void execute() {

        int[] activeSites = context.getActiveSites();

        String prjDir = context.workspace().getActiveProjectPath()+"/";

        int done =0;

        if(initFlag){
            if(flowCount==0)
            {

                MapDatalog.getInstance().init();
            }
        }else{

            if(calFlag.equals("PROD")){
                if(flowCount<maxCount-1){
                    flowCount++;
                }else{
                    flowCount=0;
                    done=1;

                    MapDatalog.getInstance().export(prjDir+calDataFilePath_prod,activeSites);
                }

            }


            else if(calFlag.equals("INIT") ){
                if(flowCount<maxCount-1){
                    flowCount++;
                }else{
                    flowCount=0;
                    MapDatalog.getInstance().export(prjDir+calDataFilePath,activeSites);
                }

            }
            else
            {
                println("ERROR,please check the flag <CalFlag>......");
            }

        }
    }
}
