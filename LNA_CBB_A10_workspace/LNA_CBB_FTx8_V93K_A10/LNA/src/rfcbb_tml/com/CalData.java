package src.rfcbb_tml.com;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;

//@SuppressWarnings("restriction")

/**
 *
 * this class defines a new data class to store data and provide methods to write data to file and read data from file
 *
 *
 *
 *
 * @since 1.0.0
 * @author 308770
 *
 */
public class CalData {


    /**
     * class RowDataMap is a defined data container to store row data of calibration file.
     * Row data is like this: <br>
     * 2     HB1_IN1_MHB1_OUT_B41    2593        2.66  2.71 2.64 2.68 2.49 2.33 2.35  2.31  <br> <br>
     *
     * 2 is the LBID, (HB1_IN1_MHB1_OUT_B41,2593) is the key, others are MultisiteDdouble Data <br>
     *
     * Also, lots fo methods are defined to read/update value from data container write data container value to file.
     *
     * @since 1.0.0
     * @author 308770
     *
     */
    public class RowDataMap {
        public Map<Key,MultiSiteDouble> calMap=new TreeMap<Key,MultiSiteDouble>( new Comparator<Key>(){// NF_eff,InLoss,TotalLoss->MSD
            @Override
            public int compare (Key o1,Key o2)
            {
                int ret;
                ret = o1.item.compareTo(o2.item);

                if(ret ==0)
                {
                    ret = o1.freq.compareTo(o2.freq);
                }

                return ret;

            }
        });

        /**
         * this method is used to update value for specified key in arguments
         *
         * @param itemName      item name of the Key
         * @param freq          frequency of the key
         * @param valuePerRow   value in string
         * @param activeSites   activeSites
         * @return updated Row data
         *
         * @since  1.0.0
         */

        public RowDataMap setValuePerRow (String itemName, Double freq, ArrayList<String> valuePerRow, int[] activeSites){
            MultiSiteDouble tmpValue= new MultiSiteDouble();
            if(itemName.contains("golden")){
                tmpValue.set(Double.valueOf(valuePerRow.get(1)));
//                for(int site : activeSites)
//                {
//                   Double value = new Double(0.0);
//                   if(valuePerRow.get(site).equals(""))
//                   {
//                       value = 0.0;
//                       System.out.println("ERROR: Gloden Value is null!!!");
//                   }
//                   else
//                   {
//                       value = Double.valueOf(valuePerRow.get(site));
//                   }
//                   tmpValue.set(site,value);
//                }
            }else if(   itemName.contains("TotalLoss")    || itemName.contains("OutLoss")    ){
                for(int site : activeSites){
                    Double value = new Double(0.0);
                    if(valuePerRow.get(site+2).equals("")){
                        value = 0.0;
                    }else{
                        value = Double.valueOf(valuePerRow.get(site+2));
                    }
                    tmpValue.set(site, value );
                }
            }else{
                for(int site : activeSites){
                    Double value = new Double(0.0);
                    if(valuePerRow.get(site+1).equals("")){
                        value = 0.0;
                    }else{
                        value = Double.valueOf(valuePerRow.get(site+1));
                    }
                    tmpValue.set(site, value );
                }
            }
            Key tmpKey = new Key(itemName,freq);
            this.calMap.put(tmpKey,tmpValue);
            return this;
        }
    }
    public Integer m_currentLB = -99;
    public Map<Key_Port,RowDataMap > m_CalDataMap = null;

    public CalData(){ }

    private static class CalDataInstance {
        private static final CalData INSTANCE = new CalData();
        private static final CalData FixedInstance = new CalData();
    }

    public static CalData getInstance (){
        return CalDataInstance.INSTANCE;
    }
    public static CalData getFixedInstance(){
        return CalDataInstance.FixedInstance;
    }

    /**
     * read data from file to data Container
     *
     * @param fileName          ODS file name
     * @param sheetName         string contains lots of sheets stores cal data
     * @param activeSites       activeSites
     * @param offline           Flag indicates offline or online
     *
     * @since 1.0.0
     */
    public void init(String fileName,String sheetName, int[] activeSites,MultiSiteBoolean offline) {
        if(m_CalDataMap==null){
            m_CalDataMap = readCalDataFromFile(fileName, new ArrayList<String>(Arrays.asList(sheetName.split("\\s*;\\s*"))), activeSites);

        }
    }

