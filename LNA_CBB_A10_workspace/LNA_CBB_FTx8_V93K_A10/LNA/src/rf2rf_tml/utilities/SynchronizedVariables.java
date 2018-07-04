//
// Copyright (c) 2016 Advantest. All rights reserved.
//
// Contributors:
// 	Advantest - initial implementation
//
// NOTICE: ADVANTEST PROVIDES THIS SOFTWARE TO YOU ONLY UPON YOUR ACCEPTANCE OF ADVANTEST'S
// TERMS OF USE. USE OF THIS SAMPLE SOURCE CODE IS GOVERNED BY AND SUBJECT TO THE ADVANTEST
// LICENSE AND SERVICE AGREEMENT FOR APPLICATION SOFTWARE AND SERVICES AND IS PROVIDED "AS-IS",
// WITHOUT WARRANTY OF ANY KIND, UNLESS USE OF SUCH SAMPLE SOURCE CODE IS OTHERWISE EXPRESSLY
// GOVERNED BY AN AGREEMENT SIGNED BY ADVANTEST.
//

/// Original code from SOCLibTML, written by JS & KH
//  The authors name are in initial so that they do not get too many "support questions".
//  Only people who knows them knows...
//
/*
 *
 *
        /// Example


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Inside Test Suite A  --- Set gain
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        SyncMultiSiteDouble  gain_val = SyncMultiSiteDouble.create(RF_IN+"_"+RF_OUT+"_B"+band+"Gain_high");  // Create Map data if not create
        gain_val.set(new MultiSiteDouble(0.0));  // Set a default bad value, in case something went wrong.
        gain_val.reserve();  // MUST be done before ReleaseTester! Reserve the var, after this no thread can get() this. Until the next set() is done.
        releaseTester();

        gain_val.set(gain); // Set value. Unlock var



        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////Inside Test Suite B --- Get gain
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        SyncMultiSiteDouble  gain_NF = SyncMultiSiteDouble.create(RF_IN+"_"+RF_OUT+"_B"+band+"Gain_high");
        gain_NF.get();  /// Get() value, timeout in 10s.


 *
 *
 *
 *
 */



package src.rf2rf_tml.utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import xoc.dta.datatypes.MultiSiteBoolean;
import xoc.dta.datatypes.MultiSiteDouble;
import xoc.dta.datatypes.MultiSiteLong;
import xoc.dta.datatypes.MultiSiteString;


public class SynchronizedVariables
{
    public static abstract class SynchronizedVariableBase
    {
        protected Semaphore locker = new Semaphore(1);
        protected String id = "Default";
        protected long timeOut_us = 10000000; //default 10000000
        protected static boolean debug = false;

        protected SynchronizedVariableBase(String _id)
        {
            id = _id;
        }

        public String getId(){return id;}

        public void reserve()
        {
            if(debug){System.out.println("SynchronizedVariables.reserve(), id = " + id);}
            if(0 == locker.availablePermits())
            {
                System.err.println("Trying to lock SynchronizedVariable " + id + " this is already locked");
                //TODO: Need to throw an error here
                if (0 == locker.availablePermits()) {
                    locker.release();
                }
            }

            // we have guaranteed there will be no wait time
            if(false == lock())
            {
                System.err.println("SynchronizedVariables.reserve(), id = " + id + ", Unable to acquire lock");
                //TODO: Need to throw an error here
                if (0 == locker.availablePermits()) {
                    locker.release();
                }
            }
        }

        public void setTimeout(double _timeOut)
        {
            timeOut_us = Math.round(_timeOut*1000000.0);
            if(debug){System.out.println("SynchronizedVariables.setTimeout(" + _timeOut + "), id = " + id + ", TimeOut_us = " + timeOut_us);}
        }

        protected boolean lock()
        {
            if(debug){System.out.println("SynchronizedVariables.lock(), id = " + id + ", TimeOut_us = " + timeOut_us);}
            boolean status = false;
            try
            {
                status = locker.tryAcquire(timeOut_us, TimeUnit.MICROSECONDS);
                if(false == status)
                {
                    System.err.println("Unable to retrieve Synchronized Variable " + id + ". Try increasing the timeout value");
                }
            }
            catch(InterruptedException e)
            {
                System.out.println("Synchronized Variable " + id + " was interupted");
            }
            if(debug){System.out.println("SynchronizedVariables.lock(), status = " + status);}
            return status;
        }

