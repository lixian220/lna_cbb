package src.char_tools_tml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDigInOutResults;
import xoc.dta.testdescriptor.IFunctionalTestDescriptor;



/**
 *
 * This test method performs a Shmoo (SHM) test with the setup data specified in the calling test
 * suite. It executes multiple functional test at different conditions of two dimension variables
 * and prints all test results. So it helps to report device performance regarding to the two
 * dimension variables .<br>
 * <br>
 *
 * <b>In this test method, it mainly to test the operation frequency of MIPI readback actions. </b>
 * <br>
 * This test output the datalog to console and specified file.
 *
 * @param Tper
 *            specification variable for test period
 * @param Vcoef
 *            specification variable for DPS Voltage
 * @param Tcoef_LL
 *            lower boundary for the test period shift
 * @param Tcoef_UL
 *            upper boundary for the test period shift
 * @param Vcoef_LL
 *            lower boundary for the DPS Voltage shift
 * @param Vcoef_UL
 *            upper boundary for the DPS Voltage shift
 * @param Tpoints
 *            counts of test period shifts
 * @param Vpoints
 *            counts of DPS Voltage shifts
 * @param Vclamp_EN
 *            Flag to turn on/off Voltage Clmap for DPS
 * @param filePath
 *            PM datalog save path
 * @param mPins
 *            signal name to judge functional test result
 *
 *
 * @param measurement
 *            measurement server interface
 * @param ftd
 *            Functional test descriptor
 *
 *
 * @author 308770
 * @since 1.0.0
 *
 *
 */


public class SHM_R extends TestMethod {

    @In public String  Tper;
    @In public String  Vcoef;
    @In public Double  Tcoef_UL;
    @In public Double  Tcoef_LL;
    @In public Double  Vcoef_UL;
    @In public Double  Vcoef_LL;
    @In public Integer Tpoints;
    @In public Integer Vpoints;
    @In public Integer Vclamp_EN;
    @In public String  filePath;
    @In public String  mPin;


    public IMeasurement measurement;
    public IFunctionalTestDescriptor ftd;

    @SuppressWarnings("unused")
    private static String path = "/tmp/CHAR_LOG/";

