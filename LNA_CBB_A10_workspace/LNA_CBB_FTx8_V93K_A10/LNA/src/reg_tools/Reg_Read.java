package src.reg_tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import xoc.dta.datatypes.MultiSiteBoolean;


/**
*
* This class provide register read utility functions to finish read register from ODS sheet
*
*
* @author  308770
* @since   1.0.0
*
*
*/

public class Reg_Read {



    public Reg_Read(){ }


    public Map<String,String> m_RegDataMap = null;
    private static class RegDataInstance {
        private static final Reg_Read INSTANCE = new Reg_Read();
    }
    public static Reg_Read getInstance (){
        return RegDataInstance.INSTANCE;
    }


    public void init(String fileName,String sheetName,int[] activeSites,MultiSiteBoolean offline){
        if(m_RegDataMap == null)
        {
            m_RegDataMap = readRegDataFromFile(fileName,new ArrayList<String>(Arrays.asList(sheetName.split("\\s*;\\s*"))),activeSites);
        }


    }


    /**
     * This method is used to read register from ODS sheet and store it in designed data structures
     *
     * @param fileName      reference to the ODS file name
     * @param rdShtNameAry  reference to the sheet name string list to be read from ODS file name
     * @param activeSites   activated sites information
     *
     */
    private Map<String,String> readRegDataFromFile(String fileName,ArrayList<String> rdShtNameAry,int[] activeSites){
        Map<String,String> RegDataMap = new TreeMap<>();

        File file = new File(fileName);
        if(!file.exists())
        {
            System.out.println("Error! Exception in reading from file: "+fileName);
            return RegDataMap;
        }
        SpreadSheet document = null;
        try{
            document = SpreadSheet.createFromFile(file);
        }catch(IOException e){
            System.out.println("Error! Exception in reading from file: "+fileName);
            e.printStackTrace();
        }
        if(document == null)
        {
            return RegDataMap;
        }
        int count = document.getSheetCount();

        for(int shtID = 2;shtID<count;shtID++)//except Tests,Profile
        {
            Sheet sheet = document.getSheet(shtID);
            String shtName = sheet.getName();
            System.out.println("Read sheet Name: "+shtName);
            if(rdShtNameAry.contains(shtName))
            {
               RegDataMap = rdSheet(RegDataMap,sheet,activeSites);

            }

        }
        return RegDataMap;

    }


    /**
     * This method is used to detail read method  to read register from ODS file
     *
     * @param RegDataMap    reference to key of register
     * @param sheet         reference to the single sheet name in the ODS file name
     * @param activeSites   activated sites information
     *
     */
    private Map<String,String> rdSheet(Map<String,String> RegDataMap, Sheet sheet,int[] activeSites){
        String shtName = sheet.getName();
        Map<Integer, String> PerRegData = new LinkedHashMap<>();

        int colCount = sheet.getColumnCount();//lie
        int rowCount = sheet.getRowCount(); //hang

        int maxRowCount = 1000;//need be changed!!!!!
        int maxColCount = 50;

        if(rowCount>maxRowCount)
        {
            sheet.setRowCount(maxRowCount);
            rowCount = maxRowCount;
        }
        if(colCount > maxColCount)
        {
            sheet.setColumnCount(maxRowCount);
            colCount = maxColCount;
        }

        for(int row=1;row<rowCount;row++){

            ArrayList<String> valuePerRow = new ArrayList<>();
            for(int col=0;col<colCount;col++)
            {
                MutableCell<SpreadSheet> cell = sheet.getCellAt(col,row);
                String cellValue = cell.getValue().toString();
                valuePerRow.add(col,cellValue);//index---->value (Testsuite , RegType)
            }
            if(valuePerRow.get(0).equals(""))
            {
                continue;
            }
            if(shtName.contains("POR")&&row>1)
            {

                PerRegData = readPorData(valuePerRow,PerRegData,shtName,row,activeSites);
            }
            else if(shtName.contains("Common"))
            {
                RegDataMap = readComData(valuePerRow,RegDataMap,shtName,row,activeSites);

            }



        }//end loop
        int size = PerRegData.size();
        String Sum = "";
        for(int i=2;i<size+2;i++)
        {
            if(PerRegData.containsKey(i)&&!PerRegData.get(i).isEmpty())
            {
                Sum = Sum+PerRegData.get(i);

            }
            else
            {
                continue;
            }

        }
        if(shtName.contains("POR"))
        {

            RegDataMap.put(shtName, Sum);


        }
        return RegDataMap;




    }




    /**
     * This method is used to detail read method  to read POR register
     *
     * @param valuePerRow   reference to key of register
     * @param sheet         reference to the single sheet name in the ODS file name
     * @param activeSites   activated sites information
     *
     */


    private Map<Integer,String> readPorData(ArrayList<String> valuePerRow,Map<Integer,String> PerRegData,String shtName, int row,int[]activeSites)
    {
        Integer Index = row;
        String PerAddress = valuePerRow.get(0);
        String PerData = valuePerRow.get(1);
        String Addr_Data = PerAddress+"_"+PerData+";";
        if(!PerRegData.containsKey(Index))
        {
            if(Index.toString() != null&&PerData!=null&&PerAddress!=null)
            {
                PerRegData.put(Index, Addr_Data);
            }
        }

        return PerRegData;
    }


    /**
     * This method is used to detail read method  to read None_POR register
     *
     * @param valuePerRow   reference to key of register
     * @param sheet         reference to the single sheet name in the ODS file name
     * @param activeSites   activated sites information
     *
     */

    private Map<String,String> readComData(ArrayList<String> valuePerRow,Map<String,String> RegDataMap,String shtName, int row,int[]activeSites)
    {
        String testsuite =String.valueOf(valuePerRow.get(0));
        String RegType = String.valueOf(valuePerRow.get(1));
        String Reg_Data =String.valueOf(valuePerRow.get(2));
        String Reg_Key = testsuite+"_"+RegType;
        if(!RegDataMap.containsKey(Reg_Key))
        {
            if(testsuite!=null&&RegType!=null)
            {
               RegDataMap.put(Reg_Key,Reg_Data);
            }
            else
            {
                System.out.println("ERROR: pls add testsuite and RegType in your ods file");
            }
        }
        return RegDataMap;

    }


    /**
     * This method is used to detail read method  to read None_POR register
     *
     * @param Reg_Key       reference to key of register
     *
     * @throws Exception
     *             If reg data is not exist
     *
     */

    public String getValue(String Reg_Key)
    {

        if(m_RegDataMap == null)
        {
            System.out.println("ERROR!!!! Reg Data Map is Empty");
        }
        else if(m_RegDataMap.isEmpty())
        {
            System.out.println("ERROR!!!! Reg Data Map is Empty");
        }
        else if(m_RegDataMap.containsKey(Reg_Key))
        {
            return m_RegDataMap.get(Reg_Key);
        }
        System.out.println("ERROR!!!! no value found for :testsuite "+Reg_Key);

        return null;
    }








}
