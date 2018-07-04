package src.rfcbb_tml.com;

/**
 * THis class defines a Key to identify data in calibration/golen data container.
 * it use LBID and testname to crate Key.
 *
 *
 * @author 308770
 *
 */
public class Key_Port {
    public final  Integer lbid;
    public final  String test;

    public Key_Port(Integer lbid, String test) {
        this.lbid = lbid;
        this.test = test;
    }

    /**
     * compare whether two Key_port are the same
     *
     * @param o  one Key_port object to compare
     *
     * @return if two objects same, return true; else return false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Key_Port)) {
            return false;
        }
        Key_Port key_Port = (Key_Port) o;
        return lbid.equals(key_Port.lbid) && test.equals(key_Port.test);
    }
    @Override
    public int hashCode() {
        int result = lbid+ test.length()*31;
        return result;
    }
    @SuppressWarnings("unused")
    private void println(String string) {
        // TODO Auto-generated method stub
        System.out.println(string);
    }


    /**
     * compare whether two Key_port are the same
     *
     * @param o  one Key_port object to compare
     *
     * @return if two objects same, return 0; else return difference of LBID
     */
    public int compareTo(Key_Port o){
        int ret = this.lbid - o.lbid;

        if(ret ==0)
        {
            ret = this.test.compareTo(o.test);
        }

        return ret;
    }
}
