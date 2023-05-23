package com.mobileplus.dummytriluc.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.data.remote.ApiConstants.ASSIGN_ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.AVATAR_PATH
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CLASS_ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CLASS_ID_ARR
import com.mobileplus.dummytriluc.data.remote.ApiConstants.CODE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.DATA
import com.mobileplus.dummytriluc.data.remote.ApiConstants.DAY_RANGE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.EMAIL
import com.mobileplus.dummytriluc.data.remote.ApiConstants.FILE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.FOLDER_ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.KEYWORD
import com.mobileplus.dummytriluc.data.remote.ApiConstants.KEY_ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.LEVEL
import com.mobileplus.dummytriluc.data.remote.ApiConstants.MASTER_ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.MESSAGE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.NAME
import com.mobileplus.dummytriluc.data.remote.ApiConstants.OBJECT_TYPE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.OLD_PASSWORD
import com.mobileplus.dummytriluc.data.remote.ApiConstants.PAGE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.PAGINATE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.PARENT_ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.PASSWORD
import com.mobileplus.dummytriluc.data.remote.ApiConstants.RE_PASSWORD
import com.mobileplus.dummytriluc.data.remote.ApiConstants.ROOM_ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.SORT
import com.mobileplus.dummytriluc.data.remote.ApiConstants.SUBJECT
import com.mobileplus.dummytriluc.data.remote.ApiConstants.TITLE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.TYPE
import com.mobileplus.dummytriluc.data.remote.ApiConstants.USER
import com.mobileplus.dummytriluc.data.remote.ApiConstants.USER_ID
import com.mobileplus.dummytriluc.data.remote.ApiConstants.USER_ID_ARR
import com.mobileplus.dummytriluc.data.remote.ApiConstants.VIDEO_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_ADD_MANY_MEMBER_INTO_GROUP
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_ADD_MEMBER_IN_GROUP
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_CHAT_SEND
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_CREATE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_DELETE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_DETAIL
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_LIST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_LIST_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_LIST_PRACTICE_DELETE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_LIST_PRACTICE_DETAIL
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_LIST_PRACTICE_SAVE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_COACH_SESSION_SAVE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_DELETE_ASSIGN
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_DELETE_ASSIGN_CLASS
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_DELETE_DISCIPLE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_DELETE_DISCIPLE_GROUP
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_DELETE_MEMBER_FROM_GROUP
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_DELETE_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_DELETE_TRAINER_DRAFT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_ALL_LIST_ASSIGN_CLASS
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_ALL_MEMBER_IN_GROUP
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_ASSIGNED
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_CHALLENGE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_CHALLENGE_DETAIL
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_CHALLENGE_MORE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_CLASS_JOIN
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_CONFIG
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_DISCIPLE_GROUP_LIST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_DISCIPLE_LIST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_DISCIPLE_RANKING
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_DISCIPLE_WAITING
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_FEED
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_FEED_DETAIL
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_HOME_LIST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_INFO_USER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_LESSON_DETAIL_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_LIST_LEVEL
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_LIST_SUBJECT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_MEMBER_IN_GROUP
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_NOTIFICATION
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_NOTIFICATION_DETAIL
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_OTP
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_POWER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_PRACTICE_AVG
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_PRACTICE_GUEST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_PRACTICE_LIST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_PRACTICE_LIST_FOLDER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_PRACTICE_LIST_MORE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_PUNCH
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_RANK
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_STATISTICAL_COACH
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_SUBJECT_DATA
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_TRAINER_CHECK_MASTER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_TRAINER_DRAFT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_TRAINER_DRAFT_DETAIL
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_TRAINER_DRAFT_FOLDER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_TRAINER_LIST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_GET_USER_GUEST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_LIST_ROOM_CHAT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_LOGIN
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_LOGIN_SOCIAL
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_LOGOUT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_ACCEPT_DISCIPLE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_ASSIGN
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_CREATE_DRAFT_FOLDER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_DISCIPLE_GROUP_CREATE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_EDITOR_FOLDER_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_EDITOR_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_EDIT_NAME_DISCIPLE_GROUP
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_JOIN_CHALLENGE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_NEW_PASSWORD
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_REJECT_DISCIPLE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_RENAME_DRAFT_FOLDER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_SAVE_FOLDER_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_SUBMIT_CHALLENGE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_SUBMIT_MULTI_PRACTICE_RESULT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_SUBMIT_PRACTICE_RESULT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_SUBMIT_TRAINER_DRAFT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_TRAINER_REGISTER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_TRAINER_REQUEST
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_TRAINER_REQUEST_REMOVE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_POST_UPDATE_DRAFT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_REGISTER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_RESULT_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_ROOM_CHAT
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_SAVE_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_SEARCH_PRACTICE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_SEARCH_TRAINER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_START_CHALLENGE
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_UPDATE_AVATAR
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_UPDATE_INFO_USER
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_UPDATE_PASSWORD
import com.mobileplus.dummytriluc.data.remote.ApiEndPoint.URL_UPLOAD_FILE
import com.mobileplus.dummytriluc.data.request.*
import com.mobileplus.dummytriluc.data.request.session.CoachSessionCreateRequest
import com.mobileplus.dummytriluc.data.request.session.CoachSessionSavedListRequest
import com.mobileplus.dummytriluc.ui.utils.extensions.deviceId
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Single
import org.json.JSONObject
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File

