package com.mobileplus.dummytriluc.data

import com.mobileplus.dummytriluc.data.local.db.DbHelper
import com.mobileplus.dummytriluc.data.local.prefs.PrefsHelper
import com.mobileplus.dummytriluc.data.remote.ApiHelper

interface DataManager : ApiHelper, PrefsHelper, DbHelper {
    fun logout()
}