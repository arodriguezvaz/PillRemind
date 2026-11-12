package com.andres.pillremind.ui.components

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.IntakeVisualState
import com.andres.pillremind.domain.model.IntakeWithMedication
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz para [IntakeCard].
 *
 * Comprueban que una toma ya registrada se puede deshacer desde su tarjeta,
 * que es el comportamiento que no cubren los tests del ViewModel.
 */
@RunWith(AndroidJUnit4::class)
class IntakeCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun elBotonTomadoEstaHabilitadoYDeshaceLaToma() {
        var pulsado = false
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val textoTomado = context.getString(R.string.tomado)

        composeRule.setContent {
            PillRemindTheme {
                IntakeCard(
                    intake = intake(takenAt = LocalDateTime.of(2026, 11, 5, 8, 5)),
                    visualState = IntakeVisualState.TAKEN,
                    onTake = { pulsado = true }
                )
            }
        }

        composeRule
            .onNodeWithText(textoTomado)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle { assertTrue(pulsado) }
    }

    @Test
    fun pulsarLaCardAbreLaInformacionYNoRegistraLaToma() {
        var cardPulsada = false
        var tomaPulsada = false

        composeRule.setContent {
            PillRemindTheme {
                IntakeCard(
                    intake = intake(takenAt = null),
                    visualState = IntakeVisualState.PENDING,
                    onTake = { tomaPulsada = true },
                    onCardClick = { cardPulsada = true },
                )
            }
        }

        composeRule
            .onNodeWithText("Ibuprofeno")
            .performClick()

        composeRule.runOnIdle {
            assertTrue(cardPulsada)
            assertFalse(tomaPulsada)
        }
    }

    @Test
    fun pulsarElBotonTomarNoAbreLaInformacion() {
        var cardPulsada = false
        var tomaPulsada = false
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            PillRemindTheme {
                IntakeCard(
                    intake = intake(takenAt = null),
                    visualState = IntakeVisualState.PENDING,
                    onTake = { tomaPulsada = true },
                    onCardClick = { cardPulsada = true },
                )
            }
        }

        composeRule
            .onNodeWithText(context.getString(R.string.tomar))
            .performClick()

        composeRule.runOnIdle {
            assertTrue(tomaPulsada)
            assertFalse(cardPulsada)
        }
    }

    private fun intake(takenAt: LocalDateTime?): IntakeWithMedication =
        IntakeWithMedication(
            intakeId = 1,
            medicationId = 1,
            scheduledAt = LocalDateTime.of(2026, 11, 5, 8, 0),
            scheduleId = 1,
            takenAt = takenAt,
            medicationName = "Ibuprofeno",
            dose = 600.0,
            unit = "mg",
            imageUrl = null,
            profileName = "Ana"
        )
}
