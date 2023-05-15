package com.example.mini_mes.model;

public class Machine {
    int dt_id;
    int[] path;
    String type;

    public Machine(int dt_id, int[] path, String type) {
        this.dt_id = dt_id;
        this.path = path;
        this.type = type;
    }

    public int getDt_id() {
        return dt_id;
    }
    public void setDt_id(int dt_id) {
        this.dt_id = dt_id;
    }
    public int[] getPath() {
        return path;
    }
    public void setPath(int[] path) {
        this.path = path;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public boolean isOperationCompatible(String final_type){
        if((final_type.equals("GreenProductLid") || final_type.equals("MetalProductLid") || final_type.equals("BlueProductLid")) &&
            (type.equals("LidProducer") || type.equals("DualProducer"))){
            return true;
        }
        else if((final_type.equals("GreenProductBase") || final_type.equals("MetalProductBase") || final_type.equals("BlueProductBase")) &&
                (type.equals("BaseProducer") || type.equals("DualProducer"))){
            return true;
        }
        else
            return false;

    }
}
