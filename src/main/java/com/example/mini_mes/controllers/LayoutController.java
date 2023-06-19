package com.example.mini_mes.controllers;

import com.example.mini_mes.Launcher;
import com.example.mini_mes.dijkstra.PathManager;
import com.example.mini_mes.model.Factory;
import com.example.mini_mes.utils.Alerts;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LayoutController{
    ScheduleController scheduleController;
    MachineController machineController;
    WarehouseController warehouseController;
    HistoryController historyController;

    @FXML private BorderPane mainPane;

    @FXML private Button scheduleButton;
    @FXML private Button warehouseButton;
    @FXML private Button machineButton;
    @FXML private Button historyButton;
    @FXML private Button settingsButton;

    private String navigationError = "Please ensure all configurations on the 'Settings' page are completed";

    @FXML
    private void onScheduleButtonClick(){
        Factory factory = Factory.getInstance();
        if( factory.isWaitingForDbConn() || !PathManager.getInstance().isInitialized() ){
            Alerts.showInfo(navigationError);
            return;
        }
        interruptActiveThreads();
        loadPage("Schedule");
        refreshButtonStates(scheduleButton);
    }
    @FXML
    private void onWarehouseButtonClick(){
        Factory factory = Factory.getInstance();
        if( factory.isWaitingForDbConn() || !PathManager.getInstance().isInitialized()){
            Alerts.showInfo(navigationError);
            return;
        }
        interruptActiveThreads();
        loadPage("Warehouse");
        refreshButtonStates(warehouseButton);
    }
    @FXML
    private void onMachineButtonClick(){
        Factory factory = Factory.getInstance();
        if( factory.isWaitingForDbConn() || !PathManager.getInstance().isInitialized()){
            Alerts.showInfo(navigationError);
            return;
        }
        interruptActiveThreads();
        loadPage("Machine");
        refreshButtonStates(machineButton);
    }
    @FXML
    private void onHistoryButtonClick(){
        Factory factory = Factory.getInstance();
        if( factory.isWaitingForDbConn() || !PathManager.getInstance().isInitialized() ){
            Alerts.showInfo(navigationError);
            return;
        }
        interruptActiveThreads();
        loadPage("History");
        refreshButtonStates(historyButton);
    }
    @FXML
    private void onSettingsButtonClick(){
        interruptActiveThreads();
        loadPage("Settings");
        refreshButtonStates(settingsButton);
        return;
    }

    private void interruptActiveThreads(){
        if(scheduleController!=null){
            scheduleController.interruptRefreshThread();
        }
        if(warehouseController!=null){
            warehouseController.interruptRefreshThread();
        }
        if(machineController!=null){
            machineController.interruptRefreshThread();
        }
        if(historyController!=null){
            historyController.interruptRefreshThread();
        }
    }
    private void unselectButton(Button b){
        b.getStyleClass().remove("menu-app-button-selected");
        b.getStyleClass().add("menu-app-button");
        b.setDisable(false);
    }
    private void selectButton(Button b){
        b.getStyleClass().remove("menu-app-button");
        b.getStyleClass().add("menu-app-button-selected");
        b.setDisable(true);
    }
    private void refreshButtonStates(Button clickedButton){
        // Unselect all buttons
        unselectButton(scheduleButton);
        unselectButton(warehouseButton);
        unselectButton(machineButton);
        unselectButton(historyButton);
        unselectButton(settingsButton);

        // Select clicked button
        selectButton(clickedButton);
    }

    private void loadPage(String page){
        try {
            FXMLLoader contentLoader = new FXMLLoader(Launcher.class.getResource(page+".fxml"));
            AnchorPane content = contentLoader.load();

            // Add the navigation menu to the left side of the BorderPane
            mainPane.setCenter(content);

            if(page.equals("Schedule") ){
                ScheduleController scheduleController = contentLoader.getController();
                this.scheduleController = scheduleController;
            }
            if(page.equals("Warehouse") ){
                WarehouseController warehouseController = contentLoader.getController();
                this.warehouseController = warehouseController;
            }
            if(page.equals("Machine") ){
                MachineController machineController = contentLoader.getController();
                this.machineController = machineController;
            }
            if(page.equals("History") ){
                HistoryController historyController = contentLoader.getController();
                this.historyController = historyController;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}