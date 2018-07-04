package src.mipi_tml;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupProtocolInterface;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IMeasurementResult;
import xoc.dta.testdescriptor.IParametricTestDescriptor;


/**
*
* This test method performs readcompare action test for MIPI protocol, it read register and compare with limits like a functional test <br>
*
*
* @param importSpec     setup specification name
* @param JUdgeFlag      Flag indicates whether write results to datalog stream
* @param USID           USID for MIPI protocol
* @param PatName        pattern name called by operation sequencer
*
* @version 1.0.0
* @author 308770
*
*/

public class MIPIReadCompare_TEST extends TestMethod{

    /**
     * IMeasurement
     * */
    public IMeasurement MeasReadPhyReg;


    @In public String importSpec = "";
    @In public boolean JudgeFlag = true;
    @In public String USID = "0xC";
    @In public String PatName="";

    public String readTsCallName = "";


    /**
     * This Class read the setup parameter from testtable to override init value. if the parameter not exist in
     * testtable, it will use the init value.
     * @param Addr          Address list string of register to be read
     * @param Data          Data to be write or expected
     *
     *
     * @version 1.0.0
     *
     */

    public class MipiData extends ParameterGroup{
        public String Addr = "0x00";
        public String Data = "0x00";
    }
    public IParametricTestDescriptor ptd_Rslt;
    public ParameterGroupCollection<MipiData> mipiDatas = new ParameterGroupCollection<>();

    @Override
    public void setup() {
        try
        {
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
                String pattPath = "setups.vectors.patterns."; //input parameter
                if(!PatName.equals("")){

                    ds.patternCall(pattPath + PatName);
                }
                ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup1");
                USID="<"+USID+">";
                for(MipiData perData:mipiDatas.values()){

                    String fullTSname = context.getTestSuiteName();
                    String ShortTSname = fullTSname.substring(1 + fullTSname.lastIndexOf("."));
                    if(ShortTSname.contains("GSID"))
                    {
                        USID ="0x0";
                        USID="<"+USID+">";
                        transDigSrc.addTransaction("write",USID,"<"+perData.Addr+">","<"+perData.Data+">");
                        USID ="0x8";
                        USID="<"+USID+">";
                        transDigSrc.addTransaction("read_Compare",USID,"<"+perData.Addr+">","<"+perData.Data+">");

                    }

                    else
                    {
                        // transDigSrc.addTransaction("write",USID,"<"+perData.Addr+">","<"+perData.Data+">");
                        transDigSrc.addTransaction("read_Compare",USID,"<"+perData.Addr+">","<"+perData.Data+">");

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
        MultiSiteBoolean FuncResults = new MultiSiteBoolean(false);
        MultiSiteDouble  FuncResultDb = new MultiSiteDouble(-1.0);

        IMeasurementResult results = MeasReadPhyReg.preserveResult();

        releaseTester();

        FuncResults = results.hasPassed();
        int[] activeSites = context.getActiveSites();
        for(int site: activeSites)
        {
            if ( FuncResults.get(site).equals(true))
            {
                FuncResultDb.set(site, 1.0);
            }

        }

        if(JudgeFlag) {

            ptd_Rslt.evaluate(FuncResultDb);
        }




    }
}
