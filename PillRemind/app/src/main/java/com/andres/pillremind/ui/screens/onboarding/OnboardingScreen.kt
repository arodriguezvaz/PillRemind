package com.andres.pillremind.ui.screens.onboarding

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andres.pillremind.R
import com.andres.pillremind.notifications.ExactAlarmStatus
import com.andres.pillremind.notifications.NotificationAction
import com.andres.pillremind.notifications.NotificationStatus
import com.andres.pillremind.notifications.POST_NOTIFICATIONS_PERMISSION
import com.andres.pillremind.notifications.appDetailsIntent
import com.andres.pillremind.notifications.exactAlarmSettingsIntent
import com.andres.pillremind.notifications.notificationSettingsIntent
import com.andres.pillremind.notifications.shouldShowNotificationRationale
import com.andres.pillremind.ui.components.AppButton
import com.andres.pillremind.ui.components.ButtonSize
import com.andres.pillremind.ui.theme.AppElevation
import com.andres.pillremind.ui.theme.AppSpacing
import com.andres.pillremind.ui.theme.PillRemindTheme

/**
 * Conecta la pantalla de bienvenida con su ViewModel y con los launchers de
 * permisos de Android.
 */
@Composable
fun OnboardingRoute(viewModel: OnboardingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.onNotificationPermissionResult(shouldShowNotificationRationale(activity))
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh(shouldShowNotificationRationale(activity))
    }

    OnboardingScreen(
        uiState = uiState,
        onNotificationClick = {
            when (uiState.notificationAction) {
                NotificationAction.REQUEST ->
                    notificationLauncher.launch(POST_NOTIFICATIONS_PERMISSION)

                NotificationAction.OPEN_SETTINGS ->
                    runCatching { context.startActivity(notificationSettingsIntent(context)) }

                NotificationAction.NONE -> Unit
            }
        },
        onExactAlarmClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = exactAlarmSettingsIntent(context)
                val opened = intent != null &&
                    runCatching { context.startActivity(intent) }.isSuccess

                viewModel.onExactAlarmRequestAttempted()
                if (!opened) {
                    runCatching { context.startActivity(appDetailsIntent(context)) }
                }
            }
        },
        onComplete = viewModel::complete,
        onCompleteWithoutExactAlarms = viewModel::completeWithoutExactAlarms,
    )
}

/**
 * Pantalla de bienvenida del primer arranque.
 *
 * Presenta la marca y solicita los permisos de notificaciones y alarmas
 * exactas. Es una pantalla stateless: todo su estado llega desde
 * [OnboardingUiState].
 */
@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNotificationClick: () -> Unit,
    onExactAlarmClick: () -> Unit,
    onComplete: () -> Unit,
    onCompleteWithoutExactAlarms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notificationActionLabel = when (uiState.notificationAction) {
        NotificationAction.REQUEST -> stringResource(R.string.onboarding_notifications_allow)
        NotificationAction.OPEN_SETTINGS -> stringResource(R.string.onboarding_open_settings)
        NotificationAction.NONE -> null
    }
    val exactAlarmActionLabel = if (uiState.exactAlarmStatus == ExactAlarmStatus.PENDING) {
        stringResource(R.string.onboarding_exact_alarms_allow)
    } else {
        null
    }

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                ) {
                    BrandHeader()

                    Text(
                        text = stringResource(R.string.onboarding_permissions_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { heading() },
                    )
                    Text(
                        text = stringResource(R.string.onboarding_permissions_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    PermissionCard(
                        icon = Icons.Rounded.Notifications,
                        title = stringResource(R.string.onboarding_notifications_title),
                        description = stringResource(R.string.onboarding_notifications_description),
                        status = uiState.notificationStatus.toUiStatus(),
                        actionLabel = notificationActionLabel,
                        onAction = onNotificationClick,
                    )

                    if (uiState.exactAlarmStatus != ExactAlarmStatus.NOT_REQUIRED) {
                        PermissionCard(
                            icon = Icons.Rounded.Alarm,
                            title = stringResource(R.string.onboarding_exact_alarms_title),
                            description = stringResource(R.string.onboarding_exact_alarms_description),
                            status = uiState.exactAlarmStatus.toUiStatus(),
                            actionLabel = exactAlarmActionLabel,
                            onAction = onExactAlarmClick,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                if (uiState.showExactAlarmFallback) {
                    Text(
                        text = stringResource(R.string.onboarding_exact_alarms_fallback_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = onCompleteWithoutExactAlarms) {
                        Text(stringResource(R.string.onboarding_exact_alarms_continue))
                    }
                }

                AppButton(
                    text = stringResource(R.string.onboarding_start),
                    onClick = onComplete,
                    enabled = uiState.canContinue,
                    isLoading = uiState.isSaving,
                    size = ButtonSize.Large,
                )

                Text(
                    text = stringResource(R.string.onboarding_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    val brandPill = stringResource(R.string.brand_pill)
    val brandRemind = stringResource(R.string.brand_remind)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(brandPill)
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Normal)) {
                    append(brandRemind)
                }
            },
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    status: PermissionStatusUi,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(AppElevation),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
            }
            PermissionStatusChip(status)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            actionLabel?.let { label ->
                Spacer(Modifier.height(AppSpacing.xs))
                AppButton(
                    text = label,
                    onClick = onAction,
                    size = ButtonSize.Large,
                )
            }
        }
    }
}

@Composable
private fun PermissionStatusChip(status: PermissionStatusUi) {
    val (containerColor, contentColor) = when (status) {
        PermissionStatusUi.GRANTED ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

        PermissionStatusUi.PENDING ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer

        PermissionStatusUi.BLOCKED,
        PermissionStatusUi.UNAVAILABLE ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer

        PermissionStatusUi.NOT_REQUIRED ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = stringResource(status.labelRes()),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                horizontal = AppSpacing.sm,
                vertical = AppSpacing.xxs,
            ),
        )
    }
}

