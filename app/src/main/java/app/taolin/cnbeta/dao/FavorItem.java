package app.taolin.cnbeta.dao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "FAVOR_ITEM".
 */
@Entity
public class FavorItem implements Comparable<FavorItem> {

    @Id
    private String sid;

    @NotNull
    private String title;
    private String pubtime;
    private String collecttime;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    @Generated
    public FavorItem() {
    }

    public FavorItem(String sid) {
        this.sid = sid;
    }

    @Generated
    public FavorItem(String sid, String title, String pubtime, String collecttime) {
        this.sid = sid;
        this.title = title;
        this.pubtime = pubtime;
        this.collecttime = collecttime;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public String getPubtime() {
        return pubtime;
    }

    public void setPubtime(String pubtime) {
        this.pubtime = pubtime;
    }

    public String getCollecttime() {
        return collecttime;
    }

    public void setCollecttime(String collecttime) {
        this.collecttime = collecttime;
    }

    // KEEP METHODS - put your custom methods here
    @Override
    public int compareTo(FavorItem another) {
        return another.pubtime.compareTo(pubtime);
    }
    // KEEP METHODS END

}
