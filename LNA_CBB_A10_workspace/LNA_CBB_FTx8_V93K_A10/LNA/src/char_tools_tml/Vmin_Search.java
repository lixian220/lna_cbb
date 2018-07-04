package src.char_tools_tml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;

import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDigInOutResults;
import xoc.dta.resultaccess.IProtocolInterfaceResults;
import xoc.dta.resultaccess.ITransactionSequenceResults;
import xoc.dta.testdescriptor.IFunctionalTestDescriptor;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
 *
 * This test method performs a minimum Voltage search test with the setup data specified in the calling test
 * suite. It realizes by two steps:
 * <ol>
 *      <li> execute one-dimension shmoo test (<b>Coarse search </b>) regarding to specified Voltage, and find the transition point of pass and fail. </li>
 *      <li> execute a second shmoo test with smaller steps (<b>Fine search </b>) around the transition point and find a more detail transition point   </li>
 * </ol>
 * This test output the datalog to console and specified file.
 *
 * @param Vcoef
 *            specification variable for DPS Voltage
 * @param Vcoef_LL
 *            lower boundary for the DPS Voltage shift
 * @param Vcoef_UL
 *            upper boundary for the DPS Voltage shift
 * @param Vpoints
 *            counts of DPS Voltage shifts
 * @param Vclamp_EN
 *            Flag to turn on/off Voltage Clmap for DPS
 * @param filePath
 *            PM datalog save path
 * @param signals
 *            signal name to judge functional test result
 *
 * @param measurement
 *            measurement server interface
 * @param ftd
 *            Functional test descriptor
 * @param ptd
 *            parametric test descriptor
 *
 * @author  308770
 * @since   1.0.0
 *
 *
 */

public class Vmin_Search extends TestMethod {

    @In public String Vcoef;
    @In public Double Vcoef_UL;
    @In public Double Vcoef_LL;
    @In public Integer Vpoints;
    @In public Integer Vclamp_EN;
    @In public String filePath;

    @In public String Period;

    @In public String signals = "";

    public IMeasurement measurement;
    public IFunctionalTestDescriptor ftd;
    public IParametricTestDescriptor ptd;




    public static void print(Object o){
        System.out.print(o);
    }

    @SuppressWarnings("unused")
    private static String path = "/tmp/CHAR_LOG/";
    public  boolean createFile(String fileName,String filecontent)
    {
        Boolean bool = false;
        String filenameTemp = filePath;
        File file = new File(filenameTemp);

        try {
            if(!file.exists())
            {
                file.createNewFile();
                bool = true;
                System.out.println("Success create file, the file is "+filenameTemp);
                writeFileContent(filenameTemp,filecontent);
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return bool;
    }


    public boolean writeFileContent(String filepath,String newstr)throws  java.io.IOException{
        Boolean bool = false;
        String filein = newstr+"\r\n";
        String temp = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos = null;
        PrintWriter pw = null;

        try{
            File file =new File(filepath);
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();

            for(@SuppressWarnings("unused")
            int i = 0;(temp=br.readLine())!=null;i++)
            {
                buffer.append(temp);
                buffer = buffer.append(System.getProperty("line.sperator"));
            }
            buffer.append(filein);

            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);

            pw.write(buffer.toString().toCharArray());
            pw.flush();
            bool = true;
        }

        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(pw != null)
            {
                pw.close();
            }
            if(fos !=null)
            {
                fos.close();
            }
            if(br !=null)
            {
                br.close();
            }
            if(isr !=null)
            {
                isr.close();
            }
            if(fis !=null)
            {
                fis.close();
            }
        }
        return bool;
    }

    @SuppressWarnings("unused")
    public MultiSiteBoolean JudgeMIPI(IProtocolInterfaceResults mipiRslt,double limit ){



        int[] activeSites = context.getActiveSites();
        MultiSiteBoolean Judgerst = new MultiSiteBoolean();
        ITransactionSequenceResults tsr= mipiRslt.transactSeq(readTsCallName)[0];
        Map<String, MultiSiteLong> capResults = tsr.getValueAsLong();
        for(int site: activeSites)
        {

        String[] valueNames = capResults.keySet().toArray(new String[0]);

        Integer counter=0;

        MultiSiteLong value = capResults.get("value"+counter);

        if(value.get(site) == limit)
        {

                Judgerst.set(site, true);
        }else
        {
                Judgerst.set(site, false);
        }
            counter++;


        }
        return Judgerst;

    }