    /**
     * write data in data container to ODS file
     *
     * @param fileName  file name
     * @param offline   Flag indicates offline or online
     *
     *
     * @since 1.0.0
     */

    public void export(String fileName,MultiSiteBoolean offline) {
        writeCalDataFromFile(fileName);

    }
    //read from .ods file only data with matched dutBoardId, and assign to the data structure: (use JopenDocument.. jar)
    // dutBoardId, path, port, mode, band, subband, site1, site2, ...site8
    // 1, RX, MRX_LB1, 3G, 26, 0,   0.5, 1.2, 0, 3.1, 0, 0, 0, 0
    // 3, TX, TX_LB1,  2G, 5,  0,   1.84, 1.26, 1.69,  1.71

    private void println(String string) {
        // TODO Auto-generated method stub
        System.out.println(string);
    }


    /**
     * read data from file to data container object
     *
     * @param fileName          ODS file name
     * @param rdShtNameAry      List of Calibration sheets to be read
     * @param activeSites       activeSites
     *
     * @return a map of perRow data
     *
     * @since 1.0.0
     */
    // row &column start from 0 in this class
    //Note:!!! No blank rows or columns allowed in .ods file
    //##### Bug in JopenDocument.jar!!! sheet.getCOlumnCount() sometime doesn't give correct columns, but 1024.
    private Map<Key_Port,RowDataMap > readCalDataFromFile(String fileName, ArrayList<String> rdShtNameAry, int[] activeSites) {
        Map<Key_Port,RowDataMap > calDataMap = new TreeMap<Key_Port,RowDataMap >(
                new Comparator<Key_Port>(){

                    @Override
                    public int compare (Key_Port o1,Key_Port o2)
                    {

                        int ret = o1.lbid - o2.lbid;

                        if(ret ==0)
                        {
                            ret = o1.test.compareTo(o2.test);
                        }

                        return ret;


                    }

                });



        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Error! Exception in reading from file: "+fileName);
            return calDataMap;
        }
        SpreadSheet document = null;
        try{
            document = SpreadSheet.createFromFile(file);
        }catch (IOException e) {
            System.out.println("Error! Exception in reading from file: "+fileName);
            e.printStackTrace();
        }
        if (document == null) {
            return calDataMap;
        }
        int count = document.getSheetCount();

        for(int shtID=0;shtID<count;shtID++){
            Sheet sheet = document.getSheet(shtID);
            String shtName = sheet.getName();
            println("Read sheet Name:"+shtName);
            if(rdShtNameAry.contains(shtName)){
                calDataMap=rdSheet(calDataMap,sheet,activeSites);
                //                calDataMap.putAll(rdSheet(calDataMap,sheet,activeSites));
            }
        }//for per sheet

