package com.example.mini_mes.controllers;

import com.example.mini_mes.Launcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


import java.io.IOException;

public class LayoutController {
    @FXML
    private BorderPane mainPane;

    @FXML private Button firstButton;
    @FXML private Button secondButton;
    @FXML private Button settingsButton;


    @FXML
    private void onFirstButtonClick(){

    }
    @FXML
    private void onSecondButtonClick(){

    }
    @FXML
    private void onSettingsButtonClick(){
        loadPage("Settings");
        refreshButtonStates(settingsButton);
    }


    private void unselectButton(Button b){
        b.getStyleClass().remove("menu-app-button-selected");
        b.getStyleClass().add("menu-app-button");
    }
    private void selectButton(Button b){
        b.getStyleClass().remove("menu-app-button");
        b.getStyleClass().add("menu-app-button-selected");
    }

    private void refreshButtonStates(Button clickedButton){
        // Unselect all buttons
        unselectButton(firstButton);
        unselectButton(secondButton);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}