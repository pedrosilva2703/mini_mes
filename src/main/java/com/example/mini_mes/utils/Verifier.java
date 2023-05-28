package com.example.mini_mes.utils;


import com.example.mini_mes.model.Equipment;
import com.example.mini_mes.model.EquipmentList;
import javafx.scene.control.TextField;

import java.util.*;

public final class Verifier {

    private Verifier(){}

    public static boolean isInteger(TextField tf){
        int int_field;
        try{
            int_field = Integer.parseInt(tf.getText());
        }
        catch (NumberFormatException e){
            System.out.println(e);
            return false;
        }
        catch (RuntimeException e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    public static boolean isDouble(TextField tf){
        double double_field;
        try{
            double_field = Double.parseDouble(tf.getText());
        }
        catch (NumberFormatException e){
            System.out.println(e);
            return false;
        }
        catch (RuntimeException e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    public static String isEquipmentListValid(EquipmentList list){
        Map<String, Integer> equipmentCounts = new HashMap<>();
        List<String> availableEquipmentTypes = Arrays.asList(
                "InboundEmitter",
                "InboundBuffer",
                "InboundWarehouse",
                "InboundWarehouseExit",
                "BothProducer",
                "LidProducer",
                "BaseProducer",
                "ProductionRemover",
                "ProductionEmitter",
                "ExpeditionWarehouse",
                "ExpeditionWarehouseExit",
                "ExpeditionRemover",
                "PartDisposer"
        );
        for(String eq_type : availableEquipmentTypes){
            equipmentCounts.put(eq_type, 0);
        }

        Map<String, Integer> conveyorCounts = new HashMap<>();
        List<String> availableConveyorTypes = Arrays.asList(
                "Turntable",
                "Conveyor2m",
                "Conveyor4m",
                "Conveyor6m"
        );
        for(String conv_type : availableConveyorTypes){
            conveyorCounts.put(conv_type, 0);
        }


        //Count how many equipments exist from each type
        for(Equipment eq : list.getEquipmentList() ){
            String curr_type = eq.getEquipment_type();
            int count = equipmentCounts.getOrDefault(curr_type, -1);
            //In case of this type not being in equipmentList, check conveyorList
            if(count == -1){
                count = conveyorCounts.getOrDefault(curr_type, -2);
                //If it's not in conveyorList, then this type is not supported
                if(count == -2){
                    return "The equipment of type " + curr_type + " is not supported by this application";
                }
                conveyorCounts.put(curr_type, count + 1);
                continue;
            }
            equipmentCounts.put(curr_type, count + 1);
        }

        //Verify if it has at least 1 BothProducer or 1 LidProducer AND BaseProducer
        int bothProducerCount = equipmentCounts.get("BothProducer");
        int lidProducerCount = equipmentCounts.get("LidProducer");
        int baseProducerCounter = equipmentCounts.get("BaseProducer");
        if(bothProducerCount==0 && (lidProducerCount==0 || baseProducerCounter==0) ){
            return "The layout needs at least 1 BothProducer or else 1 LidProducer and 1 BaseProducer";
        }

        //Verify if there is 1 from each type (except for the machines)
        for (Map.Entry<String, Integer> entry : equipmentCounts.entrySet()) {
            String equipmentType = entry.getKey();
            int count = entry.getValue();

            if( equipmentType.equals("BothProducer")
                ||  equipmentType.equals("LidProducer")
                ||  equipmentType.equals("BaseProducer") ){
                continue;
            }

            if(count==0){
                return "There is no "+equipmentType+" in this layout!";
            }

        }

        return "OK";

    }

}
