package com.alamkanak.weekview.sample.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApiEvent(
    @Expose
    @SerializedName("name")
    var title: String,
    @Expose
    @SerializedName("dayOfMonth")
    var dayOfMonth: Int,
    @Expose
    @SerializedName("startTime")
    var startTime: String,
    @Expose
    @SerializedName("endTime")
    var endTime: String,
    @Expose
    @SerializedName("color")
    var color: String
)
