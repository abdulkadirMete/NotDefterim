package com.nophrase.speechtotext.model;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Not extends RealmObject implements Serializable {
    @PrimaryKey
    private int id;
    private String not;
    private String date;
    private int colorNum;
    private String header;

    public Not(String not, String date, int colorNum, String header) {
        this.not = not;
        this.date = date;
        this.colorNum = colorNum;
        this.header = header;
    }

    public Not() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNot() {
        return not;
    }

    public void setNot(String not) {
        this.not = not;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getColorNum() {
        return colorNum;
    }

    public void setColorNum(int colorNum) {
        this.colorNum = colorNum;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}

