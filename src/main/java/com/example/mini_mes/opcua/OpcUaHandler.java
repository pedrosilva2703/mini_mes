package com.example.mini_mes.opcua;


import com.example.mini_mes.model.Order;
import com.example.mini_mes.model.Part;
import com.example.mini_mes.utils.Alerts;
import com.example.mini_mes.utils.Status;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;

import java.util.concurrent.ExecutionException;

public class OpcUaHandler {
    private static OpcUaHandler instance;

    private OpcUaClient client = null;
    private String address = "opc.tcp://localhost:4840/";
    private String commandsNodePath = "|var|CODESYS Control Win V3 x64.Application.commands";
    private int namespaceIndex = 4;
    private int order_feedback_size = 217;

    //Initialization methods
    private OpcUaHandler(){
        initializeOpcUaConnection();
    }
    public static OpcUaHandler getInstance(){
        if (instance == null) {
            instance = new OpcUaHandler();
        }
        return instance;
    }
    private void initializeOpcUaConnection() {
        try {
            client = OpcUaClient.create(address);
            client.connect().get();
            System.out.println("OPC-UA connected");
        } catch (UaException e) {
            e.printStackTrace();
            Alerts.showError("The connection to OPC-UA server failed");
        } catch (ExecutionException e) {
            e.printStackTrace();
            Alerts.showError("The connection to OPC-UA server failed");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Alerts.showError("The connection to OPC-UA server failed");
        }

    }

    //Methods for sending data to Digital Twin
    public boolean sendOrder(Order order){
        String orders_node_path = commandsNodePath + ".orders.";
        String part_node_path = orders_node_path + "part_info.";

        while(readBoolean(orders_node_path, "new_data") ){
            //wait until new_data is false
        }

        writeInt(order.getId(), orders_node_path, "id");
        writeInt(order.getOrder_type(), orders_node_path, "order_type");
        for (int i = 0; i < order.getTarget_group().length; i++) {
            int dt_idx = i+1;
            writeInt(order.getTarget_group()[i], orders_node_path, "target_group["+dt_idx+"]");
        }
        writeInt(order.getOrder_status(), orders_node_path, "order_status");
        for (int i = 0; i < order.getRemove_part().length; i++) {
            int dt_idx = i+1;
            writeInt(order.getRemove_part()[i], orders_node_path, "remove_part["+dt_idx+"]");
        }

        Part part = order.getPart_info();
        writeInt(part.getId(), part_node_path, "ID");
        for (int i = 0; i < part.getPath().length; i++) {
            writeInt(part.getPath()[i], part_node_path, "PATH["+i+"]");
        }
        writeInt(part.getType_base(), part_node_path, "type_base");
        writeInt(part.getType_part(), part_node_path, "type_part");
        writeInt(part.getFinal_lid(), part_node_path, "final_lid");
        writeInt(part.getFinal_base(), part_node_path, "final_base");
        writeInt(part.getStore_position(), part_node_path, "store_position");
        writeInt(part.getOp(), part_node_path, "op");
        writeInt(part.getRouting_mode(), part_node_path, "routing_mode");
        writeInt(part.getOrder_id(), part_node_path, "order_id");

        writeBool(true, orders_node_path, "new_data");

        System.out.println("\nOrder sent");
        System.out.println("Id: " + order.getId());
        System.out.println("order_type: " + order.getOrder_type());
        System.out.println("target: " + order.getTarget_group()[0]);
        System.out.println("first path" + order.getPart_info().getPath()[0]);
        return true;
    }
    public boolean isOrderFinished(Order order){
        String orders_out_node_path = commandsNodePath + ".orders_status_out";
        for(int i=1; i<order_feedback_size; i++){
            String curr_order_path = orders_out_node_path+"["+i+"].";

            int status = readInt(curr_order_path, "order_status");
            int id = readInt(curr_order_path, "part_info.order_id");

            if(id == order.getId() && status == Status.FINISHED ) {
                System.out.println("\n order " + id + " was finished \n");
                return true;
            }
        }
        return false;
    }
    public boolean setNewDataFlag(boolean value){
        String path = commandsNodePath + ".orders.";
        writeBool(value, path, "new_data");
        return true;
    }
    public boolean clearReadSigFlag(){
        String path = commandsNodePath;
        String variable = ".read_sig";

        while(readBoolean(path, variable) ){
            //wait until read_sig is false
        }
        writeBool(true, path, variable);
        while(readBoolean(path, variable) ){
            //wait until read_sig is false
        }
        return true;
    }

    public boolean writeInt(int value, String node_path, String variable){
        String identifier = node_path + variable;
        NodeId nodeId = new NodeId(namespaceIndex, identifier);

        DataValue newValue = new DataValue(new Variant((short)value));
        StatusCode statusCode = null;
        try {
            statusCode = client.writeValue(nodeId, newValue).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (statusCode.isGood()) {
            return true;
        } else {

            return false;
        }

    }
    public boolean writeUDInt(int value, String node_path, String variable){
        String identifier = node_path + variable;
        NodeId nodeId = new NodeId(namespaceIndex, identifier);
        System.out.println(identifier);
        DataValue newValue = new DataValue(new Variant( Unsigned.uint(value) ));
        StatusCode statusCode = null;
        try {
            statusCode = client.writeValue(nodeId, newValue).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (statusCode.isGood()) {
            return true;
        } else {
            System.out.println("Failed to write UDInt value, status code: " + statusCode);
            return false;
        }

    }
    public boolean writeBool(boolean value, String node_path, String variable){
        String identifier = node_path + variable;
        NodeId nodeId = new NodeId(namespaceIndex, identifier);

        DataValue newValue = new DataValue(new Variant(value));
        StatusCode statusCode = null;
        try {
            statusCode = client.writeValue(nodeId, newValue).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (statusCode.isGood()) {
            return true;
        } else {
            System.out.println("Failed to write int value, status code: " + statusCode);
            return false;
        }

    }

    public int readInt(String node_path, String variable){
        String identifier = node_path + variable;
        NodeId nodeId = new NodeId(namespaceIndex, identifier);

        UaVariableNode mm = null;
        DataValue value = null;
        try {
            mm = client.getAddressSpace().getVariableNode(nodeId);
            value = mm.readValue();
        } catch (UaException e) {
            e.printStackTrace();
            System.out.println("Failed to read int value");
            System.out.println(identifier);
        }
        int id = Integer.parseInt(value.getValue().getValue().toString());
        return id;
    }
    public boolean readBoolean(String node_path, String variable){
        String identifier = node_path + variable;
        NodeId nodeId = new NodeId(namespaceIndex, identifier);

        UaVariableNode mm = null;
        DataValue value = null;
        try {
            mm = client.getAddressSpace().getVariableNode(nodeId);
            value = mm.readValue();
        } catch (UaException e) {
            e.printStackTrace();
            System.out.println("Failed to read int value");
            System.out.println(identifier);
        }
        boolean returnValue = Boolean.parseBoolean(value.getValue().getValue().toString());
        return returnValue;
    }
}
