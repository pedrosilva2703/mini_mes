package com.example.mini_mes.controllers;

import com.example.mini_mes.Launcher;
import com.example.mini_mes.database.DatabaseHandler;
import com.example.mini_mes.model.Factory;
import com.example.mini_mes.utils.Alerts;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class SettingsController implements Initializable {
    Factory factory = Factory.getInstance();
    DatabaseHandler dbHandler;
    Preferences dbPrefs = Preferences.userNodeForPackage(SettingsController.class);

    @FXML private TextField tf_url;
    @FXML private TextField tf_port;
    @FXML private TextField tf_name;
    @FXML private TextField tf_schema;
    @FXML private TextField tf_username;
    @FXML private TextField tf_password;
    @FXML private Button btn_dbConn;

    @FXML
    private void onDbConnectButtonClicked(){
        String url          = tf_url.getText();
        String databaseName = tf_name.getText();
        String schema       = tf_schema.getText();
        String username     = tf_username.getText();
        String password     = tf_password.getText();
        int port;
        try{
            port = Integer.parseInt(tf_port.getText());
        }
        catch (NumberFormatException e){
            Alerts.showError("Port value is not an integer!");
            return;
        }

        dbHandler = DatabaseHandler.getInstance(url, port, databaseName, schema, username, password);
        if(!dbHandler.setConnection()){
            Alerts.showError("Connection failed!");
            return;
        }

        saveDbPreferences(url, port, databaseName, schema, username, password);
        updateInputsState();
    }


    // Manage input textfields and buttons
    private void updateInputsState(){
        if(     factory.isWaitingForDbConn() ){
            System.out.println("tou a espera de conection");
        }
        else{
            disableDbInputs();
            dbHandler = DatabaseHandler.getInstance();
        }
    }

    // Preferences functions
    private void saveDbPreferences(String url, int port, String databaseName, String schema, String username, String password){
        dbPrefs.put(    "url",          url);
        dbPrefs.putInt( "port",         port);
        dbPrefs.put(    "databaseName", databaseName);
        dbPrefs.put(    "schema",       schema);
        dbPrefs.put(    "username",     username);
        dbPrefs.put(    "password",     password);
    }
    private void loadDbPreferences(){
        if(dbPrefs.get("url", "").isEmpty()) return;

        tf_url.setText(      dbPrefs.get("url", "")                         );
        tf_port.setText(     Integer.toString(dbPrefs.getInt("port", 0))    );
        tf_name.setText(     dbPrefs.get("databaseName", "")                );
        tf_schema.setText(   dbPrefs.get("schema", "")                      );
        tf_username.setText( dbPrefs.get("username", "")                    );
        tf_password.setText( dbPrefs.get("password", "")                    );
    }

    private void disableDbInputs(){
        tf_url.setDisable(true);
        tf_username.setDisable(true);
        tf_name.setDisable(true);
        tf_port.setDisable(true);
        tf_schema.setDisable(true);
        tf_username.setDisable(true);
        tf_password.setDisable(true);

        btn_dbConn.setText("Connected");
        btn_dbConn.setStyle("-fx-background-color: green");
        btn_dbConn.setDisable(true);
    }
    // Initialize method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDbPreferences();
        updateInputsState();
    }
}