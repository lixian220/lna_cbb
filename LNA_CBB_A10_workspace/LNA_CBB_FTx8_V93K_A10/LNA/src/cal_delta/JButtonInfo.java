package src.cal_delta;

import java.util.LinkedHashMap;
import java.util.Map;

import xoc.dta.datatypes.MultiSiteDouble;

public class JButtonInfo {
        String testsuiteName = "";
        String[] testItem = {"InputLoss","OutputLoss","OutputLossL"};
        MultiSiteDouble mResult = new MultiSiteDouble(99.99);
        public JButtonInfo(){}
        private Map<String,Map<String,MultiSiteDouble>> m_JButtonInfo = new LinkedHashMap<>();
        public static class JButtonInfoInstance{
            private static final JButtonInfo INSTANCE = new JButtonInfo();
        }
        public static JButtonInfo getInstance(){
            return JButtonInfoInstance.INSTANCE;
        }
        public void init (){
            m_JButtonInfo.clear();
        }
        public Map<String,Map<String,MultiSiteDouble>> setMapValue(String testsuite,MultiSiteDouble[] m_Result){
            Map<String,MultiSiteDouble> m_Map = new LinkedHashMap<>();

            for(int i =0;i<m_Result.length;i++)
            {
               if(m_Result[i]!=null)
               {
                   m_Map.put(testItem[i],m_Result[i]);
               }
               else
               {
                   m_Map.put(testItem[i],new MultiSiteDouble(9999));
               }
            }

            m_JButtonInfo.put(testsuite, m_Map);
            return m_JButtonInfo;
        }
        public String ShowAllResult(){

            String A ="Fail Information: \n";
            String Temp = new String("");

            for(String key:m_JButtonInfo.keySet())
            {
                String B = key+" FAILED!\n";
                String C = "InputLoss : "+m_JButtonInfo.get(key).get(testItem[0])+"\n";
                String D = "OutputLoss : "+m_JButtonInfo.get(key).get(testItem[1])+"\n";
                String E = "OutputLossL : "+m_JButtonInfo.get(key).get(testItem[2])+"\n";
                Temp = Temp +B + C + D + E+"\n";
            }

            return (A+Temp);
        }


        public String getTestsuiteName(){
            return testsuiteName;
        }
        public String showPassFail(String Name)
        {

            String firstLine = Name + ": FAILED!\n";
            return firstLine;
        }
        public String showInputLoss(String Name,MultiSiteDouble[] mRslt)
        {

            String secondLine = Name +testItem[0]+"---> "+ ": "+mRslt[0]+"\n";
            return secondLine;


        }
        public String showOutLoss(String Name,MultiSiteDouble[] mRslt)
        {
            String thirdLine = Name +"---> "+ testItem[1]+": "+mRslt[1]+"\n";
            return thirdLine;
        }
        public String showOutLossL(String Name,MultiSiteDouble[] mRslt)
        {
            String fourthLine = Name +testItem[2]+"---> "+": "+mRslt[2]+"\n";
            return fourthLine;
        }


}
