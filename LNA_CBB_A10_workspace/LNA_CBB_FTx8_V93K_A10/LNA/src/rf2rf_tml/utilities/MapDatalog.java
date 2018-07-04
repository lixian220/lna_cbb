package src.rf2rf_tml.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import xoc.dta.datatypes.MultiSiteDouble;

/**
 * This class defines a new class {@code Key} to combine item (testsuite) and freq to identify data in data container {@code MapData}.
 *
 *
 * @since  1.1.0
 * @author 308770
 *
 */



//@SuppressWarnings("restriction")
class Key {
    public final  String item;
    public final  Double freq;

/**
 *
 * @param item    testsuite name for pathloss
 * @param freq    frequency for pathloss
 *
 * @since 1.0.0
 */
    public Key(String item, Double freq) {
        this.item = item;
        this.freq = freq;
    }

    /**
     * this method provide the equal method of the defined class {@code Key}
     *
     * @o         objects of the reference
     * @return    Pass or Fail of the equal() action
     *
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Key)) {
            return false;
        }
        Key key = (Key) o;
        return freq.equals( key.freq) && item.equals(key.item);
    }



    @Override
    public int hashCode() {
        int result = freq.intValue()+ item.length()*31;
        return result;
    }

    /**
     * this method gets Frequency of pathloss
     *
     * @return frequency
     */
    public Double getFreq(){
        return this.freq;
    }


    /**
     * this method get testsuite name of pathloss
     * @return testsuite name
     */
    public String getItem(){
        return this.item;
    }


    /**
     * this method provide the compare method of the defined class {@code Key}
     *
     * @o         objects of the reference
     * @return    0 (same) or 1(different) of the compare() action
     *
     * @since 1.0.0
     */
    public int compareTo(Key o){
        int ret = this.item.compareTo(o.item);
        if(ret ==0)
        {
            ret = this.freq.compareTo(o.freq);
        }

        return ret;
    }
}

public class MapDatalog {
    private Map<String,ItemDataMap > m_MapDatalog = new TreeMap<String ,ItemDataMap>();
    public Integer m_PBID = -99;


