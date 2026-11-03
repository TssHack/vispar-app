package com.fazli.vispar.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazli.vispar.R
import com.fazli.vispar.account.AccountManager
import com.fazli.vispar.account.AccountState
import com.fazli.vispar.account.ApiException
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidButton
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.fazli.vispar.ui.glass.glass
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CODE_LENGTH = 5

internal enum class LoginStep { Phone, Code, Name }

/**
 * Shows [content] to signed-in users. Everyone else gets the sign-in flow in its place; once they
 * are in (and, the first time, have picked a name) the content appears right there, so the user
 * lands back on the title they were opening.
 */
@Composable
fun RequireAccount(onClose: () -> Unit, content: @Composable () -> Unit) {
    val state by AccountManager.state.collectAsState()
    val needsName by AccountManager.needsName.collectAsState()
    val ready = state is AccountState.SignedIn && !needsName
    AnimatedContent(
        targetState = ready,
        transitionSpec = { (fadeIn(tween(350)) + scaleIn(initialScale = 0.98f)) togetherWith fadeOut(tween(200)) },
        label = "gate"
    ) { signedIn ->
        if (signedIn) content() else LoginFlow(onClose = onClose)
    }
}

/** Full-screen, three-step glass sign-in: phone → SMS code → (first time) name. */
@Composable
fun LoginFlow(onClose: (() -> Unit)?, modifier: Modifier = Modifier) = LoginFlowContent(onClose, modifier)

@Composable
internal fun LoginFlowContent(onClose: (() -> Unit)?, modifier: Modifier = Modifier, forcedStep: LoginStep? = null, forcedPhone: String = "") {
    val colors = Glass.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by AccountManager.state.collectAsState()
    val needsName by AccountManager.needsName.collectAsState()

    var phone by rememberSaveable { mutableStateOf(forcedPhone) }
    var codeSent by rememberSaveable { mutableStateOf(false) }
    var resendIn by rememberSaveable { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }

    val step = forcedStep ?: when {
        state is AccountState.SignedIn && needsName -> LoginStep.Name
        codeSent -> LoginStep.Code
        else -> LoginStep.Phone
    }

    LaunchedEffect(resendIn) {
        if (resendIn > 0) {
            delay(1000)
            resendIn--
        }
    }

    fun messageFor(e: Throwable): String = context.getString(
        when ((e as? ApiException)?.code) {
            "bad_phone" -> R.string.err_bad_phone
            "cooldown", "too_many_requests" -> R.string.err_too_many
            "sms_failed", "unavailable" -> R.string.err_sms_failed
            "wrong_code" -> R.string.err_wrong_code
            "code_expired" -> R.string.err_code_expired
            "too_many_attempts" -> R.string.err_too_many_attempts
            "blocked" -> R.string.err_blocked
            "network" -> R.string.err_network
            else -> R.string.err_generic
        }
    )

    suspend fun shakeIt() {
        for (x in listOf(14f, -12f, 9f, -6f, 3f, 0f)) shake.animateTo(x, tween(55))
    }

    fun sendCode() {
        val normalized = normalizePhone(phone)
        if (normalized == null) {
            error = context.getString(R.string.err_bad_phone)
            scope.launch { shakeIt() }
            return
        }
        if (busy) return
        busy = true
        error = null
        scope.launch {
            try {
                resendIn = AccountManager.requestCode(normalized)
                phone = normalized
                codeSent = true
            } catch (e: Exception) {
                error = messageFor(e)
                shakeIt()
            }
            busy = false
        }
    }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .heightIn(min = screenHeight)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = Glass.ScreenPadding, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(24.dp))
            FloatingGlassLogo(size = 132.dp)
            Spacer(Modifier.height(18.dp))
            StepDots(step.ordinal, if (step == LoginStep.Name || needsName) 3 else 2)
            Spacer(Modifier.height(22.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val dir = if (forward) 1 else -1
                    (slideInHorizontally(spring(0.8f, 380f)) { it / 3 * dir } + fadeIn(tween(220))) togetherWith
                            (slideOutHorizontally(tween(220)) { -it / 3 * dir } + fadeOut(tween(160)))
                },
                modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
                label = "step"
            ) { s ->
                GlassSurface(
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationX = shake.value.dp.toPx() },
                    shape = RoundedRectangle(36.dp),
                    style = GlassStyle.Floating,
                    tint = colors.glassTintStrong,
                    highlight = Highlight.Default,
                    shadow = Shadow(radius = 36.dp, color = Color.Black.copy(alpha = if (colors.isDark) 0.45f else 0.12f)),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (s) {
                            LoginStep.Phone -> PhoneStep(
                                phone = phone,
                                onPhone = { phone = it; error = null },
                                busy = busy,
                                onSubmit = { sendCode() }
                            )
                            LoginStep.Code -> CodeStep(
                                phone = phone,
                                busy = busy,
                                resendIn = resendIn,
                                onChangeNumber = { codeSent = false; error = null },
                                onResend = { sendCode() },
                                onSubmit = { code, clear ->
                                    if (!busy) {
                                        busy = true
                                        error = null
                                        scope.launch {
                                            try {
                                                AccountManager.verifyCode(phone, code)
                                                codeSent = false
                                            } catch (e: Exception) {
                                                error = messageFor(e)
                                                clear()
                                                shakeIt()
                                            }
                                            busy = false
                                        }
                                    }
                                }
                            )
                            LoginStep.Name -> NameStep(
                                busy = busy,
                                onSubmit = { name ->
                                    if (!busy) {
                                        busy = true
                                        error = null
                                        scope.launch {
                                            try {
                                                AccountManager.updateName(name)
                                            } catch (e: Exception) {
                                                error = messageFor(e)
                                                shakeIt()
                                            }
                                            busy = false
                                        }
                                    }
                                },
                                onSkip = { AccountManager.skipName() }
                            )
                        }
                        AnimatedVisibility(visible = error != null) {
                            ErrorChip(error.orEmpty())
                        }
                    }
                }
            }

            if (step == LoginStep.Phone) {
                Spacer(Modifier.height(22.dp))
                Row(
                    Modifier.widthIn(max = 460.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Perk(Icons.Rounded.Favorite, stringResource(R.string.perk_sync), colors.heart, Modifier.weight(1f))
                    Perk(Icons.Rounded.Tv, stringResource(R.string.perk_tv), Color(0xFF5E5CE6), Modifier.weight(1f))
                    Perk(Icons.Rounded.Verified, stringResource(R.string.perk_free), Color(0xFF34C759), Modifier.weight(1f))
                }
            }
        }

        if (onClose != null && step != LoginStep.Name) {
            LiquidIconButton(
                onClick = onClose,
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.close),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(Glass.ScreenPadding)
                    .align(Alignment.TopStart)
            )
        }
    }
}

