package com.mobileplus.dummytriluc.ui.main.ranking

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemRanking
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


/**
 * Created by KO Huyn on 1/13/2021.
 */
class RankingViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxRankings: PublishSubject<Pair<List<ItemRanking>, Page?>> = PublishSubject.create()

    val userInfo = dataManager.getUserInfo()

    fun getRanking(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getRanking(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxRankings.onNext(Pair(emptyList(), null))
                    } else {
                        val rankings: List<ItemRanking> =
                            gson.toList(response.dataObject().dataArray())
                        val idUser = dataManager.getUserInfo()?.id
                        rankings.forEach {
                            it.isCurrRank = it.userId == idUser
                        }
                        rxRankings.onNext(Pair(rankings, response.dataObject().page()))
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}