    @In public String PatName="";
    @In public String USID = "0xC";

    private String readTsCallName = "";
    @Override
    public void setup(){


    }
    @SuppressWarnings("resource")
    @Override
    public void execute() {
        String _testSuiteName = context.getTestSuiteName();
        String testSuiteName =_testSuiteName.substring(1 + _testSuiteName.lastIndexOf("."));

        String filenameTemp = filePath;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        FileOutputStream fos = null;
        PrintWriter pw = null;
        File file =new File(filenameTemp);
        if(!file.exists())
        {
            try
            {
                 file.createNewFile();
            }
                    catch(IOException e)
            {
                        e.printStackTrace();
            }
        }
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        isr = new InputStreamReader(fis);
        br = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();

        println(testSuiteName+"_Vmin_Vmax: \n");
        buffer.append(testSuiteName+"_Vmin_Vmax: \n");

        println("Pattern : "+measurement.getPatternName());
        buffer.append("Pattern : "+measurement.getPatternName());

        double Cur_V_value;
        double Vcoef_UL_ADJ,Vcoef_LL_ADJ;
        double Vmin_LL,Vmin_UL,Vmax_LL,Vmax_UL;
        double DPS_Value;
        MultiSiteDouble Vmin = new MultiSiteDouble(0.0);
        MultiSiteDouble Vmax= new MultiSiteDouble(0.0);
        double Vstep,Vmin_step,Vmax_step;
        int X;
        int F_flag=0,P_flag=0;
        int All_Fail=0;
        MultiSiteBoolean bool;


        int[] activeSites = context.getActiveSites();
        for(int site: activeSites)
        {

          MultiSiteDouble mVcoef = measurement.spec().getDouble(Vcoef);
          if(mVcoef.get(site)==1)
          {
              println("Cur_V_SPEC_value : "+mVcoef+"*100%");
              buffer.append("Cur_V_SPEC_value : "+mVcoef+"*100%"+"\n");

          }
          else
          {
              println("Cur_V_SPEC_value : "+mVcoef+"v");
              buffer.append("Cur_V_SPEC_value : "+mVcoef+"v"+"\n");
          }

          MultiSiteDouble mPeriod = measurement.spec().getDouble(Period);
          println("Cur_T_SPEC_value : "+mPeriod+"ns"+"\n");
          buffer.append("Cur_T_SPEC_value : "+mPeriod+"ns"+"\n");

          Cur_V_value = mVcoef.get(site);
          Vstep = Cur_V_value*(Vcoef_UL-Vcoef_LL)/(Vpoints-1);
          Vcoef_LL_ADJ = Cur_V_value*Vcoef_LL;
          Vcoef_UL_ADJ = Cur_V_value*Vcoef_UL;

          Vmin_LL = Vcoef_LL_ADJ;
          Vmin_UL  = Vcoef_LL_ADJ;
          Vmax_LL = Vcoef_UL_ADJ;
          Vmax_UL = Vcoef_UL_ADJ;


          if(Cur_V_value!=1&&Vclamp_EN==1)
          {
              println("###############################################");
              println("Warning!!!!!");
              println("   Cur_V_value !=1");

          }

          if(Vmax_UL>5.0)
          {
              println("#############################################");
              println("Warining!!!!!");
              println("The Vmax_UL="+Vmax_UL+" "+" , >5V ");
          }

        measurement.execute();



        if(Vcoef_UL<=1.5&&Vcoef_LL>0.3||0==Vclamp_EN)
        {
            for(X=0;X<Vpoints;X++)
            {
                DPS_Value = Vcoef_LL_ADJ+Vstep*X;

                measurement.spec().setVariable("Vcoef", DPS_Value);

                measurement.execute();
                IDigInOutResults result = measurement.digInOut(signals).preserveResults(ftd);
                bool = result.hasPassed();
                if(bool.get(site))
                {
                   P_flag++;

                   System.out.printf("P\t");
                   buffer.append("P\t");


                   if(F_flag>0)
                   {
                      Vmin_LL = DPS_Value-Vstep;//Vmin_LL is Vmin serarch Low limit
                      Vmin_UL = DPS_Value;//Vmin_UL is Vmin serarch Up limit
                      F_flag=0;
                   }
                }

                else
                {
                    F_flag++;
                    All_Fail++;
                    System.out.printf(".\t");
                    buffer.append(".\t");

                    if(P_flag>0)
                    {
                        Vmax_LL = DPS_Value - Vstep;//Vmax_LL is Vmax serarch Low limit
                        Vmax_UL= DPS_Value ;//Vmax_UL is Vmax serarch Up limit
                        P_flag =0 ;
                    }
                }

            }
            println("\n");
            buffer.append("\n");


            for(X=0;X<Vpoints;X++)
            {
                DPS_Value = Vcoef_LL_ADJ+Vstep*X;
                System.out.printf("%2.3f\t",DPS_Value);
                DecimalFormat df = new DecimalFormat("0.000\t");
                String Temp = String.valueOf(df.format(DPS_Value));
                buffer.append(Temp);
            }
            if(1 == Cur_V_value)
            {
                println(" : "+Vcoef+"(%v)"+"\n");
                buffer.append(" : "+Vcoef+"(%v)"+"\n");

            }
            else{
                println(" : "+Vcoef+"(v)"+"\n");
                buffer.append(" : "+Vcoef+"(v)"+"\n");
            }

            if(All_Fail == Vpoints)
            {
                Vmin_UL=0;
                Vmin_LL = 0;
                Vmax_LL=0;
                Vmax_UL=0;
                Vmin.set(0);
                Vmax.set(0);
                println("All_Fail, can not process Vmin/Vmax search!"+"\n");
                buffer.append("All_Fail, can not process Vmin/Vmax search!"+"\n");
            }
            else
            {
                if((Vmin_UL<=Vcoef_LL_ADJ)&&Vmin_UL>0)
                {
                    DecimalFormat df = new DecimalFormat("0.000\t");
                    Vcoef_LL_ADJ = Double.valueOf(df.format(Vcoef_LL_ADJ));
                    Vmin.set(Vcoef_LL_ADJ);
                }
                else
                {
                    Vmin_UL = Vmin_UL+0.2*Vstep;
                    Vmin_LL = Vmin_LL-0.2*Vstep;
                    Vmin_step= (Vmin_UL-Vmin_LL)/(Vpoints-1);
                    P_flag = 0;
                    for(X=0;X<Vpoints;X++)
                    {
                        DPS_Value = Vmin_LL+Vmin_step*X;
                        measurement.spec().setVariable("Vcoef", DPS_Value);

                        measurement.execute();
                        IDigInOutResults result = measurement.digInOut(signals).preserveResults(ftd);
                        bool = result.hasPassed();
                        if(bool.get(site))
                        {
                            P_flag++;
                             System.out.print("P\t");
                             buffer.append("P\t");
                            if(1 == P_flag)
                            {

                                DecimalFormat df = new DecimalFormat("0.000\t");
                                DPS_Value = Double.valueOf(df.format(DPS_Value));
                                Vmin.set(DPS_Value);
                            }
                        }
                        else
                        {
                             System.out.print(".\t");
                             buffer.append(".\t");
                        }
                    }
                    println("\n");
                    buffer.append("\n");
                    for(X=0;X<Vpoints;X++)
                    {
                        DPS_Value=Vmin_LL+Vmin_step*X;
                        System.out.printf("%2.3f\t",DPS_Value);
                        DecimalFormat df = new DecimalFormat("0.000\t");
                        String Temp = String.valueOf(df.format(DPS_Value));
                        buffer.append(Temp);
                    }

                    if(1 == Cur_V_value)
                    {
                        println(" : "+"Vmin(%v)"+"\n");
                        buffer.append(" : "+"Vmin(%v)"+"\n");

                    }
                    else
                    {
                        println(" : "+"Vmin(v)"+"\n");
                        buffer.append(" : "+"Vmin(v)"+"\n");
                    }
                }

                if(Vmax_LL>=Vcoef_UL_ADJ)
                {
                    DecimalFormat df = new DecimalFormat("0.000\t");
                    Vcoef_UL_ADJ = Double.valueOf(df.format(Vcoef_UL_ADJ));
                    Vmax.set(Vcoef_UL_ADJ);
                }
                else{
                    Vmax_UL = Vmax_UL+0.2*Vstep;
                    Vmax_LL = Vmax_LL-0.2*Vstep;
                    DecimalFormat df = new DecimalFormat("0.000\t");
                    Vmax_UL = Double.valueOf(df.format(Vmax_UL));
                    Vmax.set(Vmax_UL);

                    Vmax_step = (Vmax_UL-Vmax_LL)/(Vpoints-1);
                    F_flag=0;

                    for(X=0;X<Vpoints;X++)
                    {
                        DPS_Value = Vmax_LL+Vmax_step*X;
                        measurement.spec().setVariable("Vcoef", DPS_Value);

                        measurement.execute();
                        IDigInOutResults result = measurement.digInOut(signals).preserveResults(ftd);
                        bool = result.hasPassed();
                        if(bool.get(site))
                        {
                            P_flag++;
                             print("P"+"\t");
                             buffer.append("P"+"\t");

                        }
                        else
                        {
                            F_flag++;
                             print("."+"\t");
                             buffer.append("."+"\t");
                            if(1==F_flag)
                            {
                               Vmax.set(Double.valueOf(df.format(DPS_Value-Vmax_step)));
                            }
                        }

                    }
                    println("\n");

                    for(X=0;X<Vpoints;X++)
                    {
                        DPS_Value= Vmax_LL+Vmax_step*X;

                        if(1==Cur_V_value)
                        {
                            System.out.printf("%2.3f\t", DPS_Value);

                            String Temp = String.valueOf(df.format(DPS_Value));
                            buffer.append(Temp);

                        }
                        else{
                            System.out.printf("%2.3f\t", DPS_Value);

                            String Temp = String.valueOf(df.format(DPS_Value));
                            buffer.append(Temp);
                        }
                    }

                    if(1 == Cur_V_value)
                    {
                        println(" : "+"Vmax(%v)"+"\n");
                        buffer.append(" : "+"Vmax(%v)"+"\n");

                    }
                    else{
                        println(" : "+"Vmax(v)"+"\n");
                        buffer.append(" : "+"Vmax(v)"+"\n");
                    }
                }
            }

            if((Vmin_UL<=Vcoef_LL_ADJ)&&Vmin_UL>0)
            {
                println("\n");
                buffer.append("\n");
            }
            if(Vmax_LL>=Vcoef_UL_ADJ)
            {
                println("\n");
                buffer.append("\n");
            }

            println("Vmin = "+Vmin+"\n");
            buffer.append("Vmin = "+Vmin+"\n");
            println("Vmax = "+Vmax+"\n");
            buffer.append("Vmax = "+Vmax+"\n");


        }
        else{
            println("ERROR !!!!!!");
        }

        ptd.setTestText(testSuiteName+": Voltage_min");
        ptd.evaluate(Vmin.multiply(1.8));


        measurement.spec().setVariable("Vcoef",1.0 );



    }
        try {
            fos = new FileOutputStream(file,true);
            pw = new PrintWriter(fos,true);

            pw.write(buffer.toString().toCharArray());
            pw.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            if(pw != null)
            {
                pw.close();
            }
            if(fos !=null)
            {
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(br !=null)
            {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(isr !=null)
            {
                try {
                    isr.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(fis !=null)
            {
                try {
                    fis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return;



    }

}
