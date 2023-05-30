package com.example.mini_mes.model;



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "equipmentList")
@XmlAccessorType(XmlAccessType.FIELD)
public class EquipmentList {
    @XmlElement(name = "equipment")
    private ArrayList<Equipment> equipmentList;

    public EquipmentList() {
        equipmentList = new ArrayList<>();
    }

    public ArrayList<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(ArrayList<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public int getEquipmentIdByType(String type){
        int id = -1;
        for(Equipment eq : equipmentList){
            if(eq.getEquipment_type().equals(type) ) id = eq.getId_dt();
        }
        return id;
    }
    public ArrayList<Machine> getMachines(){
        ArrayList<Machine> machineList = new ArrayList<>();
        for(Equipment eq : equipmentList){
            if( eq.getEquipment_type().equals("BothProducer")
                || eq.getEquipment_type().equals("LidProducer")
                || eq.getEquipment_type().equals("BaseProducer")){
                machineList.add(new Machine(eq.getId_dt(), eq.getEquipment_type()) );
            }
        }
        return machineList;
    }
}
