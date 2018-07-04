package src.rf2rf_tml.utilities;

import xoc.dta.datatypes.MultiSiteDouble;


/**
 * This class defines some common used methods
 *
 * @since  1.1.0
 * @author 308770
 *
 */



public class UtMethods {


    /**
    *
    * this method is used to get the minimum power across in put power for each sites
    *
    * @param pow1   input power in db
    * @param pow2   input power in db
    * @param activeSites activates sites
    * @return MultiSiteDouble minimum power for each site
    */

public  static MultiSiteDouble min_2MSD( MultiSiteDouble pow1, MultiSiteDouble pow2,  int[] activeSites) {
    MultiSiteDouble tmpResult = new MultiSiteDouble();


//    int[] activeSites = context.getActiveSites();

    for(int site : activeSites)
    {
        if(pow1.get(site) < pow2.get(site)) {
            tmpResult.set(site, pow1.get(site));
        }
        else {
            tmpResult.set(site, pow2.get(site));
        }
    }

    return tmpResult;
}


/**
*
* this method is used to get the average power in db for the two input powers
*
* @param pow1   input power in db
* @param pow2   input power in db
* @return MultiSiteDouble average power in db
*/
public static MultiSiteDouble ave_powdb( MultiSiteDouble pow1, MultiSiteDouble pow2){

    MultiSiteDouble tmp_linear;
    MultiSiteDouble tmp_db;

    tmp_linear=db2linear(pow1).add(db2linear(pow2)).divide(2);
    tmp_db=linear2db(tmp_linear);

    return tmp_db;
}


/**
*
* this method is used to convert data from log10() to linear
*
* @param tmp   input MultiSiteDouble in log10()
* @return MultiSiteDouble value in linear
*/
public static MultiSiteDouble db2linear( MultiSiteDouble tmp){

   MultiSiteDouble tmp_linear;

   tmp_linear=tmp.divide(10).exp10();
   return tmp_linear;
}



/**
*
* this method is used to convert data from linear to log10()
*
* @param tmp   input MultiSiteDouble in linear
* @return MultiSiteDouble value in log10()
*/
public static MultiSiteDouble linear2db( MultiSiteDouble tmp){

   MultiSiteDouble tmp_linear;
   tmp_linear=tmp.log10().multiply(10);
   return tmp_linear;
}


}
