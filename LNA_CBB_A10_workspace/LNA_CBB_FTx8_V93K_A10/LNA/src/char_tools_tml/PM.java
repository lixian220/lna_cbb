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
 * This test method performs a Pin Marginal (PM) test with the setup data specified in the calling
 * test suite. It executes multiple functional test at different receive strobes of the digital
 * instruments and prints all test results. So it helps to fix timing setup and get the test suite
 * test with good margin.<br><br>
 *
 * This test output the datalog to console and specified file.
 *
 * @param Tper
 *            specification variable for test period
 * @param Roff
 *            specification variable for timing strobe offset
 * @param Roff_LL
 *            lower boundary for the timing strobe shift
 * @param Roff_UL
 *            upper boundary for the timing strobe shift
 * @param Rpoints
 *            counts of tests
 * @param filePath
 *            PM datalog save path
 * @param mPins
 *            signal name to judge functional test result
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

public class PM extends TestMethod {

    @In public String Tper;
    @In public String Roff;
    @In public Double Roff_LL;
    @In public Double Roff_UL;
    @In public Integer Rpoints;
    @In public String filePath;
    @In public String mPins;


    public IMeasurement measurement;
    public IFunctionalTestDescriptor ftd;


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
            e1.printStackTrace();
        }
        isr = new InputStreamReader(fis);
        br = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();



        double Cur_T_value;
        double Cur_Roff_value;
        int X;
        int Rpoints_1,Rpoints_0;
        double Rtmp=0;
        double Roff_UL_ADJ=0,Roff_LL_ADJ = 0;
        double Rstep = 0;
        MultiSiteBoolean result;

        int[] activeSites = context.getActiveSites();
        for(int site : activeSites)
        {
            println(testSuiteName+"_S"+site+"_PM: ");
            buffer.append(testSuiteName+"_S"+site+"_PM: \n");

            MultiSiteDouble mTcoef = measurement.spec().getDouble(Tper);
            MultiSiteDouble mRoff = measurement.spec().getDouble(Roff);
            Cur_Roff_value = mRoff.get(site);
            Cur_T_value = mTcoef.get(site);


            println("Cur_T_SPEC:"+Tper+"="+Cur_T_value*1E9+"ns");
            buffer.append("Cur_T_SPEC:"+Tper+"="+Cur_T_value*1E9+"ns"+"\n");
            println("Cur_Roff_SPEC:"+Roff+"="+Cur_Roff_value*1E9+"ns");
            buffer.append("Cur_Roff_SPEC:"+Roff+"="+Cur_Roff_value*1E9+"ns"+"\n");

            Rstep=(Roff_UL-Roff_LL)/(Rpoints-1);//ns
            Rpoints_1=(int)(Rpoints*(Roff_UL-Cur_Roff_value*1E9)/(Roff_UL-Roff_LL));
            Rpoints_0=(int)(Rpoints*(Cur_Roff_value*1E9-Roff_LL)/(Roff_UL-Roff_LL));

            Roff_LL_ADJ=Cur_Roff_value*1E9-Rstep*Rpoints_0;
            Roff_UL_ADJ=Cur_Roff_value*1E9+Rstep*Rpoints_1;


            println("Roff_LL_ADJ="+Roff_LL_ADJ+"ns");
            println("Roff_UL_ADJ="+Roff_UL_ADJ+"ns");
            buffer.append("Roff_LL_ADJ="+Roff_LL_ADJ+"ns"+"\n");
            buffer.append("Roff_UL_ADJ="+Roff_UL_ADJ+"ns"+"\n");


            measurement.execute();

            for(X=0;X<Rpoints;X++)
            {
               Rtmp = Roff_LL_ADJ + Rstep*X;
               measurement.spec().setVariable(Roff,Rtmp*1E-9);
               measurement.execute();

               IDigInOutResults Results = measurement.digInOut(mPins).preserveResults(ftd);
               result = Results.hasPassed();

               if(result.get(site))
               {
                   if(Rtmp<=(Cur_Roff_value*1E9+0.0000001)&&Rtmp>=(Cur_Roff_value*1E9-0.0000001))
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
                   if(Rtmp<=(Cur_Roff_value*1E9+0.0000001)&&Rtmp>=(Cur_Roff_value*1E9-0.0000001))
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

            System.out.printf("\t");
            buffer.append("\t");

            println("");
            buffer.append("\n");

            for(X=0;X<Rpoints;X++)
            {
                Rtmp = Roff_LL_ADJ+Rstep*X;
                float Rtmp_f = (float)Math.round(Rtmp*10)/10;
                System.out.printf(Rtmp_f+"\t");
                buffer.append(Rtmp_f+"\t");

            }

            println(" :"+Roff+"(ns)");
            buffer.append(":"+Roff+"(ns)"+"\n"+"\n");



            measurement.spec().setVariable(Roff,mRoff );





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
