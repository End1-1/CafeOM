package com.cafeom.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartnerOrderData extends DataParser {

    public List<String> mPartners = new ArrayList<>();
    public Map<String, List<GoodsData>> data = new HashMap<>();

    public static PartnerOrderData mInstance = new PartnerOrderData();


    public static PartnerOrderData parseString(String s) {
        PartnerOrderData p = DataParser.parse(s, PartnerOrderData.class);
        p.mPartners.addAll(p.data.keySet());
        return p;
    }
}
