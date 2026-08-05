package com.khanu.lisaa.core

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val WORK_NAME = "LISAA_RECOVERY"

    fun start(context: Context) {

        val request =
            PeriodicWorkRequestBuilder<RecoveryWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setInitialDelay(
                    1,
                    TimeUnit.MINUTES
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

}
