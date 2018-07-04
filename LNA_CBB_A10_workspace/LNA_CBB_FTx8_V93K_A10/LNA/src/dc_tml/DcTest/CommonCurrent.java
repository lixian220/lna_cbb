package src.dc_tml.DcTest;


import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import src.rfcbb_tml.com.RF_UserCommon;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupDcVI.IImeas;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteDoubleArray;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDcVIResults;
import xoc.dta.testdescriptor.IParametricTestDescriptor;



/**
 *
 * This test method performs a DC IDD test. the input parameters for instruments setup come from profiles of testtable.
 *
 *
 * @author  308770
 * @since   1.0.0
 *
 *
 */

public class CommonCurrent extends TestMethod {

    public class pinMeasParaGrp extends ParameterGroup{
         public IParametricTestDescriptor ptd;

        public double Range = 5;//mA
        public double SettlingTime = 5;//mS
        public String PinName = "dummyPin";
        public String importSpec ="";
        public String PatName="";
        public boolean bypassMode = false;
    }

    public ParameterGroupCollection<pinMeasParaGrp> Parameter = new ParameterGroupCollection<>();



    String dpsSignals="";

    public IMeasurement measurement;

    @SuppressWarnings("unused")
    @Override
    public void setup ()
    {

        String _testSuiteName = context.getTestSuiteName();
        String testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));

        String pattPath = "setups.vectors.patterns.";
        if(testSuiteName.contains("LNA_OFF"))
        {
            pattPath = "dsa_gen.Main.LNA_OFF.";
        }

        message(1, testSuiteName + " device setup start!" );


        String measName ="Imeas";
        long average_meas = 16;
        if (_testSuiteName.contains("GROSS")) {
            average_meas = 64;
        }

        Set<String> key = Parameter.keySet();
        Set<String> sortedKey = new TreeSet<String>();
        for(String _key : key){
            sortedKey.add(_key);
        }

        String testItemString="";
        for(Iterator<String> itr = sortedKey.iterator(); itr.hasNext();)
        {
            String tmp_str= itr.next();
            testItemString +=tmp_str;
            if(itr.hasNext())
            {
            testItemString +="_";
            }
        }

        IDeviceSetup deviceSetup = DeviceSetupFactory.createNamedInstance(testItemString);

        String patternName = "" ;
        for(String _sortedKey : sortedKey){
            pinMeasParaGrp perPinMeas = Parameter.get(_sortedKey);

            if(perPinMeas.bypassMode==true){

            }else{

                if(false == perPinMeas.importSpec.isEmpty() ) {
                    deviceSetup.importSpec(perPinMeas.importSpec);
                } else {
                    deviceSetup.importSpec(measurement.getSpecificationName());
                }

                IImeas DPS_meas1 = deviceSetup.addDcVI(perPinMeas.PinName).imeas(measName);
                DPS_meas1.setWaitTime(perPinMeas.SettlingTime*1e-3).setIrange(perPinMeas.Range*1e-3).setAverages(16);//.setRestoreIrange(true);
                dpsSignals += perPinMeas.PinName + "+";

                if(perPinMeas.PatName !=""){
                    patternName = perPinMeas.PatName;
                }
            }
        }
        deviceSetup.sequentialBegin();
        {
            if(patternName!= ""){

                deviceSetup.patternCall(pattPath + patternName);
            }
            deviceSetup.actionCall(measName);

        }deviceSetup.sequentialEnd();


        dpsSignals=dpsSignals.substring(0,dpsSignals.length()-1);

        measurement.setSetups(deviceSetup);

        message(1, context.getTestSuiteName() + ": device setup done!");


    }



    @Override
    public void execute ()
    {
        measurement.execute();
        IDcVIResults DPSResult = measurement.dcVI(dpsSignals).preserveResults();
        if (RF_UserCommon.hiddenupload()){
            releaseTester();
        }

        Set<String> key = Parameter.keySet();
        Set<String> sortedKey = new TreeSet<String>();
        for(String _key : key){
            sortedKey.add(_key);
        }
        for(String _sortedKey : sortedKey){
            pinMeasParaGrp perPinMeas = Parameter.get(_sortedKey);


            if(perPinMeas.bypassMode==true){
            }else{

                MultiSiteDoubleArray DPSvoltages = DPSResult.imeas("").getCurrent(perPinMeas.PinName);
                MultiSiteDouble value = DPSvoltages.getElement(0);



                String  _testSuiteName = context.getTestSuiteName();
                String  testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));

                if(perPinMeas.PinName.contains("VDDIO")) {

                    perPinMeas.ptd.setTestText(testSuiteName+":VDDIO").setLogLevel(40);
                    perPinMeas.ptd.evaluate(value);

                }
                if(perPinMeas.PinName.contains("VDD1P8")) {
                    perPinMeas.ptd.setTestText(testSuiteName+":VDD1P8").setLogLevel(40);
                    perPinMeas.ptd.evaluate(value);
                }

            }
        }



    }
}
