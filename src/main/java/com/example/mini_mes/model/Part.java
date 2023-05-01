package com.example.mini_mes.model;

import com.example.mini_mes.utils.Aliases;

import java.util.Arrays;

public class Part {
    int id;
    int time_generated;
    int time_removed;
    int[] process_time = new int[50];
    int[] path = new int[50];
    int path_index;
    int type_base;
    int type_part;
    int final_lid;
    int final_base;
    int final_conveyor;
    int store_position;
    int op;
    int routing_mode;
    int order_id;

    public Part(int id, int[] path, int type_base, int type_part, int store_position, int op, int order_id) {
        this.id = id;
        this.time_generated = 0;
        this.time_removed = 0;
        Arrays.fill(this.process_time, 0);
        this.path = path;
        this.path_index = 0;
        this.type_base = type_base;
        this.type_part = type_part;
        this.final_lid = 0;
        this.final_base = 0;
        this.final_conveyor = 0;
        this.store_position = store_position;
        this.op = op;
        this.routing_mode = Aliases.MODE_GIVEN_PATH;
        this.order_id = order_id;
    }

    //Getters
    public int getId() {
        return id;
    }
    public int getTime_generated() {
        return time_generated;
    }
    public int getTime_removed() {
        return time_removed;
    }
    public int[] getProcess_time() {
        return process_time;
    }
    public int[] getPath() {
        return path;
    }
    public int getPath_index() {
        return path_index;
    }
    public int getType_base() {
        return type_base;
    }
    public int getType_part() {
        return type_part;
    }
    public int getFinal_lid() {
        return final_lid;
    }
    public int getFinal_base() {
        return final_base;
    }
    public int getFinal_conveyor() {
        return final_conveyor;
    }
    public int getStore_position() {
        return store_position;
    }
    public int getOp() {
        return op;
    }
    public int getRouting_mode() {
        return routing_mode;
    }
    public int getOrder_id() {
        return order_id;
    }
}
