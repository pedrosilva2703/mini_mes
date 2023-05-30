package com.example.mini_mes.dijkstra;

import com.example.mini_mes.model.Equipment;
import com.example.mini_mes.model.EquipmentList;
import com.example.mini_mes.utils.EquipmentTravelCost;

import java.util.ArrayList;
import java.util.Arrays;

public class Dijkstra {

    public static PathCost calculateShortestPath(EquipmentList list, int sourceId, int destinationId) {
        ArrayList<Equipment> equipmentList = list.getEquipmentList();

        int[][] costMatrix = costMatrixBuilder(equipmentList);
        int sourcePos = getEquipmentPosInList(equipmentList, sourceId);
        int destPos = getEquipmentPosInList(equipmentList, destinationId);

        int totalVertex = equipmentList.size();

        int[] distances = new int[totalVertex];
        boolean[] isVisited = new boolean[totalVertex];
        int[] previousVertex = new int[totalVertex];

        Arrays.fill(distances, Integer.MAX_VALUE);
        Arrays.fill(previousVertex, -1);

        distances[sourcePos] = 0;

        for(int i = 0; i < totalVertex - 1; i++){
            int minDistance = Integer.MAX_VALUE;
            int minIdx = -1;

            //Find the unvisited vertex with the minimum distance
            for(int j = 0; j < totalVertex; j++){
                if(!isVisited[j] && distances[j] < minDistance){
                    minDistance = distances[j];
                    minIdx = j;
                }
            }

            //If there are no more vertexes to search, end the search
            if(minIdx == -1){
                break;
            }

            isVisited[minIdx] = true;

            //Update the array of known distances
            for(int j = 0; j < totalVertex; j++) {
                int cost = costMatrix[minIdx][j];
                if(     cost != 0
                        && !isVisited[j] && distances[minIdx] != Integer.MAX_VALUE
                        && (distances[minIdx] + cost < distances[j])
                ){
                    distances[j] = distances[minIdx] + cost;
                    previousVertex[j] = minIdx;
                }
            }
        }

        //Build shortest path sequence using the previousVertex array
        ArrayList<Integer> path = new ArrayList<>();
        //Starting with the destPos
        int currentVertex = destPos;
        while(currentVertex != -1){
            path.add(0, currentVertex);
            currentVertex = previousVertex[currentVertex];
        }

        //Convert the array containing the positions into the actual ids and store total cost
        ArrayList<Integer> pathIds = new ArrayList<>();
        int totalCost = 0;
        for(int i :path){
            int id = equipmentList.get(i).getId_dt();
            pathIds.add(id);

            totalCost += EquipmentTravelCost.getEquipmentTravelCost(equipmentList.get(i).getEquipment_type());
        }

        return new PathCost(pathIds, totalCost);

    }

    private static int[][] costMatrixBuilder(ArrayList<Equipment> equipmentList){
        int totalVertex = equipmentList.size();
        int[][] costMatrix = new int[totalVertex][totalVertex];

        //Initialize the costMatrix with 0 (not accessible paths)
        for(int i=0; i<totalVertex; i++){
            for(int j=0; j<totalVertex; j++){
                costMatrix[i][j] = 0;
            }
        }

        //Fill positions of vertex that are connected with the respective cost
        for(int i=0; i<totalVertex-1; i++){
            Equipment currentEquipment = equipmentList.get(i);
            String type = currentEquipment.getEquipment_type();
            int cost = EquipmentTravelCost.getEquipmentTravelCost(type);
            for(int currentNeighbourId : currentEquipment.getNeighbourIdList() ){
                int currentNeighbourPos = getEquipmentPosInList(equipmentList, currentNeighbourId);
                costMatrix[i][currentNeighbourPos] = cost;
            }
        }

        return costMatrix;

    }

    private static int getEquipmentPosInList(ArrayList<Equipment> equipmentList, int searchingId){
        int i=0;
        for(i=0; i<equipmentList.size(); i++){
            Equipment currentEquipment = equipmentList.get(i);
            if(currentEquipment.getId_dt() == searchingId){
                break;
            }
        }
        return i;
    }

}
