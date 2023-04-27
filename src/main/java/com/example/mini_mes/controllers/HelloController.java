package com.example.mini_mes.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;


public class HelloController implements Initializable {
    @FXML
    private Label welcomeText;

    private String commandsNodePath = "|var|CODESYS Control Win V3 x64.Application.commands";


    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    // Initialize method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("teste");
        opcuatest();


    }




    public void opcuatest(){
        // Connect to the server
        try {
            OpcUaClient client = OpcUaClient.create("opc.tcp://localhost:4840/");
            client.connect().get();

            //Para escrever INT
            NodeId nodeId = new NodeId(4, "|var|CODESYS Control Win V3 x64.Application.commands.orders.id");
            DataValue newValue = new DataValue(new Variant((short)12));
            StatusCode statusCode = client.writeValue(nodeId, newValue).get();
            if (statusCode.isGood()) {
                System.out.println("Value written successfully");
            } else {
                System.out.println("Failed to write value, status code: " + statusCode);
            }
            //Ler INT
            UaVariableNode mm = client.getAddressSpace().getVariableNode(nodeId);
            DataValue value = mm.readValue();
            Integer id = Integer.parseInt(value.getValue().getValue().toString());
            System.out.println( id );

            //Para escrever booleanos
            nodeId = new NodeId(4, "|var|CODESYS Control Win V3 x64.Application.commands.orders.new_data");
            newValue = new DataValue(new Variant(true));
            statusCode = client.writeValue(nodeId, newValue).get();
            if (statusCode.isGood()) {
                System.out.println("Value written successfully");
            } else {
                System.out.println("Failed to write value, status code: " + statusCode);
            }
            //Ler booleanos
            mm = client.getAddressSpace().getVariableNode(nodeId);
            value = mm.readValue();
            boolean new_data = Boolean.parseBoolean(value.getValue().getValue().toString());
            System.out.println( new_data );


            //Para escrever UDINT
            nodeId = new NodeId(4, "|var|CODESYS Control Win V3 x64.Application.commands.orders.part_info.process_time[0]");
            newValue = new DataValue(new Variant( Unsigned.uint(23) ));
            statusCode = client.writeValue(nodeId, newValue).get();
            if (statusCode.isGood()) {
                System.out.println("Value written successfully");
            } else {
                System.out.println("Failed to write value, status code: " + statusCode);
            }
            //Ler UDINT
            mm = client.getAddressSpace().getVariableNode(nodeId);
            value = mm.readValue();
            Integer time = Integer.parseInt(value.getValue().getValue().toString());
            System.out.println( time );


        } catch (UaException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }

}
