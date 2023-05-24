package com.example.mini_mes.database;

import com.example.mini_mes.model.*;
import com.example.mini_mes.utils.Aliases;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private String url;
    private int port;
    private String databaseName;
    private String schema;
    private String username;
    private String password;
    private Connection connection = null;
    /*dbHandler = new DatabaseHandler(
            "db.fe.up.pt",
            5432,
            "sie2252",
            "siefinal",
            "sie2252",
            "GDQllMDQ");*/
    private DatabaseHandler(String url, int port, String databaseName, String schema, String username, String password){
        this.url = url;
        this.port = port;
        this.databaseName = databaseName;
        this.schema = schema;
        this.username = username;
        this.password = password;
    }

    //Manage connection methods
    public static DatabaseHandler getInstance(String url, int port, String databaseName, String schema, String username, String password){
        instance = new DatabaseHandler(url, port, databaseName, schema, username, password);
        return instance;
    }
    public static DatabaseHandler getInstance(){
        return instance;
    }
    public boolean setConnection(){
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            connection = DriverManager.getConnection("jdbc:postgresql://" + url + ":" + port + "/" + databaseName +
                                                         "?currentSchema="+schema, username, password);
        } catch (SQLException | IllegalArgumentException ex) {
            System.out.println(ex);
        }

        if (connection == null) {
            return false;
        }
        if( !retrieveFactoryStatus()  )   return false;
        return true;
    }

    //Factory Lookup table methods
    public boolean updateFactoryStatus(){
        Factory factory = Factory.getInstance();
        String sql = "UPDATE factory SET     setup_status = ?,       " +
                                            "sim_status = ?,         " +
                                            "working_mode = ?,       " +
                                            "warehouse_capacity = ?, " +
                                            "weekly_production = ?;  " ;
        try {
            PreparedStatement updateStatement = connection.prepareStatement(sql);
            updateStatement.setString(1,    factory.getSetup_status()       );
            updateStatement.setString(2,    factory.getSim_status()         );
            updateStatement.setString(3,    factory.getWorking_mode()       );
            updateStatement.setInt(   4,    factory.getWarehouse_capacity() );
            updateStatement.setInt(   5,    factory.getWeekly_production()  );
            updateStatement.execute();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean retrieveFactoryStatus() {
        Factory factory = Factory.getInstance();
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM factory");
            ResultSet sqlReturnValues = stmt.executeQuery();
            sqlReturnValues.next();

            factory.setSetup_status(sqlReturnValues.getString(1));
            factory.setSim_status(sqlReturnValues.getString(2));
            factory.setWorking_mode(sqlReturnValues.getString(3) );
            factory.setWarehouse_capacity(sqlReturnValues.getInt(4) );
            factory.setWeekly_production(sqlReturnValues.getInt(5) );

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return false;
        }
        return true;
    }

    //Inbound orders methods
    public ArrayList<InboundOrder> getInboundOrdersByWeek(int filter_week){
        String sql =    "SELECT  id,\n" +
                "        week,\n" +
                "        status,\n" +
                "        FK_supplier_order\n" +
                "FROM inbound_order\n" +
                "WHERE week = ?\n" +
                "ORDER BY id ASC ";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, filter_week);
            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<InboundOrder> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                int week = sqlReturnValues.getInt(2);
                String status = sqlReturnValues.getString(3);
                returnValues.add(new InboundOrder(id, week, status, getPiecesByIO(id) ) );
            }


            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public boolean wasLastPieceFromInbound(int piece_id){
        String sql =    "SELECT COUNT(piece.id), SUM(CASE WHEN piece.week_arrived IS NOT NULL THEN 1 ELSE 0 END)\n" +
                "FROM piece\n" +
                "JOIN inbound_order ON piece.fk_inbound_order = inbound_order.id\n" +
                "WHERE inbound_order.id IN\n" +
                "    (SELECT piece.fk_inbound_order\n" +
                "     FROM piece\n" +
                "     WHERE piece.id = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, piece_id);
            ResultSet sqlReturnValues = stmt.executeQuery();
            sqlReturnValues.next();

            if( sqlReturnValues.getInt(1) != sqlReturnValues.getInt(2) ){
                return false;
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return true;
    }
    public void setInboundCompletedByLastPiece(int piece_id){
        String sql =    "UPDATE inbound_order\n" +
                "SET status = ? \n" +
                "WHERE inbound_order.id IN\n" +
                "    (SELECT piece.fk_inbound_order\n" +
                "     FROM piece\n" +
                "     WHERE piece.id = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "completed");
            stmt.setInt(2, piece_id);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    //Production orders methods
    public ArrayList<ProductionOrder> getProductionOrdersByWeek(int filter_week){
        String sql =    "SELECT  id,\n" +
                "        week,\n" +
                "        status\n" +
                "FROM production_order\n" +
                "WHERE week = ?\n" +
                "ORDER BY id ASC";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, filter_week);
            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<ProductionOrder> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                int week = sqlReturnValues.getInt(2);
                String status = sqlReturnValues.getString(3);
                returnValues.add(new ProductionOrder(id, week, status, getPiecesByPO(id)) );
            }


            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public boolean wasLastPieceFromProduction(int piece_id){
        String sql =    "SELECT COUNT(piece.id), SUM(CASE WHEN piece.week_produced IS NOT NULL THEN 1 ELSE 0 END)\n" +
                "FROM piece\n" +
                "JOIN production_order ON piece.fk_production_order = production_order.id\n" +
                "WHERE production_order.id IN\n" +
                "    (SELECT piece.fk_production_order\n" +
                "     FROM piece\n" +
                "     WHERE piece.id = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, piece_id);
            ResultSet sqlReturnValues = stmt.executeQuery();
            sqlReturnValues.next();

            if( sqlReturnValues.getInt(1) != sqlReturnValues.getInt(2) ){
                return false;
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return true;
    }
    public void setProductionCompletedByLastPiece(int piece_id){
        String sql =    "UPDATE production_order\n" +
                "SET status = ? \n" +
                "WHERE production_order.id IN\n" +
                "    (SELECT piece.fk_production_order\n" +
                "     FROM piece\n" +
                "     WHERE piece.id = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "completed");
            stmt.setInt(2, piece_id);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    //Expedition orders methods
    public ArrayList<ExpeditionOrder> getExpeditionOrdersByWeek(int filter_week){
        String sql =    "SELECT  id,\n" +
                "        week,\n" +
                "        status,\n" +
                "        FK_client_order\n" +
                "FROM expedition_order\n" +
                "WHERE week = ?\n" +
                "ORDER BY id ASC";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, filter_week);
            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<ExpeditionOrder> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                int week = sqlReturnValues.getInt(2);
                String status = sqlReturnValues.getString(3);
                returnValues.add(new ExpeditionOrder(id, week, status, getPiecesByEO(id)) );
            }


            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public boolean wasLastPieceFromExpedition(int piece_id){
        String sql =    "SELECT COUNT(piece.id), SUM(CASE WHEN piece.wh_pos IS NULL THEN 1 ELSE 0 END)\n" +
                "FROM piece\n" +
                "JOIN expedition_order ON piece.fk_expedition_order = expedition_order.id\n" +
                "WHERE expedition_order.id IN\n" +
                "    (SELECT piece.fk_expedition_order\n" +
                "     FROM piece\n" +
                "     WHERE piece.id = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, piece_id);
            ResultSet sqlReturnValues = stmt.executeQuery();
            sqlReturnValues.next();

            if( sqlReturnValues.getInt(1) != sqlReturnValues.getInt(2) ){
                return false;
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return true;
    }
    public void setExpeditionCompletedByLastPiece(int piece_id){
        String sql =    "UPDATE expedition_order\n" +
                "SET status = ? \n" +
                "WHERE expedition_order.id IN\n" +
                "    (SELECT piece.fk_expedition_order\n" +
                "     FROM piece\n" +
                "     WHERE piece.id = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "completed");
            stmt.setInt(2, piece_id);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }

    //Piece methods
    public ArrayList<Piece> getPiecesByIO(int IO_id){
        String sql =    "SELECT  id,\n" +
                "        type,\n" +
                "        status,\n" +
                "        final_type,\n" +
                "        week_arrived,\n" +
                "        week_produced,\n" +
                "        duration_production,\n" +
                "        safety_stock,\n" +
                "        wh_pos\n" +
                "FROM piece \n" +
                "WHERE FK_inbound_order = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, IO_id);
            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<Piece> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                String type = sqlReturnValues.getString(2);
                String status = sqlReturnValues.getString(3);
                String final_type = sqlReturnValues.getString(4);
                Integer week_arrived = sqlReturnValues.getInt(5);
                Integer week_produced = sqlReturnValues.getInt(6);
                Float duration_production = sqlReturnValues.getFloat(7);
                boolean safety_stock = sqlReturnValues.getBoolean(8);
                Integer wh_pos = sqlReturnValues.getInt(9);

                returnValues.add(new Piece(id, type, status, final_type, week_arrived, week_produced, duration_production, safety_stock, wh_pos) );
            }
            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public ArrayList<Piece> getPiecesByPO(int PO_id){
        String sql =    "SELECT  id,\n" +
                "        type,\n" +
                "        status,\n" +
                "        final_type,\n" +
                "        week_arrived,\n" +
                "        week_produced,\n" +
                "        duration_production,\n" +
                "        safety_stock,\n" +
                "        wh_pos\n" +
                "FROM piece \n" +
                "WHERE FK_production_order = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, PO_id);
            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<Piece> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                String type = sqlReturnValues.getString(2);
                String status = sqlReturnValues.getString(3);
                String final_type = sqlReturnValues.getString(4);
                Integer week_arrived = sqlReturnValues.getInt(5);
                Integer week_produced = sqlReturnValues.getInt(6);
                Float duration_production = sqlReturnValues.getFloat(7);
                boolean safety_stock = sqlReturnValues.getBoolean(8);
                Integer wh_pos = sqlReturnValues.getInt(9);

                returnValues.add(new Piece(id, type, status, final_type, week_arrived, week_produced, duration_production, safety_stock, wh_pos) );
            }
            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public ArrayList<Piece> getPiecesByEO(int EO_id){
        String sql =    "SELECT  id,\n" +
                "        type,\n" +
                "        status,\n" +
                "        final_type,\n" +
                "        week_arrived,\n" +
                "        week_produced,\n" +
                "        duration_production,\n" +
                "        safety_stock,\n" +
                "        wh_pos\n" +
                "FROM piece \n" +
                "WHERE FK_expedition_order = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, EO_id);
            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<Piece> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                String type = sqlReturnValues.getString(2);
                String status = sqlReturnValues.getString(3);
                String final_type = sqlReturnValues.getString(4);
                Integer week_arrived = sqlReturnValues.getInt(5);
                Integer week_produced = sqlReturnValues.getInt(6);
                Float duration_production = sqlReturnValues.getFloat(7);
                boolean safety_stock = sqlReturnValues.getBoolean(8);
                Integer wh_pos = sqlReturnValues.getInt(9);

                returnValues.add(new Piece(id, type, status, final_type, week_arrived, week_produced, duration_production, safety_stock, wh_pos) );
            }
            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public void updatePieceInbound(int id, int week_arrived, int wh_pos){
        String sql =    "UPDATE piece \n" +
                        "SET status = 'arrived', week_arrived = ?, wh_pos = ?\n" +
                        "WHERE id=?\n";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, week_arrived);
            stmt.setInt(2, wh_pos);
            stmt.setInt(3, id);
            stmt.execute();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    public void updatePieceProduction(int id, String status, int week_produced, int wh_pos, float duration_production){
        String sql =    "UPDATE piece \n" +
                "SET type = final_type, status = ?, week_produced = ?, wh_pos = ?, duration_production = ?\n" +
                "WHERE id=?\n";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, week_produced);
            stmt.setInt(3, wh_pos);
            stmt.setFloat(4, duration_production);
            stmt.setInt(5, id);

            System.out.println(stmt);
            stmt.execute();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    public void updatePieceExpedition(int id){
        String sql =    "UPDATE piece \n" +
                        "SET wh_pos = ?\n" +
                        "WHERE id=?\n";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, id);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    public ArrayList<Piece> getPiecesByStatus(String filter_status){
        String sql =    "SELECT  id,\n" +
                "        type,\n" +
                "        status,\n" +
                "        final_type,\n" +
                "        week_arrived,\n" +
                "        week_produced,\n" +
                "        duration_production,\n" +
                "        safety_stock,\n" +
                "        wh_pos\n" +
                "FROM piece \n" +
                "WHERE status = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, filter_status);
            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<Piece> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                String type = sqlReturnValues.getString(2);
                String status = sqlReturnValues.getString(3);
                String final_type = sqlReturnValues.getString(4);
                Integer week_arrived = sqlReturnValues.getInt(5);
                Integer week_produced = sqlReturnValues.getInt(6);
                Float duration_production = sqlReturnValues.getFloat(7);
                boolean safety_stock = sqlReturnValues.getBoolean(8);
                Integer wh_pos = sqlReturnValues.getInt(9);

                returnValues.add(new Piece(id, type, status, final_type, week_arrived, week_produced, duration_production, safety_stock, wh_pos) );
            }
            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    //Piece data displaying methods
    public ArrayList<Piece> getInboundPiecesByWeek(int week_filter){
        String sql =    "SELECT  piece.id,\n" +
                "        supplier_order.type,\n" +
                "        piece.status,\n" +
                "        piece.final_type,\n" +
                "        piece.week_arrived,\n" +
                "        piece.week_produced,\n" +
                "        piece.duration_production,\n" +
                "        piece.safety_stock,\n" +
                "        piece.wh_pos,\n" +
                "        client.name,\n   " +
                "        supplier.name\n" +
                "FROM piece \n" +
                "LEFT JOIN inbound_order ON piece.fk_inbound_order = inbound_order.id\n" +
                "LEFT JOIN client_order ON piece.fk_client_order = client_order.id\n" +
                "LEFT JOIN client ON client_order.fk_client = client.id\n" +
                "LEFT JOIN supplier_order ON piece.fk_supplier_order = supplier_order.id\n" +
                "LEFT JOIN supplier ON supplier_order.fk_supplier = supplier.id\n" +
                "WHERE inbound_order.week = ? AND inbound_order.status != 'canceled'\n" +
                "ORDER BY piece.id";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, week_filter);

            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<Piece> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                String type = sqlReturnValues.getString(2);
                String status = sqlReturnValues.getString(3);
                String final_type = sqlReturnValues.getString(4);
                Integer week_arrived = sqlReturnValues.getInt(5);
                Integer week_produced = sqlReturnValues.getInt(6);
                Float duration_production = sqlReturnValues.getFloat(7);
                boolean safety_stock = sqlReturnValues.getBoolean(8);
                Integer wh_pos = sqlReturnValues.getInt(9);

                Piece p = new Piece(id, type, status, final_type, week_arrived, week_produced, duration_production, safety_stock, wh_pos);

                String client = sqlReturnValues.getString(10);
                if(client==null){
                    p.setClient("None");
                }
                else{
                    p.setClient(client);
                }
                String supplier = sqlReturnValues.getString(11);
                p.setSupplier(supplier);

                returnValues.add(p);
            }
            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public ArrayList<Piece> getProductionPiecesByWeek(int week_filter){
        String sql =    "SELECT  piece.id,\n" +
                "        supplier_order.type,\n" +
                "        piece.status,\n" +
                "        piece.final_type,\n" +
                "        piece.week_arrived,\n" +
                "        piece.week_produced,\n" +
                "        piece.duration_production,\n" +
                "        piece.safety_stock,\n" +
                "        piece.wh_pos,\n" +
                "        client.name,\n   " +
                "        supplier.name\n" +
                "FROM piece \n" +
                "LEFT JOIN production_order ON piece.fk_production_order = production_order.id\n" +
                "LEFT JOIN client_order ON piece.fk_client_order = client_order.id\n" +
                "LEFT JOIN client ON client_order.fk_client = client.id\n" +
                "LEFT JOIN supplier_order ON piece.fk_supplier_order = supplier_order.id\n" +
                "LEFT JOIN supplier ON supplier_order.fk_supplier = supplier.id\n" +
                "WHERE production_order.week = ? AND production_order.status != 'canceled'\n" +
                "ORDER BY piece.id";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, week_filter);

            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<Piece> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                String type = sqlReturnValues.getString(2);
                String status = sqlReturnValues.getString(3);
                String final_type = sqlReturnValues.getString(4);
                Integer week_arrived = sqlReturnValues.getInt(5);
                Integer week_produced = sqlReturnValues.getInt(6);
                Float duration_production = sqlReturnValues.getFloat(7);
                boolean safety_stock = sqlReturnValues.getBoolean(8);
                Integer wh_pos = sqlReturnValues.getInt(9);

                Piece p = new Piece(id, type, status, final_type, week_arrived, week_produced, duration_production, safety_stock, wh_pos);

                String client = sqlReturnValues.getString(10);
                if(client==null){
                    p.setClient("None");
                }
                else{
                    p.setClient(client);
                }

                if(final_type!=null && !final_type.equals("") ){
                    p.setOperation(Aliases.getOpStringByFinalType(final_type) );
                }
                String supplier = sqlReturnValues.getString(11);
                p.setSupplier(supplier);

                returnValues.add(p);
            }
            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
    public ArrayList<Piece> getExpeditionPiecesByWeek(int week_filter){
        String sql =    "SELECT  piece.id,\n" +
                "        piece.type,\n" +
                "        piece.status,\n" +
                "        piece.final_type,\n" +
                "        piece.week_arrived,\n" +
                "        piece.week_produced,\n" +
                "        piece.duration_production,\n" +
                "        piece.safety_stock,\n" +
                "        piece.wh_pos,\n" +
                "        client.name,\n   " +
                "        supplier.name\n" +
                "FROM piece \n" +
                "LEFT JOIN expedition_order ON piece.fk_expedition_order = expedition_order.id\n" +
                "LEFT JOIN client_order ON piece.fk_client_order = client_order.id\n" +
                "LEFT JOIN client ON client_order.fk_client = client.id\n" +
                "LEFT JOIN supplier_order ON piece.fk_supplier_order = supplier_order.id\n" +
                "LEFT JOIN supplier ON supplier_order.fk_supplier = supplier.id\n" +
                "WHERE expedition_order.week = ? AND expedition_order.status != 'canceled'\n" +
                "ORDER BY piece.id";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, week_filter);

            ResultSet sqlReturnValues = stmt.executeQuery();

            ArrayList<Piece> returnValues = new ArrayList<>();

            while (sqlReturnValues.next()){
                Integer id = sqlReturnValues.getInt(1);
                String type = sqlReturnValues.getString(2);
                String status = sqlReturnValues.getString(3);
                String final_type = sqlReturnValues.getString(4);
                Integer week_arrived = sqlReturnValues.getInt(5);
                Integer week_produced = sqlReturnValues.getInt(6);
                Float duration_production = sqlReturnValues.getFloat(7);
                boolean safety_stock = sqlReturnValues.getBoolean(8);
                Integer wh_pos = sqlReturnValues.getInt(9);

                Piece p = new Piece(id, type, status, final_type, week_arrived, week_produced, duration_production, safety_stock, wh_pos);

                String client = sqlReturnValues.getString(10);
                if(client==null){
                    p.setClient("None");
                }
                else{
                    p.setClient(client);
                }
                String supplier = sqlReturnValues.getString(11);
                p.setSupplier(supplier);

                returnValues.add(p);
            }
            return returnValues;
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    //MES simulation methods
    public void setInboundRunning(int week){
        String sql =    "UPDATE inbound_order\n" +
                "SET status = ? \n" +
                "WHERE week = ? \n";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "running");
            stmt.setInt(2, week);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    public void setProductionRunning(int week){
        String sql =    "UPDATE production_order\n" +
                "SET status = ? \n" +
                "WHERE week = ? \n";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "running");
            stmt.setInt(2, week);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    public void setExpeditionRunning(int week){
        String sql =    "UPDATE expedition_order\n" +
                "SET status = ? \n" +
                "WHERE week = ? \n";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "running");
            stmt.setInt(2, week);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }


    public void setPiecesInbound(int week){
        String sql =    "UPDATE piece \n" +
                        "SET week_arrived = ?, wh_pos = 11\n" +
                        "WHERE fk_inbound_order IN\n" +
                        "(SELECT id FROM inbound_order WHERE week = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, week);
            stmt.setInt(2, week);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    public void setProductionCompleted(int week){
        String sql =    "UPDATE production_order\n" +
                "SET status = ? \n" +
                "WHERE week = ? \n";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "completed");
            stmt.setInt(2, week);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }

    public void setPiecesProduction(int week){
        String sql =    "UPDATE piece \n" +
                "SET type = final_type, week_produced = ?\n" +
                "WHERE fk_production_order IN\n" +
                "(SELECT id FROM production_order WHERE week = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, week);
            stmt.setInt(2, week);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    public void setExpeditionCompleted(int week){
        String sql =    "UPDATE expedition_order\n" +
                "SET status = ? \n" +
                "WHERE week = ? \n";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "completed");
            stmt.setInt(2, week);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }
    public void setPiecesExpedition(int week){
        String sql =    "UPDATE piece \n" +
                "SET wh_pos = ?\n" +
                "WHERE fk_expedition_order IN\n" +
                "(SELECT id FROM expedition_order WHERE week = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, week);
            stmt.execute();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
            return;
        }
        return;
    }



}
