/**
 *
 */
package src.rf2rf_tml.utilities;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.testdescriptor.IFunctionalTestDescriptor;

/**
 *  This test method can change the variables in spec and download the new values to ATE instruments.
 *
 *  user need to specify the variables you want to change and pass the new value to it by testflow variables.
 *
 *  @param var1         testflow variable, user gives the value to this variable in testflow definition
 *  @param var2         testflow variable, user gives the value to this variable in testflow definition
 *  @param importSpec   testflow variable, reference  to specification need to be import for this test
 *
 */
public class Change_SpecVar extends TestMethod {

    public IMeasurement meas_changeSpecVar;
    @In public Double var1;
    @In public Double var2;
    @In public String importSpec;

    /*
     * From a testflow, you can access the collection of parameters with the following code:
     */
    public final ParameterGroupCollection<MyParameterGroup> myParameterGroupCollection = new ParameterGroupCollection<>();

    /*
     * A group of parameters, measurements and test descriptors.
     */
    public static class MyParameterGroup extends ParameterGroup {

        public IMeasurement myMeasurement;
        public IFunctionalTestDescriptor myTestDescriptor;
        public Double myDouble;
    }

    /* (non-Javadoc)
     * @see xoc.dta.TestMethod#setup()
     */
    @Override
    public void setup() {
        IDeviceSetup deviceSetup = DeviceSetupFactory.createInstance();

        if(false == importSpec.isEmpty()) {
            deviceSetup.importSpec(importSpec);
        }
        else
        {
            deviceSetup.importSpec("setups.specs.POWERUP.normal");
        }

        deviceSetup.sequentialBegin();
            deviceSetup.parallelBegin();
            deviceSetup.patternCall("setups.vectors.patterns.dummy");
            deviceSetup.parallelEnd();
        deviceSetup.sequentialEnd();


        meas_changeSpecVar.setSetups(deviceSetup);


    }

    /* (non-Javadoc)
     * @see xoc.dta.TestMethod#update()
     */
    @Override
    public void update() {
        // TODO Auto-generated method stub
        /*
         * Update your test method parameters and measurements here.
         */
    }

    /* (non-Javadoc)
     * @see xoc.dta.TestMethod#execute()
     */
    @Override
    public void execute() {
        // TODO Auto-generated method stub
        String _testSuiteName = context.getTestSuiteName();
        String testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));

        if(testSuiteName.contains("DISCONNECT"))
        {
            context.spec("specs.levels.lev_eqn1_set1").setVariable("d_VDD1P8", var1);
            context.spec("specs.levels.lev_eqn1_set1").setVariable("d_VDD1P8", var2);
        }
        else
        {

            context.spec("specs.levels.lev_eqn1_set1").setVariable("Vcoef1", var1);
            context.spec("specs.levels.lev_eqn1_set1").setVariable("Vcoef2", var2);
        }

        meas_changeSpecVar.execute();

    }

}
