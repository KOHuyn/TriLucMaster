package com.mobileplus.dummytriluc.ui.main.news

import android.annotation.SuppressLint
import com.core.BaseViewModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemNewsFeed
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.remote.ApiConstants.DATA
import com.mobileplus.dummytriluc.data.remote.ApiConstants.DATA_PAGE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.NEXT_PAGE
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class NewsViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxNewsFeed: PublishSubject<Pair<List<ItemNewsFeed>, Page?>> = PublishSubject.create()

    fun getNewsFeed(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getFeed(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxNewsFeed.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items: List<ItemNewsFeed> =
                                gson.toList(response.dataObject().dataArray())
                            val currPage = response.dataObject().getAsJsonObject(DATA_PAGE)
                                .get("currentPage").asInt
                            val totalPage = response.dataObject().getAsJsonObject(DATA_PAGE)
                                .get("lastPage").asInt
                            rxNewsFeed.onNext(Pair(items, Page(currPage, totalPage)))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                } else if (response.isDataEmpty()) {
                    rxNewsFeed.onNext(Pair(emptyList(), null))
                } else {
                    rxMessage.onNext(response.message())
                }
                logErr("$response")
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}