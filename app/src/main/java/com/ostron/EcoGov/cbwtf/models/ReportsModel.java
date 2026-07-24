package com.ostron.EcoGov.cbwtf.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReportsModel{
    @SerializedName("qr_data_id")
    @Expose
    private String qrDataId;
    @SerializedName("qr_id")
    @Expose
    private String qrId;
    @SerializedName("hospital_code")
    @Expose
    private String hospitalCode;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("hospital_type")
    @Expose
    private String hospitalType;
    @SerializedName("cbwtf_id")
    @Expose
    private String cbwtfId;
    @SerializedName("operator_id")
    @Expose
    private String operatorId;
    @SerializedName("operator_name")
    @Expose
    private String operatorName;
    @SerializedName("district")
    @Expose
    private String district;
    @SerializedName("route")
    @Expose
    private String route;
    @SerializedName("hcf_type")
    @Expose
    private String hcfType;
    @SerializedName("color_type_hcf")
    @Expose
    private String colorTypeHcf;
    @SerializedName("hcf_weight")
    @Expose
    private String hcfWeight;
    @SerializedName("handover_date")
    @Expose
    private String handoverDate;
    @SerializedName("dispose_date")
    @Expose
    private String disposeDate;
    @SerializedName("cbwtf_weight")
    @Expose
    private String cbwtfWeight;
    @SerializedName("hcf_lat_long")
    @Expose
    private String hcfLatLong;
    @SerializedName("dispose_operator_name")
    @Expose
    private String disposeOperatorName;
    @SerializedName("lat_long_cbwtf")
    @Expose
    private String latLongCbwtf;
    @SerializedName("operator_id_cbwtf")
    @Expose
    private String operatorIdCbwtf;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("type1")
    @Expose
    private String type1;
    @SerializedName("color_type_cbwtf")
    @Expose
    private String colorTypeCbwtf;
    @SerializedName("created_on")
    @Expose
    private String createdOn;
    @SerializedName("updated_on")
    @Expose
    private String updatedOn;

    private String red;
    private String blue;
    private String yellow;
    private String white;

    private String redCbwtf = "0";
    private String blueCbwtf = "0";
    private String yellowCbwtf = "0";
    private String whiteCbwtf = "0";

    private String totalPackets;

    public String getTotalPackets() {
        return totalPackets;
    }

    public void setTotalPackets(String totalPackets) {
        this.totalPackets = totalPackets;
    }

    public String getRedCbwtf() {
        return redCbwtf;
    }

    public void setRedCbwtf(String redCbwtf) {
        this.redCbwtf = redCbwtf;
    }

    public String getBlueCbwtf() {
        return blueCbwtf;
    }

    public void setBlueCbwtf(String blueCbwtf) {
        this.blueCbwtf = blueCbwtf;
    }

    public String getYellowCbwtf() {
        return yellowCbwtf;
    }

    public void setYellowCbwtf(String yellowCbwtf) {
        this.yellowCbwtf = yellowCbwtf;
    }

    public String getWhiteCbwtf() {
        return whiteCbwtf;
    }

    public void setWhiteCbwtf(String whiteCbwtf) {
        this.whiteCbwtf = whiteCbwtf;
    }

    public String getRed() {
        return red;
    }

    public void setRed(String red) {
        this.red = red;
    }

    public String getBlue() {
        return blue;
    }

    public void setBlue(String blue) {
        this.blue = blue;
    }

    public String getYellow() {
        return yellow;
    }

    public void setYellow(String yellow) {
        this.yellow = yellow;
    }

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getQrDataId() {
        return qrDataId;
    }

    public void setQrDataId(String qrDataId) {
        this.qrDataId = qrDataId;
    }

    public String getQrId() {
        return qrId;
    }

    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHospitalType() {
        return hospitalType;
    }

    public void setHospitalType(String hospitalType) {
        this.hospitalType = hospitalType;
    }

    public String getCbwtfId() {
        return cbwtfId;
    }

    public void setCbwtfId(String cbwtfId) {
        this.cbwtfId = cbwtfId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getHcfType() {
        return hcfType;
    }

    public void setHcfType(String hcfType) {
        this.hcfType = hcfType;
    }

    public String getColorTypeHcf() {
        return colorTypeHcf;
    }

    public void setColorTypeHcf(String colorTypeHcf) {
        this.colorTypeHcf = colorTypeHcf;
    }

    public String getHcfWeight() {
        return hcfWeight;
    }

    public void setHcfWeight(String hcfWeight) {
        this.hcfWeight = hcfWeight;
    }

    public String getHandoverDate() {
        return handoverDate;
    }

    public void setHandoverDate(String handoverDate) {
        this.handoverDate = handoverDate;
    }

    public String getDisposeDate() {
        return disposeDate;
    }

    public void setDisposeDate(String disposeDate) {
        this.disposeDate = disposeDate;
    }

    public String getCbwtfWeight() {
        return cbwtfWeight;
    }

    public void setCbwtfWeight(String cbwtfWeight) {
        this.cbwtfWeight = cbwtfWeight;
    }

    public String getHcfLatLong() {
        return hcfLatLong;
    }

    public void setHcfLatLong(String hcfLatLong) {
        this.hcfLatLong = hcfLatLong;
    }

    public String getDisposeOperatorName() {
        return disposeOperatorName;
    }

    public void setDisposeOperatorName(String disposeOperatorName) {
        this.disposeOperatorName = disposeOperatorName;
    }

    public String getLatLongCbwtf() {
        return latLongCbwtf;
    }

    public void setLatLongCbwtf(String latLongCbwtf) {
        this.latLongCbwtf = latLongCbwtf;
    }

    public String getOperatorIdCbwtf() {
        return operatorIdCbwtf;
    }

    public void setOperatorIdCbwtf(String operatorIdCbwtf) {
        this.operatorIdCbwtf = operatorIdCbwtf;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType1() {
        return type1;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public String getColorTypeCbwtf() {
        return colorTypeCbwtf;
    }

    public void setColorTypeCbwtf(String colorTypeCbwtf) {
        this.colorTypeCbwtf = colorTypeCbwtf;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }


}