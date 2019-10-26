package com.andy.his.module;

import java.io.Serializable;

public class ModuleDetail implements Serializable {

    private int moduleID;

    private String moduleCounty;

    private String moduleTown;

    private String moduleVillage;

    private String moduleGroup;

    private boolean checkStatus;

    public int getModuleID() {
        return moduleID;
    }

    public void setModuleID(int moduleID) {
        this.moduleID = moduleID;
    }

    public String getModuleCounty() {
        return moduleCounty;
    }

    public void setModuleCounty(String moduleCounty) {
        this.moduleCounty = moduleCounty;
    }

    public String getModuleTown() {
        return moduleTown;
    }

    public void setModuleTown(String moduleTown) {
        this.moduleTown = moduleTown;
    }

    public String getModuleVillage() {
        return moduleVillage;
    }

    public void setModuleVillage(String moduleVillage) {
        this.moduleVillage = moduleVillage;
    }

    public String getModuleGroup() {
        return moduleGroup;
    }

    public void setModuleGroup(String moduleGroup) {
        this.moduleGroup = moduleGroup;
    }

    public boolean isCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(boolean checkStatus) {
        this.checkStatus = checkStatus;
    }
}
