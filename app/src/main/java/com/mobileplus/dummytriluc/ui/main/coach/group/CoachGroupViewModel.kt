package com.mobileplus.dummytriluc.ui.main.coach.group

import android.annotation.SuppressLint
import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.remote.ApiConstants.MESSAGE
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class CoachGroupViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxGroups: PublishSubject<Pair<List<ItemDiscipleGroup>, Page?>> = PublishSubject.create()
    val rxLoadingMember: PublishSubject<Boolean> = PublishSubject.create()
    val rxMemberInGroup: PublishSubject<Pair<Int, MutableList<ItemDisciple>>> =
        PublishSubject.create()
    val createGroupSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val deleteGroupSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val renameGroupSuccess: PublishSubject<Pair<Boolean, String>> = PublishSubject.create()

    fun getGroupList(isGuest: Boolean, page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.run { if (isGuest) getClassJoin(page) else getDiscipleGroupList(page) }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxGroups.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items =
                                gson.toList<ItemDiscipleGroup>(response.dataObject().dataArray())
                            rxGroups.onNext(Pair(items, response.dataObject().page()))
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxGroups.onNext(Pair(emptyList(), null))
                }
                logErr(response.toString())
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }

    fun getAllMemberInGroup(idGroup: Int): Disposable {
        rxLoadingMember.onNext(true)
        return dataManager.getAllMemberInGroup(idGroup)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxLoadingMember.onNext(false)
                if (response.isSuccess()) {
                    val items = gson.fromJsonSafe<MutableList<ItemDisciple>>(response.dataArray())
                    if (items != null) {
                        rxMemberInGroup.onNext(Pair(idGroup, items))
                    }
                }
            }, {
                rxLoadingMember.onNext(false)
            })
    }

    fun addMemberIntoRoom(classId: Int, userId: List<Int>): Disposable {
        return dataManager.addManyMemberIntoGroup(classId, userId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxMessage.onNext(response.message())
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun createGroup(groupName: String): Disposable {
        isLoading.onNext(true)
        return dataManager.postCreateGroup(groupName)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                createGroupSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }

    fun deleteGroup(id: Int?): Disposable {
        isLoading.onNext(true)
        return dataManager.deleteGroup(id.toString())
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                deleteGroupSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }

    fun renameGroup(id: Int, newName: String): Disposable {
        isLoading.onNext(true)
        return dataManager.postRenameGroup(id, newName)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                renameGroupSuccess.onNext(Pair(response.isSuccess(), newName))
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }

}