    @SuppressWarnings({ "unused", "resource" })
    @Override
    public void execute() {
        // TODO Auto-generated method stub


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


    double Cur_T_value;
    double Cur_V_value;
    int X,Y;
    int Tpoints_1,Tpoints_0;
    int Vpoints_1,Vpoints_0;
    double period,DPS_Value;
    double Vcoef_UL_ADJ,Vcoef_LL_ADJ;
    double T_UL_ADJ,T_LL_ADJ,Vmax;
    double Vstep = 0,Tstep=0;
    MultiSiteBoolean result;
    char tmp_v[];
    char tmp_t[];

    int[] activeSites = context.getActiveSites();
    for(int site: activeSites)
    {
        println(testSuiteName+"_S"+site+"_SHM: ");
        buffer.append(testSuiteName+"_S"+site+"_SHM: \n");

        println(measurement.getPatternName());
        buffer.append(measurement.getPatternName()+" \n");


        MultiSiteDouble mVcoef = measurement.spec().getDouble(Vcoef);
        MultiSiteDouble mTcoef = measurement.spec().getDouble(Tper);
        Cur_V_value = mVcoef.get(site);
        Cur_T_value = mTcoef.get(site);

        println("Cur_T_SPEC:"+Tper+"="+Cur_T_value*1E9+"ns");
        buffer.append("Cur_T_SPEC:"+Tper+"="+Cur_T_value*1E9+"ns"+"\n");

        if(1==Cur_V_value)
        {
            println("Cur_V_SPEC:"+Vcoef+"="+Cur_V_value+"*100%");
            buffer.append("Cur_V_SPEC:"+Vcoef+"="+Cur_V_value+"*100%"+"\n");
        }
        else
        {
            println("Cur_V_SPEC:"+Vcoef+"="+Cur_V_value+"v");
            buffer.append("Cur_V_SPEC:"+Vcoef+"="+Cur_V_value+"v"+"\n");
        }


        if(1 == Tpoints )
        {
            Tstep = 0;
        }
        else
        {
            Tstep = Cur_T_value*(Tcoef_UL-Tcoef_LL)/(Tpoints-1);
        }

        if(1 == Vpoints )
        {Vstep=0;}
        else
        {
            Vstep=Cur_V_value*(Vcoef_UL-Vcoef_LL)/(Vpoints-1);
        }

        Tpoints_1=(int)(Tpoints*(Tcoef_UL-1)/(Tcoef_UL-Tcoef_LL));
        Tpoints_0=(int)(Tpoints*(1-Tcoef_LL)/(Tcoef_UL-Tcoef_LL));
        Vpoints_1=(int)(Vpoints*(Vcoef_UL-1)/(Vcoef_UL-Vcoef_LL));
        Vpoints_0=(int)(Vpoints*(1-Vcoef_LL)/(Vcoef_UL-Vcoef_LL));

        T_LL_ADJ=Cur_T_value-Tstep*Tpoints_0;
        T_UL_ADJ=Cur_T_value+Tstep*Tpoints_1;
        Vcoef_LL_ADJ=Cur_V_value-Vstep*Vpoints_0;
        Vcoef_UL_ADJ=Cur_V_value+Vstep*Vpoints_1;
        Vmax=Vcoef_UL_ADJ;

        println("T_LL_ADJ="+T_LL_ADJ*1E9+"ns");
        println("T_UL_ADJ="+T_UL_ADJ*1E9+"ns");
        println("Vcoef_LL_ADJ="+Vcoef_LL_ADJ);
        println("Vcoef_UL_ADJ="+Vcoef_UL_ADJ);

        buffer.append("T_LL_ADJ="+T_LL_ADJ*1E9+"ns"+"\n");
        buffer.append("T_UL_ADJ="+T_UL_ADJ*1E9+"ns"+"\n");
        buffer.append("Vcoef_LL_ADJ="+Vcoef_LL_ADJ+"\n");
        buffer.append("Vcoef_UL_ADJ="+Vcoef_UL_ADJ+"\n");

        if(Cur_V_value !=1 && 1 == Vclamp_EN)
        {
            println("###########################################");
            println("  Warning!!!!!");
            println("   Cur_V_value!=1");
            println("   Step1: Please make sure if there are some abnormal errors which not restore the spec in current or front testsuite!");
            println("          If yes, you should repair the error and download level&timing spec!");
            println("   Step2: if you make sure the Vcoef spec is absolutely Power value and there no Tester system error,");
            println("         Pls download the timing&spec,and  switch the Vclamp_EN=0,then continue and be careful!!");
            println("###########################################");
            return;
        }

        if(Vmax>5)
        {
            println("###########################################");
            println("   Warning!!!!!");
            println("   The Vmax="+Vmax+" "+" , >5V ");
            println("   Please make sure if it is safe!  ");
            println("   If yes,pls modify the TestMethod specially!");
            println("###########################################");
            return;
        }

        println(Tper+"(nS)\t");
        buffer.append(Tper+"(nS)\t"+"\n");

        measurement.execute();

        if(Vcoef_UL<=1.5&&Vcoef_LL>=0.5||0==Vclamp_EN)
        {
            for(Y=0;Y<Tpoints;Y++)
            {
               DPS_Value = Cur_V_value;
               measurement.spec().setVariable(Vcoef, DPS_Value);
               measurement.execute();
               period = T_UL_ADJ-Tstep*Y;
               measurement.spec().setVariable(Tper, period);


               float period_f = (float)Math.round(period*1E9*10)/10;
               System.out.printf(period_f+"\t");
               tmp_t = Double.toString(period_f).toCharArray();
               buffer.append(period_f+"\t");

               for(X=0;X<Vpoints;X++)
               {
                   DPS_Value=Vcoef_LL_ADJ+Vstep*X;
                   measurement.spec().setVariable(Vcoef, DPS_Value);
                   measurement.execute();

                   IDigInOutResults Results = measurement.digInOut(mPin).preserveResults(ftd);
                   result = Results.hasPassed();

                   if(result.get(site))
                   {
                       if(DPS_Value<=(Cur_V_value+0.000001)&&DPS_Value>=(Cur_V_value-0.000001)&&period*1E9<=(Cur_T_value*1E9+0.0000001)&&period*1E9>=(Cur_T_value*1E9-0.0000001))
                       {
                           System.out.printf("*\t");
                           buffer.append("*\t");
                       }
                       else
                       {
                           System.out.printf("P\t");
                           buffer.append("P\t");
                       }
                   }
                   else
                   {
                       if(DPS_Value<=(Cur_V_value+0.000001)&&DPS_Value>=(Cur_V_value-0.000001)&&period*1E9<=(Cur_T_value*1E9+0.0000001)&&period*1E9>=(Cur_T_value*1E9-0.0000001))
                       {
                           System.out.printf("#\t");
                           buffer.append("#\t");
                       }
                       else
                       {
                           System.out.printf(".\t");
                           buffer.append(".\t");
                       }
                   }


               }
               println("\n");
               buffer.append("\n");

            }
            System.out.printf("\t");
            buffer.append("\t");

            for(X=0;X<Vpoints;X++)
            {
                DPS_Value = Vcoef_LL_ADJ+Vstep*X;
                float DPS_Value_F = (float)Math.round(DPS_Value*100)/100;
                System.out.printf(DPS_Value_F+"\t");
                tmp_v = Double.toString(DPS_Value_F).toCharArray();
                buffer.append(DPS_Value_F+"\t");

            }

            println(" :"+Vcoef+"("+Cur_V_value+"*%v)"+"\n");
            buffer.append(":"+Vcoef+"("+Cur_V_value+"*%v)"+"\n"+"\n");


        }
        else
        {
            println("###############################");
            println("Warning!!!!!");
            println("The Vcoef_UL or Vcoef_LL is out of Range(0.5,1.5) ");
            println("Please check the level set,the Vcoef should be coefficent of Voltage");
            println("If you make sure Vcoef is safety, pls change the Vclamp_EN=0,then continue");
            println("###############################");
        }

        measurement.spec().setVariable(Vcoef,mVcoef );
        measurement.spec().setVariable(Tper,mTcoef );




    }//end loop

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
