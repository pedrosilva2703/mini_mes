package com.example.mini_mes.controllers;

import com.example.mini_mes.model.Order;
import com.example.mini_mes.model.Part;
import com.example.mini_mes.opcua.OpcUaHandler;
import com.example.mini_mes.utils.Aliases;
import com.example.mini_mes.utils.PartProps;
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

    void testingexample(){
        OpcUaHandler opcHandler = OpcUaHandler.getInstance();

        //************************** Order 1 **************************//
        Order order1 = new Order();

        int[] path1 = new int[50];
        path1[0] = 2; path1[1] = 3; path1[2] = -1;
        Part part1 = new Part(1,path1, PartProps.Pallet, PartProps.GreenRawMaterial, 20, Aliases.NONE, order1.getId());

        order1.setEmitOrder(1, part1);

        opcHandler.sendOrder(order1);

        //************************** Esperar que a order 1 acabe ************************** //
        while(!opcHandler.isOrderFinished(order1) ){
        }

        //************************** Order 2 **************************//
        Order order2 = new Order();
        int[] path2 = new int[50];
        path1[0] = 2; path1[1] = 3; path1[2] = -1;
        Part part2 = new Part(1,path2, PartProps.Pallet, PartProps.GreenRawMaterial, 20, Aliases.NONE, order2.getId());

        order2.setOutWhOrder(3, 20, part2);

        opcHandler.sendOrder(order2);

        //************************** Esperar que a order 2 acabe ************************** //
        while(!opcHandler.isOrderFinished(order2) ){

        }

        //************************** Order 3 **************************//
        Order order3 = new Order();
        int[] path3 = new int[50];
        path3[0] = 5; path3[1] = 6; path3[2] = -1;
        Part part3 = new Part(1,path3, PartProps.Pallet, PartProps.GreenRawMaterial, 0, Aliases.NONE, order3.getId());

        order3.setNewPathOrder(4, part3);

        opcHandler.sendOrder(order3);

        //************************** Esperar que a order 3 acabe ************************** //
        while(!opcHandler.isOrderFinished(order3) ){

        }


        //************************** Order 4 **************************//
        Order order4 = new Order();

        int[] path4 = new int[50];
        path4[0] = 8; path4[1] = 9; path4[2] = 10; path4[3] = 11; path4[4] = 12; path4[5] = -1;
        Part part4 = new Part(1,path4, PartProps.EMPTY, PartProps.GreenRawMaterial, 0, Aliases.TO_LID, order4.getId());

        order4.setEmitOrder(7, part4);

        opcHandler.sendOrder(order4);

        //************************** Esperar que a order 4 acabe ************************** //
        while(!opcHandler.isOrderFinished(order4) ){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        //************************** Order 5 **************************//
        Order order5 = new Order();

        int[] path5 = new int[50];
        path5[0] = 14; path5[1] = 15; path5[2] = -1;
        Part part5 = new Part(1,path5, PartProps.Pallet, PartProps.GreenRawMaterial, 10, Aliases.NONE, order5.getId());

        order5.setEmitOrder(13, part5);

        opcHandler.sendOrder(order5);

        //************************** Esperar que a order 5 acabe ************************** //
        while(!opcHandler.isOrderFinished(order5) ){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //************************** Order 6 **************************//
        Order order6 = new Order();
        int[] path6 = new int[50];
        path6[0] = 15; path6[1] = -1;
        Part part6 = new Part(1,path6, PartProps.Pallet, PartProps.GreenRawMaterial, 10, Aliases.NONE, order6.getId());

        order6.setOutWhOrder(15, 10, part6);

        opcHandler.sendOrder(order6);


        //************************** Esperar que a order 6 acabe ************************** //
        while(!opcHandler.isOrderFinished(order6) ){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //************************** Order 7 **************************//
        Order order7 = new Order();
        int[] path7 = new int[50];
        path7[0] = 17; path7[1] = 18; path7[2] = -1;
        Part part7 = new Part(1,path7, PartProps.Pallet, PartProps.GreenRawMaterial, 0, Aliases.NONE, order7.getId());

        order7.setNewPathOrder(16, part7);

        opcHandler.sendOrder(order7);

        //************************** Esperar que a order 7 acabe ************************** //
        while(!opcHandler.isOrderFinished(order7) ){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }

}
