package com.khanu.lisaa.core

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters

class RecoveryWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        return try {

            val intent = Intent(
                applicationContext,
                LisaaCoreService::class.java
            )

            applicationContext.startForegroundService(intent)

            Result.success()

        } catch (e: Exception) {

            e.printStackTrace()

            Result.retry()

        }

    }

}
