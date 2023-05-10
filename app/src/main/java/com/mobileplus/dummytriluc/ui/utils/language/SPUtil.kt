package com.mobileplus.dummytriluc.ui.utils.language

import android.content.Context
import android.content.SharedPreferences
import com.mobileplus.dummytriluc.DummyTriLucApplication
import java.util.*

/**
 * Created by KO Huyn on 04/10/2021.
 */
class SPUtil(private val context: Context) {
    private var systemCurrentLocal = LanguageEnum.VI.locale

    init {
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        @Volatile
        private var instance: SPUtil? = null
        private val SP_NAME = "language_setting"
        private val TAG_LANGUAGE = "language_select"
        private var mSharedPreferences: SharedPreferences? = null

        fun getInstance(context: Context): SPUtil {
            if (instance == null) {
                synchronized(SPUtil::class.java) {
                    if (instance == null) {
                        instance = SPUtil(context)
                    }
                }
            }
            return instance!!
        }
    }


    fun saveLanguage(languageCode: String) {
        val edit = mSharedPreferences!!.edit()
        edit.putString(TAG_LANGUAGE, languageCode)
        edit.apply()
    }

    fun getSelectLanguage(): String {
        val defaultLocalCode =
            context.resources.configuration.locale.language
        val codeLanguage =
            LanguageEnum.values().find { it.code == defaultLocalCode }?.code ?: LanguageEnum.EN.code
        return mSharedPreferences?.getString(TAG_LANGUAGE, codeLanguage)
            ?: codeLanguage
    }

    fun getSystemCurrentLocal(): Locale {
        return systemCurrentLocal
    }

    fun setSystemCurrentLocal(local: Locale) {
        systemCurrentLocal = local
    }
}