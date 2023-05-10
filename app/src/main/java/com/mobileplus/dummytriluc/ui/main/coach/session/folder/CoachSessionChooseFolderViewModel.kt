package com.mobileplus.dummytriluc.ui.main.coach.session.folder

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice.Companion.TYPE_PRACTICE
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by KOHuyn on 4/21/2021
 */
class CoachSessionChooseFolderViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxItems: PublishSubject<Pair<List<ItemCoachPractice>, Page?>> = PublishSubject.create()

    fun getTrainerList(page: Int? = null, idFolder: Int? = null): Disposable {
        val defaultData = mutableListOf<ItemCoachPractice>()
        if (idFolder == null) {
            if (page == null || page == 1) {
                defaultData.add(
                    0, ItemCoachPractice(
                        1, null, loadStringRes(R.string.practice_free_punch), TYPE_PRACTICE,
                        null, null, null, null, null, false, 1
                    )
                )
                defaultData.add(
                    1,
                    ItemCoachPractice(
                        2, null, loadStringRes(R.string.practice_according_to_led), TYPE_PRACTICE, null, null,
                        null, null, null, false, 1
                    )
                )
            }
        }
        isLoading.onNext(true)
        return dataManager.run {
            if (idFolder == null) getTrainerList(page)
            else getListPracticeFolder(idFolder, page)
        }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    gson.fromJsonSafe<MutableList<ItemCoachPractice>>(
                        response.dataObject().dataArray()
                    )?.let { items ->
                        defaultData.addAll(items)
                        rxItems.onNext(Pair(defaultData, response.dataObject().page()))
                    } ?: rxItems.onNext(Pair(defaultData, null))
                } else {
                    rxItems.onNext(Pair(defaultData, null))
                }
                logErr("getTrainerList: $response")
            }, {
                isLoading.onNext(false)
                rxItems.onNext(Pair(defaultData, null))
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

}