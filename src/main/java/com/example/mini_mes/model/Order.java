package com.example.mini_mes.model;

import com.example.mini_mes.utils.OrdersAliases;
import com.example.mini_mes.utils.Status;

import java.util.Arrays;

public class Order {
    private static int counter = 1;
    int id;
    int order_type;
    int[] target_group = new int[100];

    int order_status;

    int[] remove_part = new int[54];

    Part part_info;

    public Order() {
        this.id = counter++;
        this.order_type = OrdersAliases.NONE;
        Arrays.fill(this.target_group, 0);
        this.order_status = Status.EMPTY;
        Arrays.fill(this.remove_part, 0);
    }

    public void setEmitOrder(int target_group, Part part_info){
        this.order_type = OrdersAliases.EMIT;
        this.target_group[0] = target_group;
        this.part_info = part_info;
    }
    public void setOutWhOrder(int target_group, int remove_part, Part part_info){
        this.order_type = OrdersAliases.OUT_WH;
        this.target_group[0] = target_group;
        this.remove_part[0] = remove_part;
        this.part_info = part_info;
    }
    public void setNewPathOrder(int target_group, Part part_info){
        this.order_type = OrdersAliases.NEW_PATH;
        this.target_group[0] = target_group;
        this.part_info = part_info;
    }

    //Getters
    public int getId() {
        return id;
    }
    public int getOrder_type() {
        return order_type;
    }
    public int[] getTarget_group() {
        return target_group;
    }
    public int getOrder_status() {
        return order_status;
    }
    public int[] getRemove_part() {
        return remove_part;
    }
    public Part getPart_info() {
        return part_info;
    }
}