        public Boolean AlreadySet()
        {
            Boolean status = lock();

            if(status)
            {
                if (0 == locker.availablePermits()) {
                    locker.release();
                }
            }

            return status;
        }
    }

    public enum VariableType {SV_STRING, SV_BOOLEAN, SV_LONG, SV_DOUBLE, SV_UNKNOWN}
    public static VariableType findTypeOfVariable(String _varName)
    {
        if(SyncMultiSiteDouble.static_variableRegistryMSD.containsKey(_varName)){ return VariableType.SV_DOUBLE;}
        if(SyncMultiSiteString.static_variableRegistryMSS.containsKey(_varName)){ return VariableType.SV_STRING;}
        if(SyncMultiSiteBoolean.static_variableRegistryMSB.containsKey(_varName)){ return VariableType.SV_BOOLEAN;}
        if(SyncMultiSiteLong.static_variableRegistryMSL.containsKey(_varName)){ return VariableType.SV_LONG;}
        return VariableType.SV_UNKNOWN;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        SyncMultiSiteDouble tmpD = SyncMultiSiteDouble.create("Dump");
        sb.append(tmpD.toString());
        SyncMultiSiteLong tmpL = SyncMultiSiteLong.create("Dump");
        sb.append(tmpL.toString());
        SyncMultiSiteBoolean tmpB = SyncMultiSiteBoolean.create("Dump");
        sb.append(tmpB.toString());
        SyncMultiSiteString tmpS = SyncMultiSiteString.create("Dump");
        sb.append(tmpS.toString());
        return sb.toString();
    }

    public static class SyncMultiSiteDouble extends SynchronizedVariableBase
    {
        private static Map<String, SyncMultiSiteDouble> static_variableRegistryMSD = new ConcurrentHashMap<String, SyncMultiSiteDouble>();
        private MultiSiteDouble value = new MultiSiteDouble(Double.NaN);

        public static final SyncMultiSiteDouble create(String _id)
        {
            if(static_variableRegistryMSD.containsKey(_id))
            {
                if(debug){System.out.println("SyncMultiSiteDouble.create(" + _id + "), found existing object");}
                return static_variableRegistryMSD.get(_id);
            }

            VariableType existingType = findTypeOfVariable(_id);
            if(!existingType.equals(VariableType.SV_UNKNOWN))
            {
                throw new RuntimeException("Variable " + _id + " alread exists as type " + existingType + ", it can not be set as SyncMultiSiteDouble");


            }

            SyncMultiSiteDouble newVal = new SyncMultiSiteDouble(_id);
            static_variableRegistryMSD.put(_id,  newVal);
            if(debug){System.out.println("SyncMultiSiteDouble.create(" + _id + "), adding new object");}
            return newVal;
        }

        protected SyncMultiSiteDouble(String _id)
        {
            super(_id);
            if(debug){System.out.println("SyncMultiSiteDouble(" + id + ")");}
        }

        public void set(MultiSiteDouble _inVal)
        {
            if(debug){System.out.println("SyncMultiSiteDouble.set(" + _inVal + "), id = " + id);}
            value.set(_inVal);
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteDouble.set(" + _inVal + ") - DONE, id = " + id);}
        }

        /**
         *
         * @param _site: Site number, starting from site 1
         * @param _inVal: value to be updated on _site
         */
        public void set(int _site, double _inVal)
        {
            if(debug){System.out.println("SyncMultiSiteDouble.set(" + _inVal + ") on site " + _site + ",  id = " + id);}
            value.set(_site, _inVal);
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteDouble.set(" + _inVal + ") on site " + _site + " - DONE, id = " + id);}
        }


        public MultiSiteDouble get()
        {
            if(debug){System.out.println("SyncMultiSiteDouble.get(), id = " + id);}
            if(false == lock())
            {
                return new MultiSiteDouble(Double.NaN);
            }
            MultiSiteDouble retVal = value.copy();
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteDouble.get() - DONE, id = " + id + ", Value: " + retVal);}
            return retVal;
        }

