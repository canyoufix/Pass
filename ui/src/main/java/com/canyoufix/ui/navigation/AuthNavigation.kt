package com.canyoufix.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.canyoufix.domain.repository.*
import com.canyoufix.ui.R
import com.canyoufix.ui.components.*
import com.canyoufix.ui.presentation.AuthViewModel
import com.canyoufix.ui.screens.AuthScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthNavigation(
    viewModel: AuthViewModel = koinViewModel(),
    onAuthenticated: (() -> Unit)? = null,
) {
    val session by viewModel.sessionState.collectAsStateWithLifecycle()
    LaunchedEffect(session) { if (session == SessionState.Unlocked) onAuthenticated?.invoke() }
    val initializationFailed by viewModel.initializationFailed.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val dark =
        when (theme) {
            AppTheme.SYSTEM -> isSystemInDarkTheme()
            AppTheme.LIGHT -> false
            AppTheme.DARK -> true
        }
    val context = LocalContext.current
    val view = LocalView.current
    SideEffect {
        context.findActivity()?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(colorScheme = glassColors(dark), shapes = GlassShapes) {
        GlassBackdrop {
            when (session) {
                SessionState.Loading ->
                    ScreenFrame("TwicePass", status, viewModel::dismissMessage) { padding ->
                        Box(
                            Modifier.fillMaxSize().padding(padding),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!initializationFailed) CircularProgressIndicator()
                            else
                                Button(onClick = viewModel::initialize) {
                                    Text(stringResource(R.string.action_retry))
                                }
                        }
                    }
                SessionState.SetupRequired,
                SessionState.Locked ->
                    AuthScreen(
                        setup = session == SessionState.SetupRequired,
                        form = form,
                        status = status,
                        onEdit = viewModel::edit,
                        onSubmit = viewModel::submit,
                        onReset = viewModel::reset,
                        onDismissMessage = viewModel::dismissMessage,
                    )
                SessionState.Unlocked ->
                    if (onAuthenticated == null) MainNavigation(onLock = viewModel::lock)
                    else
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
