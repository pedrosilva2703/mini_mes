package com.example.mini_mes.database;

import com.example.mini_mes.model.Factory;
import java.sql.*;

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

    public void setInboundCompleted(int week){
        String sql =    "UPDATE inbound_order\n" +
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