// ---- Steps ---------------------------------------------------------------------------------------

@Composable
private fun PhoneStep(phone: String, onPhone: (String) -> Unit, busy: Boolean, onSubmit: () -> Unit) {
    val colors = Glass.colors
    val focus = remember { FocusRequester() }
    StepTitle(stringResource(R.string.login_title), stringResource(R.string.login_subtitle_short))

    // +98 chip and the number, always left to right.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        GlassInput {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle.Clear, tint = colors.accent.copy(alpha = 0.16f), shadow = null)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("IR  +98", style = GlassType.Callout.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = colors.content)
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (phone.isEmpty()) Text("912 345 6789", style = GlassType.Title2, color = colors.contentTertiary, maxLines = 1)
                    BasicTextField(
                        value = phone,
                        onValueChange = { v -> onPhone(v.map { persianToAscii(it) }.filter { it.isDigit() || it == '+' || it == ' ' }.joinToString("").take(16)) },
                        singleLine = true,
                        textStyle = GlassType.Title2.copy(color = colors.content, letterSpacing = 1.sp),
                        cursorBrush = SolidColor(colors.accent),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { onSubmit() }, onDone = { onSubmit() }),
                        modifier = Modifier.fillMaxWidth().focusRequester(focus)
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    PrimaryGlassButton(stringResource(R.string.login_send_code), busy, enabled = phone.isNotBlank(), onClick = onSubmit)
    Text(stringResource(R.string.login_no_password), style = GlassType.Caption, color = colors.contentTertiary, textAlign = TextAlign.Center)
}

