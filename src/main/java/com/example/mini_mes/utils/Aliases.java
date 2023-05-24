package com.example.mini_mes.utils;

public class Aliases {
    public static final int MODE_NO_PATH    = 7;
    public static final int MODE_GIVEN_PATH = 6;
    public static final int TO_BASE         = 0;
    public static final int TO_LID          = 1;
    public static final int FROM_VISION     = -2;
    public static final int NONE            = -1;
    public static final int BY_COLOUR       = 8;
    public static final int BY_SHAPE        = 9;
    public static final int DONT_TOUCH      = 10;
    public static final int BLUE            = 11;
    public static final int GREEN           = 12;
    public static final int METAL           = 13;
    public static final int LID             = 14;
    public static final int BASE            = 15;
    public static final int RAW             = 16;
    public static final int ALL             = -7;

    public static int getOpValueByFinalType(String final_type){
        if( final_type.equals("GreenProductBase")
            || final_type.equals("BlueProductBase")
            || final_type.equals("MetalProductBase")){
            return TO_BASE;
        }
        else{
            return TO_LID;
        }
    }

    public static String getOpStringByFinalType(String final_type){
        if( final_type.equals("GreenProductBase")
                || final_type.equals("BlueProductBase")
                || final_type.equals("MetalProductBase")){
            return "To Base";
        }
        else{
            return "To Lid";
        }
    }
}
