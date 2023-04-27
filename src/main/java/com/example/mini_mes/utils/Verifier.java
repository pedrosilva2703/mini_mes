package com.example.mini_mes.utils;


import javafx.scene.control.TextField;

public final class Verifier {

    private Verifier(){}

    public static boolean isInteger(TextField tf){
        int int_field;
        try{
            int_field = Integer.parseInt(tf.getText());
        }
        catch (NumberFormatException e){
            System.out.println(e);
            return false;
        }
        catch (RuntimeException e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    public static boolean isDouble(TextField tf){
        double double_field;
        try{
            double_field = Double.parseDouble(tf.getText());
        }
        catch (NumberFormatException e){
            System.out.println(e);
            return false;
        }
        catch (RuntimeException e){
            System.out.println(e);
            return false;
        }
        return true;
    }

}