class AppApiHelper : ApiHelper, KoinComponent {
    private val gson by inject<Gson>()

    fun Any.toJson() = JSONObject(gson.toJson(this))

    /**
     * Tài khoản
     */

    override fun login(request: LoginRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_LOGIN)
            .addBodyParameter(request)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun register(request: RegisterRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_REGISTER)
            .addBodyParameter(request)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun updateAvatar(src: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_UPDATE_AVATAR)
            .addBodyParameter(AVATAR_PATH, src)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun updateUserInfo(data: UpdateInfo): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_UPDATE_INFO_USER)
            .addBodyParameter(data)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getSubjectData(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_SUBJECT_DATA)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getUserInfoSever(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_INFO_USER)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun logoutServer(uuid: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_LOGOUT)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun requestOTPPassword(email: String): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_OTP)
            .addQueryParameter(EMAIL, email)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postNewPassword(
        email: String,
        code: String,
        password: String,
        rePassword: String
    ): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_NEW_PASSWORD)
            .addBodyParameter(EMAIL, email)
            .addBodyParameter(CODE, code)
            .addBodyParameter(PASSWORD, password)
            .addBodyParameter(RE_PASSWORD, rePassword)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun loginSocial(body: SocialLoginRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_LOGIN_SOCIAL)
            .addBodyParameter(body)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun updatePassword(oldPassword: String, newPassword: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_UPDATE_PASSWORD)
            .addBodyParameter(OLD_PASSWORD, oldPassword)
            .addBodyParameter(RE_PASSWORD, newPassword)
            .addBodyParameter(PASSWORD, newPassword)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getHomeList(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_HOME_LIST)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getUserGuest(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_USER_GUEST)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * Media
     */

    override fun uploadAvatar(file: File): Single<JsonObject> {
        return Rx2AndroidNetworking.upload(URL_UPLOAD_FILE)
            .addMultipartFile(FILE, file)
            .addMultipartParameter(OBJECT_TYPE, USER)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun uploadVideo(file: File): Single<JsonObject> {
        return Rx2AndroidNetworking.upload(URL_UPLOAD_FILE)
            .addMultipartFile(FILE, file)
            .addMultipartParameter(OBJECT_TYPE, VIDEO_PRACTICE)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * Practice
     */
    override fun postSubmitPracticeResult(request: SubmitPracticeResultRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_SUBMIT_PRACTICE_RESULT)
            .addJSONObjectBody(request.toJson())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postSubmitMultiPracticeResult(request: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_SUBMIT_MULTI_PRACTICE_RESULT)
            .addBodyParameter(DATA, request)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postSubmitModeFreedomPractice(request: SubmitModeFreedomPractice): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_SUBMIT_PRACTICE_RESULT)
            .addJSONObjectBody(request.toJson())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getListPractice(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_PRACTICE_LIST)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getListPracticeMore(
        id: Int,
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?,
    ): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_PRACTICE_LIST_MORE).apply {
            subject?.let { addQueryParameter(SUBJECT, it.toString()) }
            level?.let { addQueryParameter(LEVEL, it.toString()) }
            sort?.let { addQueryParameter(SORT, it) }
            keyword?.let { addQueryParameter(KEYWORD, it) }
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getLessonPracticeDetail(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_LESSON_DETAIL_PRACTICE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun searchPractice(
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?
    ): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_SEARCH_PRACTICE).apply {
            subject?.let { addQueryParameter(SUBJECT, it.toString()) }
            level?.let { addQueryParameter(LEVEL, it.toString()) }
            sort?.let { addQueryParameter(SORT, it) }
            keyword?.let { addQueryParameter(KEYWORD, it) }
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun searchMaster(
        subject: Int?,
        level: Int?,
        sort: String?,
        keyword: String?,
        page: Int?
    ): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_SEARCH_TRAINER).apply {
            subject?.let { addQueryParameter(SUBJECT, it.toString()) }
            level?.let { addQueryParameter(LEVEL, it.toString()) }
            sort?.let { addQueryParameter(SORT, it) }
            keyword?.let { addQueryParameter(KEYWORD, it) }
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getResultPractices(idPractice: Int, page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_RESULT_PRACTICE).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.addPathParameter(ID, idPractice.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun savePractice(request: SaveExerciseRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_SAVE_PRACTICE)
            .addJSONObjectBody(request.toJson().apply { remove("more") })
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getListPracticeFolder(
        id: Int,
        page: Int?,
        isPaginate: Boolean?
    ): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_PRACTICE_LIST_FOLDER)
            .addPathParameter(ID, id.toString()).apply {
                page?.let { addQueryParameter(PAGE, page.toString()) }
                isPaginate?.let { addQueryParameter(PAGINATE, isPaginate.toString()) }
            }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getPracticeAvg(practiceId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_PRACTICE_AVG)
            .addQueryParameter(ApiConstants.PRACTICE_ID, practiceId.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * Coach
     */

    override fun getStatisticalCoach(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_STATISTICAL_COACH)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getTrainerList(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_TRAINER_LIST).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getTrainerDraft(page: Int?, folderId: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_TRAINER_DRAFT).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
            folderId?.let { addQueryParameter(FOLDER_ID, it.toString()) }
        }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getTrainerDraftFolder(parentId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_TRAINER_DRAFT_FOLDER)
            .addQueryParameter(PARENT_ID, parentId.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun createTrainerDraftFolder(name: String, parentId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_CREATE_DRAFT_FOLDER)
            .addQueryParameter(NAME, name)
            .addQueryParameter(PARENT_ID, parentId.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun renameTrainerDraftFolder(name: String, parentId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_RENAME_DRAFT_FOLDER)
            .addQueryParameter(NAME, name)
            .addQueryParameter(FOLDER_ID, parentId.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getPracticeGuest(idGuest: Int, page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_PRACTICE_GUEST).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.addPathParameter(ID, idGuest.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getTrainerDraftDetail(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_TRAINER_DRAFT_DETAIL)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postSubmitCoachDraft(request: SubmitCoachDraftRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_SUBMIT_TRAINER_DRAFT)
            .addBodyParameter(request)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun deleteDraft(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.delete(URL_DELETE_TRAINER_DRAFT)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postTrainerRequest(message: String, masterId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_TRAINER_REQUEST)
            .addBodyParameter(MESSAGE, message)
            .addBodyParameter(MASTER_ID, masterId.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postTrainerRequestRemove(masterId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_TRAINER_REQUEST_REMOVE)
            .addBodyParameter(MASTER_ID, masterId.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun checkMaster(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_TRAINER_CHECK_MASTER)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun registerCoach(request: CoachRegisterRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_TRAINER_REGISTER)
            .addBodyParameter(request)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getCoachAssignExercise(
        idClass: Int,
        page: Int?
    ): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_ALL_LIST_ASSIGN_CLASS)
            .addPathParameter(ID, idClass.toString())
            .apply {
                page?.let { addQueryParameter(PAGE, page.toString()) }
            }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postCoachAssign(request: CoachAssignExerciseRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_ASSIGN)
            .addJSONObjectBody(request.toJson())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun deleteAssign(assignId: Int, classId: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.post(if (classId != null) URL_DELETE_ASSIGN_CLASS else URL_DELETE_ASSIGN)
            .addQueryParameter(ASSIGN_ID, assignId.toString()).apply {
                classId?.let { addQueryParameter(CLASS_ID, it.toString()) }
            }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getClassJoin(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_CLASS_JOIN).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getAssigned(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_ASSIGNED).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun updateDraft(id: Int, title: String, folderId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_UPDATE_DRAFT)
            .addBodyParameter(TITLE, title)
            .addBodyParameter(FOLDER_ID, folderId.toString())
            .addBodyParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun saveFolderPractice(request: CreateCourseRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_SAVE_FOLDER_PRACTICE)
            .addJSONObjectBody(request.toJson())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun updateFolderPractice(
        idCourse: Int,
        request: CreateCourseRequest
    ): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_EDITOR_FOLDER_PRACTICE)
            .addPathParameter(ID, idCourse.toString())
            .addJSONObjectBody(request.toJson())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun updatePractice(request: SaveExerciseRequest, parentId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_EDITOR_PRACTICE)
            .addPathParameter(ID, parentId.toString())
            .addJSONObjectBody(request.toJson().apply { remove("more") })
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun deletePractice(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.delete(URL_DELETE_PRACTICE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    //class student

    override fun getDiscipleGroupList(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_DISCIPLE_GROUP_LIST).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postCreateGroup(groupName: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_DISCIPLE_GROUP_CREATE)
            .addBodyParameter(TITLE, groupName)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun deleteGroup(groupID: String): Single<JsonObject> {
        return Rx2AndroidNetworking.delete(URL_DELETE_DISCIPLE_GROUP)
            .addPathParameter(ID, groupID)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postRenameGroup(id: Int, newName: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_EDIT_NAME_DISCIPLE_GROUP)
            .addPathParameter(ID, id.toString())
            .addBodyParameter(TITLE, newName)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getMemberInGroup(idGroup: Int, dayRange: Int?, page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_MEMBER_IN_GROUP).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
            dayRange?.let { addQueryParameter(DAY_RANGE, it.toString()) }
        }
            .addPathParameter(ID, idGroup.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getAllMemberInGroup(idGroup: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_ALL_MEMBER_IN_GROUP)
            .addPathParameter(ID, idGroup.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun deleteMemberInGroup(classID: Int, userID: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.delete(URL_DELETE_MEMBER_FROM_GROUP)
            .addQueryParameter(CLASS_ID, classID.toString())
            .addQueryParameter(USER_ID, userID.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun addMemberInGroup(arrClassId: List<Int>, userID: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_ADD_MEMBER_IN_GROUP).apply {
            arrClassId.forEach { addQueryParameter(CLASS_ID_ARR, it.toString()) }
        }
            .addQueryParameter(USER_ID, userID.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun addManyMemberIntoGroup(classID: Int, userIds: List<Int>): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_ADD_MANY_MEMBER_INTO_GROUP).apply {
            userIds.forEach { addQueryParameter(USER_ID_ARR, it.toString()) }
        }
            .addQueryParameter(CLASS_ID, classID.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    //student
    override fun getDiscipleRanking(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_DISCIPLE_RANKING).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getDiscipleList(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_DISCIPLE_LIST).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getDiscipleWaiting(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_DISCIPLE_WAITING).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun acceptDisciple(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_POST_ACCEPT_DISCIPLE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun rejectDisciple(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_POST_REJECT_DISCIPLE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun deleteDisciple(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.delete(URL_DELETE_DISCIPLE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun createCoachSession(createRequest: CoachSessionCreateRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_COACH_SESSION_CREATE)
            .addJSONObjectBody(createRequest.toJson())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun savedCoachSessionListPractice(request: CoachSessionSavedListRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_COACH_SESSION_LIST_PRACTICE_SAVE)
            .addJSONObjectBody(request.toJson())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getCoachSessionPracticeSaved(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_COACH_SESSION_LIST_PRACTICE).apply {
            page?.let { addQueryParameter(PAGE, page.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun deleteCoachSessionPractice(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.delete(URL_COACH_SESSION_LIST_PRACTICE_DELETE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getCoachSessionDetailSavedList(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_COACH_SESSION_LIST_PRACTICE_DETAIL)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getCoachSessionList(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_COACH_SESSION_LIST).apply {
            page?.let { addQueryParameter(PAGE, page.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun deleteCoachSession(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.delete(URL_COACH_SESSION_DELETE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getSessionDetail(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_COACH_SESSION_DETAIL)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun saveResultSession(data: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_COACH_SESSION_SAVE)
            .addBodyParameter(DATA, data)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * Challenge
     */
    override fun getChallenge(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_CHALLENGE)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getChallengeMore(type: String, page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_CHALLENGE_MORE)
            .apply { page?.let { addQueryParameter(PAGE, page.toString()) } }
            .addQueryParameter(TYPE, type)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getChallengeDetail(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_CHALLENGE_DETAIL)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postSubmitChallenge(request: SubmitChallengeRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_SUBMIT_CHALLENGE)
            .addBodyParameter(request)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postJoinChallenge(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_POST_JOIN_CHALLENGE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun startChallenge(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_START_CHALLENGE)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * News
     */
    override fun getFeed(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_FEED).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getFeedDetail(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_FEED_DETAIL)
            .addQueryParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * Notification
     */
    override fun getNotifications(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_NOTIFICATION).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getNotificationDetail(id: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_NOTIFICATION_DETAIL)
            .addPathParameter(ID, id.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * Punch
     */
    override fun getPunch(type: String?, page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_PUNCH).apply {
            type?.let { addQueryParameter(TYPE, it) }
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)

    }

    /**
     * Power
     */
    override fun getPower(type: String?, page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_POWER).apply {
            type?.let { addQueryParameter(TYPE, it) }
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)

    }

    /**
     * Rank
     */
    override fun getRanking(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_RANK).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * Chat
     */
    override fun getListChatRoom(page: Int?): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_LIST_ROOM_CHAT).apply {
            page?.let { addQueryParameter(PAGE, it.toString()) }
        }.build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getChatRoom(keyId: Int?, type: String?, roomId: Int): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_ROOM_CHAT).apply {
            type?.let { addQueryParameter(TYPE, it) }
            keyId?.let { addQueryParameter(KEY_ID, it.toString()) }
        }
            .addQueryParameter(ROOM_ID, roomId.toString())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun postChat(chat: ChatSendRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(URL_CHAT_SEND)
            .addBodyParameter(chat)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getListSubject(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_LIST_SUBJECT)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    /**
     * Utils
     */
    override fun getListLevel(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_LIST_LEVEL)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getConfig(): Single<JsonObject> {
        return Rx2AndroidNetworking.get(URL_GET_CONFIG)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun removeToken(): Single<JsonObject> {
        return Rx2AndroidNetworking.post(ApiEndPoint.URL_REMOVE_ALL_TOKEN)
            .addBodyParameter(ApiConstants.UUID, deviceId)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun refreshToken(): Single<JsonObject> {
        return Rx2AndroidNetworking.post(ApiEndPoint.URL_REFRESH_TOKEN)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun logErrorMachine(data: BleErrorRequest): Single<JsonObject> {
        return Rx2AndroidNetworking.post(ApiEndPoint.URL_ERROR_LOG)
            .addJSONObjectBody(data.toJson())
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun connectMachine(machineName: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(ApiEndPoint.URL_REQUEST_CONNECT_MACHINE)
            .addBodyParameter(ApiConstants.MACHINE_ID, machineName)
            .addBodyParameter(ApiConstants.UUID, deviceId)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun forceConnectMachine(machineName: String): Single<JsonObject> {
        return Rx2AndroidNetworking.post(ApiEndPoint.URL_FORCE_CONNECT_MACHINE)
            .addBodyParameter(ApiConstants.MACHINE_ID, machineName)
            .addBodyParameter(ApiConstants.UUID, deviceId)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }

    override fun getDataPracticeResult(practiceId: String): Single<JsonObject> {
        return Rx2AndroidNetworking.get(ApiEndPoint.URL_PRACTICE_GET_DATA_RESULT)
            .addQueryParameter("id", practiceId)
            .build()
            .getObjectSingle(JsonObject::class.java)
    }
}