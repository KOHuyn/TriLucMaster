package com.mobileplus.dummytriluc.ui.main.coach.session.saved_list

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.ItemCoachSessionSaved
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 4/27/2021
 */
class CoachSessionSavedListViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider, private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxItemsSaved: PublishSubject<Pair<MutableList<ItemCoachSessionSaved>, Page?>> =
        PublishSubject.create()

    val rxDeleteSuccess: PublishSubject<Boolean> = PublishSubject.create()

    val rxItemPracticeSaved: PublishSubject<MutableList<ItemCoachPractice>> =
        PublishSubject.create()

    fun getListSaved(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getCoachSessionPracticeSaved(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    gson.fromJsonSafe<MutableList<ItemCoachSessionSaved>>(
                        response.dataObject().dataArray()
                    )?.let {
                        rxItemsSaved.onNext(Pair(it, response.dataObject().page()))
                    }
                } else {
                    if (response.isDataEmpty()) {
                        rxItemsSaved.onNext(Pair(mutableListOf(), null))
                    } else {
                        rxMessage.onNext(response.message())
                    }
                }
                logErr("getListSaved: $response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun deleteAtId(id: Int): Disposable {
        return dataManager.deleteCoachSessionPractice(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxDeleteSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getDetailListSaved(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getCoachSessionDetailSavedList(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    gson.fromJsonSafe<MutableList<ItemCoachPractice>>(
                        response.dataArray()
                    )?.let {
                        rxItemPracticeSaved.onNext(it)
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
                logErr("getDetailListSaved: $response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}