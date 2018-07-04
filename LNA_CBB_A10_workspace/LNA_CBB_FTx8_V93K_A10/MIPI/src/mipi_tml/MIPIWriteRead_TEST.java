package src.mipi_tml;

import java.util.Map;

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
* This test method performs "first write then read" action for MIPI protocol <br>
*
*
* @param importSpec     setup specification name
* @param JudgeFlag      Flag indicates whether write results to datalog stream
* @param Parameter      parameter group collection to read setups from testtable
* @param USID           USID for MIPI protocol
*
* @version 1.0.0
* @author 308770
*
*/
public class MIPIWriteRead_TEST extends TestMethod{

    public IMeasurement MeasReadPhyReg;

    @In public String importSpec = "";
    @In public boolean JudgeFlag = true;
    @In public String USID = "0x8";
    @In public String PatName="";

    private String readTsCallName = "";


    /**
     * This Class read the setup parameter from testtable to override init value. if the parameter not exist in
     * testtable, it will use the init value.
     *
     *
     * @param Addr          Address list string of register to be read
     * @param Data          Data to be write or expected
     * @param ptd_Rslt      ParametricTestDescriptor
     *
     *
     * @version 1.0.0
     * @author 308770
     *
     */
    public class MipiData extends ParameterGroup{
        public String Addr = "0x00";
        public String Data = "0x00";
        public IParametricTestDescriptor ptd_Rslt;
    }
    public ParameterGroupCollection<MipiData> mipiDatas = new ParameterGroupCollection<>();

    @Override
    public void setup() {
        try
        {
            // create Setup API instance
            IDeviceSetup ds = DeviceSetupFactory.createInstance();
            if(false == importSpec.isEmpty()) {
                ds.importSpec(importSpec);
            } else {
                ds.importSpec(MeasReadPhyReg.getSpecificationName());
            }

            ISetupProtocolInterface paInterface = ds.addProtocolInterface("TS_Setup1", "setups.mipi.mipi");
            paInterface.addSignalRole("DATA", "SDATA");
            paInterface.addSignalRole("CLK", "SCLK");
            ds.parallelBegin();
            ds.sequentialBegin();
            {
                String pattPath = "setups.vectors.patterns.";
                if(!PatName.equals("")){
                    ds.patternCall(pattPath + PatName);
                }
                Integer itr=0;
                ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup1");
                USID="<"+USID+">";
                for(MipiData perData:mipiDatas.values()){
                    transDigSrc.addParameter(Type.UnsignedLong, Direction.OUT, "value"+itr);

                    String fullTSname = context.getTestSuiteName();
                    String ShortTSname = fullTSname.substring(1 + fullTSname.lastIndexOf("."));
                    if(ShortTSname.contains("GSID"))
                    {
                        USID ="0x0";
                        USID="<"+USID+">";
                        transDigSrc.addTransaction("write",USID,"<"+perData.Addr+">","<"+perData.Data+">");
                        USID ="0x8";
                        USID="<"+USID+">";
                        transDigSrc.addTransaction("read",USID,"<"+perData.Addr+">","value"+itr++);

                    }

                    else
                    {
                        transDigSrc.addTransaction("write",USID,"<"+perData.Addr+">","<"+perData.Data+">");
                        transDigSrc.addTransaction("read",USID,"<"+perData.Addr+">","value"+itr++);

                    }

                }
                readTsCallName=ds.transactionSequenceCall(transDigSrc,"");
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

        Integer counter=0;
        for(MipiData perData:mipiDatas.values()){
            MultiSiteLong value = capResults.get("value"+counter);

            if(JudgeFlag){

                String  _testSuiteName = context.getTestSuiteName();
                String  testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));

                perData.ptd_Rslt.setTestText(testSuiteName+":read_"+perData.Addr+"_"+perData.Data);

                perData.ptd_Rslt.evaluate(value);
            }else{
                println("MIPI:write"+USID+" <"+perData.Addr+"> expect:<"+perData.Data+"> get:"+value);
            }
            counter++;
        }

    }
}
