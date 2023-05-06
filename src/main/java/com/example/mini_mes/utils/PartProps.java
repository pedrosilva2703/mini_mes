package com.example.mini_mes.utils;

public class PartProps {
    public static final int NONE = 11;
    public static final int BlueProductLid = 1;
    public static final int BlueProductBase = 2;
    public static final int BlueRawMaterial = 3;
    public static final int MetalProductLid = 4;
    public static final int MetalProductBase = 5;
    public static final int MetalRawMaterial = 6;
    public static final int GreenProductLid = 7;
    public static final int GreenProductBase = 8;
    public static final int GreenRawMaterial = 9;
    public static final int Assembled = 10;
    public static final int EMPTY = 0;
    public static final int Pallet = 18;
    public static final int SquarePallet = 12;
    public static final int StackableBox = 13;
    public static final int SmallBox = 14;
    public static final int MediumBox = 15;
    public static final int LargeBox = 16;
    public static final int PalletizingBox = 17;

    public static int getTypeValue(String type){
        if (type.equals("BlueProductLid"))
            return BlueProductLid;
        else if (type.equals("BlueProductBase"))
            return BlueProductBase;
        else if (type.equals("BlueRawMaterial"))
            return BlueRawMaterial;
        else if (type.equals("MetalProductLid"))
            return MetalProductLid;
        else if (type.equals("MetalProductBase"))
            return MetalProductBase;
        else if (type.equals("MetalRawMaterial"))
            return MetalRawMaterial;
        else if (type.equals("GreenProductLid"))
            return GreenProductLid;
        else if (type.equals("GreenProductBase"))
            return GreenProductBase;
        else if (type.equals("GreenRawMaterial"))
            return GreenRawMaterial;

        return EMPTY;

    }
}
