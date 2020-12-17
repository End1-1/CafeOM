package com.cafeom.data;

import java.util.ArrayList;
import java.util.List;

public class GoodsData extends DataParser {
    public String f_recid;
    public String f_stateid;
    public String f_deliver;
    public String f_id;
    public String f_partner;
    public String f_goods;
    public String f_unit;
    public String f_qty;
    public String f_correct;

    public static GoodsData mInstance;
    public List<GoodsData> data = new ArrayList<>();

    public static void parseGoods(String s) {
        mInstance = DataParser.parse(s, GoodsData.class);
    }

    public static String listToString(List<GoodsData> l) {
        return gson().toJson(l);
    }
}
