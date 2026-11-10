package com.fazli.vispar.screens

import android.icu.text.DateFormat
import android.icu.util.ULocale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fazli.vispar.R
import com.fazli.vispar.account.AccountManager
import com.fazli.vispar.account.AccountState
import com.fazli.vispar.account.ApiException
import com.fazli.vispar.account.Device
import com.fazli.vispar.account.Profile
import com.fazli.vispar.account.SyncManager
import com.fazli.vispar.account.SyncStatus
import com.fazli.vispar.ui.glass.DialogAction
import com.fazli.vispar.ui.glass.DialogActionStyle
import com.fazli.vispar.ui.glass.DialogHeader
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassAlertDialog
import com.fazli.vispar.ui.glass.GlassDialog
import com.fazli.vispar.ui.glass.GlassDivider
import com.fazli.vispar.ui.glass.GlassRow
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidButton
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.fazli.vispar.ui.glass.glass
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

/** The blue of the verification tick (same on light and dark). */
val VerifiedBlue = Color(0xFF1D9BF0)

@Composable
fun AccountScreen(navController: NavController?) {
    val state by AccountManager.state.collectAsState()
    val needsName by AccountManager.needsName.collectAsState()
    val profile = (state as? AccountState.SignedIn)?.profile
    AnimatedContent(
        targetState = profile != null && !needsName,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "account"
    ) { ready ->
        if (!ready || profile == null) {
            LoginFlow(onClose = { navController?.popBackStack() })
        } else {
            GlassScaffold(
                title = stringResource(R.string.account),
                onBack = { navController?.popBackStack() }
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Glass.ScreenPadding)
                        .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        ProfileContent(profile)
                    }
                }
            }
        }
    }
}

// ---- Profile -------------------------------------------------------------------------------------

