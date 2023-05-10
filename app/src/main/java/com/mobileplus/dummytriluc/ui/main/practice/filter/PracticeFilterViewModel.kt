package com.mobileplus.dummytriluc.ui.main.practice.filter

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemPracticeItemContent
import com.mobileplus.dummytriluc.data.model.ItemPracticeItemMaster
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.model.entity.TableLevel
import com.mobileplus.dummytriluc.data.model.entity.TableSubject
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class PracticeFilterViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson,
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxItemPractice: PublishSubject<Pair<MutableList<ItemPracticeItemContent>, Page?>> =
        PublishSubject.create()

    val rxItemMaster: PublishSubject<Pair<MutableList<ItemPracticeItemMaster>, Page?>> =
        PublishSubject.create()

    val rxLoadingPractice:PublishSubject<Boolean> =
        PublishSubject.create()

    val rxLoadingMaster:PublishSubject<Boolean> =
        PublishSubject.create()

    val rxAllLevel: PublishSubject<List<CustomSpinner.SpinnerItem>> = PublishSubject.create()
    val rxAllSubject: PublishSubject<List<CustomSpinner.SpinnerItem>> = PublishSubject.create()

    fun getDataPracticeMore(
        id: Int,
        subject: Int? = null,
        level: Int? = null,
        sort: String? = null,
        keyword: String? = null,
        page: Int? = null,
    ): Disposable {
        rxLoadingPractice.onNext(true)
        return dataManager.getListPracticeMore(id, subject, level, sort, keyword, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxLoadingPractice.onNext(false)
                try {
                    when {
                        response.isSuccess() -> {
                            val data: List<ItemPracticeItemContent> =
                                gson.toList(response.dataObject().dataArray())
                            rxItemPractice.onNext(
                                Pair(
                                    data.toMutableList(),
                                    response.dataObject().page()
                                )
                            )
                        }
                        response.isDataEmpty() -> {
                            rxItemPractice.onNext(Pair(mutableListOf(), null))
                        }
                        else -> {
                            rxMessage.onNext(response.message())
                        }
                    }
                } catch (e: Exception) {
                    e.logErr()
                }
                logErr("getDataPracticeMore $response")
            }, {
                rxLoadingPractice.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getDataMasterMore(
        id: Int,
        subject: Int? = null,
        level: Int? = null,
        sort: String? = null,
        keyword: String? = null,
        page: Int? = null,
    ): Disposable {
        rxLoadingMaster.onNext(true)
        return dataManager.getListPracticeMore(id, subject, level, sort, keyword, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxLoadingMaster.onNext(false)
                try {
                    when {
                        response.isSuccess() -> {
                            val data: List<ItemPracticeItemMaster> =
                                gson.toList(response.dataObject().dataArray())
                            rxItemMaster.onNext(
                                Pair(
                                    data.toMutableList(),
                                    response.dataObject().page()
                                )
                            )
                        }
                        response.isDataEmpty() -> {
                            rxItemMaster.onNext(Pair(mutableListOf(), null))
                        }
                        else -> {
                            rxMessage.onNext(response.message())
                        }
                    }
                } catch (e: Exception) {
                    e.logErr()
                }
                logErr("getDataMasterMore $response")
            }, {
                rxLoadingMaster.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun searchPractice(
        subject: Int? = null,
        level: Int? = null,
        sort: String? = null,
        keyword: String? = null,
        page: Int? = null
    ): Disposable {
        rxLoadingPractice.onNext(true)
        return dataManager.searchPractice(subject, level, sort, keyword, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxLoadingPractice.onNext(false)
                logErr("subject:$subject, level:$level, sort:$sort, keyword:$keyword, page")
                try {
                    when {
                        response.isSuccess() -> {
                            val data: List<ItemPracticeItemContent> =
                                gson.toList(response.dataObject().dataArray())
                            rxItemPractice.onNext(
                                Pair(
                                    data.toMutableList(),
                                    response.dataObject().page()
                                )
                            )
                        }
                        response.isDataEmpty() -> {
                            rxItemPractice.onNext(Pair(mutableListOf(), null))
                        }
                        else -> {
                            rxMessage.onNext(response.message())
                        }
                    }
                } catch (e: Exception) {
                    e.logErr()
                }
                logErr("searchPractice $response")
            }, {
                rxLoadingPractice.onNext(false)
                it.logErr()
            })
    }

    fun searchMaster(
        subject: Int? = null,
        level: Int? = null,
        sort: String? = null,
        keyword: String? = null,
        page: Int? = null
    ): Disposable {
        rxLoadingMaster.onNext(true)
        return dataManager.searchMaster(subject, level, sort, keyword, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxLoadingMaster.onNext(false)
                logErr("subject:$subject, level:$level, sort:$sort, keyword:$keyword, page")
                try {
                    when {
                        response.isSuccess() -> {
                            val data: List<ItemPracticeItemMaster> =
                                gson.toList(response.dataObject().dataArray())
                            rxItemMaster.onNext(
                                Pair(
                                    data.toMutableList(),
                                    response.dataObject().page()
                                )
                            )
                        }
                        response.isDataEmpty() -> {
                            rxItemMaster.onNext(Pair(mutableListOf(), null))
                        }
                        else -> {
                            rxMessage.onNext(response.message())
                        }
                    }
                } catch (e: Exception) {
                    e.logErr()
                }
                logErr("searchMaster $response")
            }, {
                rxLoadingMaster.onNext(false)
                it.logErr()
            })
    }

    private fun getAllLevelApi(): Disposable {
        return dataManager.getListLevel()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val levels = gson.toList<TableLevel>(response.dataArray())
                    rxAllLevel.onNext(levels.transformLevelToItemSpinner())
                    saveLevel(levels)
                }
            }, {
                it.logErr()
            })
    }

    private fun getAllSubjectApi(): Disposable {
        return dataManager.getListSubject()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val subjects = gson.toList<TableSubject>(response.dataArray())
                    rxAllSubject.onNext(subjects.transformSubjectToItemSpinner())
                    saveSubject(subjects)
                }
            }, {
                it.logErr()
            })
    }

    fun getAllLevelDb(): Disposable {
        return dataManager.getAllLevel()
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({ arr ->
                if (arr.isEmpty()) {
                    getAllLevelApi()
                } else {
                    rxAllLevel.onNext(arr.transformLevelToItemSpinner())
                }
            }, {
                it.logErr()
            })
    }

    fun getAllSubjectDb(): Disposable {
        return dataManager.getAllSubject()
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({ arr ->
                if (arr.isEmpty()) {
                    getAllSubjectApi()
                } else {
                    rxAllSubject.onNext(arr.transformSubjectToItemSpinner())
                }
            }, {
                it.logErr()
            })
    }

    private fun List<TableSubject>.transformSubjectToItemSpinner(): ArrayList<CustomSpinner.SpinnerItem> {
        val subjects = arrayListOf<CustomSpinner.SpinnerItem>()
        subjects.add(
            CustomSpinner.SpinnerItem(
                loadStringRes(R.string.all),
                AppConstants.INTEGER_DEFAULT.toString()
            )
        )
        this.map { subject ->
            subjects.add(
                CustomSpinner.SpinnerItem(
                    subject.title,
                    subject.id.toString()
                )
            )
        }
        return subjects
    }

    private fun List<TableLevel>.transformLevelToItemSpinner(): ArrayList<CustomSpinner.SpinnerItem> {
        val levels = arrayListOf<CustomSpinner.SpinnerItem>()
        levels.add(
            CustomSpinner.SpinnerItem(
                loadStringRes(R.string.all),
                AppConstants.INTEGER_DEFAULT.toString()
            )
        )
        this.map { level ->
            levels.add(CustomSpinner.SpinnerItem(level.title, level.id.toString()))
        }
        return levels
    }

    private fun saveSubject(subjects: List<TableSubject>): Disposable {
        return dataManager.saveSubjects(subjects)
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({
                logErr("save subjects success")
            }, {
                it.logErr()
            })
    }

    private fun saveLevel(levels: List<TableLevel>): Disposable {
        return dataManager.saveLevels(levels)
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({
                logErr("save level success")
            }, {
                it.logErr()
            })
    }

}