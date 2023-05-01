package com.example.mini_mes.tasks;

import com.example.mini_mes.database.DatabaseHandler;
import com.example.mini_mes.model.Factory;
import com.example.mini_mes.model.Order;
import com.example.mini_mes.model.Part;
import com.example.mini_mes.opcua.OpcUaHandler;
import com.example.mini_mes.utils.Aliases;
import com.example.mini_mes.utils.PartProps;
import javafx.concurrent.Task;

public class MesTask extends Task<Void> {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();

    @Override
    protected Void call() throws Exception{
        while(true){
            if(/*!Factory.getInstance().isOngoingWeek()*/ false ){
                //Retrieves factory status until is ready to start the week
                dbHandler.retrieveFactoryStatus();
            }
            else{
                //Week was started by mini-ERP
                OpcUaHandler opcHandler = OpcUaHandler.getInstance();

                //************************** Order 1 **************************//
                Order order1 = new Order();

                int[] path1 = new int[50];
                path1[0] = 2; path1[1] = 3; path1[2] = -1;
                Part part1 = new Part(1,path1, PartProps.Pallet, PartProps.GreenRawMaterial, 20, Aliases.NONE, order1.getId());

                order1.setEmitOrder(1, part1);

                opcHandler.sendOrder(order1);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                opcHandler.setNewDataFlag(false);

                //************************** Esperar que a order 1 acabe ************************** //
                while(!opcHandler.isOrderFinished(order1) ){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //************************** Order 2 **************************//
                Order order2 = new Order();
                int[] path2 = new int[50];
                path1[0] = 2; path1[1] = 3; path1[2] = -1;
                Part part2 = new Part(1,path2, PartProps.Pallet, PartProps.GreenRawMaterial, 20, Aliases.NONE, order2.getId());

                order2.setOutWhOrder(3, 20, part2);

                opcHandler.sendOrder(order2);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                opcHandler.setNewDataFlag(false);

                //************************** Esperar que a order 2 acabe ************************** //
                while(!opcHandler.isOrderFinished(order2) ){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //************************** Order 3 **************************//
                Order order3 = new Order();
                int[] path3 = new int[50];
                path3[0] = 5; path3[1] = 6; path3[2] = -1;
                Part part3 = new Part(1,path3, PartProps.Pallet, PartProps.GreenRawMaterial, 0, Aliases.NONE, order3.getId());

                order3.setNewPathOrder(4, part3);

                opcHandler.sendOrder(order3);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                opcHandler.setNewDataFlag(false);

                //************************** Esperar que a order 3 acabe ************************** //
                while(!opcHandler.isOrderFinished(order3) ){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //************************** Order 4 **************************//
                Order order4 = new Order();

                int[] path4 = new int[50];
                path4[0] = 8; path4[1] = 9; path4[2] = 10; path4[3] = 11; path4[4] = 12; path4[5] = -1;
                Part part4 = new Part(1,path4, PartProps.EMPTY, PartProps.GreenRawMaterial, 0, Aliases.TO_LID, order4.getId());

                order4.setEmitOrder(7, part4);

                opcHandler.sendOrder(order4);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                opcHandler.setNewDataFlag(false);

                //************************** Esperar que a order 4 acabe ************************** //
                while(!opcHandler.isOrderFinished(order4) ){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                //************************** Order 5 **************************//
                Order order5 = new Order();

                int[] path5 = new int[50];
                path5[0] = 14; path5[1] = 15; path5[2] = -1;
                Part part5 = new Part(1,path5, PartProps.Pallet, PartProps.GreenRawMaterial, 10, Aliases.NONE, order5.getId());

                order5.setEmitOrder(13, part5);

                opcHandler.sendOrder(order5);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                opcHandler.setNewDataFlag(false);

                //************************** Esperar que a order 5 acabe ************************** //
                while(!opcHandler.isOrderFinished(order5) ){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //************************** Order 6 **************************//
                Order order6 = new Order();
                int[] path6 = new int[50];
                path6[0] = 15; path6[1] = -1;
                Part part6 = new Part(1,path6, PartProps.Pallet, PartProps.GreenRawMaterial, 10, Aliases.NONE, order6.getId());

                order6.setOutWhOrder(15, 10, part6);

                opcHandler.sendOrder(order6);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                opcHandler.setNewDataFlag(false);

                //************************** Esperar que a order 6 acabe ************************** //
                while(!opcHandler.isOrderFinished(order6) ){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //************************** Order 7 **************************//
                Order order7 = new Order();
                int[] path7 = new int[50];
                path7[0] = 17; path7[1] = 18; path7[2] = -1;
                Part part7 = new Part(1,path7, PartProps.Pallet, PartProps.GreenRawMaterial, 0, Aliases.NONE, order7.getId());

                order7.setNewPathOrder(16, part7);

                opcHandler.sendOrder(order7);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                opcHandler.setNewDataFlag(false);

                //************************** Esperar que a order 7 acabe ************************** //
                while(!opcHandler.isOrderFinished(order7) ){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }



                break;
            }
        }


        return null;

    }
}
