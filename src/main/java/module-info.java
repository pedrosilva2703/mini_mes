module com.example.mini_mes {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.mini_mes to javafx.fxml;
    exports com.example.mini_mes;
}