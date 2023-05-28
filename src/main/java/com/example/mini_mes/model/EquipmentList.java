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
}
