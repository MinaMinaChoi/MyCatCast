package com.example.cmina.mycastcast.util;

import java.util.ArrayList;
import java.util.Comparator;

import static android.R.attr.description;

/**
 * Created by cmina on 2017-02-09.
 */

public class RssItem {

    private String episodeTitle = null;
   // private String description = null;
    private String musicUrl = null;
    private String pubdate = null;
    private String duration = null;
    private String castImage = null;
    private String castTitle = null;
    private String category = null;

    public RssItem(){

    }
    public RssItem(String castTitle, String castImage, String musicUrl, String episodeTitle) {
        this.castTitle = castTitle;
        this.castImage = castImage;
        this.musicUrl = musicUrl;
        this.episodeTitle = episodeTitle;
    }

    private static ArrayList<RssItem> itemList;

    public void setEpisodeTitle(String value) {
        episodeTitle = value;
    }

    public void setCategory(String value) {
        category = value;
    }

    public void setMusicUrl(String value) {
        musicUrl = value;
    }

    public void setPubdate(String value) {
        pubdate = value;
    }

    public void setDuration(String strCharacters) {
        duration = strCharacters;
    }

    public void setCastImage(String value) { castImage = value ;}

    public void setCastTitle(String value) { castTitle =  value;}


    public String getCategory() {
        return category;
    }

    public String getEpisodeTitle() {return episodeTitle ; }

    public String getMusicUrl() {return musicUrl;}

    public String getPubdate() {return pubdate;}

    public String getDuration() {return duration;}

    public String getCastImage() { return castImage;}

    public String getCastTitle() { return castTitle; }

    public static ArrayList<RssItem> getItemList() {
        return itemList;
    }


    public final static Comparator<RssItem> DATE_COMPARATOR = new Comparator<RssItem>() {

        @Override
        public int compare(RssItem o1, RssItem o2) {
            int ret  ;

            if (o1.getPubdate().compareTo(o2.getPubdate()) < 0)     // item1이 작은 경우,
                ret = 1 ;
                //   else if (o1.getDate().compareTo(o2.getDate()) == 0 )
                //     ret = (int) ((o2.getCurrTime())-(o1.getCurrTime()));

            else                                                // item1이 큰 경우,
                ret = -1 ;

            return ret ;
        }
    };


}
