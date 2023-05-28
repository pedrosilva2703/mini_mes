package com.example.mini_mes.controllers;

import com.example.mini_mes.Launcher;
import com.example.mini_mes.database.DatabaseHandler;
import com.example.mini_mes.model.Equipment;
import com.example.mini_mes.model.EquipmentList;
import com.example.mini_mes.model.Factory;
import com.example.mini_mes.tasks.MesTask;
import com.example.mini_mes.utils.Alerts;
import com.example.mini_mes.utils.Verifier;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

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

        dbHandler = DatabaseHandler.getInstance();
        MesTask mesTask = new MesTask();
        Thread thread = new Thread(mesTask);
        thread.setDaemon(true);
        thread.start();

        updateInputsState();
    }

    @FXML
    private void onLoadButtonClicked(){
        EquipmentList equipmentList;
        try {
            System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, JAXBContextFactory.class.getName());
            File file = new File("layout.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(EquipmentList.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            equipmentList = (EquipmentList) unmarshaller.unmarshal(file);

        } catch (JAXBException e) {
            e.printStackTrace();
            Alerts.showError("XML file parsing error");
            return;
        }

        String layoutVerifierResult = Verifier.isEquipmentListValid(equipmentList);
        if(!layoutVerifierResult.equals("OK") ){
            Alerts.showError(layoutVerifierResult);
            return;
        }




    }

    // Manage input textfields and buttons
    private void updateInputsState(){
        if(     factory.isWaitingForDbConn() ){

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