package com.mobileplus.dummytriluc.ui.utils.language

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import java.util.*

/**
 * Created by KO Huyn on 04/10/2021.
 */
object LocalManageUtil {
    private const val TAG = "LocalManageUtil"

    fun getSystemLocale(context: Context): Locale {
        return SPUtil.getInstance(context).getSystemCurrentLocal()
    }

    fun getSetLanguageLocale(context: Context): Locale {
        return LanguageEnum.values()
            .find { it.code == SPUtil.getInstance(context).getSelectLanguage() }?.locale
            ?: LanguageEnum.VI.locale
    }

    fun saveSelectLanguage(context: Context?, languageCode: LanguageEnum) {
        if (context == null) return
        SPUtil.getInstance(context).saveLanguage(languageCode.code)
        setApplicationLanguage(context)
    }

    fun setLocal(context: Context?): Context? {
        return if (context == null) null else updateResources(
            context,
            getSetLanguageLocale(context)
        )
    }

    private fun updateResources(context: Context, locale: Locale): Context? {
        var contextZ = context
        Locale.setDefault(locale)
        val res = contextZ.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        contextZ = contextZ.createConfigurationContext(config)
        return contextZ
    }

    fun setApplicationLanguage(context: Context) {
        val resources = context.applicationContext.resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        val locale = getSetLanguageLocale(context)
        config.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            context.applicationContext.createConfigurationContext(config)
            Locale.setDefault(locale)
        }
        resources.updateConfiguration(config, dm)
    }

    fun setContextLanguage(context: Context?) {
        if (context == null) return
        val resources = context.resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        val locale = getSetLanguageLocale(context)
        config.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
            context.createConfigurationContext(config)
            Locale.setDefault(locale)
        }
        resources.updateConfiguration(config, dm)
    }

    private fun saveSystemCurrentLanguage(context: Context) {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }
        Log.d(TAG, locale.language)
        SPUtil.getInstance(context).setSystemCurrentLocal(locale)
    }

    fun onConfigurationChanged(context: Context) {
        saveSystemCurrentLanguage(context)
        setLocal(context)
        setApplicationLanguage(context)
    }
}