package com.example.mini_mes.tasks;

import com.example.mini_mes.database.DatabaseHandler;
import com.example.mini_mes.dijkstra.PathManager;
import com.example.mini_mes.model.*;
import com.example.mini_mes.opcua.OpcUaHandler;
import com.example.mini_mes.utils.Aliases;
import com.example.mini_mes.utils.PartProps;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.Random;

public class MesTask extends Task<Void> {
    DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    
    private int getAvailableRawWhPos(){
        ArrayList<Piece> pieces_in_wh = dbHandler.getPiecesByStatus("storing");
        pieces_in_wh.addAll(dbHandler.getPiecesByStatus("stored"));

        return getAvailableWhPos(pieces_in_wh);
    }
    private int getAvailableFinalWhPos(){
        ArrayList<Piece> pieces_in_wh = dbHandler.getPiecesByStatus("produced");

        return getAvailableWhPos(pieces_in_wh);
    }

    private int getAvailableWhPos(ArrayList<Piece> pieces_in_wh){
        int wh_capacity = Factory.getInstance().getWarehouse_capacity();
        int return_position;
        boolean available;

        for(return_position=1; return_position<=wh_capacity; return_position++){
            available = true;
            //Verify is this position is already occupied
            for(Piece p : pieces_in_wh){
                if(return_position==p.getWh_pos()) available=false;
            }

            if(available==true) break;
        }

        return return_position;
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


            Factory.getInstance().incrementWeek();
            if(false) break;
            int current_week = Factory.getInstance().getCurrent_week();

            OpcUaHandler opcHandler = OpcUaHandler.getInstance();
            PathManager pM = PathManager.getInstance();
            //------------------- Expedition Orders setup -------------------//

            //Definir os paths e targets a serem usados
            int[] path_out_1 = pM.getExpeditionWhToWhExit().getPath();
            int target_out_1 = pM.getExpeditionWhToWhExit().getTarget();

            int[] path_out_2 = pM.getExpeditionWhExitToRemover().getPath();
            int target_out_2 = pM.getExpeditionWhExitToRemover().getTarget();

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
                Part partinfo_outwh = new Part( p.getId(),
                                                path_out_1,
                                                PartProps.Pallet,
                                                PartProps.getTypeValue(p.getType()),
                                                p.getWh_pos(),
                                                Aliases.NONE,
                                                order_outwh.getId());
                order_outwh.setOutWhOrder(target_out_1, p.getWh_pos(), partinfo_outwh);

                //Enviar a ordem para a DT
                opcHandler.sendOrder(order_outwh);
                removed_pieces++;

                //Atualiza info na db
                dbHandler.updatePieceExpedition(p.getId());
                dbHandler.updatePieceStatus(p.getId(), "shipping");
            }


            //------------------- Inbound Orders setup -------------------//
            //Definir os paths e targets a serem usados
            int[] path_in_1 = pM.getInboundEmitToBuffer().getPath();
            int target_in_1 = pM.getInboundEmitToBuffer().getTarget();

            int[] path_in_2 = pM.getInboundBufferToWh().getPath();
            int target_in_2 = pM.getInboundBufferToWh().getTarget();

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
                int available_wh_pos = getAvailableRawWhPos();

                Part partinfo_emit = new Part(  p.getId(),
                                            path_in_1,
                                            PartProps.Pallet,
                                            PartProps.getTypeValue(p.getType()),
                                            available_wh_pos,
                                            Aliases.NONE,
                                            order_emit.getId());
                order_emit.setEmitOrder(target_in_1, partinfo_emit);

                //Enviar a ordem para a DT
                opcHandler.sendOrder(order_emit);
                emitted_pieces++;

                //Atualiza info na db
                dbHandler.updatePieceInbound(p.getId(), current_week, available_wh_pos );
                dbHandler.updatePieceStatus(p.getId(), "storing");
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
                        dbHandler.updatePieceStatus(lastPieceId, "shipped");
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
                        Part partinfo_outwh = new Part( p.getId(),
                                                        path_out_1,
                                                        PartProps.Pallet,
                                                        PartProps.getTypeValue(p.getType()),
                                                        p.getWh_pos(),
                                                        Aliases.NONE,
                                                        order_outwh.getId());
                        order_outwh.setOutWhOrder(target_out_1, p.getWh_pos(), partinfo_outwh);

                        //Enviar a ordem para a DT
                        opcHandler.sendOrder(order_outwh);
                        removed_pieces++;

                        //Atualiza info na db
                        dbHandler.updatePieceExpedition(p.getId());
                        dbHandler.updatePieceStatus(p.getId(), "shipping");
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
                        dbHandler.updatePieceStatus(lastPieceId, "stored");
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
                        int available_wh_pos = getAvailableRawWhPos();

