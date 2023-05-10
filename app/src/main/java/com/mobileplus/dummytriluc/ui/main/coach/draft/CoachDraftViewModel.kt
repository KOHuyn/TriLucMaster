package com.mobileplus.dummytriluc.ui.main.coach.draft

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.ItemCoachDraftFolder
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 3/12/2021
 */
class CoachDraftViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxListCoachMore: PublishSubject<Pair<List<ItemCoachPractice>, Page?>> =
        PublishSubject.create()

    val rxListDraftFolder: PublishSubject<List<ItemCoachDraftFolder>> = PublishSubject.create()

    val rxDeleteDraft: PublishSubject<Boolean> = PublishSubject.create()

    val rxCreateFolderSuccess: PublishSubject<Boolean> = PublishSubject.create()

    val rxRenameFolderSuccess: PublishSubject<Boolean> = PublishSubject.create()

    val rxEmptyData: PublishSubject<Boolean> = PublishSubject.create()

    fun deleteDraft(id: Int): Disposable {
        return dataManager.deleteDraft(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    rxDeleteDraft.onNext(true)
                }
            }, {
                it.printStackTrace()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getAllDraft(parentId: Int) {
        isLoading.onNext(true)
        dataManager.getTrainerDraftFolder(parentId)
            .zipWith(dataManager.getTrainerDraft(null, parentId), { folderJson, draftItemsJson ->
                val folderArr: List<ItemCoachDraftFolder> = if (folderJson.isSuccess()) {
                    gson.fromJsonSafe<List<ItemCoachDraftFolder>>(folderJson.dataArray())
                        ?: listOf()
                } else {
                    listOf()
                }
                var pageItemsDraft:Page? = null
                val itemArr: List<ItemCoachPractice> = if (draftItemsJson.isSuccess()) {
                    pageItemsDraft = draftItemsJson.dataObject().pageOrNull()
                    gson.fromJsonSafe<List<ItemCoachPractice>>(draftItemsJson.dataObject().dataArray())
                        ?: listOf()
                } else {
                    listOf()
                }
                folderArr to (itemArr to pageItemsDraft)
            })
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({
                isLoading.onNext(false)
                rxListDraftFolder.onNext(it.first)
                rxListCoachMore.onNext(it.second)
                val isEmptyData = it.first.isEmpty() && it.second.first.isEmpty()
                rxEmptyData.onNext(isEmptyData)
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                rxEmptyData.onNext(true)
            }).addTo(disposable)
    }

    fun getTrainerDraftFolder(parentId: Int = 0): Disposable {
        return dataManager.getTrainerDraftFolder(parentId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr(response.toString())
                if (response.isSuccess()) {
                    val items = gson.toList<ItemCoachDraftFolder>(response.dataArray())
                    rxListDraftFolder.onNext(items)
                } else {
                    rxListDraftFolder.onNext(emptyList())
                }
            }, {
                logErr(it.message ?: "null")
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getTrainerDraft(page: Int? = null, folderId: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getTrainerDraft(page, folderId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                logErr(response.toString())
                if (response.isSuccess()) {
                    val items =
                        gson.toList<ItemCoachPractice>(
                            response.dataObject().dataArray()
                        )
                    rxListCoachMore.onNext(
                        Pair(
                            items.toMutableList(),
                            response.dataObject().page()
                        )
                    )
                } else {
//                    rxMessage.onNext(response.message())
                    rxListCoachMore.onNext(Pair(emptyList(), null))
                }
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun createFolderDraft(name: String, parentId: Int): Disposable {
        return dataManager.createTrainerDraftFolder(name, parentId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxCreateFolderSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                logErr(it.message ?: "null")
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun renameFolderDraft(name: String, folderId: Int): Disposable {
        return dataManager.renameTrainerDraftFolder(name, folderId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxRenameFolderSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                logErr(it.message ?: "null")
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun updateDraft(id: Int, title: String, folderId: Int = 0): Disposable {
        isLoading.onNext(true)
        return dataManager.updateDraft(id, title, folderId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                logErr("updateDraft(id: $id, title: $title, folderId: $folderId):\n $response")
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}