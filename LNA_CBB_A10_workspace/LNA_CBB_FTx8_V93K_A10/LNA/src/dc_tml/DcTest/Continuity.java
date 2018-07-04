/*******************************************************************************
 * Copyright (c) 2015 Advantest. All rights reserved.
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/
package src.dc_tml.DcTest;

import java.util.HashSet;
import java.util.Set;

import src.rfcbb_tml.com.RF_UserCommon;
import xoc.dsa.DeviceSetupFactory;
import xoc.dsa.IDeviceSetup;
import xoc.dsa.ISetupDcVI;
import xoc.dsa.ISetupDigInOut;
import xoc.dta.ParameterGroup;
import xoc.dta.ParameterGroupCollection;
import xoc.dta.TestMethod;
import xoc.dta.UncheckedDTAException;
import xoc.dta.measurement.IMeasurement;
import xoc.dta.resultaccess.IDigInOutActionResults;
import xoc.dta.resultaccess.IDigInOutActionResults.IIforceVmeasResults;
import xoc.dta.testdescriptor.IParametricTestDescriptor;

/**
 * This test method performs a continuity test with the setup data specified in the calling test suite
 * to checks for the continuity of the test signal paths, and for the open/short circuits.<br>
 * To specify the mandatory parameters in your test suite, enter the following lines to its definition:
 * <pre>
 * suite ContinuityTest calls com.advantest.itee.tml.dctml.Continuity {
 *     importedSpecFile = setupRef(&lt;fully qualified name of specification&gt;);
 *     dpsSignals = "DPS signals name";
 *
 *     signalGroup [IO_Pos] = {
 *         signals = "IO";
 *         forceCurrent = 1E-3;
 *         settlingTime = 1E-3;
 *     };
 *
 *     signalGroup [IO_Neg] = {
 *         signals = "IO";
 *         forceCurrent = -2E-3;
 *         settlingTime = 2E-3;
 *     };
 *
 *     ...
 *
 *     signalGroup [GROUPN] = {
 *         signals = "signalName";
 *         forceCurrent = -2E-3;
 *         settlingTime = 2E-3;
 *     };
 * }
 * </pre>
 *
 * @since 8.0.2
 */
public class Continuity extends TestMethod {

    /**
     * Group of I/O signals to test, with settings and limits.
     *
     * @since 8.0.2
     */
    public ParameterGroupCollection<ContinuitySignalInfo>  signalGroup = new ParameterGroupCollection<>();

    /**
     * Specification file to import for signal group names/aliases (optional).
     */
    public String importedSpecName = "";

    /**
     * DPS signals.
     *
     * @since 8.0.2
     */
    public String dpsSignals = "";

    /**
     * DPS current clamp, currently only supports DPS128 (optional).
     *
     * @since 8.0.2
     */
    public double dpsSignalsIclamp = Double.NaN;

    /**
     * Main object for test execution.
     *
     * @since 8.0.2
     */
    private IMeasurement measurementRun;

    private final Set<String> configuredSignalsSet = new HashSet<String>();

    @Override
    public void setup() {
        try {
            // 1. Create specification and operating sequence files.
            IDeviceSetup deviceSetup = DeviceSetupFactory.createInstance();

            // 1.1. Optional import of an additional specification file.
            if (importedSpecName != null && !importedSpecName.equals("")) {
                deviceSetup.importSpec(importedSpecName);
            }

            // 1.2. Connect DPS before measurement, set the DPS vforce to zero volt to keep it in lowImpedance, and set the current clamp
            ISetupDcVI dpsSetup = deviceSetup.addDcVI(dpsSignals);
            dpsSetup.setConnect(true).setDisconnect(true).level().setVforce(0.0);
            if (!Double.isNaN(dpsSignalsIclamp)) {
                dpsSetup.level().setIclamp(dpsSignalsIclamp);
            }

            // 1.3. Create setup info for each signal group.
            for (ContinuitySignalInfo signalInfo : signalGroup.values()) {
                signalInfo.setup(deviceSetup);
            }

            // 2. Link setup info with the measurement.
            //            measurementRun.setSetupRef(deviceSetup);
            measurementRun.setSetups(deviceSetup);

            // 3. Print generated specification and operating sequence files path to the console.
            message(20, "[Continuity] The generated specification file path: "
                    + measurementRun.getSpecificationName());
            message(20, "[Continuity] The generated operating sequence file path: "
                    + measurementRun.getOperatingSequenceName());


        } catch (Exception e) {
            throw new UncheckedDTAException("[Continuity] Create standard multi-group continuity setup files failed : " + e);
        }
    }

