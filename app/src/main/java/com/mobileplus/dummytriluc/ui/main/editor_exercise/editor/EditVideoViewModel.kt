package com.mobileplus.dummytriluc.ui.main.editor_exercise.editor

import com.core.BaseViewModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.entity.TableLevel
import com.mobileplus.dummytriluc.data.model.entity.TableSubject
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.SaveExerciseRequest
import com.mobileplus.dummytriluc.data.response.DetailDraftResponse
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File

class EditVideoViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val savePracticeSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val rxDetailDraftResponse: PublishSubject<DetailDraftResponse> = PublishSubject.create()
    val rxDetailExerciseResponse: PublishSubject<DetailPracticeResponse> = PublishSubject.create()

    val rxSubjectArr: PublishSubject<List<CustomSpinner.SpinnerItem>> = PublishSubject.create()
    val rxLevelArr: PublishSubject<List<CustomSpinner.SpinnerItem>> = PublishSubject.create()

    fun getLevelArr(): Disposable {
        isLoading.onNext(true)
        return dataManager.getListLevel()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val levels = gson.toList<TableLevel>(response.dataArray())
                        .map { CustomSpinner.SpinnerItem(it.title, it.id.toString()) }
                    rxLevelArr.onNext(levels)
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                it.printStackTrace()
                rxMessage.onNext(it.getErrorMsg())
                isLoading.onNext(false)
            })
    }

    fun getSubjectArr(): Disposable {
        isLoading.onNext(true)
        return dataManager.getSubjectData()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val subjects = gson.toList<TableSubject>(response.dataArray())
                        .map { CustomSpinner.SpinnerItem(it.title, it.id.toString()) }
                    rxSubjectArr.onNext(subjects)
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
                isLoading.onNext(false)
            })
    }

    fun getDetailDraft(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getTrainerDraftDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                try {
                    if (response.isSuccess()) {
                        try {
                            val data =
                                gson.fromJson(
                                    response.dataObject(),
                                    DetailDraftResponse::class.java
                                )
                            rxDetailDraftResponse.onNext(data)
                        } catch (e: Exception) {
                            rxMessage.onNext(loadStringRes(R.string.data_not_available))
                        }
                    } else {
                        rxMessage.onNext(response.message())
                    }
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    rxMessage.onNext(e.message.toString())
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    rxMessage.onNext(e.message.toString())
                }
            }, {
                isLoading.onNext(false)
                it.printStackTrace()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getDetailPractice(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getLessonPracticeDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val dataResponse =
                        gson.fromJsonSafe<DetailPracticeResponse>(response.dataObject())
                    if (dataResponse != null) {
                        rxDetailExerciseResponse.onNext(dataResponse)
                    }
                    logErr("getDataDetailPractice:${response.dataObject()}")
                } else {
                    rxMessage.onNext(response.message())
                }
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.printStackTrace()
            })
    }

    fun updatePractice(request: SaveExerciseRequest): Disposable {
        isLoading.onNext(true)
        return if (request.moreInformation.imagePath != null) {
            dataManager.uploadAvatar(File(request.moreInformation.imagePath!!))
                .flatMap { responseImage ->
                    request.imagePath = responseImage.dataObject().get(ApiConstants.SRC).asString
                    dataManager.updatePractice(request, request.moreInformation.idParent ?: 0)
                }
        } else {
            dataManager.updatePractice(request, request.moreInformation.idParent ?: 0)
        }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr("updatePractice:${gson.toJson(request)}")
                logErr("updatePractice:$response")
                isLoading.onNext(false)
                savePracticeSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun savePractice(request: SaveExerciseRequest): Disposable {
        isLoading.onNext(true)
        return if (request.imagePath == null) {
            dataManager.uploadAvatar(File(request.moreInformation.imagePath!!))
                .flatMap { responseImage ->
                    request.imagePath = responseImage.dataObject().get(ApiConstants.SRC).asString
                    dataManager.savePractice(request)
                }
        } else {
            dataManager.savePractice(request)
        }
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr("requestSave:${gson.toJson(request)}")
                logErr("responseSave:$response")
                isLoading.onNext(false)
                savePracticeSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }


}