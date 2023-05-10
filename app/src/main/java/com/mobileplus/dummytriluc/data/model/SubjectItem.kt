package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


data class SubjectItem(
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("image_path")
    @Expose
    val thumb: String? = null,
    @SerializedName("title")
    @Expose
    val name: String? = null,
    var isSelected: Boolean = false
) {
    companion object {
        fun getType(): Type {
            return object : TypeToken<List<SubjectItem>>() {}.type
        }
    }
}
