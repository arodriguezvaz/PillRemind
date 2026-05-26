package com.andres.pillremind.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.andres.pillremind.domain.repository.IntakeRepository
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@AndroidEntryPoint
class MarkAsTakenReceiver : BroadcastReceiver() {

    @Inject
    lateinit var intakeRepository: IntakeRepository

    override fun onReceive(context: Context, intent: Intent) {
        val intakeId = intent.getIntExtra("intake_id", -1)
        if (intakeId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val intake = intakeRepository.getIntakeById(intakeId)
                intakeRepository.updateIntake(intake!!.copy(takenAt = LocalDateTime.now()))
                NotificationManagerCompat.from(context).cancel(intakeId)
            } catch (e: Exception) {
                Log.e("PillDebug", "MarkAsTaken - Error", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}