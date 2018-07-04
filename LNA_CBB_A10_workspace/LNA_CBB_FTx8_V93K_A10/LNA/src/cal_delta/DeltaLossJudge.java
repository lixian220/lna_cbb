package src.cal_delta;

import src.rfcbb_tml.com.CalData;
import src.rfcbb_tml.com.RF_UserCommon;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.annotations.Out;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

public class DeltaLossJudge extends TestMethod {
    /*
     * 20180419
     * @wangping
     * Hisilicon want to put inLoss/OutLoss in cal_ods file.
     * put delta inLoss/OutLoss in cal_prod_ods file.
     * Judge delta Loss and generate GUI Pass/Fail file in the window.
     */
    @In public Integer maxCount = 100;
    @In public String importSpec = "setups.specs.RF.normal";
    @In public String calDataFilePath_prod="calibration/LNA_CAL_prod.ods";
    @In public String sheetName= "cal_InLoss;cal_OutLoss;cal_OutLossL";
    @In public Double mFreq = 7.8e6;
    @In public boolean flowCtrl = false;
    public IParametricTestDescriptor ptdInLoss;
    public IParametricTestDescriptor ptdOutLoss;
    public IParametricTestDescriptor ptdOutLossL;
    public static MultiSiteBoolean mResult1 = new MultiSiteBoolean(true);
    public static MultiSiteBoolean mResult2 = new MultiSiteBoolean(true);
    public static MultiSiteBoolean mResult3 = new MultiSiteBoolean(true);
    @Out public static MultiSiteBoolean mResult = new MultiSiteBoolean(false);
    private static Integer flowCount = 0;


    @Override
    public void setup(){
    }
    @SuppressWarnings("static-access")
    @Override
    public void execute() {
        // TODO Auto-generated method stub

       String _testSuiteName = context.getTestSuiteName();
       String testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));
       MultiSiteDouble Rslt_InLoss = new MultiSiteDouble(999);
       MultiSiteDouble Rslt_OutLoss = new MultiSiteDouble(999);
       MultiSiteDouble Rslt_OutLossL = new MultiSiteDouble(999);
       String prjDir = context.workspace().getActiveProjectPath()+"/";
       String pathLossFile_prod =prjDir + calDataFilePath_prod;
       int[] activeSites = context.getActiveSites();
       MultiSiteBoolean offline = context.testProgram().variables().getBoolean("SYS.OFFLINE");
       CalData CalData_prod = new CalData();
       CalData_prod.m_currentLB = RF_UserCommon.getDUT_ID();
        if(flowCount < maxCount-1)
        {
            if(flowCtrl)
            {
                flowCount++;
                println("Testsuite : "+testSuiteName+" is "+"Running!!");
            }
            else
            {
                println("Testsuite : "+testSuiteName+" is "+"Running!!");
            }
        }
        else
        {
                JButtonInfo mJButtonInfo = JButtonInfo.getInstance();
                MultiSiteDouble[] mArrayRslt = new MultiSiteDouble[3];
                mArrayRslt[0] = new MultiSiteDouble(999.99);
                mArrayRslt[1] = new MultiSiteDouble(999.99);
                mArrayRslt[2] = new MultiSiteDouble(999.99);
                CalData_prod.init(pathLossFile_prod, sheetName, activeSites, offline);
                Rslt_InLoss = CalData_prod.getValue(testSuiteName, "cal_InLoss", 0.0);

                Double tmpFreq1 = mFreq;
                println("INFO: Testsuite "+context.getTestSuiteName()+" Freq ="+mFreq + " MHz"+" InLoss :"+Rslt_InLoss);
                Rslt_OutLoss = CalData_prod.getValue(testSuiteName,"cal_OutLoss", tmpFreq1);
                if(!testSuiteName.contains("MHB3_OUT")) {
                    Rslt_OutLossL = CalData_prod.getValue(testSuiteName,"cal_OutLossL", tmpFreq1);
                    ptdOutLossL.setTestText("OutDeltaLossL");
                    ptdOutLossL.evaluate(Rslt_OutLossL);
                }

                ptdInLoss.setTestText("InDeltaLoss");
                ptdInLoss.evaluate(Rslt_InLoss);
                ptdOutLoss.setTestText("OutDeltaLoss");
                ptdOutLoss.evaluate(Rslt_OutLoss);



            int a = 0;
            for(int site : activeSites)
            {
                if(!ptdInLoss.getPassFail().get(site)||!ptdOutLoss.getPassFail().get(site)||(!ptdOutLossL.getPassFail().get(site)/*&&!testSuiteName.contains("MHB3_OUT")*/) )
                {
                  a = 1;
                }
                if(a==1)
                {
                    mArrayRslt[0].set(Rslt_InLoss);
                    mArrayRslt[1].set(Rslt_OutLoss);
                    mArrayRslt[2].set(Rslt_OutLossL);

                }

                mResult1.set(site,ptdInLoss.getPassFail().get(site)&&mResult1.get(site));
                mResult2.set(site,ptdOutLoss.getPassFail().get(site)&&mResult2.get(site));
                mResult3.set(site,ptdOutLossL.getPassFail().get(site)&&mResult3.get(site));
                mResult.set(site,mResult1.get(site)&&mResult2.get(site)&mResult3.get(site));
            }


            if(a == 1)
            {
                mJButtonInfo.setMapValue(testSuiteName, mArrayRslt);

            }
        }


    }




}

