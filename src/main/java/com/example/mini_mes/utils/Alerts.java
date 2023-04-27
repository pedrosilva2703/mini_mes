package com.example.mini_mes.utils;

import javafx.scene.control.Alert;

public final class Alerts {

    private Alerts(){}

    public static void showError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
