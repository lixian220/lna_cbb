package src.mipi_tml;

import java.util.Set;
import java.util.TreeSet;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupProtocolInterface;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.measurement.IMeasurement;


/**
*
* This test method performs write action test for MIPI protocol <br>
*
*
* @param importSpec     setup specification name
* @param Parameter      parameter group collection to read setups from testtable
* @param USID           USID for MIPI protocol
*
* @version 1.0.0
* @author 308770
*
*/

public class MIPIWrite_TEST extends TestMethod{

    public IMeasurement MeasReadPhyReg;


    @In public String USID = "0xC";

    public ParameterGroupCollection<MipiData> Parameter = new ParameterGroupCollection<>();



    /**
     * This Class read the setup parameter from testtable to override init value. if the parameter not exist in
     * testtable, it will use the init value.
     *
     *
     * @param importSpec    setup specification for test setup
     * @param Addr          Address list string of register to be read
     * @param Data          Data to be write or expected
     * @param bypassMode    whether to bypass this testsuite
     *
     *
     * @version 1.0.0
     * @author 308770
     *
     */
    public class MipiData extends ParameterGroup{
        public String Addr = "0x00";
        public String Data = "0x00";
        public String importSpec = "";
        public boolean bypassMode = false;
    }


    @Override
    public void setup() {
        try
        {
            IDeviceSetup ds = DeviceSetupFactory.createInstance();

            ISetupProtocolInterface paInterface = ds.addProtocolInterface("TS_Setup1", "setups.mipi.mipi");
            paInterface.addSignalRole("DATA", "SDATA");
            paInterface.addSignalRole("CLK", "SCLK");

            ds.parallelBegin();
            ds.sequentialBegin();
            {
                ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("TS_Setup1");
                USID="<"+USID+">";

                Set<String> key = Parameter.keySet();
                Set<String> sortedKey = new TreeSet<String>();
                for(String _key : key){
                    sortedKey.add(_key);
                }

                for(String _sortedKey : sortedKey){
                    MipiData perData = Parameter.get(_sortedKey);

                    if(perData.bypassMode==true){
                        /*
                         * if bypass items, just do nothing
                         */

                    }else{

                    if(false == perData.importSpec.isEmpty()) {
                        ds.importSpec(perData.importSpec);
                    } else {
                        ds.importSpec(MeasReadPhyReg.getSpecificationName());
                    }


                    transDigSrc.addTransaction("write",USID,"<"+perData.Addr+">","<"+perData.Data+">");

                    }

                }

                ds.transactionSequenceCall(transDigSrc,"");
            }
            ds.sequentialEnd();
            ds.parallelEnd();
            MeasReadPhyReg.setSetups(ds);
        }

        catch (Exception e)
        {
            System.out.println("Something went wrong :-(");
            e.printStackTrace();
        }
    }


    @Override
    public void execute() {


        MeasReadPhyReg.execute();

    }



}
