package com.ecom.util;

import java.util.LinkedHashMap;

public class IndianStatesMap {

    // Using LinkedHashMap to maintain insertion order
    public static LinkedHashMap<String, String> getStatesLinkedHashMap() {
        LinkedHashMap<String, String> states = new LinkedHashMap<>();

        // States
        states.put("AP", "Andhra Pradesh");
        states.put("AR", "Arunachal Pradesh");
        states.put("AS", "Assam");
        states.put("BR", "Bihar");
        states.put("CT", "Chhattisgarh");
        states.put("GA", "Goa");
        states.put("GJ", "Gujarat");
        states.put("HR", "Haryana");
        states.put("HP", "Himachal Pradesh");
        states.put("JK", "Jammu and Kashmir");
        states.put("JH", "Jharkhand");
        states.put("KA", "Karnataka");
        states.put("KL", "Kerala");
        states.put("MP", "Madhya Pradesh");
        states.put("MH", "Maharashtra");
        states.put("MN", "Manipur");
        states.put("ML", "Meghalaya");
        states.put("MZ", "Mizoram");
        states.put("NL", "Nagaland");
        states.put("OR", "Odisha");
        states.put("PB", "Punjab");
        states.put("RJ", "Rajasthan");
        states.put("SK", "Sikkim");
        states.put("TN", "Tamil Nadu");
        states.put("TG", "Telangana");
        states.put("TR", "Tripura");
        states.put("UT", "Uttarakhand");
        states.put("UP", "Uttar Pradesh");
        states.put("WB", "West Bengal");

        // Union Territories
        states.put("AN", "Andaman and Nicobar Islands");
        states.put("CH", "Chandigarh");
        states.put("DN", "Dadra and Nagar Haveli");
        states.put("DD", "Daman and Diu");
        states.put("DL", "Delhi");
        states.put("LD", "Lakshadweep");
        states.put("PY", "Puducherry");

        return states;
    }

}
