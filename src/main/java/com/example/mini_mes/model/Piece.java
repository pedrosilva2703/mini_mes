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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFinal_type() {
        return final_type;
    }

    public void setFinal_type(String final_type) {
        this.final_type = final_type;
    }

    public Integer getWeek_arrived() {
        return week_arrived;
    }

    public void setWeek_arrived(Integer week_arrived) {
        this.week_arrived = week_arrived;
    }

    public Integer getWeek_produced() {
        return week_produced;
    }

    public void setWeek_produced(Integer week_produced) {
        this.week_produced = week_produced;
    }

    public Float getDuration_production() {
        return duration_production;
    }

    public void setDuration_production(Float duration_production) {
        this.duration_production = duration_production;
    }

    public boolean isSafety_stock() {
        return safety_stock;
    }

    public void setSafety_stock(boolean safety_stock) {
        this.safety_stock = safety_stock;
    }

    public Integer getWh_pos() {
        return wh_pos;
    }

    public void setWh_pos(Integer wh_pos) {
        this.wh_pos = wh_pos;
    }
}
