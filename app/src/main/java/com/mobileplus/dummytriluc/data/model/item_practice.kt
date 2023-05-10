package com.mobileplus.dummytriluc.data.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import java.lang.reflect.Type

interface BasePracticeItem {
    companion object {
        const val TYPE_HEADER = 1
        const val TYPE_BODY = 2
        const val TYPE_CHILD_MASTER = 3
        const val TYPE_CHILD_PRACTICE = 4
        const val TYPE_FOLDER_PRACTICE = 5
        const val TYPE_SINGLE_PRACTICE = 6
        const val TYPE_CHILD_LOCAL = 7
    }

    fun getType(): Int
}

data class ItemPracticeItemContent(
    @Expose
    @SerializedName("image_path")
    val img: String? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("id")
    val id: Int = -1,
    @Expose
    @SerializedName("subject")
    val subject: Subject? = null,
    @Expose
    @SerializedName("level_id")
    val levelId: Int? = null,
    @Expose
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("video_thumb")
    @Expose
    val videoThumb: String? = null,
    @SerializedName("video_path_origin")
    @Expose
    val videoPathOrigin: String? = null,
    @Expose
    val type: String? = null,
    var isSelected: Boolean = false
) : BasePracticeItem {
    data class Subject(@Expose val id: Int? = null, @Expose val title: String? = null)

    companion object {
        const val TYPE_FOLDER = "folder"
        const val TYPE_PRACTICE = "practice"
        fun getType(): Type {
            return object : TypeToken<ArrayList<ItemPracticeItemContent>>() {}.type
        }
    }

    override fun getType(): Int = BasePracticeItem.TYPE_CHILD_PRACTICE
}

data class ItemPracticeItemMaster(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("full_name")
    val nameMaster: String? = null,
    @Expose
    @SerializedName("avatar_path")
    val imgMaster: String? = null,
    @Expose
    val subject: Subject? = null,
    @Expose
    @SerializedName("number_practice")
    val countPractice: Int? = null,
    @Expose
    @SerializedName("number_student")
    val countDisciple: Int? = null,
) : BasePracticeItem {
    data class Subject(@Expose val id: Int? = null, @Expose val title: String? = null)

    override fun getType(): Int = BasePracticeItem.TYPE_CHILD_MASTER
    fun getCountPracticeMaster() =
        if (countPractice != null)
            String.format(loadStringRes(R.string.value_exercise), countPractice.toString())
        else null

    fun getCountDiscipleMaster() =
        if (countDisciple != null)
            String.format(loadStringRes(R.string.value_disciple), countDisciple.toString())
        else null

    fun getCountPracticeDisciple(): String? =
        when {
            countPractice != null && countDisciple != null ->
                String.format(loadStringRes(R.string.value_exercise), countPractice.toString()) +
                        " - " +
                        String.format(
                            loadStringRes(R.string.value_disciple),
                            countDisciple.toString()
                        )
            countPractice != null && countDisciple == null ->
                String.format(loadStringRes(R.string.value_exercise), countPractice.toString())
            countPractice == null && countDisciple != null ->
                String.format(loadStringRes(R.string.value_disciple), countDisciple.toString())
            countPractice == null || countDisciple == null -> null
            else -> null
        }
}

data class ItemPracticeContent(
    var list: MutableList<BasePracticeItem>, val isLocal: Boolean = false
) : BasePracticeItem {
    override fun getType(): Int = BasePracticeItem.TYPE_BODY
}

data class ItemTitlePractice(
    @Expose val id: Int,
    @Expose val title: String,
    @Expose val type: String? = null
) : BasePracticeItem {
    companion object {
        const val TYPE_PRACTICE = "practice"
        const val TYPE_FOLDER = "folder"
        const val TYPE_MASTER = "master"
    }

    override fun getType(): Int = BasePracticeItem.TYPE_HEADER
}

data class ItemPracticeFolder(
    @Expose
    @SerializedName("id")
    val id: Int? = null,
    @Expose
    @SerializedName("title")
    val title: String? = null,
    @Expose
    @SerializedName("image_path")
    val imgPath: String? = null,
    @Expose
    @SerializedName("video_path_real")
    val videoPathReal: String? = null,
    @Expose
    @SerializedName("content")
    val content: String? = null,
    @Expose
    @SerializedName("type")
    val type: String? = null,
) {
    companion object {
        const val TYPE_FOLDER = "folder"
        const val TYPE_PRACTICE = "practice"
    }
}

data class ItemPracticeLocal(
    val id: Int,
    @DrawableRes
    val imgPath: Int,
    val title: String,
    val description: String
) : BasePracticeItem {
    override fun getType(): Int = BasePracticeItem.TYPE_CHILD_LOCAL
}