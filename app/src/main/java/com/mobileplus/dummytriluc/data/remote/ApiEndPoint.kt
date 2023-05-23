package com.mobileplus.dummytriluc.data.remote

import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.data.remote.ApiConstants.ID

object ApiEndPoint {

    private const val BASE_URL = BuildConfig.BASE_URL

    /**
     * Tài khoản
     */

    const val URL_GET_OTP = "$BASE_URL/retrieval"
    const val URL_POST_NEW_PASSWORD = "$BASE_URL/remember"
    const val URL_LOGIN = "$BASE_URL/login"
    const val URL_LOGIN_SOCIAL = "$BASE_URL/login-social"
    const val URL_REGISTER = "$BASE_URL/register"
    const val URL_LOGOUT = "$BASE_URL/auth/logout"
    const val URL_GET_INFO_USER = "$BASE_URL/auth/user-info"
    const val URL_UPDATE_INFO_USER = "$BASE_URL/auth/update-user-info"
    const val URL_UPDATE_PASSWORD = "$BASE_URL/auth/update-password"
    const val URL_UPDATE_AVATAR = "$BASE_URL/auth/update-avatar"
    const val URL_GET_SUBJECT_DATA = "$BASE_URL/subject/list"
    const val URL_GET_HOME_LIST = "$BASE_URL/home/list"
    const val URL_GET_USER_GUEST = "$BASE_URL/auth/user-info/{$ID}"

    /**
     * Media
     */
    const val URL_UPLOAD_FILE = "$BASE_URL/media/file-upload"

    /**
     * Practice
     */

    const val URL_POST_SUBMIT_PRACTICE_RESULT = "$BASE_URL/practice/save-result"
    const val URL_POST_SUBMIT_MULTI_PRACTICE_RESULT = "$BASE_URL/practice/save-result-multy-user"
    const val URL_GET_PRACTICE_LIST = "$BASE_URL/practice/list"
    const val URL_GET_PRACTICE_LIST_MORE = "$BASE_URL/practice/list/{$ID}"
    const val URL_GET_LESSON_DETAIL_PRACTICE = "$BASE_URL/practice/detail/{$ID}"
    const val URL_SEARCH_PRACTICE = "$BASE_URL/practice/search"
    const val URL_SEARCH_TRAINER = "$BASE_URL/trainer/search"
    const val URL_RESULT_PRACTICE = "$BASE_URL/practice/detail/result/{$ID}"
    const val URL_SAVE_PRACTICE = "$BASE_URL/trainer/save-practice"
    const val URL_GET_PRACTICE_LIST_FOLDER = "$BASE_URL/practice/load-more-list-folder/{$ID}"
    const val URL_GET_PRACTICE_AVG = "$BASE_URL/practice/get-avg-result"

    /**
     * Coach
     */
    const val URL_GET_STATISTICAL_COACH = "$BASE_URL/trainer/statistical"
    const val URL_GET_TRAINER_LIST = "$BASE_URL/trainer/list"
    const val URL_GET_TRAINER_DRAFT = "$BASE_URL/trainer/draft"
    const val URL_GET_PRACTICE_GUEST = "$BASE_URL/practice/list_by_user/{$ID}"
    const val URL_POST_SUBMIT_TRAINER_DRAFT = "$BASE_URL/trainer/save-draft"
    const val URL_GET_TRAINER_DRAFT_DETAIL = "$BASE_URL/trainer/detail/{$ID}"
    const val URL_DELETE_TRAINER_DRAFT = "$BASE_URL/trainer/delete-draft/{$ID}"
    const val URL_POST_TRAINER_REQUEST = "$BASE_URL/trainer/request"
    const val URL_POST_TRAINER_REQUEST_REMOVE = "$BASE_URL/trainer/request_remove"
    const val URL_GET_TRAINER_CHECK_MASTER = "$BASE_URL/trainer/check_master"
    const val URL_POST_TRAINER_REGISTER = "$BASE_URL/trainer/register"
    const val URL_GET_ALL_LIST_ASSIGN_CLASS = "$BASE_URL/trainer/list-assign-class/{$ID}"
    const val URL_POST_ASSIGN = "$BASE_URL/trainer/assign"
    const val URL_DELETE_ASSIGN_CLASS = "$BASE_URL/trainer/delete-assign-class"
    const val URL_DELETE_ASSIGN = "$BASE_URL/trainer/delete-assign"
    const val URL_GET_CLASS_JOIN = "$BASE_URL/practice/list_class_joined"
    const val URL_GET_ASSIGNED = "$BASE_URL/trainer/list-assign"
    const val URL_GET_TRAINER_DRAFT_FOLDER = "$BASE_URL/trainer/draft-folder"
    const val URL_POST_CREATE_DRAFT_FOLDER = "$BASE_URL/trainer/create-folder-draft"
    const val URL_POST_RENAME_DRAFT_FOLDER = "$BASE_URL/trainer/rename-folder-draft"
    const val URL_POST_UPDATE_DRAFT = "$BASE_URL/trainer/update-my-practice-draft"
    const val URL_POST_SAVE_FOLDER_PRACTICE = "$BASE_URL/trainer/save-practice-folder"
    const val URL_POST_EDITOR_FOLDER_PRACTICE = "$BASE_URL/trainer/update-practice-folder/{$ID}"
    const val URL_POST_EDITOR_PRACTICE = "$BASE_URL/trainer/update-practice/{$ID}"
    const val URL_DELETE_PRACTICE = "$BASE_URL/trainer/delete-practice/{$ID}"

