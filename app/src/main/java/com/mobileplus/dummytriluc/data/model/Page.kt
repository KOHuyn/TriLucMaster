package com.mobileplus.dummytriluc.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


/**
 * Created by KO Huyn on 12/17/2020.
 */
data class Page(
    @Expose
    @SerializedName("current_page")
    var currPage: Int = -1,
    @Expose
    @SerializedName("last_page")
    var totalPage: Int = -1,
    var isLoading: Boolean = false,
) {
    val isDefaultPage get() = currPage == 1

    fun updatePage(page: Page?) {
        if (page == null) {
            isLoading = false
            return
        }
        currPage = page.currPage
        totalPage = page.totalPage
        isLoading = false
    }
}