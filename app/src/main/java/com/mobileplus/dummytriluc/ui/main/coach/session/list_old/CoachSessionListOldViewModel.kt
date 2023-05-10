package com.mobileplus.dummytriluc.ui.main.coach.session.list_old

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemCoachSessionOld
import com.mobileplus.dummytriluc.data.model.ItemCoachSessionSaved
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class CoachSessionListOldViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxItemsOld: PublishSubject<Pair<MutableList<ItemCoachSessionOld>, Page?>> =
        PublishSubject.create()

    val rxDeleteSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun getListOld(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getCoachSessionList(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    gson.fromJsonSafe<MutableList<ItemCoachSessionOld>>(
                        response.dataObject().dataArray()
                    )?.let {
                        rxItemsOld.onNext(Pair(it, response.dataObject().page()))
                    }
                } else {
                    if (response.isDataEmpty()) {
                        rxItemsOld.onNext(Pair(mutableListOf(), null))
                    } else {
                        rxMessage.onNext(response.message())
                    }
                }
                logErr("getListOld: $response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun deleteSessionAtId(id: Int): Disposable {
        return dataManager.deleteCoachSession(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxDeleteSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}