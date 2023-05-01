package com.example.mini_mes.model;

public class Piece {
    Integer id;
    String type;
    String status;
    String final_type;
    Integer week_arrived;
    Integer week_produced;
    Float duration_production;
    boolean safety_stock;
    Integer wh_pos;

    int[] path = new int[50];
    int current_location;

    public Piece(Integer id, String type, String status, String final_type, Integer week_arrived, Integer week_produced, Float duration_production, boolean safety_stock, Integer wh_pos) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.final_type = final_type;
        this.week_arrived = week_arrived;
        this.week_produced = week_produced;
        this.duration_production = duration_production;
        this.safety_stock = safety_stock;
        this.wh_pos = wh_pos;
    }


}
