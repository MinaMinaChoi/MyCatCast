package com.example.cmina.mycastcast.util;

/**
 * Created by cmina on 2017-02-28.
 */
//메인프레그먼트에서 사용하는 아이템클래스, 구독목록sqlite에서도 사용.

public class RecyclerCastItem {
    String castImage;
    String castTitle;
    String castDbName;
    String category;

    public String getCastImage() {
        return castImage;
    }

    public String getCastTitle() {
        return castTitle;
    }

    public String getCastDbName() {
        return castDbName;
    }

    public String getCategory() {
        return category;
    }

    public RecyclerCastItem(String castImage, String castTitle, String castDbName, String category) {
        this.castImage = castImage;
        this.castTitle = castTitle;
        this.castDbName = castDbName;
        this.category = category;
    }

}
