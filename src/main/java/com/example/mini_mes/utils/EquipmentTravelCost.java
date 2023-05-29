package com.example.mini_mes.utils;

import org.eclipse.milo.opcua.sdk.client.model.types.variables.BaseVariableType;

import java.util.HashMap;
import java.util.Map;

public final class EquipmentTravelCost {
    private static Map<String, Integer> availableEquipment = initializeAvailableEquipment();

    private static Map<String, Integer> initializeAvailableEquipment(){
        Map<String, Integer> availableEquipment = new HashMap<>();
        availableEquipment.put("InboundEmitter", 2);
        availableEquipment.put("InboundBuffer", 2);
        availableEquipment.put("InboundWarehouse", 2);
        availableEquipment.put("InboundWarehouseExit", 2);
        availableEquipment.put("BothProducer", 2);
        availableEquipment.put("LidProducer", 2);
        availableEquipment.put("BaseProducer", 2);
        availableEquipment.put("ProductionRemover", 2);
        availableEquipment.put("ProductionEmitter", 2);
        availableEquipment.put("ExpeditionWarehouse", 2);
        availableEquipment.put("ExpeditionWarehouseExit", 2);
        availableEquipment.put("ExpeditionRemover", 2);
        availableEquipment.put("PartDisposer", 2);
        availableEquipment.put("Turntable", 2);
        availableEquipment.put("Conveyor2m", 2);
        availableEquipment.put("Conveyor4m", 4);
        availableEquipment.put("Conveyor6m", 6);

        return availableEquipment;
    }



    public static int getEquipmentTravelCost(String equipmentType){
        return availableEquipment.get(equipmentType);
    }
}
