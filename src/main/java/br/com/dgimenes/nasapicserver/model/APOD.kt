package br.com.dgimenes.nasapicserver.model

import com.google.gson.annotations.SerializedName

data class APOD(@SerializedName("hdurl") val hdUrl: String?,
                val url: String,
                @SerializedName("media_type") val mediaType: String,
                val explanation: String,
                val title: String)
