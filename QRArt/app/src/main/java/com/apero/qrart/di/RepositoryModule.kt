package com.apero.qrart.di

import com.apero.qrart.data.repository.history.QRHistoryRepository
import com.apero.qrart.data.repository.history.QRHistoryRepositoryImpl
import com.apero.qrart.data.repository.qrgenerate.QRGenerateRepository
import com.apero.qrart.data.repository.qrgenerate.QRGenerateRepositoryImpl
import com.apero.qrart.data.repository.template.QRTemplateRepository
import com.apero.qrart.data.repository.template.QRTemplateRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Created by KO Huyn on 26/06/2023.
 */
@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    @ViewModelScoped
    fun provideQrHistoryRepository(): QRHistoryRepository {
        return QRHistoryRepositoryImpl()
    }

    @Provides
    @ViewModelScoped
    fun provideQRGenerateRepository(): QRGenerateRepository {
        return QRGenerateRepositoryImpl()
    }

    @Provides
    @ViewModelScoped
    fun provideQRTemplateRepository(): QRTemplateRepository {
        return QRTemplateRepositoryImpl()
    }
}