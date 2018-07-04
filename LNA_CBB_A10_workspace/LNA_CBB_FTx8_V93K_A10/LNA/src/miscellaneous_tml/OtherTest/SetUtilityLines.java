package src.miscellaneous_tml.OtherTest;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupUtility;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.measurement.IMeasurement;


public class SetUtilityLines extends TestMethod {

    public IMeasurement measurement;

    @In    public String importSpec ="setups.specs.spc_lev_eqn1_spec3_set1_tim_eqn1_spec1_set1";


    public class UtilitySettings extends ParameterGroup {
        public String pins = "";
        public long     value = 0;

    }
    public ParameterGroupCollection<UtilitySettings> utilitySettings = new ParameterGroupCollection<>();

    @Override
    public void setup()
    {
        if(messageLogLevel >=1) {
            println("device setup start!");
        }
        IDeviceSetup devSetup=DeviceSetupFactory.createInstance();

        for(UtilitySettings tmpSettings : utilitySettings.values()) {
            ISetupUtility thisUtilSetup = devSetup.addUtility(tmpSettings.pins);
            thisUtilSetup.setValue(tmpSettings.value);
        }

        measurement.setSetups(devSetup);

        message(1,"device Utility Settings setup done!"  );

    }

    @Override
    public void execute() {
        measurement.activate();
        //        measurement.execute();
    }


}

