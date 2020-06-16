package com.muchen.tweetstormmaker.models;

import com.google.gson.annotations.SerializedName;

public class StatusId {
    @SerializedName("id_str")
    private String statusId;

    public void setStatusId(String statusId) { this.statusId = statusId; }

    public String getStatusId() { return statusId; }

    public StatusId(){}
}