    //class_student
    const val URL_GET_DISCIPLE_GROUP_LIST = "$BASE_URL/trainer/class/list"
    const val URL_POST_DISCIPLE_GROUP_CREATE = "$BASE_URL/trainer/class/create"
    const val URL_DELETE_DISCIPLE_GROUP = "$BASE_URL/trainer/class/delete/{$ID}"
    const val URL_POST_EDIT_NAME_DISCIPLE_GROUP = "$BASE_URL/trainer/class/rename/{$ID}"
    const val URL_GET_MEMBER_IN_GROUP = "$BASE_URL/trainer/class/detail/{$ID}"
    const val URL_GET_ALL_MEMBER_IN_GROUP = "$BASE_URL/trainer/class/detail_all/{$ID}"
    const val URL_DELETE_MEMBER_FROM_GROUP = "$BASE_URL/trainer/class/remove_student"
    const val URL_ADD_MEMBER_IN_GROUP = "$BASE_URL/trainer/class/add_student"
    const val URL_ADD_MANY_MEMBER_INTO_GROUP = "$BASE_URL/trainer/class/add_many_student"

    //student
    const val URL_GET_DISCIPLE_RANKING = "$BASE_URL/trainer/rank_student"
    const val URL_GET_DISCIPLE_LIST = "$BASE_URL/trainer/student/list"
    const val URL_GET_DISCIPLE_WAITING = "$BASE_URL/trainer/request_list"
    const val URL_POST_ACCEPT_DISCIPLE = "$BASE_URL/trainer/student/accept/{$ID}"
    const val URL_POST_REJECT_DISCIPLE = "$BASE_URL/trainer/student/reject/{$ID}"
    const val URL_DELETE_DISCIPLE = "$BASE_URL/trainer/student/delete/{$ID}"

    //session
    const val URL_COACH_SESSION_CREATE = "$BASE_URL/session/create"
    const val URL_COACH_SESSION_DELETE = "$BASE_URL/session/{$ID}"
    const val URL_COACH_SESSION_LIST = "$BASE_URL/session/list"
    const val URL_COACH_SESSION_SAVE = "$BASE_URL/session/save-result"
    const val URL_COACH_SESSION_LIST_PRACTICE = "$BASE_URL/session/practice_list"
    const val URL_COACH_SESSION_LIST_PRACTICE_DETAIL =
        "$BASE_URL/session/practice_list/detail/{$ID}"
    const val URL_COACH_SESSION_LIST_PRACTICE_SAVE = "$BASE_URL/session/practice_list/save"
    const val URL_COACH_SESSION_LIST_PRACTICE_DELETE = "$BASE_URL/session/practice_list/{$ID}"
    const val URL_COACH_SESSION_DETAIL = "$BASE_URL/session/detail/{$ID}"


    /**
     * Tin tức
     */
    const val URL_GET_FEED = "$BASE_URL/news/feed"
    const val URL_GET_FEED_DETAIL = "$BASE_URL/news/detail"

    /**
     * Notification
     */
    const val URL_GET_NOTIFICATION = "$BASE_URL/notification/list/"
    const val URL_GET_NOTIFICATION_DETAIL = "$BASE_URL/notification/read/{$ID}"

    /**
     * Challenge
     */

    const val URL_GET_CHALLENGE = "$BASE_URL/challenge"
    const val URL_GET_CHALLENGE_MORE = "$BASE_URL/challenge/more"
    const val URL_GET_CHALLENGE_DETAIL = "$BASE_URL/challenge/detail/{$ID}"
    const val URL_POST_SUBMIT_CHALLENGE = "$BASE_URL/challenge/save-result"
    const val URL_POST_JOIN_CHALLENGE = "$BASE_URL/challenge/join/{$ID}"
    const val URL_START_CHALLENGE = "$BASE_URL/challenge/start/{$ID}"

    /**
     * Punch
     */
    const val URL_GET_PUNCH = "$BASE_URL/home/chart_punch"

    /**
     * Power
     */
    const val URL_GET_POWER = "$BASE_URL/home/chart_power"

    /**
     * Rank
     */
    const val URL_GET_RANK = "$BASE_URL/home/rank"

    /**
     * Chat
     */
    const val URL_LIST_ROOM_CHAT = "$BASE_URL/chat/get_list_room"
    const val URL_ROOM_CHAT = "$BASE_URL/chat/get"
    const val URL_CHAT_SEND = "$BASE_URL/chat/send"

    /**
     * Utils
     */
    const val URL_GET_LIST_SUBJECT = "$BASE_URL/subject/list"
    const val URL_GET_LIST_LEVEL = "$BASE_URL/level/list"
    const val URL_GET_CONFIG = "$BASE_URL/config"
    const val URL_REMOVE_ALL_TOKEN = "$BASE_URL/remove_all_token"
    const val URL_REFRESH_TOKEN = "$BASE_URL/auth/refresh-token"
    const val URL_ERROR_LOG = "$BASE_URL/machine_error/log"
    const val URL_REQUEST_CONNECT_MACHINE = "$BASE_URL/requestConnectMachine"
    const val URL_FORCE_CONNECT_MACHINE = "$BASE_URL/forceConnect"
    const val URL_PRACTICE_GET_DATA_RESULT = "$BASE_URL/practice/getDataResult"
}