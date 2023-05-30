package com.example.mini_mes.dijkstra;


import com.example.mini_mes.model.Equipment;
import com.example.mini_mes.model.EquipmentList;
import com.example.mini_mes.model.Machine;
import com.example.mini_mes.model.TargetPath;

import java.util.ArrayList;

public class PathManager {
    private static PathManager instance;
    private boolean initialized;

    TargetPath inboundEmitToBuffer, inboundBufferToWh;
    TargetPath productionWhToWhExit;
    ArrayList<Machine> machineList;
    TargetPath productionEmitToWh, productionEmitToDisposer;
    TargetPath expeditionWhToWhExit, expeditionWhExitToRemover;


    private PathManager() {
        initialized = false;
    }

    public static PathManager getInstance() {
        if (instance == null) {
            instance = new PathManager();
        }
        return instance;
    }

    public void initialize(EquipmentList equipmentList) {
        if (initialized) {
            throw new IllegalStateException("PathManager already initialized.");
        }

        // Check if the inputs are valid and handle errors
        if (!generatePaths(equipmentList)) {
            throw new IllegalArgumentException("Invalid parameter.");
        }

        // Initialize the singleton

        initialized = true;
    }

    private boolean generatePaths(EquipmentList equipmentList) {
        int id_source, id_destination;
        PathCost pathCost;

        //For inbound command orders
        id_source = equipmentList.getEquipmentIdByType("InboundEmitter");
        id_destination = equipmentList.getEquipmentIdByType("InboundBuffer");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.inboundEmitToBuffer = new TargetPath(id_source, pathCost.path);

        id_source = equipmentList.getEquipmentIdByType("InboundBuffer");
        id_destination = equipmentList.getEquipmentIdByType("InboundWarehouse");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.inboundBufferToWh = new TargetPath(id_source, pathCost.path);

        //For production command orders




        //For expedition command orders
        id_source = equipmentList.getEquipmentIdByType("ExpeditionWarehouse");
        id_destination = equipmentList.getEquipmentIdByType("ExpeditionWarehouseExit");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.expeditionWhToWhExit = new TargetPath(id_source, pathCost.path);

        id_source = equipmentList.getEquipmentIdByType("ExpeditionWarehouseExit");
        id_destination = equipmentList.getEquipmentIdByType("ExpeditionRemover");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.expeditionWhExitToRemover = new TargetPath(id_source, pathCost.path);


        for(int i=0; i<inboundEmitToBuffer.getPath().length; i++ ){
            System.out.println(inboundEmitToBuffer.getPath()[i] );
        }
        return true;
    }


    public TargetPath getInboundEmitToBuffer() {return inboundEmitToBuffer;}
    public TargetPath getInboundBufferToWh() {return inboundBufferToWh;}
    public TargetPath getProductionWhToWhExit() {return productionWhToWhExit;}
    public ArrayList<Machine> getMachineList() {return machineList;}
    public TargetPath getProductionEmitToWh() {return productionEmitToWh;}
    public TargetPath getProductionEmitToDisposer() {return productionEmitToDisposer;}
    public TargetPath getExpeditionWhToWhExit() {return expeditionWhToWhExit;}
    public TargetPath getExpeditionWhExitToRemover() {return expeditionWhExitToRemover;}
}
