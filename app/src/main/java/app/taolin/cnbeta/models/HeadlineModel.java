package app.taolin.cnbeta.models;

/**
 * @author taolin
 * @version v1.0
 * @date Jul 14, 2016.
 * @description
 */

public class HeadlineModel {
    public String sid;
    public String title;
    public String thumb;
    public int index;

    @Override
    public boolean equals(Object o) {
        if (o instanceof HeadlineModel) {
            HeadlineModel h = (HeadlineModel) o;
            return sid.equals(h.sid) && index == h.index;
        }
        return false;
    }
}
