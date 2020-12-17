package com.cafeom.data;

import java.util.List;

public class IdNameData {
    public String f_id;
    public String f_name;
    public List<IdNameData> data;

    public static IdNameData parseOrders(String s) {
        return DataParser.parse(s, IdNameData.class);
    }
}
