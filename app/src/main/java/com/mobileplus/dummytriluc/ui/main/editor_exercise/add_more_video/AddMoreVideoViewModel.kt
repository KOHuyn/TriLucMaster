package com.mobileplus.dummytriluc.ui.main.editor_exercise.add_more_video

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.*
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 3/31/2021
 */
class AddMoreVideoViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxListCoachMore: PublishSubject<Pair<List<ItemEditorExercise>, Page?>> =
        PublishSubject.create()

    val rxListDraftFolder: PublishSubject<List<ItemCoachDraftFolder>> = PublishSubject.create()

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
                        gson.toList<ItemEditorExercise>(
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
}