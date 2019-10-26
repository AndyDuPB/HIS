package com.andy.his.homestead;

import java.io.Serializable;

public class HomeSteadDetail implements Serializable {

    private int moduleID;

    private String homeSteadID;

    private String homeSteadType;

    private String homeSteadKey;

    private String homeSteadValue;

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

    public String getHomeSteadType() {
        return homeSteadType;
    }

    public void setHomeSteadType(String homeSteadType) {
        this.homeSteadType = homeSteadType;
    }

    public String getHomeSteadKey() {
        return homeSteadKey;
    }

    public void setHomeSteadKey(String homeSteadKey) {
        this.homeSteadKey = homeSteadKey;
    }

    public String getHomeSteadValue() {
        return homeSteadValue;
    }

    public void setHomeSteadValue(String homeSteadValue) {
        this.homeSteadValue = homeSteadValue;
    }
}
