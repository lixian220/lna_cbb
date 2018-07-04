package src.mipi_tml;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupProtocolInterface;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dsa.ISetupTransactionSeqDef.Direction;
import xoc.dsa.ISetupTransactionSeqDef.Type;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IProtocolInterfaceResults;
import xoc.dta.resultaccess.ITransactionSequenceResults;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
 *
 * This test method performs readback action test for MIPI protocol, and compare the readback values with limits <br>
 *
 * @param JudgeFlag   Flag indicates whether write results to datalog stream
 *
 *
 * @version 1.0.0
 * @author 308770
 *
 */


public class MIPIRead_TEST extends TestMethod{

    /**
     * IMeasurement
     * */
    public IMeasurement MeasReadPhyReg;

    @In public boolean JudgeFlag = true;

    private String readTsCallName = "";


    /**
     * This Class read the setup parameter from testtable to override init value. if the parameter not exist in
     * testtable, it will use the init value.
     *
     * @param importSpec    setup specification for test setup
     * @param PatName       Pattern name called in operation sequencer
     * @param signals       Signal name
     * @param Addr          Address list string of register to be read
     * @param Data          Data to be write or expected
     * @param USID          USID for MIPI protocol
     * @param bypassMode    whether to bypass this testsuite
     * @param ptd           IParametricTestDescriptor<br><br>
     *
     *
     * @version 1.0.0
     *
     */

    public class MipiData extends ParameterGroup{
        public String importSpec = "";
        public String PatName="";
        public String signals = "";
        public String Addr = "0x00";//mA
        public String Data = "0x00";//mS
        public String USID = "0xC";
        public IParametricTestDescriptor ptd;
        public boolean bypassMode = false;
    }
    public ParameterGroupCollection<MipiData> Parameter = new ParameterGroupCollection<>();

    @Override
    public void setup() {

        Set<String> key = Parameter.keySet();
        Set<String> sortedKey = new TreeSet<String>();
        for(String _key : key){
            sortedKey.add(_key);
        }

        try
        {
            IDeviceSetup ds = DeviceSetupFactory.createInstance();

            ISetupProtocolInterface paInterface = ds.addProtocolInterface("TS_Setup1", "setups.mipi.mipi");
            paInterface.addSignalRole("DATA", "SDATA");
            paInterface.addSignalRole("CLK", "SCLK");
            ds.parallelBegin();
            ds.sequentialBegin();
            {
                for(String _sortedKey : sortedKey){
                    MipiData perData = Parameter.get(_sortedKey);

                    if(perData.bypassMode == true){
                        /*
                         * if bypass items, just do nothing
                         */

                    }else{
                    String pattPath = "setups.vectors.patterns."; //input parameter
                    if(false == perData.importSpec.isEmpty()) {
                        ds.importSpec(perData.importSpec);
                    } else {
                        ds.importSpec(MeasReadPhyReg.getSpecificationName());
                    }

                    if(!perData.PatName.equals("")){

                        ds.patternCall(pattPath + perData.PatName);
                    }
                    Integer itr=0;
                    ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup1");
                    perData.USID="<"+perData.USID+">";
                    transDigSrc.addParameter(Type.UnsignedLong, Direction.OUT, "value"+itr);

                    String fullTSname = context.getTestSuiteName();
                    String ShortTSname = fullTSname.substring(1 + fullTSname.lastIndexOf("."));
                    if(ShortTSname.contains("GSID"))
                    {
                        perData.USID ="0x0";
                        perData.USID="<"+perData.USID+">";
                        transDigSrc.addTransaction("write",perData.USID,"<"+perData.Addr+">","<"+perData.Data+">");
                        perData.USID ="0xC";
                        perData.USID="<"+perData.USID+">";
                        transDigSrc.addTransaction("read",perData.USID,"<"+perData.Addr+">","value"+itr++);

                    }

                    else
                    {
                        // transDigSrc.addTransaction("write",USID,"<"+perData.Addr+">","<"+perData.Data+">");
                        transDigSrc.addTransaction("read",perData.USID,"<"+perData.Addr+">","value"+itr++);

                    }
                    readTsCallName=ds.transactionSequenceCall(transDigSrc,"");

                    }
                }
            }
            ds.sequentialEnd();
            ds.parallelEnd();
            MeasReadPhyReg.setSetups(ds);
        }catch (Exception e){
            System.out.println("Something went wrong :-(");
            e.printStackTrace();
        }
    }


    @Override
    public void execute() {


        MeasReadPhyReg.execute();

        IProtocolInterfaceResults mipiRslt= MeasReadPhyReg.protocolInterface("MIPI").preserveResults();
        releaseTester();

        ITransactionSequenceResults tsr= mipiRslt.transactSeq(readTsCallName)[0];
        Map<String, MultiSiteLong> capResults = tsr.getValueAsLong();

        Set<String> key = Parameter.keySet();
        Set<String> sortedKey = new TreeSet<String>();
        for(String _key : key){
            sortedKey.add(_key);
        }


        Integer counter=0;
        for(String _sortedKey : sortedKey){
            MipiData perData = Parameter.get(_sortedKey);

            if(perData.bypassMode==true){
                /*
                 * if bypass items, just do nothing
                 */

            }else{

                MultiSiteLong value = capResults.get("value"+counter);

                if(JudgeFlag){

                    String  _testSuiteName = context.getTestSuiteName();
                    String  testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));

                    perData.ptd.setTestText(testSuiteName+":read_"+perData.Addr+"_"+perData.Data);

                    perData.ptd.evaluate(value);
                }else{
                    println("MIPI:write"+perData.USID+" <"+perData.Addr+"> expect:<"+perData.Data+"> get:"+value);
                }
                counter++;
            }
        }
    }
}
