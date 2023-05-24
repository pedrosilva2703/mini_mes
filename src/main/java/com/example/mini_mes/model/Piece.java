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

    Machine allocated_machine;
    long start_production;

    String client;
    String operation;
    String supplier;

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

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Integer getId() {
        return id;
    }

    public long getStart_production() {
        return start_production;
    }

    public void setStart_production(long start_production) {
        this.start_production = start_production;
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

    public void setAllocated_machine(Machine machine){
        allocated_machine = machine;
    }
    public Machine getAllocated_machine(){ return allocated_machine;}
}