    public class ItemDataMap {
        public Map<Key,List<MultiSiteDouble> > itemMap=new TreeMap<Key ,List<MultiSiteDouble>>(new Comparator<Key>(){

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

        public ItemDataMap setValue(String itemName, MultiSiteDouble datalog){
            Double freqDefault=0.0;
            return setValue(itemName,freqDefault,datalog);
        }
        public ItemDataMap setValue(String itemName, Double Freq, MultiSiteDouble datalog){
            Key key=new Key(itemName,Freq);
            if(itemMap.containsKey(key)){
                this.itemMap.get(key).add(datalog);
            }else{
                List<MultiSiteDouble> tmpAry = new ArrayList<MultiSiteDouble>();
                tmpAry.add(datalog);
                this.itemMap.put(key,tmpAry);
            }
            return this;
        }
    }

    public MapDatalog(){}
    public static class MapDatalogInstance {
        private static final MapDatalog INSTANCE = new MapDatalog();
    }
    public static MapDatalog getInstance (){
        return MapDatalogInstance.INSTANCE;
    }
    public void init() {
        m_MapDatalog.clear();
    }
    private void println(String string) {
        // TODO Auto-generated method stub
        System.out.println(string);
    }


    @SuppressWarnings("unused")
    private void wtODS_mem(String fileName){

        SpreadSheet document = SpreadSheet.create(1,1,1);
        Sheet sheet = document.getSheet(0);
        sheet.setName("Tests");
        Sheet softwareBinSheet = document.addSheet("SoftWare_Bins");

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
    public void export(String fileName, int[] activeSites) {

        if(this.m_PBID<=0||this.m_MapDatalog==null||this.m_MapDatalog.isEmpty()){
            println("not init data map");
            return;
        }
        this.showAllCalData();
        try{
            File file= Paths.get(fileName).toFile();
            SpreadSheet document = SpreadSheet.createFromFile(file);
            //   File file = new File(fileName);
            //            if (!file.exists()) {
            //                System.out.println("Error! Exception in reading from file: "+fileName);
            //                //                return calDataMap;
            //            }
            //            SpreadSheet document = null;
            //            try{
            //                document = SpreadSheet.createFromFile(file);
            //            }catch (IOException e) {
            //                System.out.println("Error! Exception in reading from file: "+fileName);
            //                e.printStackTrace();
            //            }
            //            Sheet sheet_InLoss = document.getSheet("cal_InLoss");
            //            Sheet sheet_TotalLoss = document.getSheet("cal_TotalLoss");
            writeCalData2File(document,"cal_NFeff",activeSites);
          writeCalData2File(document,"cal_InLoss",activeSites);
          writeCalData2File(document,"cal_OutLoss",activeSites);
          writeCalData2File(document,"cal_OutLossL",activeSites);



            document.saveAs(file);
        }catch (IOException e) {
            System.out.println("Error! Exception in reading from file: "+fileName);
            e.printStackTrace();
        }
    }




    public boolean hasKey(String testSuite, String item){
        return hasKey(testSuite,item,0.0);
    }
    public boolean hasKey(String testSuite, String item,Double freq){
        if(m_MapDatalog.containsKey(testSuite)){
            if(m_MapDatalog.get(testSuite).itemMap.containsKey(new Key(item,freq))){
                return true;
            }
        }
        return false;
    }

    public int addAB(int a,int b){
        return a+b;
    }
    public int add1(int a){
        return a+1;
    }
    @SuppressWarnings("unused")
    private void sampleMap(){
        List<Integer> aaaa=new ArrayList<Integer>(5);
        aaaa.set(0, 1);
        aaaa.set(1, 3);
        aaaa.set(2, 5);
        aaaa.set(3, 7);
        aaaa.set(4, 9);
        Stream<Integer> abc= aaaa.stream();
        Stream<Integer> stmAdd1 = abc.map(new Function<Integer,Integer>() {
            @Override
            public Integer apply(Integer t){
                return t+1;
            }
        });

    }
    public MultiSiteDouble getMeanValue(String testSuite, String item, int[] activeSites){
        return getMeanValue(testSuite,item,0.0,activeSites);
    }
    public MultiSiteDouble getMeanValue(String testSuite, String item, Double freq, int[] activeSites){
        MultiSiteDouble mean =new MultiSiteDouble(0.0);

        if(this.hasKey(testSuite, item,freq)){
            List<MultiSiteDouble> ary = m_MapDatalog.get(testSuite).itemMap.get(new Key(item,freq));
            MultiSiteDouble sum= new MultiSiteDouble(0.0);

            for(MultiSiteDouble itr: ary){
                MultiSiteDouble tmpValue = itr;
                println("INFO : "+testSuite+": "+item+tmpValue+"\n");
                for(int itrSite:activeSites){
                    if(tmpValue.get(itrSite)==null||Math.abs(tmpValue.get(itrSite))>10.0){
                        tmpValue.set(itrSite, 0.0);
                    }
                }

                sum = sum.add(tmpValue);

            }
            mean= sum.divide((double)ary.size());



//            for (int i =1; i<ary.size(); i++)
//            {
//                MultiSiteDouble tmpValue = ary.get(i);
//                for(int itrSite:activeSites){
//                    if(tmpValue.get(itrSite).toString().equals("NaN")){
//                        tmpValue.set(itrSite, 0.0);
//                    }
//                }
//                sum = sum.add(tmpValue);
//            }
//            mean= sum.divide((double)ary.size()-1);




        }
        return mean;
    }



    private void writeCalData2File(SpreadSheet document, String sheetName , int[] activeSites) {
        Sheet sheet = document.getSheet(sheetName);
        int colCount = sheet.getColumnCount();
        int rowCount = sheet.getRowCount();

        int maxRowCount=400;
        int maxColCount=20;


        Integer startRow = 0;
        Integer stopRow = 0;

        //                //a workaround to avoid the bug/issue
        if(rowCount >maxRowCount){
            //  System.out.println("Attention! rowCount: "+ rowCount +", exceeds maxRowCount: " + maxRowCount +" in pathLossFile! Pls double check.");
            sheet.setRowCount(maxRowCount);
            rowCount = maxRowCount;
            //System.out.println("Now Temporarily set rowCount= "+ rowCount);
        }

        if (colCount >maxColCount){
            // System.out.println("Attention! ColCount: "+ colCount +", exceeds maxColCount: " + maxColCount +" in pathLossFile! Pls double check.");
            sheet.setColumnCount(maxColCount);
            colCount = maxColCount;
            //System.out.println("Now Temporarily set colCount= "+ colCount);
        }

        {//1st row
            Integer itrCol=0;
            sheet.setValueAt("LB", itrCol++, 0);
            sheet.setValueAt("Test", itrCol++, 0);
            if(sheetName.contains("TotalLoss") || sheetName.contains("OutLoss")){
                sheet.setValueAt("Frequency", itrCol++, 0);
            }
            sheet.setValueAt("Site1", itrCol++, 0);
            sheet.setValueAt("Site2", itrCol++, 0);
            sheet.setValueAt("Site3", itrCol++, 0);
            sheet.setValueAt("Site4", itrCol++, 0);
            sheet.setValueAt("Site5", itrCol++, 0);
            sheet.setValueAt("Site6", itrCol++, 0);
            sheet.setValueAt("Site7", itrCol++, 0);
            sheet.setValueAt("Site8", itrCol++, 0);
        }
        // write ODS
        if(m_MapDatalog==null){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else if(m_MapDatalog.isEmpty()){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else{
            Integer itrRow=1;
            boolean chkLBID = true;


            if(chkLBID)
            {//search LBID position
                while(itrRow<rowCount){
                    // println("ROW"+itrRow+" LBID:("+sheet.getValueAt(0, itrRow).toString() +")");//col & row
                    if (sheet.getValueAt(0,itrRow).toString().isEmpty())
                    {
                        break;
                    }
                    Integer tmpLBID=Integer.valueOf( sheet.getValueAt(0,itrRow).toString());
                    if(tmpLBID.equals(this.m_PBID)) {
                        break;
                    }
                    itrRow++;
                }


                //if exist row length = target row length
                startRow = itrRow;
                while(itrRow<rowCount){
                    if (sheet.getValueAt(0,itrRow).toString().isEmpty())
                    {
                        break;
                    }

                    Integer tmpLBID=Integer.valueOf( sheet.getValueAt(0,itrRow).toString());
                    if(tmpLBID.equals(this.m_PBID)) {
                        itrRow++;
                    } else {
                        break;
                    }
                }
                stopRow = itrRow;


                if(!startRow.equals(stopRow) )
                {
                    itrRow = startRow;
                }
            }
            for(String testSuite:m_MapDatalog.keySet()){
                for(Key tmpKey : m_MapDatalog.get(testSuite).itemMap.keySet()){
                    if(tmpKey.getItem().equals(sheetName)){
                        Integer itrCol=0;
                        sheet.setValueAt(m_PBID, itrCol++, itrRow);
                        sheet.setValueAt(testSuite, itrCol++, itrRow);
                        if(sheetName.contains("TotalLoss") || sheetName.contains("OutLoss")){

                            sheet.setValueAt(tmpKey.getFreq(), itrCol++, itrRow); // freq??
                        }
                        MultiSiteDouble value = this.getMeanValue(testSuite,sheetName,tmpKey.getFreq(),activeSites);

                        sheet.setValueAt(value.get(1), itrCol++, itrRow);
                        sheet.setValueAt(value.get(2), itrCol++, itrRow);
                        sheet.setValueAt(value.get(3), itrCol++, itrRow);
                        sheet.setValueAt(value.get(4), itrCol++, itrRow);
                        sheet.setValueAt(value.get(5), itrCol++, itrRow);
                        sheet.setValueAt(value.get(6), itrCol++, itrRow);
                        sheet.setValueAt(value.get(7), itrCol++, itrRow);
                        sheet.setValueAt(value.get(8), itrCol++, itrRow);
                        itrRow++;
                    }
                }
            }
        }


    }

  //without LBID check
    @SuppressWarnings("unused")
    private void writeCalData2File_org(SpreadSheet document, String sheetName , int[] activeSites) {
        Sheet sheet = document.getSheet(sheetName);
        int colCount = sheet.getColumnCount();
        int rowCount = sheet.getRowCount();

        int maxRowCount=400;
        int maxColCount=20;
        //                //a workaround to avoid the bug/issue
        if(rowCount >maxRowCount){
            //  System.out.println("Attention! rowCount: "+ rowCount +", exceeds maxRowCount: " + maxRowCount +" in pathLossFile! Pls double check.");
            sheet.setRowCount(maxRowCount);
            rowCount = maxRowCount;
            //System.out.println("Now Temporarily set rowCount= "+ rowCount);
        }

        if (colCount >maxColCount){
            // System.out.println("Attention! ColCount: "+ colCount +", exceeds maxColCount: " + maxColCount +" in pathLossFile! Pls double check.");
            sheet.setColumnCount(maxColCount);
            colCount = maxColCount;
            //System.out.println("Now Temporarily set colCount= "+ colCount);
        }

        {//1st row
            Integer itrCol=0;
            sheet.setValueAt("LB", itrCol++, 0);
            sheet.setValueAt("Test", itrCol++, 0);
            if(sheetName.contains("TotalLoss")){
                sheet.setValueAt("Frequency", itrCol++, 0);
            }
            sheet.setValueAt("Site1", itrCol++, 0);
            sheet.setValueAt("Site2", itrCol++, 0);
            sheet.setValueAt("Site3", itrCol++, 0);
            sheet.setValueAt("Site4", itrCol++, 0);
            sheet.setValueAt("Site5", itrCol++, 0);
            sheet.setValueAt("Site6", itrCol++, 0);
            sheet.setValueAt("Site7", itrCol++, 0);
            sheet.setValueAt("Site8", itrCol++, 0);
        }
        // write ODS
        if(m_MapDatalog==null){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else if(m_MapDatalog.isEmpty()){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else{
            Integer itrRow=1;
            boolean chkLBID = false;
            if(chkLBID)
            {//search LBID position
                while(itrRow<rowCount){
                    println("ROW"+itrRow+"LBID:"+sheet.getValueAt(0, itrRow).toString());//col & row
                    Integer tmpLBID=Integer.getInteger(sheet.getValueAt(0,itrRow).toString());
                    if(tmpLBID.equals(this.m_PBID)) {
                        break;
                    }
                    itrRow++;
                }

                //if exist row length = target row length
                Integer startRow = itrRow;
                while(itrRow<rowCount){
                    Integer tmpLBID=Integer.getInteger(sheet.getValueAt(0, itrRow).toString());
                    if(tmpLBID.equals(this.m_PBID) ) {
                        itrRow++;
                    } else {
                        break;
                    }
                }
                Integer stopRow = itrRow;
                Integer lengthLBID= stopRow-startRow+1;
                Integer sizeOfItemMap = this.getItemSize(sheetName);
                if(lengthLBID>sizeOfItemMap){
                    println("ERROR!!!! Row number of PBID:"+this.m_PBID+" is not correct,"+lengthLBID+" is larger than"+sizeOfItemMap);
                }else if(lengthLBID<sizeOfItemMap){
                    Integer tmpLBID=Integer.getInteger(sheet.getValueAt(0,itrRow).toString());
                    if(tmpLBID!=0){
                        println("ERROR!!!! Row number of PBID:"+this.m_PBID+" is not correct,"+lengthLBID+" is larger than"+sizeOfItemMap);
                    }
                }
            }
            for(String testSuite:m_MapDatalog.keySet()){
                for(Key tmpKey : m_MapDatalog.get(testSuite).itemMap.keySet()){
                    if(tmpKey.getItem().equals(sheetName)){
                        Integer itrCol=0;
                        sheet.setValueAt(m_PBID, itrCol++, itrRow);
                        sheet.setValueAt(testSuite, itrCol++, itrRow);
                        if(sheetName.contains("TotalLoss")){
                            sheet.setValueAt(tmpKey.getFreq(), itrCol++, itrRow);
                        }
                        MultiSiteDouble value = this.getMeanValue(testSuite,sheetName,tmpKey.getFreq(),activeSites);

                        sheet.setValueAt(value.get(1), itrCol++, itrRow);
                        sheet.setValueAt(value.get(2), itrCol++, itrRow);
                        sheet.setValueAt(value.get(3), itrCol++, itrRow);
                        sheet.setValueAt(value.get(4), itrCol++, itrRow);
                        sheet.setValueAt(value.get(5), itrCol++, itrRow);
                        sheet.setValueAt(value.get(6), itrCol++, itrRow);
                        sheet.setValueAt(value.get(7), itrCol++, itrRow);
                        sheet.setValueAt(value.get(8), itrCol++, itrRow);
                        itrRow++;
                    }
                }
            }
        }


    }

    public Integer getItemSize(String item){
        Integer size=0;
        for(String testSuite:m_MapDatalog.keySet()){
            for(Key tmpKey:m_MapDatalog.get(testSuite).itemMap.keySet()){
                if(tmpKey.getItem().contains(item)) {
                    size++;
                }
            }
        }
        return size;
    }

    public void setValue(String testSuite, String item, MultiSiteDouble data){
        setValue(testSuite, item, 0.0,data);
    }
    public void setValue(String testSuite, String item, Double freq, MultiSiteDouble data){
        ItemDataMap tmpMap = new ItemDataMap();
        if(!m_MapDatalog.containsKey(testSuite)){
            m_MapDatalog.put(testSuite, tmpMap);
        }
        Key key = new Key(item,freq);
        if(!m_MapDatalog.get(testSuite).itemMap.containsKey(key)){
            List<MultiSiteDouble> list = new ArrayList<MultiSiteDouble>();
            list.add(data);
            m_MapDatalog.get(testSuite).itemMap.put(key,list);
        }else{
            m_MapDatalog.get(testSuite).itemMap.get(key).add(data);
        }


    }

    public MultiSiteDouble getValue(int lbid,String test,String item, Double freq){
        return new MultiSiteDouble(0);
    }

    public void showAllCalData(){
        println("--------show all list Start-----------------");
        if(m_MapDatalog==null){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else if(m_MapDatalog.isEmpty()){
            println("ERROR!!!! Cal Data Map is Empty");
        }
        else{
            for(String testSuite:m_MapDatalog.keySet()){
                for(Key key:m_MapDatalog.get(testSuite).itemMap.keySet()){
                    List<MultiSiteDouble> tmpAry = m_MapDatalog.get(testSuite).itemMap.get(key);
                    for(MultiSiteDouble tmpvalue: tmpAry){
                         println("PB"+this.m_PBID+" :"+testSuite+":"+key.getItem()+":"+key.getFreq() +":"+tmpvalue);
                    }
                }
            }
        }
        //        println("--------show all list End-----------------");
    }


}



