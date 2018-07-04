package src.miscellaneous_tml.TestControl;

import xoc.dta.TestMethod;


public class ExtUtilityPS_OFF extends TestMethod {

    @Override
    public void execute ()
    {
        println("Before OFF:  "+        context.tester().utilityPower().isEnabled());
        context.tester().utilityPower().disable();
        println("After OFF:  "+        context.tester().utilityPower().isEnabled());
        println("Ext Utility PS OFF");

    }
}
