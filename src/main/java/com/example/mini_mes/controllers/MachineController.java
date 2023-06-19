package com.example.mini_mes.controllers;

import com.example.mini_mes.database.DatabaseHandler;
import com.example.mini_mes.model.Factory;
import com.example.mini_mes.model.Machine;
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

public class MachineController implements Initializable {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    Factory factory = Factory.getInstance();

    @FXML private TableColumn<Machine, Integer> tc_Current;
    @FXML private TableColumn<Machine, Integer> tc_Defective;
    @FXML private TableColumn<Machine, Integer> tc_Id;
    @FXML private TableColumn<Machine, Integer> tc_Total;
    @FXML private TableColumn<Machine, String> tc_Type;
    @FXML private TableView<Machine> tv_machine;

    ArrayList<Machine> mList;

    Thread refreshUI_Thread;

    public void interruptRefreshThread(){
        refreshUI_Thread.interrupt();
    }

    private void startRefreshUI_Thread(){
        refreshUI_Thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                updateUI();

                System.out.println("Machine executing");
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
        mList = dbHandler.getMachines(factory.getCurrent_week() );
        
        //Clear tables and add new data
        tv_machine.getItems().clear();
        if( mList != null ){
            tv_machine.getItems().addAll( mList );
            tv_machine.setPrefHeight( (tv_machine.getItems().size()+1.20) * tv_machine.getFixedCellSize() );
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tc_Current.setCellValueFactory(new PropertyValueFactory<Machine, Integer>("current_pieces") );
        tc_Defective.setCellValueFactory(new PropertyValueFactory<Machine, Integer>("total_defective") );
        tc_Id.setCellValueFactory(new PropertyValueFactory<Machine, Integer>("dt_id") );;
        tc_Total.setCellValueFactory(new PropertyValueFactory<Machine, Integer>("total_produced") );
        tc_Type.setCellValueFactory(new PropertyValueFactory<Machine, String>("type") );

        updateUI();

        startRefreshUI_Thread();
    }
}
