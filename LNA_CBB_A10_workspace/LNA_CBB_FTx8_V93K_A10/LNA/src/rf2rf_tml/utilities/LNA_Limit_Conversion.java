package src.rf2rf_tml.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.jopendocument.util.FileUtils;

import xoc.dta.TestMethod;
import xoc.dta.annotations.In;
import xoc.dta.workspace.IWorkspace;


public class LNA_Limit_Conversion extends TestMethod {

    public String Limit_Table ="setups/testtable/files/Limit_Conversion_Demo.ods";
    public String Org_ODS = "setups/testtable/files/LNA_RF_FT_Test.ods";
    public String Upd_ODS = "setups/testtable/files/LNA_RF_FT_Test_New.ods";
    public String ODS_Sheet = "Tests";
    @In public String[] Limit_Sheet = {"Gain_HG1","Gain_MLP","Gain_Sweep","IIP3","H3"};


    @SuppressWarnings({ "fallthrough", "incomplete-switch" })
    private Map<List<String>,List<String>> rdTests(Sheet sheet,int LOW,int High){
        int colCount = sheet.getColumnCount();
        int rowCount = sheet.getRowCount();

        int maxRowCount=500;
        int maxColCount=15;

        if(rowCount >maxRowCount){
             sheet.setRowCount(maxRowCount);
            rowCount = maxRowCount;
        }
        if (colCount >maxColCount){
            sheet.setColumnCount(maxColCount);
            colCount = maxColCount;
        }

        Map<List<String>,List<String>> TestMap = new HashMap<>();

        for(int row=2;row<rowCount;row++){

            String cell_Testsuite = "";
            String cell_Testtext = "";
            String cell_FT_Low = "";
            String cell_FT_High = "";

            for(int col=0;col<colCount;col++){
                    MutableCell<SpreadSheet> cell = sheet.getCellAt(col,row);
                    switch (col){
                    case 0:
                        cell_Testsuite = cell.getValue().toString();         //Testsuite name
                    case 3:
                        cell_Testtext = cell.getValue().toString();

                    }//Test text


                    if(col == LOW)
                    {
                        cell_FT_Low = cell.getValue().toString();            // FT low limit

                    }
                    else if(col == High)
                    {

                        cell_FT_High = cell.getValue().toString();           // FT high limit
                    }



            }
            List<String> LimitList = new ArrayList<String>();
            List<String> TestsuiteTesttext = new ArrayList<>();
            LimitList.add(0, cell_FT_Low);//Low limit
            LimitList.add(1,cell_FT_High);//High limit

            if(!cell_Testsuite.isEmpty()&&!cell_Testtext.isEmpty())
            {

                TestsuiteTesttext.add(0,cell_Testsuite);
                TestsuiteTesttext.add(1,cell_Testtext);

                TestMap.put(TestsuiteTesttext, LimitList);
            }
            else
            {
                continue;
            }

        }
        return TestMap;

    }


    @SuppressWarnings({ "fallthrough", "incomplete-switch" })
    private Map<List<String>,List<String>> rdInputTable(String FileName,String[] sheetName)
            throws IOException
    {

        File file = Paths.get(FileName).toFile();

        SpreadSheet document = SpreadSheet.createFromFile(file);

        Map<List<String>,List<String>> InputTableMap = new HashMap<>();


        for(int i = 0;i<sheetName.length;i++){

            Sheet sheet = document.getSheet(sheetName[i]);

            int colCount = sheet.getColumnCount();
            int rowCount = sheet.getRowCount();

            int maxRowCount=400;
            int maxColCount=15;

            if(rowCount >maxRowCount){
                sheet.setRowCount(maxRowCount);
                rowCount = maxRowCount;
            }
            if (colCount >maxColCount){
                sheet.setColumnCount(maxColCount);
                colCount = maxColCount;
            }


            for(int row=1;row<rowCount;row++){

                String cell_Testsuite = "";
                String cell_FT_Low = "";
                String cell_FT_High = "";

                List<String> LimitList = new ArrayList<String>();
                List<String> TestsuiteTesttext = new ArrayList<>();
                for(int col=0;col<colCount;col++){
                    MutableCell<SpreadSheet> cell = sheet.getCellAt(col,row);
                    switch (col){
                    case 0:
                        cell_Testsuite = cell.getValue().toString();
                    case 2:

                        cell_FT_Low = cell.getValue().toString();
                    case 3:
                        cell_FT_High = cell.getValue().toString();

                    }


                }
                LimitList.add(0, cell_FT_Low);
                LimitList.add(1,cell_FT_High);

                String testsuiteName = "";
                String testTextName="";
                if(!cell_Testsuite.isEmpty())
                {
                    List<String> FullTestText = Arrays.asList(cell_Testsuite.split("\\s*_\\s*"));//split testsuite;
                    testsuiteName = cell_Testsuite.substring(0, cell_Testsuite.lastIndexOf(":"));//get testsuite name;
                    if(sheet.getName().contains("Gain")||sheet.getName().contains("LF_Rej"))
                    {
                        testTextName = FullTestText.get(FullTestText.size()-2)+"_"+FullTestText.get(FullTestText.size()-1);//for Gain_HG1...
                    }
                    else
                    {
                        testTextName = FullTestText.get(FullTestText.size()-1);
                    }

                    TestsuiteTesttext.add(0,testsuiteName);
                    TestsuiteTesttext.add(1,testTextName);

                }
                InputTableMap.put(TestsuiteTesttext, LimitList);

                println("INFO---InputTableMap-- :"  +InputTableMap );
            }
        }

        return InputTableMap;

    }


