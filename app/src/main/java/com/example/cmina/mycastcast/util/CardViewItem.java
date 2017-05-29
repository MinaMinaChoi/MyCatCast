package com.example.cmina.mycastcast.util;

/**
 * Created by cmina on 2017-02-28.
 */

public class CardViewItem {
    int image;
    String imagetitle;

    public int getImage() {
        return image;
    }

    public String getImagetitle(){
        return imagetitle;
    }

    public CardViewItem(int image, String imagetitle) {
        this.image = image;
        this.imagetitle = imagetitle;
    }
}
