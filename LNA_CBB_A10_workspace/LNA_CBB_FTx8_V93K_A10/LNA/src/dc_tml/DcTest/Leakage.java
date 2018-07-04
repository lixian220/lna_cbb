package src.dc_tml.DcTest;
/*******************************************************************************
 * Copyright (c) 2015 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/

import src.rfcbb_tml.com.RF_UserCommon;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupDigInOut.IVforceImeas;
import xoc.dsa.ISetupPattern;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.UncheckedDTAException;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDigInOutActionResults;
import xoc.dta.resultaccess.IPerSignalPassFail;
import xoc.dta.testdescriptor.IFunctionalTestDescriptor;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
 * This test method measures the leakage current at a digital device input or output or IO.
 * To perform the leakage measurement you can either use the PPMU or boardADC, it doesn't support HPPMU.
 *
 * You can't configure specification and operating file via the measurmentRun directly in test suite,
 * test method will generate them from scratch, if you have additional setting e.g. signal group names/aliases,
 * you can pass specification file to test method by the corresponding parameter.
 *
 * Specify the mandatory parameters in your test suite, you can define test suite like below:
 * <pre>
 *    suite LeakageTest calls com.advantest.itee.tml.dctml.Leakage {
 *        importedSpecFile = setupRef(&lt;fully qualified name of specification&gt;);
 *        patternName = setupRef(&lt;fully qualified name of pattern&gt;);
 *        prefunction = To_Stop_Vec;
 *
 *        signalGroup [ALL_LowLeakage] = {
 *            signals = "signalsName";
 *            forceVoltage = 0.0;
 *            settlingTime = 10E-3;
 *            iRange = 50E-3;
 *            stopVec = 10;
 *        };
 *
 *        signalGroup [ALL_HighLeakage] = {
 *            signals = "signalsName";
 *            forceVoltage = 4.1;
 *            settlingTime = 10E-3;
 *            iRange = 56E-3;
 *            stopVec = 20;
 *        };
 *    };
 * </pre>
 *
 * @since 8.0.4
 *
 */
public class Leakage extends TestMethod {

    /**
     * The name of the functional test descriptor that is used to evaluate pre-function test (optional).
     *
     * @since 8.0.4
     */
    public IFunctionalTestDescriptor preFuncTestDescriptor;

    /**
     * Group of I/O signals to test, with settings and limits (mandatory).
     *
     * @since 8.0.4
     */

    public ParameterGroupCollection<LeakageSignalInfo> Parameter = new ParameterGroupCollection<>();

    /**
     * Specification file to import for timing and level setting (mandatory).
     *
     * @since 8.0.4
     */
    public String importedSpecName = "";

    /**
     * Pattern file to run pre-functional test (optional).
     *
     * @since 8.0.4
     */
    public String patternName = "";

    /**
     * Whether to execute pre-function test, default is No (optional).
     *
     * @since 8.0.4
     */
    //   public PreFunctionType prefunction = PreFunctionType.No;
    public PreFunctionType prefunction = PreFunctionType.All;

    /**
     * Measure signal groups in serial or parallel when pre-function is No or All, default is serial (optional).
     *
     * @since 8.0.4
     */
    public MeasureModeType measureMode = MeasureModeType.Serial;

    /**
     * Pre function type.
     *
     * @since 8.0.4
     */
    public enum PreFunctionType {
        /**
         * Do not run pattern.
         *
         * @since 8.0.4
         */
        No,

        /**
         * Do measurement at specified vector.
         *
         * @since 8.0.4
         */
        To_Stop_Vec,

        /**
         * Run the whole pattern.
         *
         * @since 8.0.4
         */
        All,
    }

    public enum MeasureModeType {
        /**
         * Measure signal groups in serial.
         *
         * @since 8.0.4
         */
        Serial,

        /**
         * Measure signal groups in parallel.
         *
         * @since 8.0.4
         */
        Parallel,
    }


    private IMeasurement measurementRun;


    @SuppressWarnings("unused")
    private IPerSignalPassFail prefuncResults = null;

