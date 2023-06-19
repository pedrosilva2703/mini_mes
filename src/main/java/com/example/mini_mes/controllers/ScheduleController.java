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

public class ScheduleController implements Initializable {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    Factory factory = Factory.getInstance();

    @FXML private AnchorPane anchor_EO;
    @FXML private AnchorPane anchor_IO;
    @FXML private AnchorPane anchor_PO;

    @FXML private TableView<Piece> tv_EO;
    @FXML private TableColumn<Piece, String> tc_EO_client;
    @FXML private TableColumn<Piece, String> tc_EO_status;
    @FXML private TableColumn<Piece, String> tc_EO_supplier;
    @FXML private TableColumn<Piece, String> tc_EO_type;
    @FXML private TableView<Piece> tv_IO;
    @FXML private TableColumn<Piece, String> tc_IO_client;
    @FXML private TableColumn<Piece, String> tc_IO_status;
    @FXML private TableColumn<Piece, String> tc_IO_supplier;
    @FXML private TableColumn<Piece, String> tc_IO_type;
    @FXML private TableView<Piece> tv_PO;
    @FXML private TableColumn<Piece, String> tc_PO_client;
    @FXML private TableColumn<Piece, Double> tc_PO_duration;
    @FXML private TableColumn<Piece, String> tc_PO_operation;
    @FXML private TableColumn<Piece, String> tc_PO_status;
    @FXML private TableColumn<Piece, String> tc_PO_type;

    ArrayList<Piece> ioList;
    ArrayList<Piece> poList;
    ArrayList<Piece> eoList;

    Thread refreshUI_Thread;

    public void interruptRefreshThread(){
        refreshUI_Thread.interrupt();
    }

    private void startRefreshUI_Thread(){
        refreshUI_Thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                updateUI();

                System.out.println("Schedule executing");
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
        ioList = dbHandler.getInboundPiecesByWeek(factory.getCurrent_week());
        poList = dbHandler.getProductionPiecesByWeek(factory.getCurrent_week());
        eoList = dbHandler.getExpeditionPiecesByWeek(factory.getCurrent_week());

        //Clear tables and add new data
        tv_IO.getItems().clear();
        if( ioList != null ){
            tv_IO.getItems().addAll( ioList );
            tv_IO.setPrefHeight( (tv_IO.getItems().size()+1.20) * tv_IO.getFixedCellSize() );
        }
        tv_PO.getItems().clear();
        if( poList != null ){
            tv_PO.getItems().addAll( poList );
            tv_PO.setPrefHeight( (tv_PO.getItems().size()+1.20) * tv_PO.getFixedCellSize() );
        }
        double io_height = anchor_IO.getPrefHeight()*0.75+tv_IO.getPrefHeight();
        AnchorPane.setTopAnchor(anchor_PO, io_height);

        tv_EO.getItems().clear();
        if( eoList != null ){
            tv_EO.getItems().addAll( eoList );
            tv_EO.setPrefHeight( (tv_EO.getItems().size()+1.20) * tv_EO.getFixedCellSize() );
        }
        // Constraint the TOP of the 3nd table to the Height of the 2nd table
        double po_height = anchor_PO.getPrefHeight()*0.75+tv_PO.getPrefHeight() + io_height;
        AnchorPane.setTopAnchor(anchor_EO, po_height);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tc_IO_client.setCellValueFactory(new PropertyValueFactory<Piece, String>("client") );
        tc_IO_status.setCellValueFactory(new PropertyValueFactory<Piece, String>("status") );
        tc_IO_supplier.setCellValueFactory(new PropertyValueFactory<Piece, String>("supplier") );;
        tc_IO_type.setCellValueFactory(new PropertyValueFactory<Piece, String>("type") );

        tc_PO_client.setCellValueFactory(new PropertyValueFactory<Piece, String>("client") );
        tc_PO_status.setCellValueFactory(new PropertyValueFactory<Piece, String>("status") );
        tc_PO_operation.setCellValueFactory(new PropertyValueFactory<Piece, String>("operation") );;
        tc_PO_type.setCellValueFactory(new PropertyValueFactory<Piece, String>("type") );
        tc_PO_duration.setCellValueFactory(new PropertyValueFactory<Piece, Double>("duration_production") );

        tc_EO_client.setCellValueFactory(new PropertyValueFactory<Piece, String>("client") );
        tc_EO_status.setCellValueFactory(new PropertyValueFactory<Piece, String>("status") );
        tc_EO_supplier.setCellValueFactory(new PropertyValueFactory<Piece, String>("supplier") );;
        tc_EO_type.setCellValueFactory(new PropertyValueFactory<Piece, String>("type") );

        updateUI();

        startRefreshUI_Thread();
    }
}