    private void GenerateNewTestMap(Map<List<String>,List<String>> TestMap,Map<List<String>,List<String>> InputTableMap,Map<List<String>,List<String>> NewTestMap){

        Vector<List<String>> testsuiteOfInputTable = new Vector<>();

        for(Entry<List<String>,List<String>> entry:InputTableMap.entrySet())
        {
            testsuiteOfInputTable.add(entry.getKey());
        }
        println("INFO :"+testsuiteOfInputTable);
        NewTestMap.putAll(TestMap);

          for(List<String> keyOfTestMap:TestMap.keySet())
          {
              for(int i =0;i<testsuiteOfInputTable.size();i++)
              {
                  if(keyOfTestMap.get(0).equals(testsuiteOfInputTable.get(i).get(0))&&keyOfTestMap.get(1).equals(testsuiteOfInputTable.get(i).get(1)))
                  {

                          NewTestMap.replace(testsuiteOfInputTable.get(i), InputTableMap.get(testsuiteOfInputTable.get(i)));
                      }


                  else
                  {

                      continue;

                  }

              }

          }

          println("INFO INFO---NewTestMap---------:"+NewTestMap);

           }

    @SuppressWarnings({ "fallthrough", "unused", "incomplete-switch" })
    private void exportFile(Map<List<String>,List<String>> NewTestMap,String FileName,String sheetName,int LOW,int HIGH)
            throws IOException
    {

        File file= Paths.get(FileName).toFile();


        SpreadSheet document = SpreadSheet.createFromFile(file);

        Sheet sheet = document.getSheet(sheetName);

        int colCount = sheet.getColumnCount();
        int rowCount = sheet.getRowCount();

        int maxRowCount=400;
        int maxColCount=20;
        if (rowCount > maxRowCount){
            sheet.setRowCount(maxRowCount);
            rowCount = maxRowCount;
        }
        if (colCount >maxColCount){
            sheet.setColumnCount(maxColCount);
            colCount = maxColCount;
        }
        for(int row = 2;row<rowCount;row++)
        {
            String cell_Testsuite = "";
            String cell_Testtext = "";
                        String cell_FT_Low = "";
                        String cell_FT_High = "";
            for(int col=0;col<colCount;col++){
                MutableCell<SpreadSheet> cell = sheet.getCellAt(col,row);
                switch (col){
                case 0:
                    cell_Testsuite = cell.getValue().toString();

                case 3:
                    cell_Testtext = cell.getValue().toString();
                }
                if(col == LOW)
                {
                    cell_FT_Low = cell.getValue().toString();
                }
                if(col == HIGH)
                {
                    cell_FT_High = cell.getValue().toString();
                }
            }
            List<String> list = new ArrayList<>();
            List<String> listlimit = new ArrayList<>();
            if(!cell_Testsuite.isEmpty()&&!cell_Testtext.isEmpty())
            {


                list.add(0,cell_Testsuite);
                list.add(1,cell_Testtext);
                listlimit.add(0,cell_FT_Low);
                listlimit.add(0,cell_FT_High);
            }
            else
            {
                continue;
            }



            Vector<List<String>> testsuiteOfNewTests = new Vector<>();

            for(Entry<List<String>,List<String>> entry:NewTestMap.entrySet())
            {
                testsuiteOfNewTests.add(entry.getKey());
            }

            for(int i =0;i<testsuiteOfNewTests.size();i++) {

                if(testsuiteOfNewTests.get(i).get(0).equals(cell_Testsuite)&&testsuiteOfNewTests.get(i).get(1).equals(cell_Testtext))
                {
                    Object Low_Limit = (float)(Math.round(Float.parseFloat(NewTestMap.get(testsuiteOfNewTests.get(i)).get(0))*10))/10;
                    Object High_Limit = (float)(Math.round(Float.parseFloat(NewTestMap.get(testsuiteOfNewTests.get(i)).get(1))*10))/10;
                    sheet.setValueAt(Low_Limit, LOW,row);
                    sheet.setValueAt(High_Limit, HIGH,row);
                    MutableCell<SpreadSheet> cell_upd = sheet.getCellAt(4,row);

                }
                else
                {
                        continue;
                             }
            }



        }


        try {
            document.saveAs(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }





    }
    private void copyFile(String oldPath,String destPath)
    {
        File src = new File(oldPath);
        File dest = new File(destPath);
        try {
            FileUtils.copyFile(src, dest);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }




    @SuppressWarnings("unused")
    @Override
    public void execute() {
        context.workspace();
        // TODO Auto-generated method stub
        String PrjDir = IWorkspace.getActiveProjectPath()+"/";
        String LimitTableFile = PrjDir + Limit_Table;

        String Upd_Ods = PrjDir + Upd_ODS;
        String ODS_FILE = PrjDir + Org_ODS;

        File file = Paths.get(LimitTableFile).toFile();
        File file_ods = Paths.get(ODS_FILE).toFile();

        SpreadSheet document_ods = null;
        SpreadSheet document = null;



        try{
            document_ods = SpreadSheet.createFromFile(file_ods);
        }
        catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        @SuppressWarnings("null")
        Sheet sheet_ods = document_ods.getSheet(ODS_Sheet);
        Map<List<String>,List<String>> TestMap = new HashMap<>();
        TestMap = rdTests(sheet_ods,4,6);
        println("The size of MapOrg is "+TestMap.size());


        try{
             document = SpreadSheet.createFromFile(file);

        }
        catch(IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<List<String>,List<String>> NewTestMap = new HashMap<>();

        Map<List<String>,List<String>> InputTableMap = new HashMap<>();


        try {
            InputTableMap = rdInputTable(LimitTableFile, Limit_Sheet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        GenerateNewTestMap(TestMap,InputTableMap,NewTestMap);


        copyFile(ODS_FILE,Upd_Ods);


            try {
                exportFile(NewTestMap,Upd_Ods,ODS_Sheet,4,6);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }




    }

}