        return calDataMap;
    }





    /**
     * read data from file to data container object
     *
     * @param valuePerRow     read string of row data
     * @param calDataMap      data container store golden value row data
     * @param shtName         sheet name for golden sheets
     * @param row             roe count number
     * @param activeSites     activeSites
     *
     * @return a map of perRow data for golden
     *
     * @since 1.0.0
     */
    private Map<Key_Port,RowDataMap> readGoldenData(ArrayList<String> valuePerRow, Map<Key_Port,RowDataMap > calDataMap,String shtName, int row,int[]activeSites){
        int lbid = 0;
        Double freq = 0.0;

        String fullName = valuePerRow.get(0);
        String nl[] = fullName.split("\\s*_\\s*");
        String test = String.join("_", Arrays.copyOfRange(nl, 0, 5));
        String item = shtName+"_"+nl[5];//Golden_Gain+"_HP"

        RowDataMap tmpData = new RowDataMap();
        tmpData.setValuePerRow(item, freq,valuePerRow, activeSites);
        Key_Port key_Port = new Key_Port(lbid,test);

        if(calDataMap.containsKey(key_Port)){
            RowDataMap existData = calDataMap.get(key_Port);
            Key key_item = new Key(item,freq);
            if(existData.calMap.containsKey(key_item)){
                println("ERROR!!!!Duplicate Setting in sht:"+item+" row:"+row+" :"+test+"_"+freq);
            }else{
                existData.setValuePerRow(item, freq, valuePerRow,activeSites);
            }
        }else{
            calDataMap.put(key_Port, tmpData);
        }
        return calDataMap;
    }

    /**
     * read data from file to data container object
     *
     * @param valuePerRow     read string of row data
     * @param calDataMap      data container store golden value row data
     * @param shtName         sheet name for golden sheets
     * @param row             roe count number
     * @param activeSites     activeSites
     *
     * @return a map of perRow data for calibration
     *
     * @since 1.0.0
     */
    private Map<Key_Port,RowDataMap> readCalData(ArrayList<String> valuePerRow, Map<Key_Port,RowDataMap > calDataMap,String shtName, int row,int[]activeSites){
        int lbid = Long.valueOf(valuePerRow.get(0)).intValue();
        String test = valuePerRow.get(1);
        String item = shtName;
        Double freq = 0.0;

        if( item.contains("OutLoss") )   {
            freq = Double.valueOf(valuePerRow.get(2)).doubleValue();
        }

        RowDataMap tmpData = new RowDataMap();
        tmpData.setValuePerRow(item, freq,valuePerRow, activeSites);
        Key_Port key_Port = new Key_Port(lbid,test);
        //        println("init: "+lbid+":"+test+":"+shtName+":"+freq);

        if(calDataMap.containsKey(key_Port)){
            RowDataMap existData = calDataMap.get(key_Port);
            Key key_item = new Key(item,freq);
            if(existData.calMap.containsKey(key_item)){
                println("ERROR!!!!Duplicate Setting in sht:"+item+" row:"+row+" :"+test+"_"+freq);
            }else{
                existData.setValuePerRow(item, freq, valuePerRow,activeSites);
            }
        }else{
            calDataMap.put(key_Port, tmpData);
        }
        return calDataMap;
    }


    /**
     *
     * read data from sheets (cal/golden) and store data in data container class
     *
     * @param calDataMap    data container class stores read data
     * @param sheet         sheets where data from
     * @param activeSites   activeSites
     *
     * @return  a map of perRow data for calibration/golden
     *
     * @since 1.0.0
     */
    private Map<Key_Port,RowDataMap > rdSheet(Map<Key_Port,RowDataMap > calDataMap,Sheet sheet,int[] activeSites){
        String shtName = sheet.getName();

        int colCount = sheet.getColumnCount();
        int rowCount = sheet.getRowCount();

        int maxRowCount=52;
        int maxColCount=15;
        //                //a workaround to avoid the bug/issue
        if(rowCount >maxRowCount){
//             System.out.println("Attention! rowCount: "+ rowCount +", exceeds maxRowCount: " + maxRowCount +" in pathLossFile! Pls double check.");
            sheet.setRowCount(maxRowCount);
            rowCount = maxRowCount;
//             System.out.println("Now Temporarily set rowCount= "+ rowCount);
        }

        if (colCount >maxColCount){
            // System.out.println("Attention! ColCount: "+ colCount +", exceeds maxColCount: " + maxColCount +" in pathLossFile! Pls double check.");
            sheet.setColumnCount(maxColCount);
            colCount = maxColCount;
            // System.out.println("Now Temporarily set colCount= "+ colCount);
        }
        for(int row=1; row < (rowCount/*-1*/); row++){ //omit first row  //TODO: // todo: correct bugs, that last row will be read
//                        println("Read Row:"+row);
            ArrayList<String> valuePerRow= new ArrayList<>();
            for(int col=0; col < colCount; col++){
                MutableCell<SpreadSheet> cell = sheet.getCellAt(col,row);
//                                println("::"+cell.getValue());
                String cellValue = cell.getValue().toString();
                //

                valuePerRow.add(col, cellValue);
            }//each col

            if(valuePerRow.get(0).equals("")){
                continue;
            }
            if(shtName.contains("golden")){ //"golden_Gain":golden_NF
                calDataMap = readGoldenData(valuePerRow,calDataMap,shtName,row,activeSites);
            }else if(shtName.contains("cal_")){//cal_NFeff":cal_InLoss;cal_TotalLoss
                //                println("Row:"+row);
                calDataMap = readCalData(valuePerRow,calDataMap,shtName,row,activeSites);
            }
        }//each row
        return calDataMap;
    }

    @SuppressWarnings("unused")
    private void wtODS_mem(String fileName){

        SpreadSheet document = SpreadSheet.create(1,1,1);
        Sheet sheet = document.getSheet(0);
        sheet.setName("Tests");
        Sheet softwareBinSheet = document.addSheet("SoftWare_Bins");

        // exception if col/row index out of range
        softwareBinSheet.setColumnCount(10);
        softwareBinSheet.setRowCount(100);

        MutableCell<SpreadSheet> cell = softwareBinSheet.getCellAt(0,0);
        cell.setValue("SoftBin");

        try{
            document.saveAs(new File(fileName));
        }catch(IOException e){
            e.printStackTrace();
        }

    }



    private void writeCalDataFromFile(String fileName) {
        @SuppressWarnings("unused")
        Map<Key,RowDataMap > calDataMap = new TreeMap<Key,RowDataMap >();


        try{
            File file= Paths.get(fileName).toFile();
            SpreadSheet document = SpreadSheet.createFromFile(file);
            document.saveAs(file);
        }catch (IOException e) {
            System.out.println("Error! Exception in reading from file: "+fileName);
            e.printStackTrace();
        }


        for(Map.Entry<Key_Port, RowDataMap> entry:m_CalDataMap.entrySet()){
            entry.getKey();
            entry.getValue();
        }
    }


    /**
     * Update data in dataContainer
     *
     * @param lbid      LBID
     * @param test      item name for Key of data container
     * @param shtName   sheetname
     * @param freq      frequency for key of data container
     * @param calData   Multisite data to be write
     *
     *
     * @since 1.0.0
     */

    public void setValue(int lbid, String test, String shtName, Double freq, MultiSiteDouble calData){
        Key_Port key_Port = new Key_Port(lbid,test);
        if(m_CalDataMap==null){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else if(m_CalDataMap.isEmpty()){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else if (m_CalDataMap.containsKey(key_Port)){
            Key key_Item=new Key(shtName,freq);
            if(m_CalDataMap.get(key_Port).calMap.containsKey(key_Item)) {
                m_CalDataMap.get(key_Port).calMap.get(key_Item).set(calData);
            }
        }
        println("ERROR!!!!no value found for:"+test+"_"+shtName+"_"+freq);
    }

    /**
     *
     * Access to specified data in data container
     *
     * @param test  item name for Key of data container
     * @param item  sheet name
     * @param freq  frequency for key of data container
     *
     * @return MultiSiteDouble(0)
     */
    public MultiSiteDouble getValue(String test,String item, Double freq){
        Integer lbid=RF_UserCommon.getDUT_ID();   //this.m_currentLB;
        return getValue(lbid,test,item, freq);
    }
    public MultiSiteDouble getValue(int lbid,String test,String item, Double freq){
        Key_Port tmpKey_Port = new Key_Port(lbid,test);
        if(m_CalDataMap==null){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else if(m_CalDataMap.isEmpty()){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else if (m_CalDataMap.containsKey(tmpKey_Port)){
            Key tmpKey_item = new Key(item,freq);
            if(m_CalDataMap.get(tmpKey_Port).calMap.containsKey(tmpKey_item)) {
//                println("INFO: value found for:PB"+lbid+":"+test+":"+item+":"+freq);
                return m_CalDataMap.get(tmpKey_Port).calMap.get(tmpKey_item);
            }
        }
        println("ERROR!!!!no value found for:PB"+lbid+":"+test+":"+item+":"+freq);
        return new MultiSiteDouble(0);
    }


    /**
     *
     * print All Calibration data to console
     *
     * @since 1.0.0
     */
    public void showAllCalData(){
        println("show all list-----------------");
        if(m_CalDataMap==null){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else if(m_CalDataMap.isEmpty()){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else{
            for(Key_Port key_port:m_CalDataMap.keySet()){
                if (m_CalDataMap.containsKey(key_port)){

                    for(Key key:m_CalDataMap.get(key_port).calMap.keySet()){
                        println("PB"+key_port.lbid+":"+key_port.test+":"+key.item+":"+key.freq+":"+m_CalDataMap.get(key_port).calMap.get(key));
                    }
                }

            }
        }
        println("show all list-----------------");
        //        println("ERROR!!!!no value found for:"+test+"_"+shtName+"_"+freq);
    }
}