@Composable
private fun CodeStep(
    phone: String,
    busy: Boolean,
    resendIn: Int,
    onChangeNumber: () -> Unit,
    onResend: () -> Unit,
    onSubmit: (String, clear: () -> Unit) -> Unit,
) {
    val colors = Glass.colors
    var value by remember { mutableStateOf(TextFieldValue("")) }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var focused by remember { mutableStateOf(false) }

    StepTitle(stringResource(R.string.login_enter_code), null)
    // "Sent to 0912…" with a small edit chip.
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.login_sent_to), style = GlassType.Callout, color = colors.contentSecondary)
        LiquidButton(onClick = onChangeNumber, height = 34.dp, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(phone, style = GlassType.Callout, color = colors.content)
            }
            Icon(Icons.Rounded.Edit, stringResource(R.string.login_change_number), tint = colors.contentSecondary, modifier = Modifier.size(15.dp))
        }
    }

    // Five glass cells over an invisible field that does the actual typing (and SMS autofill).
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in 0 until CODE_LENGTH) {
                    OtpCell(
                        char = value.text.getOrNull(i),
                        active = focused && (i == value.text.length || (i == CODE_LENGTH - 1 && value.text.length == CODE_LENGTH)),
                    )
                }
            }
            BasicTextField(
                value = value,
                onValueChange = { v ->
                    val digits = v.text.map { persianToAscii(it) }.filter { it.isDigit() }.joinToString("").take(CODE_LENGTH)
                    value = TextFieldValue(digits, TextRange(digits.length))
                    if (digits.length == CODE_LENGTH) {
                        keyboard?.hide()
                        onSubmit(digits) { value = TextFieldValue("") ; runCatching { focus.requestFocus() } }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                textStyle = GlassType.Body.copy(color = Color.Transparent),
                cursorBrush = SolidColor(Color.Transparent),
                modifier = Modifier
                    .matchParentSizeCompat()
                    .alpha(0.01f)
                    .focusRequester(focus)
                    .onFocusChanged { focused = it.isFocused }
            )
        }
    }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    if (busy) {
        LiquidLoader(size = 40.dp)
    } else {
        PrimaryGlassButton(
            stringResource(R.string.login_verify),
            busy = false,
            enabled = value.text.length == CODE_LENGTH,
            onClick = { onSubmit(value.text) { value = TextFieldValue("") } }
        )
    }
    if (resendIn > 0) {
        Text(stringResource(R.string.login_resend_in, resendIn), style = GlassType.Callout, color = colors.contentTertiary)
    } else {
        LiquidButton(onClick = onResend, height = 40.dp, surfaceColor = Color.Transparent) {
            Text(stringResource(R.string.login_resend), style = GlassType.Callout, color = colors.content)
        }
    }
}

@Composable
private fun NameStep(busy: Boolean, onSubmit: (String) -> Unit, onSkip: () -> Unit) {
    val colors = Glass.colors
    var name by rememberSaveable { mutableStateOf("") }
    val valid = name.trim().length >= 2

    StepTitle(stringResource(R.string.name_title), stringResource(R.string.name_subtitle))
    // Live avatar preview.
    Box(
        Modifier
            .size(84.dp)
            .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle.Clear, tint = colors.accent.copy(alpha = 0.18f), shadow = null),
        contentAlignment = Alignment.Center
    ) {
        val initial = name.trim().firstOrNull()?.uppercase()
        if (initial != null) Text(initial, style = GlassType.LargeTitle.copy(fontSize = 36.sp), color = colors.content)
        else Icon(Icons.Rounded.Person, null, tint = colors.contentSecondary, modifier = Modifier.size(40.dp))
    }
    GlassTextField(
        value = name,
        onValueChange = { name = it.take(40) },
        placeholder = stringResource(R.string.name_placeholder),
        icon = Icons.Rounded.Person,
        keyboardType = KeyboardType.Text,
        onDone = { if (valid) onSubmit(name.trim()) },
    )
    PrimaryGlassButton(stringResource(R.string.name_continue), busy, enabled = valid) { onSubmit(name.trim()) }
    LiquidButton(onClick = onSkip, height = 38.dp, surfaceColor = Color.Transparent) {
        Text(stringResource(R.string.name_later), style = GlassType.Callout, color = colors.contentSecondary)
    }
}

// ---- Pieces --------------------------------------------------------------------------------------

