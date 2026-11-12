package com.andres.pillremind.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.ui.theme.PillRemindTheme
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz para [MedicationCard].
 *
 * Comprueban que la card y el botón tienen acciones independientes: pulsar la
 * card abre la información del medicamento y pulsar el botón ejecuta su acción
 * sin abrir también la card.
 */
@RunWith(AndroidJUnit4::class)
class MedicationCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pulsarLaCardAbreLaInformacionYNoEjecutaElBoton() {
        var cardPulsada = false
        var botonPulsado = false

        composeRule.setContent {
            PillRemindTheme {
                MedicationCard(
                    medication = medication(),
                    accion = { botonPulsado = true },
                    buttonText = "Ver",
                    onCardClick = { cardPulsada = true },
                )
            }
        }

        composeRule
            .onNodeWithText("Enalapril")
            .performClick()

        composeRule.runOnIdle {
            assertTrue(cardPulsada)
            assertFalse(botonPulsado)
        }
    }

    @Test
    fun pulsarElBotonNoAbreLaCard() {
        var cardPulsada = false
        var botonPulsado = false

        composeRule.setContent {
            PillRemindTheme {
                MedicationCard(
                    medication = medication(),
                    accion = { botonPulsado = true },
                    buttonText = "Ver",
                    onCardClick = { cardPulsada = true },
                )
            }
        }

        composeRule
            .onNodeWithText("Ver")
            .performClick()

        composeRule.runOnIdle {
            assertTrue(botonPulsado)
            assertFalse(cardPulsada)
        }
    }

    private fun medication(): Medication = Medication(
        id = 1,
        profileId = 1,
        name = "Enalapril",
        dose = 10.0,
        unit = "mg",
        startDate = LocalDate.of(2026, 3, 12),
    )
}
