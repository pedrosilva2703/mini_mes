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
            while(!Factory.getInstance().isOngoingWeek() ){
                //Retrieves factory status until is ready to start the week
                dbHandler.retrieveFactoryStatus();
            }
            Factory.getInstance().incrementWeek();
            OpcUaHandler opcHandler = OpcUaHandler.getInstance();
            //Week was started by mini-ERP
            if(false) break;

            //Expedition Orders
            //Definir os paths a serem usados
            //Obter uma lista de EO da semana atual
            //Criar uma lista de peças a retirar do WH
            //Criar contador de peças retiradas
            //Criar contador de peças enviadas
            //Criar uma lista de NEWPATH_OUT orders em execução
            //Criar uma order OUTWH da primeira peça

            //Inbound Orders
            //Definir os paths e targets a serem usados
            int[] path_in_1 = new int[50];
            int[] path_in_2 = new int[50];
            path_in_1[0] = 2; path_in_1[1] = -1;
            path_in_2[0] = 3; path_in_2[1] = -1;
            int target_in_1 = 1;
            int target_in_2 = 2;

            //Obter uma lista de IO da semana atual
            ArrayList<InboundOrder> IO_list = dbHandler.getInboundOrders(Factory.getInstance().getCurrent_week() );

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
            }

            //while ainda existirem peças a serem enviadas OU a serem armazenadas
            while(stored_pieces != inbound_pieces.size() ){
                //--------EXPEDITION ORDERS
                //Percorrer a lista de NEWPATH_OUT running
                    //Se uma order estiver finished
                        //Atualizar a peça como delivered
                        //Se todas as peças da EO estiverem delivered
                            //Atualizar a EO e CO
                        //Retirar a NEWPATH order desta lista

                //Se a order OUTWH não for NULL ou estiver finished
                    //Criar uma NEWPATH_OUT order para essa peça
                    //Enviar a order para a DT
                    //Colocar a order para a lista de NEWPATH_OUT orders running
                    //Se ainda existirem peças a serem retiradas
                        //Fazer uma nova OUTWH order com a próxima peça
                        //Enviar para a DT
                    //Se não
                        //A OUTWH passa a ser null

                //--------INBOUND ORDERS
                //Percorrer a lista de NEWPATH_IN running
                for(int i = 0; i < NewPathIn_List.size(); i++){
                    Order curr_order = NewPathIn_List.get(i);
                    //Se uma order estiver finished
                    if(opcHandler.isOrderFinished(curr_order) ){
                        //Atualizar a peças
                        //Se todas as peças da IO estiverem armazenadas
                            //Atualizar a IO
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
                    }
                    else{
                        //A EMIT passa a ser null
                        order_emit = null;
                    }


                }

            }


        }




        return null;

    }
}