    @Override
    public void execute() {
        measurementRun.execute();

        // 1. Reserve test result for each signal group.
        for (ContinuitySignalInfo signalInfo : signalGroup.values()) {
            signalInfo.reserveResult();
        }

        // 2. Release tester hardware.
        //    this.releaseTester();
        if (RF_UserCommon.hiddenupload()){
            this.releaseTester();
        }


        // 3. Upload and log the result for each signal group.
        for (ContinuitySignalInfo signalInfo : signalGroup.values()) {
            signalInfo.evaluate();
        }
    }

    /**
     * Parameter group for the I/O signals to be measured.
     *
     * @since 8.0.2
     */
    public class ContinuitySignalInfo extends ParameterGroup {

        /**
         * The name of the parametric test descriptor that is used to evaluate the results of this group.
         *
         * @since 8.0.2
         */
        public IParametricTestDescriptor parametricTestDescriptor;

        /**
         * Signal list of this group.
         *
         * @since 8.0.2
         */
        public String signals = "";

        /**
         * Force current value.
         *
         * @since 8.0.2
         */
        public double forceCurrent = Double.NaN;

        /**
         * Settling time between force current and measurement, default is 1ms.
         *
         * @since 8.0.2
         */
        public double settlingTime = 1.0E-3d;

        /**
         * Store measurement result.
         *
         * @since 8.0.2
         */
        //        private IDcVIResults dcVIResult = null;
        private IDigInOutActionResults dcVIResult = null;

        /**
         * @param id
         *            Id of the continuity signal group.
         *
         * @since 8.0.2
         */

        /**
         * Create the signals specification and operating sequence files.
         *
         * @param deviceSetup
         *            The setup API context.
         * @throws Exception
         *             If create setup files failed.
         *
         * @since 8.0.2
         */
        void setup(IDeviceSetup deviceSetup) throws Exception {
            // 1. Connect the signals before measurement and disconnect after measurement.
            //            ISetupDcVI dcvi = deviceSetup.addDcVI(signals);
            ISetupDigInOut dcvi = deviceSetup.addDigInOut(signals);
            if(!configuredSignalsSet.contains(signals)){
                //dcvi.setConnect(true);//.setDisconnect(true);
                configuredSignalsSet.add(signals);
            }

            // 2. Measure continuity on this group.
            if (!Double.isNaN(forceCurrent)) {
                deviceSetup.actionCall(dcvi.iforceVmeas(getId()).setForceValue(forceCurrent).setIrange(Math.abs(forceCurrent))
                        .setWaitTime(settlingTime).setVexpected(2.0).getName());
            } else {
                //dcvi.level().set.setVforce(0.0);
            }
        }

        /**
         * Preserve the test result.
         *
         * @since 8.0.2
         */
        void reserveResult() {
            //            dcVIResult = measurementRun.dcVI(signals).preserveResults();
            dcVIResult = measurementRun.digInOut(signals).preserveActionResults();
        }

        /**
         * Get actions test results and log results.
         *
         * @since 8.0.2
         */
        void evaluate() {
            // 1. Get IFVM actions test results.
            //            MultiSiteDoubleArray ifvmActionResult = dcVIResult.iforceVmeas(getId());
            IIforceVmeasResults ifvmActionResult = dcVIResult.iforceVmeas(getId());

            // 2. Depending on parameters, current has been measured.
            // The verbosity of data log is controlled by log level defined in test table. Default log level is 10.
            if (!Double.isNaN(forceCurrent)) {
                parametricTestDescriptor.evaluate(ifvmActionResult,0);
            }
        }
    }

}
