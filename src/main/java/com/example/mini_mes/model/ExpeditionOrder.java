package com.example.mini_mes.model;

import java.util.ArrayList;

public class ExpeditionOrder {
    Integer id;
    int week;
    int quantity;
    String status;
    ArrayList<Piece> pieces;

    public ExpeditionOrder(Integer id, int week, String status, ArrayList<Piece> pieces) {
        this.id = id;
        this.week = week;
        this.pieces = pieces;
        this.status = status;
        this.quantity = pieces.size();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(ArrayList<Piece> pieces) {
        this.pieces = pieces;
    }

    public void addPiece(Piece p){
        this.pieces.add(p);
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

}
