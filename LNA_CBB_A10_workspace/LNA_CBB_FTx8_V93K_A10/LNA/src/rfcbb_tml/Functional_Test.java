package src.rfcbb_tml;

import src.rfcbb_tml.com.RF_UserCommon;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDigInOutResults;
import xoc.dta.testdescriptor.IFunctionalTestDescriptor;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
 * This test method performs functional test and parametric judgment of test results
 *
 * @param importSpec    setup specification for this test
 * @param PatName       Pattern Name for this Functional test
 * @param JudgeFlag     Flag to indicate whether to datalog test result
 *
 * @version 1.0.0
 * @author 308770
 *
 */
public class Functional_Test extends TestMethod {

    @In public String importSpec;
    @In public String PatName;
    @In public boolean JudgeFlag=true;

    public IMeasurement measurement;

    public IFunctionalTestDescriptor ftd_func;
    public IParametricTestDescriptor ftd;
    public String signals = "";

    @Override
    public void setup ()
    {
        String _testSuiteName = context.getTestSuiteName();
        _testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));
        String pattPath = "setups.vectors.patterns."; //input parameter
        if(_testSuiteName.indexOf("DFT")!=-1)
        {
            pattPath = "setups.vectors.DFT.";
        }
        else if(_testSuiteName.contains("POR_13dB"))
        {
            pattPath="dsa_gen.Main.PORRegBurst_POR13dB.";
        }
        else if(_testSuiteName.contains("POR_15dB")||_testSuiteName.contains("POR15dB"))
        {
            pattPath="dsa_gen.Main.PORRegBurst_POR15dB.";
        }
        else if(_testSuiteName.contains("POR_18dB"))
        {
            pattPath="dsa_gen.Main.PORRegBurst_POR18dB.";
        }
        else if(_testSuiteName.contains("LNA_OFF"))
        {
            pattPath = "dsa_gen.Main.LNA_OFF.";
        }



        IDeviceSetup deviceSetup = DeviceSetupFactory.createNamedInstance(_testSuiteName);
        if(false == importSpec.isEmpty()) {
            deviceSetup.importSpec(importSpec);
        } else {
            deviceSetup.importSpec(measurement.getSpecificationName());
        }

        deviceSetup.sequentialBegin();
        {
            if(PatName!= ""){

                deviceSetup.patternCall(pattPath + PatName);
            }
            deviceSetup.waitCall(1e-3);

        }deviceSetup.sequentialEnd();


        measurement.setSetups(deviceSetup);
    }

    @Override
    public void execute ()
    {
        measurement.execute();
        MultiSiteBoolean FuncResults = new MultiSiteBoolean(true);
        MultiSiteDouble  FuncResultDb = new MultiSiteDouble(-1.0);

        // 1. Preserve the measurement result.
        IDigInOutResults results = measurement.digInOut(signals).preserveResults(ftd_func);


        // 2. Release the tester.
        if (RF_UserCommon.hiddenupload()){
            releaseTester();
        }


        // 3. Evaluate the results.

        FuncResults = results.hasPassed();

        String _testSuiteName = context.getTestSuiteName();
        _testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));


        int[] activeSites = context.getActiveSites();
        for(int site: activeSites)
        {
            if ( FuncResults.get(site).equals(true))
            {
                FuncResultDb.set(site, 1.0);
            }

        }

        if(JudgeFlag) {
            ftd.setTestText(_testSuiteName+":passFunc");
            ftd.evaluate(FuncResultDb);
        }

    }
}
