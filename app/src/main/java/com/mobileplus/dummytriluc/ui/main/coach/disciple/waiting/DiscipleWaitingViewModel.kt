package com.mobileplus.dummytriluc.ui.main.coach.disciple.waiting

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.data.model.ItemDiscipleWaiting
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


/**
 * Created by KO Huyn on 1/19/2021.
 */
class DiscipleWaitingViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxListWaiting: PublishSubject<Pair<List<ItemDiscipleWaiting>, Page?>> =
        PublishSubject.create()

    val rxRequest: PublishSubject<Boolean> = PublishSubject.create()

    fun acceptDisciple(idStudent: Int): Disposable {
        return dataManager.acceptDisciple(idStudent)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxRequest.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
                logErr("acceptDisciple:$response")
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun rejectDisciple(idStudent: Int): Disposable {
        return dataManager.rejectDisciple(idStudent)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxRequest.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
                logErr("rejectDisciple:$response")
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getListWaiting(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getDiscipleWaiting(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxListWaiting.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items =
                                gson.toList<ItemDiscipleWaiting>(response.dataObject().dataArray())
                            rxListWaiting.onNext(Pair(items, response.dataObject().page()))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxListWaiting.onNext(Pair(emptyList(), null))
                }
                logErr(response.toString())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}