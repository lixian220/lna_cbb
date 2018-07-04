package src.rf2rf_tml;

import java.util.Map;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupDcVI.IVforce;
import xoc.dsa.ISetupProtocolInterface;
import xoc.dsa.ISetupTransactionSeqDef;
import xoc.dsa.ISetupTransactionSeqDef.Direction;
import xoc.dsa.ISetupTransactionSeqDef.Type;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IProtocolInterfaceResults;
import xoc.dta.resultaccess.ITransactionSequenceResults;
import xoc.dta.testdescriptor.IFunctionalTestDescriptor;
import xoc.dta.testdescriptor.IParametricTestDescriptor;


/**
 * This Test method performs settling time test for MIPI protocol.
 * It measure the time from powerup to MIPI write action take effect.
 *
 *
 *
 * @since 1.0.0
 * @author 308770
 *
 */

public class PORSettlingTime_TEST extends TestMethod {


    public IMeasurement measReadPhyReg;
    String lastAddr= "";
    String lastData="";
    Long lastDataInt = 0L;
    Integer readbackCount =100;
    private String readTsCallName = "";


    /**
     * This Class read the setup parameter from testtable to override init value. if the parameter not exist in
     * testtable, it will use the init value.
     *
     * @param Addr  Address for register
     * @param Data  Data for register
     *
     *
     * @author 308770
     *
     */
    public class MipiData extends ParameterGroup{
        public String Addr = "0x00";
        public String Data = "0x00";
        public IParametricTestDescriptor ptd_Rslt;
    }
    public ParameterGroupCollection<MipiData> mipiDatas = new ParameterGroupCollection<>();

    public IFunctionalTestDescriptor ftd_func;
    public IParametricTestDescriptor ftd;



    @Override
    public void setup() {

      IDeviceSetup  ds = DeviceSetupFactory.createInstance();

      ds.importSpec("specs.POWERUP.por_settling_Read");
    ISetupProtocolInterface paInterface = ds.addProtocolInterface("por_settlingtime", "MIPI.mipi");
      paInterface.addSignalRole("DATA", "SDATA");
      paInterface.addSignalRole("CLK", "SCLK");
      ISetupTransactionSeqDef transDigSrc= paInterface.addTransactionSequenceDef("por_settlingtime");

      lastAddr = "0x48";
      lastData = "0x0F";
      lastDataInt=15L;

      ds.sequentialBegin();{
          ds.insertOpSeqComment("disconnect");
          IVforce vfAction = ds.addDcVI("VDDIO+VDD1P8").vforce();
          vfAction.setForceValue(0.0).setIrange(50e-3 ).setIclamp(50e-3);
          ds.actionCall(vfAction);

          ds.parallelBegin();{

              ds.sequentialBegin();{
                  ds.insertOpSeqComment("connect");
                  IVforce vfconnect = ds.addDcVI("VDDIO+VDD1P8").vforce();
                  vfconnect.setForceValue(1.8).setIrange(50e-3 ).setIclamp(50e-3);
                  ds.waitCall(100e-6);//prepare settling time
                  ds.actionCall(vfconnect);
              }ds.sequentialEnd();
              ds.sequentialBegin();{
                  ds.insertOpSeqComment("MIPI read write");

                  for (int itr =0; itr<readbackCount; itr++)
                  {
                      transDigSrc.addTransaction("write", "<0xC>", "<0x48>", "<0x0F>");
                      transDigSrc.addParameter(Type.UnsignedLong, Direction.OUT, "value"+itr);
                      transDigSrc.addTransaction("read","<0xC>","<0x48>","value"+itr);

                  }

                  readTsCallName=ds.transactionSequenceCall(transDigSrc,"");
              }ds.sequentialEnd();

          }ds.parallelEnd();
      }ds.sequentialEnd();

      measReadPhyReg.setSetups(ds);


    }


    @Override
    public void execute() {
        // TODO Auto-generated method stub

        measReadPhyReg.execute();

        MultiSiteDouble por_settlingtime =new MultiSiteDouble(0.0);
        MultiSiteLong   por_readOK_index =new MultiSiteLong(1);

        IProtocolInterfaceResults mipiRslt1= measReadPhyReg.protocolInterface("MIPI").preserveResults();
        ITransactionSequenceResults tsr1= mipiRslt1.transactSeq(readTsCallName)[0];

        Map<String, MultiSiteLong> capResults1 = tsr1.getValueAsLong();


        int[] activeSites = context.getActiveSites();
        for(int site: activeSites)
        {
            for(Integer counter=0; counter< readbackCount; counter++)
            {
                MultiSiteLong value = capResults1.get("value"+counter);

                if(lastDataInt.equals(value.get(site)))
                {

                    por_readOK_index.set(site, counter+1);
                    por_settlingtime.set(site, 57 + por_readOK_index.get(site)*2.08);
                    counter = readbackCount;
                    break;
                }
            }

        }


        ftd.evaluate(por_settlingtime.divide(1e6));

    }

}
