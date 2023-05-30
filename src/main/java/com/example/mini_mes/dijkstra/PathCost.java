package com.example.mini_mes.dijkstra;

import java.util.ArrayList;

public class PathCost {
    ArrayList<Integer> path;
    int dijkstraCost;

    public PathCost(ArrayList<Integer> path, int dijkstraCost) {
        this.path = path;
        this.dijkstraCost = dijkstraCost;
    }

    public ArrayList<Integer> getPath() {
        return path;
    }

    public void setPath(ArrayList<Integer> path) {
        this.path = path;
    }

    public int getDijkstraCost() {
        return dijkstraCost;
    }

    public void setDijkstraCost(int dijkstraCost) {
        this.dijkstraCost = dijkstraCost;
    }
}
