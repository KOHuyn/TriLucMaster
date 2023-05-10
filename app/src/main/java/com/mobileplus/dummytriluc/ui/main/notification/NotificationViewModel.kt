package com.mobileplus.dummytriluc.ui.main.notification

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.NotificationObjService
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 2/22/2021
 */
class NotificationViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxNotifications: PublishSubject<Pair<List<NotificationObjService>, Page?>> =
        PublishSubject.create()

    fun getListNotification(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getNotifications(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxNotifications.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val listNotification: List<NotificationObjService> =
                                gson.toList(response.dataObject().dataArray())
                            rxNotifications.onNext(
                                Pair(
                                    listNotification,
                                    response.dataObject().page()
                                )
                            )
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}