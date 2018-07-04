package src.miscellaneous_tml.TestControl;

import xoc.dta.TestMethod;


public class ExtUtilityPS extends TestMethod {



    @Override
    public void execute ()
    {
       println("Before Enable:  "+        context.tester().utilityPower().isEnabled());
       context.tester().utilityPower().enable();
       println("After Enable:  "+        context.tester().utilityPower().isEnabled());
       println("Ext Utility PS ON");

    }
}
