package com.mobileplus.dummytriluc.ui.main.coach.group.detail.member

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

class CoachGroupDetailMemberViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxMembers: PublishSubject<Pair<List<ItemDisciple>, Page?>> = PublishSubject.create()
    var rxDeleteMemberSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun moveMemberToGroups(arrClassID: List<Int>, memberID: Int): Disposable {
        return dataManager.addMemberInGroup(arrClassID, memberID)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxMessage.onNext(response.message())
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun deleteMemberInGroup(classID: Int, memberID: Int): Disposable {
        return dataManager.deleteMemberInGroup(classID, memberID)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxDeleteMemberSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getAllMemberInGroup(idGroup: Int, dayRange: Int? = null, page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getMemberInGroup(idGroup, dayRange, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxMembers.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items =
                                gson.toList<ItemDisciple>(response.dataObject().dataArray())
                            rxMembers.onNext(Pair(items, response.dataObject().page()))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxMembers.onNext(Pair(emptyList(), null))
                }
                logErr(response.toString())
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }
}