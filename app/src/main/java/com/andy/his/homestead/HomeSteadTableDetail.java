package com.andy.his.homestead;

import java.io.Serializable;

public class HomeSteadTableDetail implements Serializable {

    private int moduleID;

    private String homeSteadID;

    private String moduleInfo;

    private String homeSteadNumber;

    private String houseHolder;

    public int getModuleID() {
        return moduleID;
    }

    public void setModuleID(int moduleID) {
        this.moduleID = moduleID;
    }

    public String getHomeSteadID() {
        return homeSteadID;
    }

    public void setHomeSteadID(String homeSteadID) {
        this.homeSteadID = homeSteadID;
    }

    public String getModuleInfo() {
        return moduleInfo;
    }

    public void setModuleInfo(String moduleInfo) {
        this.moduleInfo = moduleInfo;
    }

    public String getHomeSteadNumber() {
        return homeSteadNumber;
    }

    public void setHomeSteadNumber(String homeSteadNumber) {
        this.homeSteadNumber = homeSteadNumber;
    }

    public String getHouseHolder() {
        return houseHolder;
    }

    public void setHouseHolder(String houseHolder) {
        this.houseHolder = houseHolder;
    }
}
