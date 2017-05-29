package com.example.cmina.mycastcast.util;

/**
 * Created by cmina on 2017-02-09.
 */

public class CastListItem {
    String castTitle;
    String castImage;
    String castdbname;
    String updateDate;
    Integer feedcount;



    public CastListItem(String castdbname, String casttitle, String castimage) {
    }

    public CastListItem(String castdbname, String casttitle, String castimage, String updateDate, Integer feedcount) {
    }

    public void setCastdbname(String castdbname) {
        this.castdbname =castdbname;
    }

    public void setCastTitle(String title) {
        castTitle = title;
    }

    public void setCastImage(String image) {
        castImage = image;
    }

    public void setFeedcount(Integer feedcount) {
        this.feedcount = feedcount;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getCastTitle() {
        return castTitle;
    }

    public String getCastImage() {
        return castImage;
    }

    public String getCastdbname() {
        return castdbname;
    }

    public Integer getFeedcount() {
        return feedcount;
    }

    public String getUpdateDate() {
        return updateDate;
    }
}