/**
 * Estado visual común de los permisos.
 */
private enum class PermissionStatusUi {
    GRANTED,
    PENDING,
    BLOCKED,
    UNAVAILABLE,
    NOT_REQUIRED,
}

private fun PermissionStatusUi.labelRes(): Int = when (this) {
    PermissionStatusUi.GRANTED -> R.string.onboarding_status_granted
    PermissionStatusUi.PENDING -> R.string.onboarding_status_pending
    PermissionStatusUi.BLOCKED -> R.string.onboarding_status_blocked
    PermissionStatusUi.UNAVAILABLE -> R.string.onboarding_status_unavailable
    PermissionStatusUi.NOT_REQUIRED -> R.string.onboarding_status_not_required
}

private fun NotificationStatus.toUiStatus(): PermissionStatusUi = when (this) {
    NotificationStatus.GRANTED -> PermissionStatusUi.GRANTED
    NotificationStatus.REQUESTABLE -> PermissionStatusUi.PENDING
    NotificationStatus.BLOCKED -> PermissionStatusUi.BLOCKED
}

private fun ExactAlarmStatus.toUiStatus(): PermissionStatusUi = when (this) {
    ExactAlarmStatus.GRANTED -> PermissionStatusUi.GRANTED
    ExactAlarmStatus.PENDING -> PermissionStatusUi.PENDING
    ExactAlarmStatus.NOT_REQUIRED -> PermissionStatusUi.NOT_REQUIRED
    ExactAlarmStatus.UNAVAILABLE -> PermissionStatusUi.UNAVAILABLE
}

@Preview(showBackground = true, name = "Bienvenida pendiente")
@Composable
private fun OnboardingScreenPendingPreview() {
    PillRemindTheme {
        OnboardingScreen(
            uiState = OnboardingUiState(
                notificationStatus = NotificationStatus.REQUESTABLE,
                notificationAction = NotificationAction.REQUEST,
                exactAlarmStatus = ExactAlarmStatus.PENDING,
                canContinue = false,
            ),
            onNotificationClick = {},
            onExactAlarmClick = {},
            onComplete = {},
            onCompleteWithoutExactAlarms = {},
        )
    }
}

@Preview(
    showBackground = true,
    fontScale = 1.5f,
    name = "Bienvenida concedida",
)
@Composable
private fun OnboardingScreenGrantedPreview() {
    PillRemindTheme {
        OnboardingScreen(
            uiState = OnboardingUiState(
                notificationStatus = NotificationStatus.GRANTED,
                notificationAction = NotificationAction.NONE,
                exactAlarmStatus = ExactAlarmStatus.GRANTED,
                canContinue = true,
            ),
            onNotificationClick = {},
            onExactAlarmClick = {},
            onComplete = {},
            onCompleteWithoutExactAlarms = {},
        )
    }
}