    @Override
    public void setup() {
        try {
            if (importedSpecName == null || importedSpecName.trim().equals("")) {
                throw new UncheckedDTAException("[Leakage] Please input valid values for parameters: 'importedSpecFile'.");
            }
            // 1. Create specification and operating sequence files.
            IDeviceSetup deviceSetup = DeviceSetupFactory.createInstance();

            // 1.1. Import of an additional specification file.
            deviceSetup.importSpec(importedSpecName);

            // 1.2. Call the pattern according the parameter.
            ISetupPattern setupPattern = null;
            if (prefunction == PreFunctionType.To_Stop_Vec || prefunction == PreFunctionType.All) {
                if (patternName == null || patternName.equals("")) {
                    throw new UncheckedDTAException("[Leakage] Please input valid values for parameters: 'patternName', when do prefunctional test.");
                }

                if (prefunction == PreFunctionType.To_Stop_Vec) {
                    setupPattern = deviceSetup.getPatternRef(patternName);
                    deviceSetup.patternCall(setupPattern);
                } else {
                    deviceSetup.patternCall(patternName);
                }
            }

            // 1.3. Create setup info for each signal group.
            if (measureMode == MeasureModeType.Parallel) {
                deviceSetup.parallelBegin();
                {
                    for (LeakageSignalInfo signalInfo : Parameter.values()) {
                        signalInfo.setup(deviceSetup, setupPattern);
                    }
                }
                deviceSetup.parallelEnd();
            } else {
                deviceSetup.parallelBegin();
                {
                    for (LeakageSignalInfo signalInfo : Parameter.values()) {
                        deviceSetup.sequentialBegin();
                        {
                            if(signalInfo.getId().contains("Low")){
                                signalInfo.setup(deviceSetup, setupPattern);
                            }
                        }
                        deviceSetup.sequentialEnd();
                    }
                }
                deviceSetup.parallelEnd();

                deviceSetup.parallelBegin();
                {
                    for (LeakageSignalInfo signalInfo : Parameter.values()) {
                        deviceSetup.sequentialBegin();
                        {
                            if(signalInfo.getId().contains("High")){
                                signalInfo.setup(deviceSetup, setupPattern);
                            }
                        }
                        deviceSetup.sequentialEnd();
                    }
                }
                deviceSetup.parallelEnd();
            }

            // 2. Link setup info with the measurement.
            measurementRun.setSetups(deviceSetup);

            // 3. Print generated specification and operating sequence files path to the console.
            message(20, "[Leakage] The generated specification file path: "
                    + measurementRun.getSpecificationName());
            message(20, "[Leakage] The generated operating sequence file path: "
                    + measurementRun.getOperatingSequenceName());

        } catch (Exception e) {
            throw new UncheckedDTAException("[Leakage] Create standard multi-group Leakage setup files failed : " + e);
        }
    }

    @Override
    public void execute() {
        measurementRun.execute();

        // 1. Reserve pre-functional test result.
        if (prefunction != PreFunctionType.No) {
            prefuncResults = measurementRun.preservePerSignalPassFail();
        }

        // 2. Reserve test result for each signal group.
        for (LeakageSignalInfo signalInfo : Parameter.values()) {
            signalInfo.reserveResult();
        }


        // 3. Release tester hardware.
        //this.releaseTester();
        if (RF_UserCommon.hiddenupload()){
            this.releaseTester();
        }



        // 4. Upload and log the pre-function result.




        // 5. Upload and log the leakage result for each signal group.
        for (LeakageSignalInfo signalInfo : Parameter.values()) {
            signalInfo.evaluate();
        }



    }

    public class LeakageSignalInfo extends ParameterGroup {

        public IParametricTestDescriptor ptd;
        public IParametricTestDescriptor ptd1;

        public IParametricTestDescriptor ptd2;

        public IParametricTestDescriptor ptd3;

        public IParametricTestDescriptor ptd4;


        public String signals = "";

        public double forceVoltage = Double.NaN;

        public double iRange = Double.NaN;

        public double settlingTime = 1.0E-3d;


        public int stopVec = 0;


        public boolean isHighAccuracy = false;


        private static final int FIRST_ACTION_INDEX = 0;


        private IDigInOutActionResults digInOutResult;


        void setup(IDeviceSetup deviceSetup, ISetupPattern setupPattern) throws Exception {
            if (signals == null || signals.trim().equals("") || Double.isNaN(forceVoltage)) {
                throw new UncheckedDTAException("[Leakage] Please input valid values for signal group[" + getId() +
                        "] parameters: 'signals', 'forceVoltage'.");
            }
            double lowLimit = ptd1.getLowLimit() == null ? 0
                    : ptd1.getLowLimit().doubleValue();
            double highLimit = ptd1.getHighLimit() == null ? 0
                    : ptd1.getHighLimit().doubleValue();

            // 1. Get current range from limits, or user specified value.
            double maxValue = Math.max(Math.abs(lowLimit), Math.abs(highLimit));
            iRange = (Double.isNaN(iRange) ? maxValue : iRange);

            // 2. Do measurement on the signal group.
            IVforceImeas vfimAction = deviceSetup.addDigInOut(signals).vforceImeas(getId());
            vfimAction.setForceValue(forceVoltage).setWaitTime(settlingTime).setIrange(iRange);//.setHighAccuracy(isHighAccuracy);

            if (prefunction == PreFunctionType.To_Stop_Vec) {
                setupPattern.addActions(stopVec, vfimAction);
            } else {
                if(measureMode == MeasureModeType.Serial) {
                    deviceSetup.actionCall(vfimAction);
                } else {
                    deviceSetup.sequentialBegin();
                    {
                        deviceSetup.actionCall(vfimAction);
                    }
                    deviceSetup.sequentialEnd();
                }
            }
        }

        void reserveResult() {
            digInOutResult = measurementRun.digInOut(signals).preserveActionResults();
        }

        void evaluate() {

            if(getId().contains("High")){
                ptd.setTestText("IIH:"+signals);
            }else{
                ptd.setTestText("IIL:"+signals);
            }
            ptd.evaluate(digInOutResult.vforceImeas(getId()).getCurrent(signals), FIRST_ACTION_INDEX);

        }

    }

}
