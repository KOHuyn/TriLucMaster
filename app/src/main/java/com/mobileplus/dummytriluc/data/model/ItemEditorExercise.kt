package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by KOHuyn on 3/30/2021
 */
data class ItemEditorExercise(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("image_path")
    val imagePath: String? = null,
    @Expose
    @SerializedName("video_path")
    val videoPath: String? = null,
    @Expose
    @SerializedName("folder_id")
    val folderId: Int? = null,
    @Expose
    @SerializedName("video_path_origin")
    val videoPathOrigin: String? = null,
    var isSelected:Boolean = false
)