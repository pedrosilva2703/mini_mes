package com.example.mini_mes.tasks;

import com.example.mini_mes.database.DatabaseHandler;
import com.example.mini_mes.model.*;
import com.example.mini_mes.opcua.OpcUaHandler;
import com.example.mini_mes.utils.Aliases;
import com.example.mini_mes.utils.OrdersAliases;
import com.example.mini_mes.utils.PartProps;
import javafx.concurrent.Task;

import java.util.ArrayList;

public class BackupFirstSuccess extends Task<Void> {
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
            //Week was started by mini-ERP
            if(false) break;

            Factory.getInstance().incrementWeek();
            int current_week = Factory.getInstance().getCurrent_week();

            OpcUaHandler opcHandler = OpcUaHandler.getInstance();

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
            while(stored_pieces != inbound_pieces.size() || delivered_pieces != expedition_pieces.size() ){
                //------------------- Expedition Orders -------------------//

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

            //------------------- Production Orders setup -------------------//
            //Criar as arraylist de paths e targets a serem usados
            int target_outwh_prod = 3;
            int[] path_outwh_prod = new int[50];
            path_outwh_prod[0] = 2; path_outwh_prod[1] = 3; path_outwh_prod[2] = -1;

            int target_newpath_pallet_remover = 4;
            int[] path_newpath_pallet_remover = new int[50];
            path_newpath_pallet_remover[0] = 5; path_newpath_pallet_remover[1] = 6; path_newpath_pallet_remover[2] = -1;

            int target_emit_prod = 7;
            ArrayList<Machine> machine_list = new ArrayList<>();
            int[] path_machine1 = new int[50];
            path_machine1[0] = 8; path_machine1[1] = 9; path_machine1[2] = 10; path_machine1[3] = 11;
            path_machine1[4] = 12; path_machine1[5] = 13; path_machine1[6] = 14; path_machine1[7] = 15;
            path_machine1[8] = 29; path_machine1[9] = 30; path_machine1[10] = -1;
            int[] path_machine2 = new int[50];
            path_machine2[0] = 8; path_machine2[1] = 9; path_machine2[2] = 16; path_machine2[3] = 17;
            path_machine2[4] = 18; path_machine2[5] = 19; path_machine2[6] = 20; path_machine2[7] = 27;
            path_machine2[8] = 28; path_machine2[9] = 29; path_machine2[10] = 30; path_machine2[11] = -1;
            int[] path_machine3 = new int[50];
            path_machine3[0] = 8; path_machine3[1] = 9; path_machine3[2] = 21; path_machine3[3] = 22;
            path_machine3[4] = 23; path_machine3[5] = 24; path_machine3[6] = 25; path_machine3[7] = 26;
            path_machine3[8] = 27; path_machine3[9] = 28; path_machine3[10] = 29; path_machine3[11] = 30;
            path_machine3[12] = -1;

            machine_list.add(new Machine(11, path_machine1, "DualProducer"));
            machine_list.add(new Machine(18, path_machine2, "DualProducer"));
            machine_list.add(new Machine(25, path_machine3, "DualProducer"));


            //Criar uma lista das Production Orders para esta semana
            ArrayList<ProductionOrder> PO_list = dbHandler.getProductionOrdersByWeek(current_week);

            //Criar uma lista desordenada de peças a serem produzidas
            ArrayList<Piece> unsorted_production_pieces = new ArrayList<>();
            for(ProductionOrder po : PO_list){
                for(Piece p : po.getPieces() ){
                    unsorted_production_pieces.add(p);
                }
            }


            //Criar uma lista ordenada de peças scheduled
            ArrayList<Piece> sorted_production_pieces = new ArrayList<>();
            //Enquanto a lista desordenada não estiver empty
            while(!unsorted_production_pieces.isEmpty()){
                //Para cada máquina, na lista ordenada de máquinas
                for(Machine m : machine_list){
                    //Percorrer a lista desordenada de peças
                    for(Piece p : unsorted_production_pieces){
                        //Se a peça é compatível com a máquina
                        if(m.isOperationCompatible(p.getFinal_type() ) ) {
                            //Adiciona o id desta máquina à peça
                            p.setAllocated_machine(m);
                            //Adiciona esta peça à lista de peças scheduled
                            sorted_production_pieces.add(p);
                            //Remove esta peça da lista desordenada
                            unsorted_production_pieces.remove(p);
                            break;
                        }
                    }
                }
            }



            //Criar um contador de peças retiradas e peças processadas armazenadas
            int removed_prod_pieces = 0, completed_prod_pieces = 0;
            //Criar uma order de OUTWH_prod
            Order order_OUTWH_prod = null;
            //Se a lista de peças scheduled não for empty
            if(!sorted_production_pieces.isEmpty()) {
                //Coloca os parâmetros da primeira peça
                order_OUTWH_prod = new Order();
                Piece p = sorted_production_pieces.get(0);
                Part partinfo_outwh_prod = new Part(    p.getId(),
                        path_outwh_prod,
                        PartProps.Pallet,
                        PartProps.getTypeValue(p.getType()),
                        p.getWh_pos(), //alterar
                        Aliases.NONE,
                        order_OUTWH_prod.getId());
                order_OUTWH_prod.setOutWhOrder(target_outwh_prod, p.getWh_pos(), partinfo_outwh_prod);
                //Envia a ordem para a DT
                opcHandler.sendOrder(order_OUTWH_prod);
                //Incrementa contador de peças retiradas
                removed_prod_pieces++;
            }

            //Criar uma order de NEWPATH_pallet_remover
            Order order_NEWPATH_pallet_remover = null;
            //Criar uma lista de orders de EMIT_prod
            ArrayList<Order> EMIT_prod_List = new ArrayList<>();

            System.out.println();

            //Enquanto o contador de peças processadas armazenadas != do numero total de peças
            while(sorted_production_pieces.size() != completed_prod_pieces){

                //Se a order OUTWH_prod não for null ou estiver finished
                if(order_OUTWH_prod!=null && opcHandler.isOrderFinished(order_OUTWH_prod) ){
                    //Criar uma NEWPATH_pallet_remover para esta peça
                    order_NEWPATH_pallet_remover = new Order();
                    Part partinfo_newpath_pallet_remover = new Part(order_OUTWH_prod.getPart_info().getId(),
                            path_newpath_pallet_remover,
                            order_OUTWH_prod.getPart_info().getType_base(),
                            order_OUTWH_prod.getPart_info().getType_part(),
                            order_OUTWH_prod.getPart_info().getStore_position(),
                            order_OUTWH_prod.getPart_info().getOp(),
                            order_NEWPATH_pallet_remover.getId());
                    order_NEWPATH_pallet_remover.setNewPathOrder(target_newpath_pallet_remover, partinfo_newpath_pallet_remover);
                    //Enviar a ordem para a DT
                    opcHandler.sendOrder(order_NEWPATH_pallet_remover);

                    //Se ainda existirem peças a serem retiradas do WH
                    if(removed_prod_pieces != sorted_production_pieces.size()) {
                        //Fazer nova OUTWH_prod
                        order_OUTWH_prod = new Order();
                        Piece p = sorted_production_pieces.get(removed_prod_pieces);
                        Part partinfo_outwh_prod = new Part(    p.getId(),
                                path_outwh_prod,
                                PartProps.Pallet,
                                PartProps.getTypeValue(p.getType()),
                                p.getWh_pos(), //alterar
                                Aliases.NONE,
                                order_OUTWH_prod.getId());
                        order_OUTWH_prod.setOutWhOrder(target_outwh_prod, p.getWh_pos(), partinfo_outwh_prod);
                        //Envia a ordem para a DT
                        opcHandler.sendOrder(order_OUTWH_prod);
                        //Incrementa contador de peças retiradas
                        removed_prod_pieces++;
                    }
                    //Senao
                    else {
                        //A OUTWH_prod passa a ser null
                        order_OUTWH_prod = null;
                    }
                }

                //Se a order NEWPATH_pallet_remover não for null ou estiver finished
                if(order_NEWPATH_pallet_remover != null && opcHandler.isOrderFinished(order_NEWPATH_pallet_remover)) {

                    //Criar uma NEWPATH_prod order para essa peça
                    Order order_EMIT_prod = new Order();
                    //Obter a Piece equivalente à Part (para obter a máquina alocada)
                    Piece current_piece = null;
                    for(Piece p : sorted_production_pieces){
                        if(p.getId() == order_NEWPATH_pallet_remover.getPart_info().getId() ){
                            current_piece = p;
                            break;
                        }
                    }
                    //Obter a transformação desejada
                    int op = Aliases.getOpByFinalType(current_piece.getFinal_type());
                    //Utilizar o path para essa máquina
                    Part partinfo_EMIT_prod = new Part(current_piece.getId(),
                            current_piece.getAllocated_machine().getPath(),
                            PartProps.EMPTY,
                            order_NEWPATH_pallet_remover.getPart_info().getType_part(),
                            order_NEWPATH_pallet_remover.getPart_info().getStore_position(),
                            op,
                            order_EMIT_prod.getId());
                    order_EMIT_prod.setEmitOrder(target_emit_prod, partinfo_EMIT_prod);


                    //Enviar a order de newpath
                    opcHandler.sendOrder(order_EMIT_prod);

                    //Adicionar esta order à lista de orders NEWPATH_prod running
                    EMIT_prod_List.add(order_EMIT_prod);
                    //Coloca a NEWPATH_pallet_remover a null
                    order_NEWPATH_pallet_remover = null;

                    completed_prod_pieces++;
                }

                //Percorrer a lista de NEWPATH_prod orders running
                //Se a order estiver finished
                //Cria uma EMIT_prod_scan order para essa peça
                //Faz um random para decidir se essa peça é defeituosa ou não
                //Se for defeituosa, usar o path para a saída
                //Se nao, armazenar no WH
                //Retira as infos da peça e atualiza na db
                //Retira a order da lista de NEWPATH_prod

                //Percorrer a lista de EMIT_prod_scan
                //Se a order estiver finished
                //Se for a última peça ad Production Order
                //Coloca como finished na DB
                //Incrementa contador da peças armazenadas
                //Remove a EMIT_prod_scan da lista
            }


            Factory.getInstance().setSim_status("waiting_week_start");
            dbHandler.updateFactoryStatus();
        }




        return null;

    }
}
