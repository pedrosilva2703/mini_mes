package com.example.mini_mes.model;

import com.example.mini_mes.utils.EquipmentTravelCost;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlRootElement(name = "equipment")
@XmlAccessorType(XmlAccessType.FIELD)
public class Equipment {
    @XmlElement(name = "id_dt")
    private int id_dt;
    @XmlElement(name = "equipment_type")
    private String equipment_type;

    @XmlElementWrapper(name = "neighbourIdList")
    @XmlElement(name = "id")
    private ArrayList<Integer> neighbourIdList;

    public Equipment() {
    }

    public int getId_dt() {
        return id_dt;
    }

    public void setId_dt(int id_dt) {
        this.id_dt = id_dt;
    }

    public String getEquipment_type() {
        return equipment_type;
    }

    public void setEquipment_type(String equipment_type) {
        this.equipment_type = equipment_type;
    }

    public ArrayList<Integer> getNeighbourIdList() {
        return neighbourIdList;
    }


    public void setNeighbourIdList(ArrayList<Integer> neighbourIdList) {
        this.neighbourIdList = neighbourIdList;
    }


}