        public double get(int _site)
        {
            if(debug){System.out.println("SyncMultiSiteDouble.get("+ _site + "), id = " + id);}
            if(false == lock())
            {
                return new MultiSiteDouble(Double.NaN).get(_site);
            }
            MultiSiteDouble retVal = value.copy();
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteDouble.get("+ _site + ") - DONE, id = " + id + ", Value: " + retVal);}
            return retVal.get(_site);
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Debug printing for SynchMultiSiteDouble\n");
            for(String _key: static_variableRegistryMSD.keySet())
            {
                sb.append(_key + " : " + static_variableRegistryMSD.get(_key).get());
                sb.append("\n");
            }
            sb.append("\n\n");
            return sb.toString();
        }

        public static void dumpVariables()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Debug printing for SynchMultiSiteDouble\n");
            for(String _key: static_variableRegistryMSD.keySet())
            {
                sb.append(_key + " : " + static_variableRegistryMSD.get(_key).get());
                sb.append("\n");
            }
            sb.append("\n\n");
            System.out.println(sb.toString());
        }

        public static boolean isKeyExists(String _key)
        {
            if (static_variableRegistryMSD.containsKey(_key))
            {
                return true;
            }

            return false;
        }
    }

    public static class SyncMultiSiteLong extends SynchronizedVariableBase
    {
        private static Map<String, SyncMultiSiteLong> static_variableRegistryMSL = new ConcurrentHashMap<String, SyncMultiSiteLong>();
        private MultiSiteLong value = new MultiSiteLong(0);

        public static final SyncMultiSiteLong create(String _id)
        {
            if(static_variableRegistryMSL.containsKey(_id))
            {
                if(debug){System.out.println("SyncMultiSiteLong.create(" + _id + "), found existing object");}
                return static_variableRegistryMSL.get(_id);
            }

            VariableType existingType = findTypeOfVariable(_id);
            if(!existingType.equals(VariableType.SV_UNKNOWN))
            {
                throw new RuntimeException("Variable " + _id + " alread exists as type " + existingType + ", it can not be set as SyncMultiSiteLong");
            }

            SyncMultiSiteLong newVal = new SyncMultiSiteLong(_id);
            static_variableRegistryMSL.put(_id,  newVal);
            if(debug){System.out.println("SyncMultiSiteLong.create(" + _id + "), adding new object");}
            return newVal;
        }

        protected SyncMultiSiteLong(String _id)
        {
            super(_id);
            if(debug){System.out.println("SyncMultiSiteLong(" + id + ")");}
        }

        public void set(MultiSiteLong _inVal)
        {
            if(debug){System.out.println("SyncMultiSiteLong.set(" + _inVal + "), id = " + id);}
            value.set(_inVal);
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteLong.set(" + _inVal + ") - DONE, id = " + id);}
        }

        /**
         *
         * @param _site: Site number, starting from site 1
         * @param _inVal: value to be updated on _site
         */
        public void set(int _site, long _inVal)
        {
            if(debug){System.out.println("SyncMultiSiteLong.set(" + _inVal + ") on site " + _site + " , id = " + id);}
            value.set(_site, _inVal);
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteLong.set(" + _inVal + ") on site " + _site + " - DONE, id = " + id);}
        }

        public MultiSiteLong get()
        {
            if(debug){System.out.println("SyncMultiSiteLong.get(), id = " + id);}
            if(false == lock())
            {
                return new MultiSiteLong(0);
            }
            MultiSiteLong retVal = value.copy();
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteLong.get() - DONE, id = " + id + ", Value: " + retVal);}
            return retVal;
        }

        /**
         *
         * @param _site: site index (starting from 1)
         * @return long value of _site
         */
        public long get(int _site)
        {
            if(debug){System.out.println("SyncMultiSiteLong.get("+ _site + "), id = " + id);}
            if(false == lock())
            {
                return new MultiSiteLong(0).get(_site);
            }
            MultiSiteLong retVal = value.copy();
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteLong.get("+ _site + ") - DONE, id = " + id + ", Value: " + retVal);}
            return retVal.get(_site);
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Debug printing for SynchMultiSiteLong\n");
            for(String _key: static_variableRegistryMSL.keySet())
            {
                sb.append(_key + " : " + static_variableRegistryMSL.get(_key).get());
                sb.append("\n");
            }
            sb.append("\n\n");
            return sb.toString();
        }

        public static void dumpVariables()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Debug printing for SynchMultiSiteLong\n");
            for(String _key: static_variableRegistryMSL.keySet())
            {
                sb.append(_key + " : " + static_variableRegistryMSL.get(_key).get());
                sb.append("\n");
            }
            sb.append("\n\n");
            System.out.println(sb.toString());
        }

        public static boolean isKeyExists(String _key)
        {
            if (static_variableRegistryMSL.containsKey(_key))
            {
                return true;
            }

            return false;
        }
    }

    public static class SyncMultiSiteBoolean extends SynchronizedVariableBase
    {
        private static Map<String, SyncMultiSiteBoolean> static_variableRegistryMSB = new ConcurrentHashMap<String, SyncMultiSiteBoolean>();
        private MultiSiteBoolean value = new MultiSiteBoolean(0);

        public static final SyncMultiSiteBoolean create(String _id)
        {
            if(static_variableRegistryMSB.containsKey(_id))
            {
                if(debug){System.out.println("SyncMultiSiteBoolean.create(" + _id + "), found existing object");}
                return static_variableRegistryMSB.get(_id);
            }

            VariableType existingType = findTypeOfVariable(_id);
            if(!existingType.equals(VariableType.SV_UNKNOWN))
            {
                throw new RuntimeException("Variable " + _id + " alread exists as type " + existingType + ", it can not be set as SyncMultiSiteBoolean");
            }

            SyncMultiSiteBoolean newVal = new SyncMultiSiteBoolean(_id);
            static_variableRegistryMSB.put(_id,  newVal);
            if(debug){System.out.println("SyncMultiSiteBoolean.create(" + _id + "), adding new object");}
            return newVal;
        }

        protected SyncMultiSiteBoolean(String _id)
        {
            super(_id);
            if(debug){System.out.println("SyncMultiSiteBoolean(" + id + ")");}
        }

        public void set(MultiSiteBoolean _inVal)
        {
            if(debug){System.out.println("SyncMultiSiteBoolean.set(" + _inVal + "), id = " + id);}
            value.set(_inVal);
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteBoolean.set(" + _inVal + ") - DONE, id = " + id);}
        }

        /**
         *
         * @param _site: Site number, starting from site 1
         * @param _inVal: value to be updated on _site
         */
        public void set(int _site, boolean _inVal)
        {
            if(debug){System.out.println("SyncMultiSiteBoolean.set(" + _inVal + ") on site " + _site + " , id = " + id);}
            value.set(_site, _inVal);
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteBoolean.set(" + _inVal + ") on site " + _site + " - DONE, id = " + id);}
        }

        public MultiSiteBoolean get()
        {
            if(debug){System.out.println("SyncMultiSiteBoolean.get(), id = " + id);}
            if(false == lock())
            {
                return new MultiSiteBoolean(0);
            }
            MultiSiteBoolean retVal = value.copy();
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteBoolean.get() - DONE, id = " + id + ", Value: " + retVal);}
            return retVal;
        }

        /**
         *
         * @param _site: site index (starting from 1)
         * @return boolean value of _site
         */
        public boolean get(int _site)
        {
            if(debug){System.out.println("SyncMultiSiteBoolean.get("+ _site + "), id = " + id);}
            if(false == lock())
            {
                return new MultiSiteBoolean(0).get(_site);
            }
            MultiSiteBoolean retVal = value.copy();
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteBoolean.get("+ _site + ") - DONE, id = " + id + ", Value: " + retVal);}
            return retVal.get(_site);
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Debug printing for SynchMultiSiteBoolean\n");
            for(String _key: static_variableRegistryMSB.keySet())
            {
                sb.append(_key + " : " + static_variableRegistryMSB.get(_key).get());
                sb.append("\n");
            }
            sb.append("\n\n");
            return sb.toString();
        }

        public static void dumpVariables()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Debug printing for SynchMultiSiteBoolean\n");
            for(String _key: static_variableRegistryMSB.keySet())
            {
                sb.append(_key + " : " + static_variableRegistryMSB.get(_key).get());
                sb.append("\n");
            }
            sb.append("\n\n");
            System.out.println(sb.toString());
        }

        public static boolean isKeyExists(String _key)
        {
            if (static_variableRegistryMSB.containsKey(_key))
            {
                return true;
            }

            return false;
        }
    }

    public static class SyncMultiSiteString extends SynchronizedVariableBase
    {
        private static Map<String, SyncMultiSiteString> static_variableRegistryMSS = new ConcurrentHashMap<String, SyncMultiSiteString>();
        private MultiSiteString value = new MultiSiteString(0);

        public static final SyncMultiSiteString create(String _id)
        {
            if(static_variableRegistryMSS.containsKey(_id))
            {
                if(debug){System.out.println("SyncMultiSiteString.create(" + _id + "), found existing object");}
                return static_variableRegistryMSS.get(_id);
            }

            VariableType existingType = findTypeOfVariable(_id);
            if(!existingType.equals(VariableType.SV_UNKNOWN))
            {
                throw new RuntimeException("Variable " + _id + " alread exists as type " + existingType + ", it can not be set as SyncMultiSiteString");
            }

            SyncMultiSiteString newVal = new SyncMultiSiteString(_id);
            static_variableRegistryMSS.put(_id,  newVal);
            if(debug){System.out.println("SyncMultiSiteString.create(" + _id + "), adding new object");}
            return newVal;
        }

        protected SyncMultiSiteString(String _id)
        {
            super(_id);
            if(debug){System.out.println("SyncMultiSiteString(" + id + ")");}
        }

        public void set(MultiSiteString _inVal)
        {
            if(debug){System.out.println("SyncMultiSiteString.set(" + _inVal + "), id = " + id);}
            value.set(_inVal);
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteString.set(" + _inVal + ") - DONE, id = " + id);}
        }

        /**
         *
         * @param _site: Site number, starting from site 1
         * @param _inVal: value to be updated on _site
         */
        public void set(int _site, String _inVal)
        {
            if(debug){System.out.println("SyncMultiSiteString.set(" + _inVal + ") on site " + _site + " , id = " + id);}
            value.set(_site, _inVal);
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteString.set(" + _inVal + ") on site " + _site + " - DONE, id = " + id);}
        }

        public MultiSiteString get()
        {
            if(debug){System.out.println("SyncMultiSiteString.get(), id = " + id);}
            if(false == lock())
            {
                return new MultiSiteString("");
            }
            MultiSiteString retVal = value.copy();
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteString.get() - DONE, id = " + id + ", Value: " + retVal);}
            return retVal;
        }

        /**
         *
         * @param _site: site index (starting from 1)
         * @return long value of _site
         */
        public String get(int _site)
        {
            if(debug){System.out.println("SyncMultiSiteString.get("+ _site + "), id = " + id);}
            if(false == lock())
            {
                return new MultiSiteString("").get(_site);
            }
            MultiSiteString retVal = value.copy();
            if (0 == locker.availablePermits()) {
                locker.release();
            }
            if(debug){System.out.println("SyncMultiSiteString.get("+ _site + ") - DONE, id = " + id + ", Value: " + retVal);}
            return retVal.get(_site);
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Debug printing for SyncMultiSiteString\n");
            for(String _key: static_variableRegistryMSS.keySet())
            {
                sb.append(_key + " : " + static_variableRegistryMSS.get(_key).get());
                sb.append("\n");
            }
            sb.append("\n\n");
            return sb.toString();
        }

        public static void dumpVariables()
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Debug printing for SyncMultiSiteString\n");
            for(String _key: static_variableRegistryMSS.keySet())
            {
                sb.append(_key + " : " + static_variableRegistryMSS.get(_key).get());
                sb.append("\n");
            }
            sb.append("\n\n");
            System.out.println(sb.toString());
        }

        public static boolean isKeyExists(String _key)
        {
            if (static_variableRegistryMSS.containsKey(_key))
            {
                return true;
            }

            return false;
        }

    }
}
