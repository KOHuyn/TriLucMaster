package com.mobileplus.dummytriluc.ui.main.notification.detail

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.response.NotificationDetailApiResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 2/25/2021
 */
class NotificationDetailViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxNotificationDetailResponse: PublishSubject<NotificationDetailApiResponse> =
        PublishSubject.create()

    fun getNotificationDetail(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getNotificationDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    try {
                        rxNotificationDetailResponse.onNext(
                            gson.fromJson(
                                response.dataObject(),
                                NotificationDetailApiResponse::class.java
                            )
                        )
                    } catch (e: Exception) {
                        e.logErr()
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}