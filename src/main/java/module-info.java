module com.example.mini_mes {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires postgresql;
    requires java.prefs;
    requires org.eclipse.milo.opcua.sdk.client;
    requires org.eclipse.milo.opcua.stack.core;
    requires java.xml.bind;
    requires org.eclipse.persistence.moxy;


    opens com.example.mini_mes to javafx.fxml;
    exports com.example.mini_mes;
    exports com.example.mini_mes.controllers;
    opens com.example.mini_mes.controllers to javafx.fxml;

    exports com.example.mini_mes.model;
    opens com.example.mini_mes.model;
    exports com.example.mini_mes.dijkstra;
    opens com.example.mini_mes.dijkstra;

}