/** The glass bolt, drifting gently as if it floated in liquid, with a soft glow under it. */
@Composable
fun FloatingGlassLogo(size: Dp, modifier: Modifier = Modifier) {
    val colors = Glass.colors
    val t = rememberInfiniteTransition(label = "logo")
    val y by t.animateFloat(-5f, 5f, infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "y")
    val tilt by t.animateFloat(-3f, 3f, infiniteRepeatable(tween(3400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "r")
    Box(modifier.size(size * 1.25f), contentAlignment = Alignment.Center) {
        // Glow.
        Box(
            Modifier
                .size(size)
                .clip(Capsule())
                .background(
                    Brush.radialGradient(
                        listOf(
                            (if (colors.isDark) Color.White else colors.accent).copy(alpha = if (colors.isDark) 0.16f else 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )
        Image(
            painter = painterResource(R.drawable.splash_logo),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationY = y.dp.toPx()
                    rotationZ = tilt
                    shadowElevation = 0f
                }
        )
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    val colors = Glass.colors
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until total) {
            val width by animateDpAsState(if (i == current) 26.dp else 8.dp, spring(0.7f, 400f), label = "dot")
            Box(
                Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(Capsule())
                    .background(if (i <= current) colors.content else colors.content.copy(alpha = 0.22f))
            )
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String?) {
    val colors = Glass.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = GlassType.Title, color = colors.content, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(subtitle, style = GlassType.Callout, color = colors.contentSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun OtpCell(char: Char?, active: Boolean) {
    val colors = Glass.colors
    val scale = remember { Animatable(1f) }
    LaunchedEffect(char) {
        if (char != null) {
            scale.snapTo(1.12f)
            scale.animateTo(1f, spring(0.45f, 500f))
        }
    }
    Box(
        Modifier
            .size(width = 52.dp, height = 62.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .glass(
                LocalBackdrop.current,
                RoundedRectangle(18.dp),
                colors,
                GlassStyle(blur = 8.dp, refractionHeight = 12.dp, refractionAmount = 22.dp),
                tint = when {
                    !colors.isDark -> Color.Black.copy(alpha = if (char != null) 0.07f else 0.04f)
                    char != null -> colors.content.copy(alpha = 0.10f)
                    else -> colors.glassTint
                },
                shadow = null
            )
            .border(
                if (active) 1.6.dp else 1.dp,
                colors.content.copy(alpha = if (active) 0.85f else if (colors.isDark) 0.10f else 0.20f),
                RoundedRectangle(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (char != null) {
            Text(char.toString(), style = GlassType.Title.copy(fontSize = 26.sp), color = colors.content)
        } else if (active) {
            val t = rememberInfiniteTransition(label = "caret")
            val a by t.animateFloat(1f, 0f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "a")
            Box(Modifier.size(width = 2.dp, height = 24.dp).alpha(a).background(colors.content))
        }
    }
}

/** A glass capsule holding an input. */
@Composable
private fun GlassInput(content: @Composable BoxScope.() -> Unit) {
    val colors = Glass.colors
    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .glass(
                LocalBackdrop.current, Capsule(), colors,
                GlassStyle(blur = 10.dp, refractionHeight = 14.dp, refractionAmount = 28.dp),
                tint = if (colors.isDark) colors.glassTint else Color.Black.copy(alpha = 0.04f),
                shadow = null
            )
            .border(1.dp, colors.content.copy(alpha = if (colors.isDark) 0.08f else 0.16f), Capsule())
            .padding(start = 10.dp, end = 18.dp),
        contentAlignment = Alignment.CenterStart,
        content = content
    )
}

/** Text field in a glass capsule (also used by the profile's edit-name dialog). */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    onDone: () -> Unit = {},
) {
    val colors = Glass.colors
    GlassInput {
        Row(Modifier.padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = colors.contentSecondary, modifier = Modifier.size(22.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(placeholder, style = GlassType.Headline, color = colors.contentTertiary, maxLines = 1)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = GlassType.Headline.copy(color = colors.content),
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PrimaryGlassButton(text: String, busy: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = Glass.colors
    LiquidButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        tint = colors.accent,
        height = 56.dp,
        enabled = enabled && !busy,
    ) {
        if (busy) {
            LiquidLoader(size = 28.dp)
        } else {
            Text(text, style = GlassType.Headline, color = colors.onAccent)
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = colors.onAccent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ErrorChip(text: String) {
    val colors = Glass.colors
    Row(
        Modifier
            .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle.Clear, tint = colors.danger.copy(alpha = 0.18f), shadow = null)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = colors.danger, modifier = Modifier.size(18.dp))
        Text(text, style = GlassType.Callout, color = colors.content)
    }
}

@Composable
private fun Perk(icon: ImageVector, text: String, tint: Color, modifier: Modifier = Modifier) {
    val colors = Glass.colors
    Column(
        modifier
            .glass(LocalBackdrop.current, RoundedRectangle(22.dp), colors, GlassStyle.Regular, shadow = null)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Text(text, style = GlassType.Caption, color = colors.contentSecondary, textAlign = TextAlign.Center, maxLines = 2)
    }
}

private fun Modifier.matchParentSizeCompat(): Modifier = this.fillMaxWidth().height(62.dp)

// ---- Helpers -------------------------------------------------------------------------------------

internal fun persianToAscii(c: Char): Char = when (c) {
    in '۰'..'۹' -> '0' + (c - '۰')
    in '٠'..'٩' -> '0' + (c - '٠')
    else -> c
}

/** Same rules as the server: +98…, 0098…, 98…, 9…, Persian digits → 09XXXXXXXXX, or null. */
fun normalizePhone(input: String): String? {
    var d = input.map { persianToAscii(it) }.filter { it.isDigit() }.joinToString("")
    when {
        d.startsWith("0098") -> d = "0" + d.drop(4)
        d.startsWith("98") && d.length == 12 -> d = "0" + d.drop(2)
        d.startsWith("9") && d.length == 10 -> d = "0$d"
    }
    return d.takeIf { it.length == 11 && it.startsWith("09") }
}
