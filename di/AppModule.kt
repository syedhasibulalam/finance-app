package com.achievemeaalk.freedjf.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.achievemeaalk.freedjf.data.db.DatabaseMigrations
import com.achievemeaalk.freedjf.data.db.MyFinDatabase
import com.achievemeaalk.freedjf.data.db.accounts.AccountDao
import com.achievemeaalk.freedjf.data.db.budgets.BudgetDao
import com.achievemeaalk.freedjf.data.db.categories.CategoryDao
import com.achievemeaalk.freedjf.data.db.recurring.RecurringTransactionDao
import com.achievemeaalk.freedjf.data.db.transactions.TransactionDao
import com.achievemeaalk.freedjf.data.preferences.PreferencesRepository
import com.achievemeaalk.freedjf.util.getPredefinedCategories
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Provider
import javax.inject.Singleton

@Module(includes = [SecurityModule::class])
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @LocaleAwareContext
    fun provideLocaleAwareContext(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): Context {
        val language = sharedPreferences.getString(PreferencesRepository.KEY_LANGUAGE, "en") ?: "en"
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    @Singleton
    @Provides
    fun provideMyFinDatabase(
        @ApplicationContext context: Context,
        categoryDaoProvider: Provider<CategoryDao>
    ): MyFinDatabase {
        return Room.databaseBuilder(
            context,
            MyFinDatabase::class.java,
            "monefy_db"
        )
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .fallbackToDestructiveMigrationOnDowngrade()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val categoryDao = categoryDaoProvider.get()
                            val predefinedCategories = getPredefinedCategories(context)
                            predefinedCategories.forEach {
                                categoryDao.insertCategory(it)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }).build()
    }

    @Singleton
    @Provides
    fun provideTransactionDao(database: MyFinDatabase): TransactionDao = database.transactionDao()

    @Singleton
    @Provides
    fun provideAccountDao(database: MyFinDatabase): AccountDao = database.accountDao()

    @Singleton
    @Provides
    fun provideBudgetDao(database: MyFinDatabase): BudgetDao = database.budgetDao()

    @Singleton
    @Provides
    fun provideCategoryDao(database: MyFinDatabase): CategoryDao = database.categoryDao()

    @Singleton
    @Provides
    fun provideRecurringTransactionDao(database: MyFinDatabase): RecurringTransactionDao = database.recurringTransactionDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("monefy_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        sharedPreferences: SharedPreferences,
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepository(sharedPreferences, context)
    }

    @Singleton
    @Provides
    @ApplicationScope
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Default)
    }
}