@Composable
private fun ProfileContent(profile: Profile) {
    val colors = Glass.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncStatus by SyncManager.status.collectAsState()
    var devices by remember { mutableStateOf<List<Device>?>(null) }
    var editName by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var revoke by remember { mutableStateOf<Device?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AccountManager.reloadProfile()
        devices = runCatching { AccountManager.devices() }.getOrElse { emptyList() }
        SyncManager.schedule(context)
    }

    // Header: avatar, name with the blue tick, phone, member since.
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Avatar(profile)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    profile.displayName.ifBlank { stringResource(R.string.profile_no_name) },
                    style = GlassType.Title,
                    color = if (profile.displayName.isBlank()) colors.contentSecondary else colors.content
                )
                if (profile.verified) {
                    Icon(Icons.Rounded.Verified, stringResource(R.string.verified), tint = VerifiedBlue, modifier = Modifier.size(24.dp))
                }
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(profile.phone, style = GlassType.Callout, color = colors.contentSecondary)
            }
            memberSince(profile.createdAt)?.let {
                Text(stringResource(R.string.member_since, it), style = GlassType.Caption, color = colors.contentTertiary)
            }
            if (profile.verified) {
                Row(
                    Modifier
                        .padding(top = 6.dp)
                        .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle.Clear, tint = VerifiedBlue.copy(alpha = 0.18f), shadow = null)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Verified, null, tint = VerifiedBlue, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.verified_account), style = GlassType.Caption, color = colors.content)
                }
            }
            Spacer(Modifier.height(6.dp))
            LiquidButton(onClick = { editName = true }, height = 42.dp) {
                Icon(Icons.Rounded.Edit, null, tint = colors.content, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.edit_name), style = GlassType.Headline, color = colors.content)
            }
        }
    }

    // Sync.
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        GlassRow(
            icon = Icons.Rounded.CloudSync,
            title = stringResource(R.string.sync_title),
            subtitle = when (val s = syncStatus) {
                SyncStatus.Syncing -> stringResource(R.string.sync_running)
                is SyncStatus.Failed -> stringResource(R.string.sync_failed)
                is SyncStatus.Done -> stringResource(R.string.sync_last, relativeTime(s.atMs))
                SyncStatus.Idle -> SyncManager.lastSyncMs(context).takeIf { it > 0 }
                    ?.let { stringResource(R.string.sync_last, relativeTime(it)) } ?: stringResource(R.string.sync_msg)
            },
            iconTint = Color(0xFF0A84FF),
            onClick = { scope.launch { SyncManager.syncNow(context) } },
            trailing = {
                if (syncStatus == SyncStatus.Syncing) LiquidLoader(size = 26.dp)
                else Icon(Icons.Rounded.Sync, null, tint = colors.contentSecondary, modifier = Modifier.size(22.dp))
            }
        )
    }

    // Devices.
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        Column(Modifier.padding(vertical = 6.dp)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Devices, null, tint = colors.contentSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.devices), style = GlassType.Headline, color = colors.content)
            }
            val list = devices
            when {
                list == null -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { LiquidLoader(size = 36.dp) }
                list.isEmpty() -> Text(stringResource(R.string.devices_none), style = GlassType.Callout, color = colors.contentSecondary, modifier = Modifier.padding(16.dp))
                else -> list.forEachIndexed { i, d ->
                    if (i > 0) GlassDivider()
                    GlassRow(
                        icon = when (d.kind) { "tv" -> Icons.Rounded.Tv; "tablet" -> Icons.Rounded.Tablet; else -> Icons.Rounded.PhoneAndroid },
                        title = d.name.ifBlank { stringResource(R.string.device_unknown) },
                        subtitle = if (d.current) stringResource(R.string.device_this) else
                            stringResource(R.string.device_last_seen, relativeTime(SyncManager.parseIso(d.lastSeenAt) ?: 0)),
                        iconTint = if (d.current) Color(0xFF34C759) else Color(0xFF8E8E93),
                        trailing = if (d.current) null else {
                            {
                                LiquidIconButton(
                                    onClick = { revoke = d },
                                    icon = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.device_sign_out),
                                    size = 36.dp,
                                    iconSize = 18.dp
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    // Sign out / delete.
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        Column(Modifier.padding(vertical = 6.dp)) {
            GlassRow(
                icon = Icons.AutoMirrored.Rounded.Logout,
                title = stringResource(R.string.sign_out),
                subtitle = stringResource(R.string.sign_out_msg),
                iconTint = Color(0xFFFF9F0A),
                onClick = { confirmSignOut = true },
                trailing = null
            )
            GlassDivider()
            GlassRow(
                icon = Icons.Rounded.DeleteForever,
                title = stringResource(R.string.delete_account),
                subtitle = stringResource(R.string.delete_account_msg),
                iconTint = colors.danger,
                onClick = { confirmDelete = true },
                trailing = null
            )
        }
    }
    actionError?.let { Text(it, style = GlassType.Callout, color = colors.danger, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }

    if (editName) {
        var name by remember { mutableStateOf(profile.displayName) }
        var saving by remember { mutableStateOf(false) }
        GlassDialog(onDismissRequest = { editName = false }) {
            DialogHeader(stringResource(R.string.edit_name), stringResource(R.string.edit_name_msg), Icons.Rounded.Person)
            GlassTextField(value = name, onValueChange = { name = it.take(40) }, placeholder = stringResource(R.string.name_placeholder), icon = Icons.Rounded.Edit, keyboardType = KeyboardType.Text)
            DialogAction(stringResource(R.string.save), onClick = {
                if (!saving) {
                    saving = true
                    scope.launch {
                        runCatching { AccountManager.updateName(name) }
                            .onFailure { actionError = context.getString(R.string.err_generic) }
                        saving = false
                        editName = false
                    }
                }
            }, style = DialogActionStyle.Primary)
            DialogAction(stringResource(R.string.cancel), onClick = { editName = false }, style = DialogActionStyle.Plain)
        }
    }
    revoke?.let { d ->
        GlassAlertDialog(
            title = stringResource(R.string.device_sign_out),
            message = stringResource(R.string.device_sign_out_msg, d.name),
            confirmText = stringResource(R.string.device_sign_out),
            onConfirm = {
                revoke = null
                scope.launch {
                    runCatching { AccountManager.revokeDevice(d.id) }
                        .onSuccess { devices = devices?.filterNot { it.id == d.id } }
                        .onFailure { actionError = context.getString(R.string.err_generic) }
                }
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { revoke = null },
            icon = Icons.Rounded.Devices,
            destructive = true
        )
    }
    if (confirmSignOut) {
        GlassAlertDialog(
            title = stringResource(R.string.sign_out),
            message = stringResource(R.string.sign_out_confirm),
            confirmText = stringResource(R.string.sign_out),
            onConfirm = {
                confirmSignOut = false
                scope.launch {
                    SyncManager.syncNow(context)
                    AccountManager.signOut()
                }
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { confirmSignOut = false },
            icon = Icons.AutoMirrored.Rounded.Logout,
        )
    }
    if (confirmDelete) {
        GlassAlertDialog(
            title = stringResource(R.string.delete_account),
            message = stringResource(R.string.delete_account_confirm),
            confirmText = stringResource(R.string.delete_account),
            onConfirm = {
                confirmDelete = false
                scope.launch {
                    runCatching { AccountManager.deleteAccount() }
                        .onFailure { actionError = context.getString(R.string.err_generic) }
                }
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { confirmDelete = false },
            icon = Icons.Rounded.DeleteForever,
            destructive = true
        )
    }
}

// ---- Pieces --------------------------------------------------------------------------------------

@Composable
fun Avatar(profile: Profile?, size: androidx.compose.ui.unit.Dp = 88.dp) {
    val colors = Glass.colors
    Box(
        Modifier
            .size(size)
            .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle.Clear, tint = colors.accent.copy(alpha = 0.20f), shadow = null),
        contentAlignment = Alignment.Center
    ) {
        val initial = profile?.displayName?.trim()?.firstOrNull()?.toString()
        if (initial != null) {
            Text(initial.uppercase(), style = GlassType.LargeTitle.copy(fontSize = (size.value * 0.42f).sp), color = colors.content)
        } else {
            Icon(Icons.Rounded.Person, null, tint = colors.contentSecondary, modifier = Modifier.size(size * 0.5f))
        }
    }
}





// ---- Helpers -------------------------------------------------------------------------------------



@Composable
private fun memberSince(iso: String): String? {
    val ms = SyncManager.parseIso(iso) ?: return null
    val locale = LocalConfiguration.current.locales[0]
    val uLocale = if (locale.language == "fa") ULocale("fa_IR@calendar=persian") else ULocale.forLocale(locale)
    return runCatching { DateFormat.getDateInstance(DateFormat.LONG, uLocale).format(Date(ms)) }.getOrNull()
}

@Composable
private fun relativeTime(ms: Long): String {
    val diff = (System.currentTimeMillis() - ms).coerceAtLeast(0)
    val minutes = diff / 60_000
    return when {
        ms <= 0 -> "—"
        minutes < 1 -> stringResource(R.string.time_just_now)
        minutes < 60 -> stringResource(R.string.time_minutes_ago, minutes.toInt())
        minutes < 60 * 24 -> stringResource(R.string.time_hours_ago, (minutes / 60).toInt())
        else -> stringResource(R.string.time_days_ago, (minutes / 60 / 24).toInt())
    }
}
