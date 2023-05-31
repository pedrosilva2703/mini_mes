package com.example.mini_mes.model;

import com.example.mini_mes.dijkstra.PathCost;

public class Machine {
    int dt_id;
    String type;
    int current_pieces;
    int total_defective;
    int total_produced;

    TargetPath targetPath;
    PathCost pathCost;

    public Machine(int dt_id, String type) {
        this.dt_id = dt_id;
        this.type = type;
    }

    public int getDt_id() {
        return dt_id;
    }
    public void setDt_id(int dt_id) {
        this.dt_id = dt_id;
    }
    public int[] getPath() {
        return targetPath.getPath();
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getCurrent_pieces() {return current_pieces;}
    public void setCurrent_pieces(int current_pieces) {this.current_pieces = current_pieces;}
    public int getTotal_defective() {return total_defective;}
    public void setTotal_defective(int total_defective) {this.total_defective = total_defective;}
    public int getTotal_produced() {return total_produced;}
    public void setTotal_produced(int total_produced) {this.total_produced = total_produced;}

    public TargetPath getTargetPath() {return targetPath;}
    public void setTargetPath(TargetPath targetPath) {this.targetPath = targetPath;}

    public PathCost getPathCost() {return pathCost;}
    public void setPathCost(PathCost pathCost) {this.pathCost = pathCost;}

    public boolean isOperationCompatible(String final_type){
        if((final_type.equals("GreenProductLid") || final_type.equals("MetalProductLid") || final_type.equals("BlueProductLid")) &&
            (type.equals("LidProducer") || type.equals("BothProducer"))){
            return true;
        }
        else if((final_type.equals("GreenProductBase") || final_type.equals("MetalProductBase") || final_type.equals("BlueProductBase")) &&
                (type.equals("BaseProducer") || type.equals("BothProducer"))){
            return true;
        }
        else
            return false;

    }
}
