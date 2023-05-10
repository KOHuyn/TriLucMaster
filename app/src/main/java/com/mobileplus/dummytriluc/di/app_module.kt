package com.mobileplus.dummytriluc.di

import androidx.room.Room
import com.google.gson.GsonBuilder
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.AppDataManager
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.local.db.AppDatabase
import com.mobileplus.dummytriluc.data.local.db.AppDbHelper
import com.mobileplus.dummytriluc.data.local.db.DbHelper
import com.mobileplus.dummytriluc.data.local.prefs.AppPrefsHelper
import com.mobileplus.dummytriluc.data.local.prefs.PrefsHelper
import com.mobileplus.dummytriluc.data.remote.ApiHelper
import com.mobileplus.dummytriluc.data.remote.AppApiHelper
import com.mobileplus.dummytriluc.service.TriLucNotification
import com.utils.SchedulerProvider
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single { SchedulerProvider() }
    single { AppPrefsHelper(get(), "dummytriluc", get()) as PrefsHelper }
    single { AppApiHelper() as ApiHelper }
    single { AppDbHelper(get()) as DbHelper }
    single { AppDataManager(get(), get(), get()) as DataManager }
    single { GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()!! }
    single { Room.databaseBuilder(get(), AppDatabase::class.java, "triluc.sqlite").build() }
    single {
        ViewPump.builder()
            .addInterceptor(
                CalligraphyInterceptor(
                    CalligraphyConfig.Builder().setDefaultFontPath(
                        "fonts/metrophobic_regular.ttf"
                    ).setFontAttrId(R.attr.fontPath).build()
                )
            ).build()
    }
    single { TriLucNotification(get()) }
}

val dummyModule = listOf(appModule, viewModule)