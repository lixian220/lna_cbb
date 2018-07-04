package src.reg_tools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupPattern;
import xoc.dta.TestMethod;

/**
*
* This class provide multiple methods to create binary pattern according to user defined protocol sequencer .
*
*
* @author  308770
* @since   1.0.0
*
*
*/

public class CreatePORPatternFromODS extends TestMethod {
    protected static final String MIPI_Pinlist="SCLK+SDATA+USID0";
    protected static final char SCLK_0      =   '0';
    protected static final char SCLK_1      =   '1';
    protected static final char SCLK_clk    =   'P';//check spec.normal
    protected static final char DATA_0      =   '0';
    protected static final char DATA_1      =   '1';
    protected static final char DATA_L      =   'L';
    protected static final char DATA_H      =   'H';
    protected static final char USID_Z      =   'Z';

    /**
     * The {@link create_mipi_write_pattern} interface provide a way to use test method to create pattern
     * According to the sequencer defined inside.
     * In this case, the target defined sequencer id for the <b>WRITE</b> action of MIPI .
     * @param ds   reference to the {@link DeviceSetup}
     * @param addr reference to the address of the register to be written
     * @param data reference to the data need to be written
     * @return ISetupPattern
     */
    protected ISetupPattern create_mipi_write_pattern(IDeviceSetup ds,int addr,int data){
        int a =0;
        int b =0;
        String name=new String();
        name=String.format("MIPI_W_0x%02x_0x%02x", addr,data);
        ISetupPattern pat=ds.createPattern(name, 1, MIPI_Pinlist);
        pat.genVecBegin(5)
           .addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}))    //IDLE
           .genVecEnd();
        pat.addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_1,USID_Z}));   //SSC 1
        pat.addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}));   //SSC 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF SA[3:0] 3
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF SA[3:0] 2
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF SA[3:0] 1
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF SA[3:0] 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 3
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 2
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 1
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF P

        for(int i=7;i>=0;i--){

            if((addr & (1<<i)) != 0){
                pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Addr[7:0] i
                a++;
            }
            else{
                pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Addr[7:0] i
            }
        }
        if(a%2==0)
        {
            pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Addr[7:0] P
        }
        else
        {
            pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Addr[7:0] P
        }

        for(int i=7;i>=0;i--){
            if((data & (1<<i)) != 0){
                pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Data i
                b++;
            }
            else{
                pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Data i
            }
        }
        if(b%2 == 0)
        {
            pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Data[7:0] P
        }
        else
        {
            pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Data[7:0] P
        }

        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Data[7:0] BP

        pat.genVecBegin(5)
           .addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}))
           .genVecEnd();

        return pat;
    }


    /**
     * The {@link create_mipi_expect_pattern} interface provide a way to use test method to create pattern
     * According to the sequencer defined inside class.
     * In this case, the target defined sequencer id for the <b>Expect</b> action of MIPI.<br>
     * In the Expect action, receive action has been translated to dedicated characters defined in the wavetable of timing specification.
     * @param ds   reference to the {@link DeviceSetup}
     * @param addr reference to the address of the register to be written
     * @param data reference to the data need to be written
     * @return ISetupPattern
     */
    protected ISetupPattern create_mipi_expect_pattern(IDeviceSetup ds,int addr,int data){
        int a = 0;
        int b = 0;
        String name=new String();
        name=String.format("MIPI_EV_0x%02x_0x%02x", addr,data);
        ISetupPattern pat=ds.createPattern(name, 1, MIPI_Pinlist);
        pat.genVecBegin(5)
           .addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}))    //IDLE
           .genVecEnd();
        pat.addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_1,USID_Z}));   //SSC 1
        pat.addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}));   //SSC 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF SA[3:0] 3
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF SA[3:0] 2
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF SA[3:0] 1
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF SA[3:0] 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 3
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 2
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 1
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 0
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF P

        for(int i=7;i>=0;i--){

            if((addr & (1<<i)) != 0){
                pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Addr[7:0] i
                a++;
            }
            else{
                pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Addr[7:0] i
            }
        }
        if(a%2 == 0)
        {
            pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Addr[7:0] P
        }
        else
        {
            pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Addr[7:0] P
        }
        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Addr[7:0] BP

        for(int i=7;i>=0;i--){
            if((data & (1<<i)) != 0){
                pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_H,USID_Z}));   //DF Data i
                b++;
            }
            else{
                pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_L,USID_Z}));   //DF Data i
            }
        }

        if(b%2==0)
        {
            pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_H,USID_Z}));   //DF Data[7:0] P
        }
        else
        {
           pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_L,USID_Z}));   //DF Data[7:0] P
        }

        pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Data[7:0] BP

        pat.genVecBegin(5)
           .addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}))
           .genVecEnd();

        return pat;
    }


    /**
     * The {@link create_por_pattern} interface provide a way to use test method to create POR pattern in <b> burst </b> mode format
     * from the inputs of spreadsheet. The burst label calls all generated sub labels.
     * @param ds         reference to the {@link DeviceSetup}
     * @param ods_file   reference to the ODS fine name where inputs come from
     * @param sheet_name reference to the sheet name where inputs come from
     * @return    None
     */

    protected void create_por_pattern(IDeviceSetup ds,String ods_file,String sheet_name){
        File file=new File(context.testProgram().variables().getString("SYS.TESTPROGRAM_DIR").get()+"/"+ods_file);

        try {
            final Sheet sheet=SpreadSheet.createFromFile(file).getSheet(sheet_name);

            String BurstpatternName = "";

            BurstpatternName = sheet_name + "_Write";

            ISetupPattern por_pat=ds.createPattern(BurstpatternName, 1, MIPI_Pinlist);

            int y=sheet.getCurrentRegion(0, 0).getEndPoint().y+1;
            for(int i=2;i<y;i++){
                int addr=Integer.valueOf(sheet.getCellAt(0, i).getTextValue().replaceAll("^0[xX]|[g-zG-Z]", ""),16);
                int data=Integer.valueOf(sheet.getCellAt(1, i).getTextValue().replaceAll("^0[xX]|[g-zG-Z]", ""),16);
                por_pat.addPatternCall(create_mipi_write_pattern(ds,addr,data));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * The {@link create_por_pattern_MAIN} interface provide a way to use test method to create POR pattern in <b> MAIN Pattern </b> format
     * from the inputs of spreadsheet
     * @param ds         reference to the {@link DeviceSetup}
     * @param ods_file   reference to the ODS fine name where inputs come from
     * @param sheet_name reference to the sheet name where inputs come from
     * @return    None
     */
    protected void create_por_pattern_MAIN(IDeviceSetup ds,String ods_file,String sheet_name){
        File file=new File(context.testProgram().variables().getString("SYS.TESTPROGRAM_DIR").get()+"/"+ods_file);

        try {
            final Sheet sheet=SpreadSheet.createFromFile(file).getSheet(sheet_name);

            String BurstpatternName = "";

            BurstpatternName = sheet_name + "_Write";

            ISetupPattern por_pat=ds.createPattern(BurstpatternName, 1, MIPI_Pinlist);

            int y=sheet.getCurrentRegion(0, 0).getEndPoint().y+1;
            for(int i=2;i<y;i++){
                int addr=Integer.valueOf(sheet.getCellAt(0, i).getTextValue().replaceAll("^0[xX]|[g-zG-Z]", ""),16);
                int data=Integer.valueOf(sheet.getCellAt(1, i).getTextValue().replaceAll("^0[xX]|[g-zG-Z]", ""),16);

                int a =0;
                int b =0;
                por_pat.genVecBegin(5)
                   .addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}))    //IDLE
                   .genVecEnd();
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_1,USID_Z}));   //SSC 1
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}));   //SSC 0
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF SA[3:0] 3
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF SA[3:0] 2
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF SA[3:0] 1
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF SA[3:0] 0
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF 0
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 3
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 2
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 1
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //CF BC[3:0] 0
                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //CF P

                for(int m=7;m>=0;m--){

                    if((addr & (1<<m)) != 0){
                        por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Addr[7:0] i
                        a++;
                    }
                    else{
                        por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Addr[7:0] i
                    }
                }
                if(a%2==0)
                {
                    por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Addr[7:0] P
                }
                else
                {
                    por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Addr[7:0] P
                }

                for(int m=7;m>=0;m--){
                    if((data & (1<<m)) != 0){
                        por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Data i
                        b++;
                    }
                    else{
                        por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Data i
                    }
                }
                if(b%2 == 0)
                {
                    por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_1,USID_Z}));   //DF Data[7:0] P
                }
                else
                {
                    por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Data[7:0] P
                }

                por_pat.addVector(String.copyValueOf(new char[] {SCLK_clk   ,DATA_0,USID_Z}));   //DF Data[7:0] BP

                por_pat.genVecBegin(5)
                   .addVector(String.copyValueOf(new char[] {SCLK_0     ,DATA_0,USID_Z}))
                   .genVecEnd();

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * The {@link create_por_read_pattern} interface provide a way to use test method to create POR pattern in <b> burst </b> mode format
     * from the inputs of spreadsheet. The burst label calls all generated sub labels. <br>
     * It calls {@link create_mipi_expect_pattern} function to generated readback action in MIPI protocol
     * @param ds         reference to the {@link DeviceSetup}
     * @param ods_file   reference to the ODS fine name where inputs come from
     * @param sheet_name reference to the sheet name where inputs come from
     * @return    None
     */

    protected void create_por_read_pattern(IDeviceSetup ds,String ods_file,String sheet_name){
        File file=new File(context.testProgram().variables().getString("SYS.TESTPROGRAM_DIR").get()+"/"+ods_file);

        try {
            final Sheet sheet=SpreadSheet.createFromFile(file).getSheet(sheet_name);
            String BurstpatternName = "";
            BurstpatternName = sheet_name + "_Read";
            ISetupPattern por_pat=ds.createPattern(BurstpatternName, 1, MIPI_Pinlist);

            int y=sheet.getCurrentRegion(0, 0).getEndPoint().y+1;
            for(int i=2;i<y;i++){
                int addr=Integer.valueOf(sheet.getCellAt(0, i).getTextValue().replaceAll("^0[xX]|[g-zG-Z]", ""),16);
                int data=Integer.valueOf(sheet.getCellAt(1, i).getTextValue().replaceAll("^0[xX]|[g-zG-Z]", ""),16);
                por_pat.addPatternCall(create_mipi_expect_pattern(ds,addr,data));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String ODSFileName;
    public List<String> POR_Sheets;

    @Override
    public void setup() {
        // Setup API to create a burst - create Setup API instance.

        IDeviceSetup deviceSetup = DeviceSetupFactory.createNamedInstance("RF_PRO");
        for(String sht:POR_Sheets){

            if(sht.contains("Read_POR15dB"))
            {
                create_por_read_pattern(deviceSetup,ODSFileName,sht);
            }
            else
            {
                create_por_pattern_MAIN(deviceSetup,ODSFileName,sht);
            }

        }
    }


    @Override
    public void execute() {
        println("Do Nothing!");
    }
}
