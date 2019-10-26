package com.andy.his.module;

public class ModuleSpinnerDetail {

    private int moduleID;

    private String showText;

    public int getModuleID() {
        return moduleID;
    }

    public void setModuleID(int moduleID) {
        this.moduleID = moduleID;
    }

    public String getShowText() {
        return showText;
    }

    public void setShowText(String showText) {
        this.showText = showText;
    }

    @Override
    public String toString() {
        return this.getShowText();
    }
}
