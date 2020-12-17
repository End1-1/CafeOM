package com.cafeom.data;

import java.util.ArrayList;
import java.util.List;

public class OrderData extends DataParser {
    public String f_id;
    public String f_date;
    public String f_time;
    public String f_branchid;
    public String f_branchname = "";
    public String f_storeid;
    public String f_storename = "";
    public String f_login;
    public List<GoodsData> mGoodsOrder = new ArrayList<>();

    public static OrderData mInstance = new OrderData();
    public List<OrderData> data = new ArrayList<>();

    public void clear() {
        f_storename = "";
        f_branchname = "";
        f_storeid = "";
        f_branchid = "";
        f_id = "";
        mGoodsOrder.clear();
    }

    public static void parseOrders(String s) {
        mInstance = DataParser.parse(s, OrderData.class);
    }
}