                        Part partinfo_emit = new Part(  p.getId(),
                                                        path_in_1,
                                                        PartProps.Pallet,
                                                        PartProps.getTypeValue(p.getType()),
                                                        available_wh_pos,
                                                        Aliases.NONE,
                                                        order_emit.getId());
                        order_emit.setEmitOrder(target_in_1, partinfo_emit);

                        //Enviar a ordem para a DT
                        opcHandler.sendOrder(order_emit);
                        emitted_pieces++;

                        //Atualiza info na db
                        dbHandler.updatePieceInbound(p.getId(), current_week, available_wh_pos );
                        dbHandler.updatePieceStatus(p.getId(), "storing");
                    }
                    else{
                        //A EMIT passa a ser null
                        order_emit = null;
                    }
                }
            }

            ///Clear orders_array in DT
            opcHandler.clearReadSigFlag();

            //------------------- Production Orders setup -------------------//
            //Criar as arraylist de paths e targets a serem usados
            int target_outwh_prod = pM.getProductionWhToWhExit().getTarget();
            int[] path_outwh_prod = pM.getProductionWhToWhExit().getPath();

            ArrayList<Machine> mchnList = pM.getMachineList();
            int target_newpath_prod = mchnList.get(0).getTargetPath().getTarget();

            double defective_probability = Factory.getInstance().getDefectiveProbability();
            int target_emit_prod = pM.getProductionEmitToWh().getTarget();
            int[] path_emit_prod_1 = pM.getProductionEmitToWh().getPath();
            int[] path_emit_prod_2 = pM.getProductionEmitToDisposer().getPath();

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
                for(Machine m : mchnList){
                    //Percorrer a lista desordenada de peças
                    for(Piece p : unsorted_production_pieces){
                        //Se a peça é compatível com a máquina
                        if(m.isOperationCompatible(p.getFinal_type() ) ) {
                            //Adiciona o id desta máquina à peça
                            p.setAllocated_machine(m);
                            dbHandler.updatePieceDestinationMachine(p.getId(), m.getDt_id() );
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
                                                        p.getWh_pos(),
                                                        Aliases.NONE,
                                                        order_OUTWH_prod.getId());
                order_OUTWH_prod.setOutWhOrder(target_outwh_prod, p.getWh_pos(), partinfo_outwh_prod);
                //Envia a ordem para a DT
                opcHandler.sendOrder(order_OUTWH_prod);
                //Incrementa contador de peças retiradas
                removed_prod_pieces++;
            }

            //Criar uma lista de orders de EMIT_prod
            ArrayList<Order> NEWPATH_prod_List = new ArrayList<>();

            //Criar uma order para INWH_prod
            Order order_INWH_prod = null;

            //Enquanto o contador de peças processadas armazenadas != do numero total de peças
            while(sorted_production_pieces.size() != completed_prod_pieces){

                //Se a order order_OUTWH_prod não for null ou estiver finished
                if(order_OUTWH_prod != null && opcHandler.isOrderFinished(order_OUTWH_prod)) {

                    //Criar uma NEWPATH_prod order para essa peça
                    Order order_NEWPATH_prod = new Order();
                    //Obter a Piece equivalente à Part (para obter a máquina alocada)
                    Piece current_piece = null;
                    for(Piece p : sorted_production_pieces){
                        if(p.getId() == order_OUTWH_prod.getPart_info().getId() ){
                            current_piece = p;
                            break;
                        }
                    }

                    //Guardar o tempo a que iniciou a produçao
                    current_piece.setStart_production(System.currentTimeMillis());

                    //Obter a transformação desejada
                    int op = Aliases.getOpValueByFinalType(current_piece.getFinal_type());
                    //Utilizar o path para essa máquina
                    Part partinfo_NEWPATH_prod = new Part(  current_piece.getId(),
                                                            current_piece.getAllocated_machine().getPath(),
                                                            PartProps.EMPTY,
                                                            order_OUTWH_prod.getPart_info().getType_part(),
                                                            order_OUTWH_prod.getPart_info().getStore_position(),
                                                            op,
                                                            order_NEWPATH_prod.getId());

                    //Primeiro é necessário enviar uma NewProps order igual à NewPath
                    Order order_NEWPROPS_prod = new Order();
                    order_NEWPROPS_prod.setNewPropsOrder(target_newpath_prod, partinfo_NEWPATH_prod);
                    opcHandler.sendOrder(order_NEWPROPS_prod);

                    //Quando NEWPROPS estiver completa, envia a NewPath
                    order_NEWPATH_prod.setNewPathOrder(target_newpath_prod, partinfo_NEWPATH_prod);
                    opcHandler.sendOrder(order_NEWPATH_prod);

                    //Adicionar esta order à lista de orders NEWPATH_prod running
                    NEWPATH_prod_List.add(order_NEWPATH_prod);

                    dbHandler.updatePieceStatus(current_piece.getId(), "producing");

                    //Se ainda existirem peças a serem retiradas do WH
                    if(removed_prod_pieces != sorted_production_pieces.size()) {
                        //Fazer nova OUTWH_prod
                        order_OUTWH_prod = new Order();
                        Piece p = sorted_production_pieces.get(removed_prod_pieces);
                        Part partinfo_outwh_prod = new Part(    p.getId(),
                                                                path_outwh_prod,
                                                                PartProps.Pallet,
                                                                PartProps.getTypeValue(p.getType()),
                                                                p.getWh_pos(),
                                                                Aliases.NONE,
                                                                order_OUTWH_prod.getId());
                        order_OUTWH_prod.setOutWhOrder(target_outwh_prod, p.getWh_pos(), partinfo_outwh_prod);
                        //Envia a ordem para a DT
                        opcHandler.sendOrder(order_OUTWH_prod);
                        //Incrementa contador de peças retiradas
                        removed_prod_pieces++;
                    }
                    else {
                        //A OUTWH_prod passa a ser null
                        order_OUTWH_prod = null;
                    }
                }


                //Percorrer a lista de NEWPATH_prod orders running
                for(int i = 0; i < NEWPATH_prod_List.size(); i++){
                    //Se a order_INWH_prod ainda estiver ativa, ignorar
                    if(order_INWH_prod!=null) break;

                    Order curr_order = NEWPATH_prod_List.get(i);
                    //Se a order estiver finished
                    if(opcHandler.isOrderFinished(curr_order) ){
                        //Faz um random para decidir se essa peça é defeituosa ou não
                        Random random = new Random();
                        double randomValue = random.nextDouble();

                        //Se for defeituosa, usar o path para a saída, senão armazenar no WH
                        int[] path_emit_prod;
                        String status = "";
                        if(randomValue <= defective_probability){
                            path_emit_prod = path_emit_prod_2;
                            status = "defective";
                        }
                        else{
                            path_emit_prod = path_emit_prod_1;
                            status = "produced";
                        }

                        order_INWH_prod = new Order();
                        int raw_type = curr_order.getPart_info().getType_part();
                        int op = curr_order.getPart_info().getOp();
                        int final_type = PartProps.getFinalTypeValue(raw_type, op);
                        int available_wh_pos = getAvailableFinalWhPos();

                        Part partinfo_emit = new Part(  curr_order.getPart_info().getId(),
                                                        path_emit_prod,
                                                        PartProps.Pallet,
                                                        final_type,
                                                        available_wh_pos,
                                                        Aliases.NONE,
                                                        order_INWH_prod.getId());

                        order_INWH_prod.setEmitOrder(target_emit_prod, partinfo_emit);
                        opcHandler.sendOrder(order_INWH_prod);

                        //Obter a Piece equivalente à Part (para obter o tempo inicial)
                        float duration_production = 0;
                        for(Piece p : sorted_production_pieces){
                            if(p.getId() == order_INWH_prod.getPart_info().getId() ){
                                duration_production = (System.currentTimeMillis()-p.getStart_production())/1000;
                                System.out.println(duration_production);
                                break;
                            }
                        }

                        dbHandler.updatePieceProduction(curr_order.getPart_info().getId(), status, current_week, available_wh_pos, duration_production);

                        NEWPATH_prod_List.remove(i);
                        i--;
                    }
                }

                //Se a order order_INWH_prod não for null ou estiver finished
                if(order_INWH_prod != null && opcHandler.isOrderFinished(order_INWH_prod)) {
                    //Se for a última peça da Production Order
                    int lastPieceId = order_INWH_prod.getPart_info().getId();
                    if(dbHandler.wasLastPieceFromProduction(lastPieceId) ){
                        //Atualizar a PO
                        dbHandler.setProductionCompletedByLastPiece(lastPieceId);
                    }
                    completed_prod_pieces++;

                    //Limpar a order_INWH_prod
                    order_INWH_prod = null;
                }
            }

            ///Clear orders_array in DT
            opcHandler.clearReadSigFlag();

            //End the week
            Factory.getInstance().setSim_status("waiting_week_start");
            dbHandler.updateFactoryStatus();
        }




        return null;

    }
}
