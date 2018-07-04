package src.rfcbb_tml.com;


public class Key {
    public final  String item;    
    public final  Double freq;    

    public Key(String item, Double freq) {
        this.item = item;
        this.freq = freq;
    }

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




    public int compareTo(Key o){
        int ret;
        ret = this.item.compareTo(o.item);

        if(ret ==0)
        {
            ret = this.freq.compareTo(o.freq);
        }

        return ret;
    }
}
