package com.appventurez.bmwms.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HospitalModel {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "hcf_code")
    public String hcf_code;

    @ColumnInfo(name = "hcf_name")
    public String hcf_name;

    @ColumnInfo(name = "waste_color")
    public String waste_color;

    @ColumnInfo(name = "waste_weight")
    public String waste_weight;

    @ColumnInfo(name = "waste_weight_g")
    public String waste_weight_g;

    @ColumnInfo(name = "qr_code")
    public String qr_code;


    public HospitalModel() {
    }

    public HospitalModel(String hcf_code, String hcf_name, String waste_color, String waste_weight,String waste_weight_g,String qr_code) {
        this.hcf_code = hcf_code;
        this.hcf_name = hcf_name;
        this.waste_color = waste_color;
        this.waste_weight = waste_weight;
        this.waste_weight_g = waste_weight_g;
        this.qr_code = qr_code;
    }

    public String getQr_code() {
        return qr_code;
    }

    public void setQr_code(String qr_code) {
        this.qr_code = qr_code;
    }

    public String getWaste_weight_g() {
        return waste_weight_g;
    }

    public void setWaste_weight_g(String waste_weight_g) {
        this.waste_weight_g = waste_weight_g;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getHcf_code() {
        return hcf_code;
    }

    public void setHcf_code(String hcf_code) {
        this.hcf_code = hcf_code;
    }

    public String getHcf_name() {
        return hcf_name;
    }

    public void setHcf_name(String hcf_name) {
        this.hcf_name = hcf_name;
    }

    public String getWaste_color() {
        return waste_color;
    }

    public void setWaste_color(String waste_color) {
        this.waste_color = waste_color;
    }

    public String getWaste_weight() {
        return waste_weight;
    }

    public void setWaste_weight(String waste_weight) {
        this.waste_weight = waste_weight;
    }
}
