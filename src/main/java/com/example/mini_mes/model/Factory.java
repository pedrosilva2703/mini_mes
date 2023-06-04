package com.example.mini_mes.model;

public class Factory {
    private static Factory instance;

    private int warehouse_capacity;
    private int weekly_production;
    // Setup status: waiting_db_conn, waiting_factory_params, done
    private String setup_status;
    // Simulation status: waiting_sim_start, ongoing_week, waiting_week_start
    private String sim_status;
    private int current_week = 0;
    private double defectiveProbability;

    private Factory(){
        this.setup_status = "waiting_db_conn";
        this.sim_status = "waiting_sim_start";
    }

    public static Factory getInstance(){
        if (instance == null) {
            instance = new Factory();
        }
        return instance;
    }

    public String getSetup_status(){    return setup_status;        }
    public String getSim_status(){      return sim_status;          }
    public int getWarehouse_capacity(){ return warehouse_capacity;  }
    public int getWeekly_production(){  return weekly_production;   }
    public int getCurrent_week(){       return current_week;   }


    public boolean isWaitingForDbConn(){    return this.setup_status.equals("waiting_db_conn");          }
    public boolean isWaitingForParams(){    return this.setup_status.equals("waiting_factory_params");  }
    public boolean isSetupDone(){           return this.setup_status.equals("done");                    }

    public boolean isOngoingWeek(){         return this.sim_status.equals("ongoing_week"); }
    public boolean isWaitingSimStart(){         return this.sim_status.equals("waiting_sim_start"); }
    public boolean isWaitingWeekStart(){         return this.sim_status.equals("waiting_week_start"); }

    public void setDbConnected(){   this.setup_status = "waiting_factory_params";   }
    public void setSetupDone(){     this.setup_status = "done";                     }

    public void setWarehouse_capacity(int capacity){    this.warehouse_capacity = capacity; }
    public void setWeekly_production(int prod){         this.weekly_production = prod;      }
    public void setSetup_status(String setup_status) {
        this.setup_status = setup_status;
    }
    public void setSim_status(String sim_status) {
        this.sim_status = sim_status;
    }
    public void setCurrent_week(int current_week) {
        this.current_week = current_week;
    }

    public void incrementWeek(){this.current_week++;}

    public double getDefectiveProbability() {return defectiveProbability;}
    public void setDefectiveProbability(double defectiveProbability) {this.defectiveProbability = defectiveProbability;}
}
