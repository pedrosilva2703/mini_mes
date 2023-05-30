package com.example.mini_mes.model;

import java.util.ArrayList;

public class TargetPath {
    int target;
    int[] path;

    public TargetPath(int target, ArrayList<Integer> rawPath) {
        this.target = target;
        this.path = new int[50];
        rawPath.remove(0);
        int i;
        for(i=0; i<rawPath.size(); i++){
            this.path[i] = rawPath.get(i);
        }
        this.path[i] = -1;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int[] getPath() {
        return path;
    }

    public void setPath(int[] path) {
        this.path = path;
    }
}
