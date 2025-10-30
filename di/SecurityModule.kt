package com.achievemeaalk.freedjf.di

import android.content.Context
import com.achievemeaalk.freedjf.data.security.SecurityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.achievemeaalk.freedjf.data.security.SecurityRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecurityRepository(@ApplicationContext context: Context): SecurityRepository {
        return SecurityRepositoryImpl(context)
    }
}
