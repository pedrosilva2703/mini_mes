package com.example.mini_mes.dijkstra;


import com.example.mini_mes.model.Equipment;
import com.example.mini_mes.model.EquipmentList;
import com.example.mini_mes.model.Machine;
import com.example.mini_mes.model.TargetPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        id_source = equipmentList.getEquipmentIdByType("InboundWarehouse");
        id_destination = equipmentList.getEquipmentIdByType("InboundWarehouseExit");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.productionWhToWhExit = new TargetPath(id_source, pathCost.path);

            //To produce pieces in machines
        ArrayList<Machine> unsortedMachineList = equipmentList.getMachines();

        int id_WhExit = equipmentList.getEquipmentIdByType("InboundWarehouseExit");
        int id_ProdRemover = equipmentList.getEquipmentIdByType("ProductionRemover");
        PathCost pathCost_WhExitToMachine;
        PathCost pathCost_MachineToRemover;
        for(Machine m : unsortedMachineList){
            pathCost_WhExitToMachine = Dijkstra.calculateShortestPath(equipmentList, id_WhExit, m.getDt_id());
            pathCost_MachineToRemover = Dijkstra.calculateShortestPath(equipmentList, m.getDt_id(), id_ProdRemover);

            //remove the machine id to avoid duplicate id in path
            pathCost_MachineToRemover.getPath().remove(0);

            pathCost = pathCost_WhExitToMachine;
            pathCost.getPath().addAll(pathCost_MachineToRemover.getPath());
            pathCost.setDijkstraCost(pathCost.getDijkstraCost() + pathCost_MachineToRemover.getDijkstraCost() );

            m.setPathCost(pathCost);
            m.setTargetPath(new TargetPath(id_WhExit, pathCost.getPath()));
        }

            //Sort the machine list by pathcost
        Collections.sort(unsortedMachineList, new Comparator<>() {
            @Override
            public int compare(Machine m1, Machine m2) {
                int cost1 = m1.getPathCost().dijkstraCost;
                int cost2 = m2.getPathCost().dijkstraCost;
                return Integer.compare(cost1, cost2);
            }
        });

        this.machineList = unsortedMachineList;


            //To store pieces in wh
        id_source = equipmentList.getEquipmentIdByType("ProductionEmitter");
        id_destination = equipmentList.getEquipmentIdByType("ExpeditionWarehouse");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.productionEmitToWh = new TargetPath(id_source, pathCost.path);

            //To dispose defective pieces
        id_source = equipmentList.getEquipmentIdByType("ProductionEmitter");
        id_destination = equipmentList.getEquipmentIdByType("PartDisposer");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.productionEmitToDisposer = new TargetPath(id_source, pathCost.path);

        //For expedition command orders
        id_source = equipmentList.getEquipmentIdByType("ExpeditionWarehouse");
        id_destination = equipmentList.getEquipmentIdByType("ExpeditionWarehouseExit");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.expeditionWhToWhExit = new TargetPath(id_source, pathCost.path);

        id_source = equipmentList.getEquipmentIdByType("ExpeditionWarehouseExit");
        id_destination = equipmentList.getEquipmentIdByType("ExpeditionRemover");
        pathCost = Dijkstra.calculateShortestPath(equipmentList, id_source, id_destination);
        this.expeditionWhExitToRemover = new TargetPath(id_source, pathCost.path);


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
