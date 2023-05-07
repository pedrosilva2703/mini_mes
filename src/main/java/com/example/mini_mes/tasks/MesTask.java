package com.example.mini_mes.tasks;

import com.example.mini_mes.database.DatabaseHandler;
import com.example.mini_mes.model.*;
import com.example.mini_mes.opcua.OpcUaHandler;
import com.example.mini_mes.utils.Aliases;
import com.example.mini_mes.utils.OrdersAliases;
import com.example.mini_mes.utils.PartProps;
import javafx.concurrent.Task;

import java.util.ArrayList;

public class MesTask extends Task<Void> {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();


    private void test(){
        System.out.println("aa");
    }
    @Override
    protected Void call() throws Exception{
        while(true){
            dbHandler.retrieveFactoryStatus();
            while(!Factory.getInstance().isOngoingWeek() ){
                //Retrieves factory status until is ready to start the week
                dbHandler.retrieveFactoryStatus();
            }
            Factory.getInstance().incrementWeek();
            int current_week = Factory.getInstance().getCurrent_week();

            OpcUaHandler opcHandler = OpcUaHandler.getInstance();

            //Week was started by mini-ERP
            if(false) break;

            //------------------- Expedition Orders setup -------------------//

            //Definir os paths e targets a serem usados
            int[] path_out_1 = new int[50];
            int[] path_out_2 = new int[50];
            path_out_1[0] = 15; path_out_1[1] = 16; path_out_1[2] = -1;
            path_out_2[0] = 17; path_out_2[1] = 18; path_out_2[2] = -1;
            int target_out_1 = 15;
            int target_out_2 = 16;

            //Obter uma lista de EO da semana atual
            ArrayList<ExpeditionOrder> EO_list = dbHandler.getExpeditionOrdersByWeek(current_week);

            //Criar uma lista de peças a retirar do WH
            ArrayList<Piece> expedition_pieces = new ArrayList<>();
            for(ExpeditionOrder eo : EO_list){
                for(Piece p : eo.getPieces() ){
                    expedition_pieces.add(p);
                }
            }

            //Criar contador de peças retiradas e enviadas
            int removed_pieces = 0, delivered_pieces = 0;

            //Criar uma lista de NEWPATH_OUT orders em execução
            ArrayList<Order> NewPathOut_List = new ArrayList<>();

            //Criar uma order OUTWH da primeira peça
            Order order_outwh = null;
            if(!expedition_pieces.isEmpty() ) {
                order_outwh = new Order();
                Piece p = expedition_pieces.get(0);
                Part partinfo_outwh = new Part(p.getId(),
                        path_out_1,
                        PartProps.Pallet,
                        PartProps.getTypeValue(p.getType()),
                        p.getId(), //alterar
                        Aliases.NONE,
                        order_outwh.getId());
                order_outwh.setOutWhOrder(target_out_1, p.getWh_pos(), partinfo_outwh);

                //Enviar a ordem para a DT
                opcHandler.sendOrder(order_outwh);
                removed_pieces++;

                //Atualiza info na db
                dbHandler.updatePieceExpedition(p.getId());
            }


            //------------------- Inbound Orders setup -------------------//
            //Definir os paths e targets a serem usados
            int[] path_in_1 = new int[50];
            int[] path_in_2 = new int[50];
            path_in_1[0] = 2; path_in_1[1] = -1;
            path_in_2[0] = 3; path_in_2[1] = -1;
            int target_in_1 = 1;
            int target_in_2 = 2;

            //Obter uma lista de IO da semana atual
            ArrayList<InboundOrder> IO_list = dbHandler.getInboundOrdersByWeek(current_week);

            //Criar uma lista de peças a emitir
            ArrayList<Piece> inbound_pieces = new ArrayList<>();
            for(InboundOrder io : IO_list){
                for(Piece p : io.getPieces() ){
                    inbound_pieces.add(p);
                }
            }

            //Criar contador de peças emitidas e peças armazenadas
            int emitted_pieces = 0, stored_pieces = 0;

            //Criar uma lista de NEWPATH_IN orders em execução
            ArrayList<Order> NewPathIn_List = new ArrayList<>();

            //Criar uma order EMIT da primeira peça
            Order order_emit = null;
            if(!inbound_pieces.isEmpty() ){
                order_emit = new Order();
                Piece p = inbound_pieces.get(0);
                Part partinfo_emit = new Part(  p.getId(),
                                            path_in_1,
                                            PartProps.Pallet,
                                            PartProps.getTypeValue(p.getType()),
                                            p.getId(), //alterar
                                            Aliases.NONE,
                                            order_emit.getId());
                order_emit.setEmitOrder(target_in_1, partinfo_emit);

                //Enviar a ordem para a DT
                opcHandler.sendOrder(order_emit);
                emitted_pieces++;

                //Atualiza info na db
                dbHandler.updatePieceInbound(p.getId(), current_week, p.getId() );
            }

            //while ainda existirem peças a serem enviadas OU a serem armazenadas
            while(stored_pieces != inbound_pieces.size() ){
                //------------------- Expedition Orders setup -------------------//

                //Percorrer a lista de NEWPATH_OUT running
                for(int i = 0; i < NewPathOut_List.size(); i++){
                    Order curr_order = NewPathOut_List.get(i);
                    //Se uma order estiver finished
                    if(opcHandler.isOrderFinished(curr_order) ){
                        //Se todas as peças da EO estiverem delivered
                        int lastPieceId = curr_order.getPart_info().getId();
                        if(dbHandler.wasLastPieceFromExpedition(lastPieceId) ){
                            //Atualizar a EO
                            dbHandler.setExpeditionCompletedByLastPiece(lastPieceId);
                        }
                        delivered_pieces++;
                        //Retirar a NEWPATH order desta lista
                        NewPathOut_List.remove(i);
                        i--;
                    }
                }
                //Se a order OUTWH não for NULL ou estiver finished
                if(order_outwh!=null && opcHandler.isOrderFinished(order_outwh) ) {
                    //Criar uma NEWPATH_OUT order para essa peça
                    Order order_NewPathOut = new Order();
                    Part partinfo_NewPathOut = new Part(order_outwh.getPart_info().getId(),
                            path_out_2,
                            order_outwh.getPart_info().getType_base(),
                            order_outwh.getPart_info().getType_part(),
                            order_outwh.getPart_info().getStore_position(),
                            order_outwh.getPart_info().getOp(),
                            order_NewPathOut.getId());
                    order_NewPathOut.setNewPathOrder(target_out_2, partinfo_NewPathOut);
                    //Enviar a order para a DT
                    opcHandler.sendOrder(order_NewPathOut);
                    //Colocar a order para a lista de NEWPATH_OUT orders running
                    NewPathOut_List.add(order_NewPathOut);
                    //Se ainda existirem peças a serem retiradas
                    if (removed_pieces != expedition_pieces.size()) {
                        //Fazer uma nova OUTWH order com a próxima peça
                        order_outwh = new Order();
                        Piece p = expedition_pieces.get(removed_pieces);
                        Part partinfo_outwh = new Part(p.getId(),
                                path_out_1,
                                PartProps.Pallet,
                                PartProps.getTypeValue(p.getType()),
                                p.getId(), //alterar
                                Aliases.NONE,
                                order_outwh.getId());
                        order_outwh.setOutWhOrder(target_out_1, p.getWh_pos(), partinfo_outwh);

                        //Enviar a ordem para a DT
                        opcHandler.sendOrder(order_outwh);
                        removed_pieces++;

                        //Atualiza info na db
                        dbHandler.updatePieceExpedition(p.getId());
                    } else {
                        //A OUTWH passa a ser null
                        order_emit = null;
                    }
                }
                //------------------- Inbound Orders -------------------//
                //Percorrer a lista de NEWPATH_IN running
                for(int i = 0; i < NewPathIn_List.size(); i++){
                    Order curr_order = NewPathIn_List.get(i);
                    //Se uma order estiver finished
                    if(opcHandler.isOrderFinished(curr_order) ){
                        //Se todas as peças da IO estiverem armazenadas
                        int lastPieceId = curr_order.getPart_info().getId();
                        if(dbHandler.wasLastPieceFromInbound(lastPieceId) ){
                            //Atualizar a IO
                            dbHandler.setInboundCompletedByLastPiece(lastPieceId);
                        }
                        stored_pieces++;
                        //Retirar a NEWPATH_IN order desta lista
                        NewPathIn_List.remove(i);
                        i--;
                    }
                }
                //Se a order EMIT não for NULL ou estiver finished
                if(order_emit!=null && opcHandler.isOrderFinished(order_emit) ){
                    //Criar uma NEWPATH_IN order para essa peça
                    Order order_NewPathIn = new Order();
                    Part partinfo_NewPathIn = new Part( order_emit.getPart_info().getId(),
                                                        path_in_2,
                                                        order_emit.getPart_info().getType_base(),
                                                        order_emit.getPart_info().getType_part(),
                                                        order_emit.getPart_info().getStore_position(),
                                                        order_emit.getPart_info().getOp(),
                                                        order_NewPathIn.getId());
                    order_NewPathIn.setNewPathOrder(target_in_2, partinfo_NewPathIn);

                    //Enviar a order para a DT
                    opcHandler.sendOrder(order_NewPathIn);

                    //Colocar a order para a lista de NEWPATH_IN orders running
                    NewPathIn_List.add(order_NewPathIn);

                    //Se ainda existirem peças a serem emitidas
                    if(emitted_pieces != inbound_pieces.size()){
                        //Fazer uma nova EMIT order com a próxima peça
                        order_emit = new Order();
                        Piece p = inbound_pieces.get(emitted_pieces);
                        Part partinfo_emit = new Part(  p.getId(),
                                                        path_in_1,
                                                        PartProps.Pallet,
                                                        PartProps.getTypeValue(p.getType()),
                                                    p.getId(), //alterar
                                                        Aliases.NONE,
                                                        order_emit.getId());
                        order_emit.setEmitOrder(target_in_1, partinfo_emit);

                        //Enviar a ordem para a DT
                        opcHandler.sendOrder(order_emit);
                        emitted_pieces++;

                        //Atualiza info na db
                        dbHandler.updatePieceInbound(p.getId(), current_week, p.getId() );
                    }
                    else{
                        //A EMIT passa a ser null
                        order_emit = null;
                    }


                }

            }


            Factory.getInstance().setSim_status("waiting_week_start");
            dbHandler.updateFactoryStatus();
        }




        return null;

    }
}
