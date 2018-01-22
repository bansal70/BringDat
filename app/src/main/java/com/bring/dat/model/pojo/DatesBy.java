package com.bring.dat.model.pojo;

/*
 * Created by rishav on 1/22/2018.
 */

public class DatesBy {
    private String dateBy;
    private boolean mSelected;

    public DatesBy(String dateBy, boolean mSelected) {
        this.dateBy = dateBy;
        this.mSelected = mSelected;
    }

    public String getDateBy() {
        return dateBy;
    }

    public void setDateBy(String dateBy) {
        this.dateBy = dateBy;
    }

    public boolean ismSelected() {
        return mSelected;
    }

    public void setmSelected(boolean mSelected) {
        this.mSelected = mSelected;
    }
}
