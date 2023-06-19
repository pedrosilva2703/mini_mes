package com.example.mini_mes.controllers;

import com.example.mini_mes.database.DatabaseHandler;
import com.example.mini_mes.model.Factory;
import com.example.mini_mes.model.Piece;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class WarehouseController implements Initializable {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    Factory factory = Factory.getInstance();

    @FXML private AnchorPane anchor_FP;
    @FXML private AnchorPane anchor_RM;

    @FXML private TableView<Piece> tv_FP;
    @FXML private TableColumn<Piece, String> tc_FP_client;
    @FXML private TableColumn<Piece, Integer> tc_FP_position;
    @FXML private TableColumn<Piece, String> tc_FP_supplier;
    @FXML private TableColumn<Piece, String> tc_FP_type;
    @FXML private TableColumn<Piece, Integer> tc_FP_week;

    @FXML private TableView<Piece> tv_RM;
    @FXML private TableColumn<Piece, String> tc_RM_client;
    @FXML private TableColumn<Piece, Integer> tc_RM_position;
    @FXML private TableColumn<Piece, String> tc_RM_supplier;
    @FXML private TableColumn<Piece, String> tc_RM_type;
    @FXML private TableColumn<Piece, Integer> tc_RM_week;

    ArrayList<Piece> fpList;
    ArrayList<Piece> rmList;

    Thread refreshUI_Thread;

    public void interruptRefreshThread(){
        refreshUI_Thread.interrupt();
    }

    private void startRefreshUI_Thread(){
        refreshUI_Thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                updateUI();

                System.out.println("Warehouse executing");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
         });

        refreshUI_Thread.setDaemon(true);
        refreshUI_Thread.start();
    }

    void updateUI(){
        //Retrieve data from database to lists
        rmList = dbHandler.getPiecesInWarehouse("stored");
        fpList = dbHandler.getPiecesInWarehouse("produced");

        //Clear tables and add new data
        tv_RM.getItems().clear();
        if( rmList != null ){
            tv_RM.getItems().addAll( rmList );
            tv_RM.setPrefHeight( (tv_RM.getItems().size()+1.20) * tv_RM.getFixedCellSize() );
        }

        tv_FP.getItems().clear();
        if( fpList != null ){
            tv_FP.getItems().addAll( fpList );
            tv_FP.setPrefHeight( (tv_FP.getItems().size()+1.20) * tv_FP.getFixedCellSize() );
        }

        // Constraint the TOP of the 2nd table to the Height of the first table
        double rm_height = anchor_RM.getPrefHeight()*0.75+tv_RM.getPrefHeight();
        AnchorPane.setTopAnchor( anchor_FP, rm_height );
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tc_RM_client.setCellValueFactory(new PropertyValueFactory<Piece, String>("client") );
        tc_RM_position.setCellValueFactory(new PropertyValueFactory<Piece, Integer>("wh_pos") );
        tc_RM_supplier.setCellValueFactory(new PropertyValueFactory<Piece, String>("supplier") );;
        tc_RM_type.setCellValueFactory(new PropertyValueFactory<Piece, String>("type") );
        tc_RM_week.setCellValueFactory(new PropertyValueFactory<Piece, Integer>("week_arrived") );

        tc_FP_client.setCellValueFactory(new PropertyValueFactory<Piece, String>("client") );
        tc_FP_position.setCellValueFactory(new PropertyValueFactory<Piece, Integer>("wh_pos") );
        tc_FP_supplier.setCellValueFactory(new PropertyValueFactory<Piece, String>("supplier") );;
        tc_FP_type.setCellValueFactory(new PropertyValueFactory<Piece, String>("type") );
        tc_FP_week.setCellValueFactory(new PropertyValueFactory<Piece, Integer>("week_produced") );

        updateUI();

        startRefreshUI_Thread();
    }
}
