package com.mobileplus.dummytriluc.ui.main.coach.assigned_exercise

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.BaseItemCoachGroup
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.model.ItemCoachGroupLesson
import com.mobileplus.dummytriluc.data.model.ItemCoachGroupTime
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 3/16/2021
 */
class AssignedExerciseViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxItems: PublishSubject<Pair<List<BaseItemCoachGroup>, Page?>> = PublishSubject.create()
    val rxDeleteSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun getAssigned(page: Int? = null): Disposable {
        isLoading.onNext(false)
        return dataManager.getAssigned(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                when {
                    response.isSuccess() -> {
                        val items =
                            gson.fromJsonSafe<MutableList<ItemCoachGroupLesson>>(
                                response.dataObject().dataArray()
                            )
                        if (items != null) {
                            val itemsBase = transformToBaseCoachGroup(items)
                            rxItems.onNext(Pair(itemsBase, response.dataObject().page()))
                        }
                    }
                    response.isDataEmpty() -> {
                        rxItems.onNext(Pair(emptyList(), null))
                    }
                    else -> {
                        rxMessage.onNext(response.message())
                    }
                }
                logErr("getAssigned:$response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    private fun transformToBaseCoachGroup(items: MutableList<ItemCoachGroupLesson>): MutableList<BaseItemCoachGroup> {
        val itemBase = mutableListOf<BaseItemCoachGroup>()

        for (i in items.indices) {
            val item = items[i]
            if (i > 0) {
                val prevItem = items[i - 1]
                if (prevItem.getDateCreate() != item.getDateCreate()) {
                    itemBase.add(
                        ItemCoachGroupTime(
                            item.getDateCreate() ?: "unknown"
                        )
                    )
                }
            } else {
                itemBase.add(
                    ItemCoachGroupTime(
                        item.getDateCreate() ?: "unknown"
                    )
                )
            }
            itemBase.add(item)
        }
        return itemBase
    }

    fun deleteAssign(assignId: Int): Disposable {
        return dataManager.deleteAssign(assignId, null)
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