package com.mobileplus.dummytriluc.ui.main.coach.create_course

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.model.entity.TableSubject
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.CreateCourseRequest
import com.mobileplus.dummytriluc.data.response.DetailPracticeFolderResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File

/**
 * Created by KOHuyn on 4/5/2021
 */
class CoachCreateCourseViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {
    val rxSubjectArr: PublishSubject<List<CustomSpinner.SpinnerItem>> = PublishSubject.create()
    val rxExercises: PublishSubject<Pair<List<ItemCoachPractice>, Page?>> =
        PublishSubject.create()
    val rxItemsExerciseEditor: PublishSubject<MutableList<ItemCoachPractice>> =
        PublishSubject.create()
    val rxCreateCourseSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val rxDetailCourseResponse: PublishSubject<DetailPracticeFolderResponse> =
        PublishSubject.create()

    val rxErrorData: PublishSubject<Unit> = PublishSubject.create()

    fun createCourse(request: CreateCourseRequest, imgFile: File): Disposable {
        logErr(gson.toJson(request))
        isLoading.onNext(true)
        return dataManager.uploadAvatar(imgFile)
            .flatMap { responseImage ->
                request.imagePath = responseImage.dataObject().get(ApiConstants.SRC).asString
                dataManager.saveFolderPractice(request)
            }.compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr("createCourse:$response")
                isLoading.onNext(false)
                rxCreateCourseSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun editorCourse(idCourse: Int, request: CreateCourseRequest, imgFile: File?): Disposable {
        logErr(gson.toJson(request))
        isLoading.onNext(true)
        return dataManager.run {
            if (imgFile != null) {
                uploadAvatar(imgFile)
                    .flatMap { responseImage ->
                        request.imagePath =
                            responseImage.dataObject().get(ApiConstants.SRC).asString
                        dataManager.updateFolderPractice(idCourse, request)
                    }
            } else {
                dataManager.updateFolderPractice(idCourse, request)
            }
        }.compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                logErr("editorCourse:$response")
                isLoading.onNext(false)
                rxCreateCourseSuccess.onNext(response.isSuccess())
                rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getListExerciseCourseNoPage(idCourse: Int): Disposable {
        return dataManager.getListPracticeFolder(idCourse, null, false)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val itemsEditor =
                        gson.fromJsonSafe<MutableList<ItemCoachPractice>>(response.dataArray())
                    if (itemsEditor != null) {
                        rxItemsExerciseEditor.onNext(itemsEditor)
                    }
                }
            }, {

            })
    }

    fun getDetailFolder(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getLessonPracticeDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    rxDetailCourseResponse.onNext(
                        gson.fromJson(
                            response.dataObject(),
                            DetailPracticeFolderResponse::class.java
                        )
                    )
                } else {
                    rxMessage.onNext(response.message())
                }
                logErr("getDetailPracticeFolder:$response")
            }, {
                rxErrorData.onNext(Unit)
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
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

    fun getExercises(page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getTrainerList(page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    if (response.isEmptyArray()) {
                        rxExercises.onNext(Pair(emptyList(), null))
                    } else {
                        try {
                            val items =
                                gson.toList<ItemCoachPractice>(response.dataObject().dataArray())
                            rxExercises.onNext(
                                Pair(items.toMutableList(), response.dataObject().page())
                            )
                        } catch (e: Exception) {
                            e.logErr()
                        }
                    }
                }
                if (response.isDataEmpty()) {
                    rxExercises.onNext(Pair(emptyList(), null))
                }
                logErr("getTrainerList: $response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}