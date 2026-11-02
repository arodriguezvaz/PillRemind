package com.andres.pillremind.ui.components

import androidx.annotation.StringRes
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.BloodType
import com.andres.pillremind.domain.model.Sex

/**
 * Devuelve el recurso de texto que representa un [Sex] en la interfaz.
 */
@StringRes
fun Sex.labelRes(): Int = when (this) {
    Sex.FEMALE -> R.string.profile_sex_female
    Sex.MALE -> R.string.profile_sex_male
    Sex.OTHER -> R.string.profile_sex_other
    Sex.PREFER_NOT_TO_SAY -> R.string.profile_sex_prefer_not_to_say
}

/**
 * Devuelve el recurso de texto que representa un [BloodType] en la interfaz.
 */
@StringRes
fun BloodType.labelRes(): Int = when (this) {
    BloodType.A_POSITIVE -> R.string.profile_blood_a_positive
    BloodType.A_NEGATIVE -> R.string.profile_blood_a_negative
    BloodType.B_POSITIVE -> R.string.profile_blood_b_positive
    BloodType.B_NEGATIVE -> R.string.profile_blood_b_negative
    BloodType.AB_POSITIVE -> R.string.profile_blood_ab_positive
    BloodType.AB_NEGATIVE -> R.string.profile_blood_ab_negative
    BloodType.O_POSITIVE -> R.string.profile_blood_o_positive
    BloodType.O_NEGATIVE -> R.string.profile_blood_o_negative
}
