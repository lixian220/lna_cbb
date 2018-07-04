package src.dc_tml.DcTest;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import src.rfcbb_tml.com.RF_UserCommon;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupDigInOut.IVforceImeas;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteDoubleArray;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDigInOutActionResults;
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


public class PPMU_Measure extends TestMethod {


    public class PMUMeasParaGrp extends ParameterGroup{
        public IParametricTestDescriptor ptd;
        public double Irange = 1.0 ;        //mA
        public double Vforce = 0.2 ;        //V
        public double SettlingTime = 5;     //ms
        public String PinName = "dummyPin";
        public String importSpec = "";
        public String PatName= "";
        public boolean bypassMode = false ;
    }

    public ParameterGroupCollection<PMUMeasParaGrp> Parameter= new ParameterGroupCollection<>();

    String PmuSignals = "";
    public IMeasurement measurement;

    @Override
    public void setup(){

        String _testSuiteName = context.getTestSuiteName();
        String testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));
        String pattPath = "dsa_gen.Main.ATEST.";

        message(1, testSuiteName + " device setup start!" );

        String measName ="Imeas";
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
            PMUMeasParaGrp perPinMeas = Parameter.get(_sortedKey);

            if(perPinMeas.bypassMode==true){

            }else{
                if(false == perPinMeas.importSpec.isEmpty()) {
                    deviceSetup.importSpec(perPinMeas.importSpec);
                }else {
                    deviceSetup.importSpec(measurement.getSpecificationName());
                }


                IVforceImeas PMU_Meas = deviceSetup.addDigInOut(perPinMeas.PinName).vforceImeas(measName);
                PMU_Meas.setForceValue(perPinMeas.Vforce).setIrange(perPinMeas.Irange*1e-3).setWaitTime(perPinMeas.SettlingTime*1e-3);
                PmuSignals += perPinMeas.PinName + "+" ;

                if(perPinMeas.PatName!=""){
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

        }
        deviceSetup.sequentialEnd();


        PmuSignals = PmuSignals.substring(0,PmuSignals.length()-1);

        measurement.setSetups(deviceSetup);

        message(1, context.getTestSuiteName() + ": device setup done!");

    }



    @Override
    public void execute() {
        // TODO Auto-generated method stub
        measurement.execute();
        IDigInOutActionResults PMUResult = measurement.digInOut(PmuSignals).preserveActionResults();
        if (RF_UserCommon.hiddenupload()){
            releaseTester();
        }
        Set<String> key = Parameter.keySet();
        Set<String> sortedKey = new TreeSet<String>();
        for(String _key : key){
            sortedKey.add(_key);
        }

        for(String _sortedKey : sortedKey){
            PMUMeasParaGrp perPinMeas = Parameter.get(_sortedKey);

            if(perPinMeas.bypassMode==true){

            }else{

            MultiSiteDoubleArray PMUCurrent = PMUResult.vforceImeas("").getCurrent(perPinMeas.PinName);
            MultiSiteDouble value = PMUCurrent.getElement(0);

            String  _testSuiteName = context.getTestSuiteName();
            String  testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));

            if(perPinMeas.PinName.contains("ATEST")) {

                perPinMeas.ptd.setTestText(testSuiteName+":ATEST").setLogLevel(40);
                perPinMeas.ptd.evaluate(value.multiply(-1.0));

            }
            }
        }


    }

}
