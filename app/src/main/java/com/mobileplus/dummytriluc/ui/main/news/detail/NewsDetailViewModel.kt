package com.mobileplus.dummytriluc.ui.main.news.detail

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.response.NewsResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 1/25/2021
 */
class NewsDetailViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxNewsResponse: PublishSubject<NewsResponse> = PublishSubject.create()

    fun getNewsDetail(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getFeedDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({
                isLoading.onNext(false)
                if (it.isSuccess()) {
                    try {
                        val response = gson.fromJson(it.dataObject(), NewsResponse::class.java)
                        rxNewsResponse.onNext(response)
                    } catch (e: Exception) {
                        e.logErr()
                    }
                } else {
                    rxMessage.onNext(it.message())
                }
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}