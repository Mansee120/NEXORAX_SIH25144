package com.health.stridox.di

import androidx.room.Room
import com.health.stridox.alarm.ReminderAlarmController
import com.health.stridox.data.PreferencesImpl
import com.health.stridox.data.StridoxDatabase
import com.health.stridox.data.createDataStore
import com.health.stridox.data.repo.ReminderRepository
import com.health.stridox.data.repo.ReminderRepositoryImpl
import com.health.stridox.domain.Preferences
import com.health.stridox.ui.main.login.LoginViewModel
import com.health.stridox.ui.main.register.RegisterViewModel
import com.health.stridox.ui.main.home.screens.profile.ProfileViewModel
import com.health.stridox.ui.main.home.screens.reminder.RemindersViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val preferencesModule = module {
    singleOf(::createDataStore)
    singleOf(::PreferencesImpl) { bind<Preferences>() }
}
val viewModelModule = module {
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RemindersViewModel)
}

val dbModule = module {

    single {
        Room.databaseBuilder(
            get(),
            StridoxDatabase::class.java,
            "StridoxDatabase.db"
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<StridoxDatabase>().intakeReminderDao() }
    single { ReminderAlarmController(androidContext()) }
}

val repoModule = module {
    singleOf(::ReminderRepositoryImpl) { bind<ReminderRepository>() }
}
