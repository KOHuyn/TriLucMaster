package com.mobileplus.dummytriluc.ui.main.coach.disciple.list

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


/**
 * Created by KO Huyn on 1/19/2021.
 */
class DiscipleListViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxDisciples: PublishSubject<Pair<List<ItemDisciple>, Page?>> = PublishSubject.create()
    val rxDeleteDiscipleSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun moveMemberToGroups(
        arrClassID: List<Int>,
        memberID: Int,
        onSuccess: () -> Unit
    ): Disposable {
        return dataManager.addMemberInGroup(arrClassID, memberID)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    onSuccess()
                }
                rxMessage.onNext(response.message())
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun deleteDisciple(idDisciple: Int): Disposable {
        return dataManager.deleteDisciple(idDisciple)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxDeleteDiscipleSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getListDisciple(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getDiscipleList(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxDisciples.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items = gson.toList<ItemDisciple>(response.dataObject().dataArray())
                            rxDisciples.onNext(Pair(items, response.dataObject().page()))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxDisciples.onNext(Pair(emptyList(), null))
                }
                logErr(response.toString())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}