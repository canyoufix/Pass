package com.canyoufix.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.canyoufix.domain.model.*
import com.canyoufix.pass.R
import com.canyoufix.ui.screens.*

private enum class Destination(val route: String, val title: Int, val icon: Int) {
    VAULT("vault", R.string.nav_vault, R.drawable.ic_storage),
    GENERATOR("generator", R.string.nav_generator, R.drawable.ic_generator),
    SETTINGS("settings", R.string.nav_settings, R.drawable.ic_settings),
}

@Composable
internal fun MainNavigation(onLock: () -> Unit) {
    val controller = rememberNavController()
    val closeVault: () -> Unit = {
        Destination.entries.forEach { controller.clearBackStack(it.route) }
        controller.popBackStack("vault", inclusive = true)
        onLock()
    }
    val backStack by controller.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val category = route == "category/{kind}"
    val openEditor: (EntryKind, String?) -> Unit = { kind, id ->
        controller.navigate("edit/" + kind.name + if (id == null) "" else "?id=" + Uri.encode(id))
    }
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (Destination.entries.any { it.route == route } || category) {
                Box(
                    Modifier.navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp),
                ) {
                    Surface(
                        Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            modifier = Modifier.height(64.dp),
                        ) {
                            Destination.entries.forEach { destination ->
                                NavigationBarItem(
                                    selected =
                                        route == destination.route ||
                                            (category && destination == Destination.VAULT),
                                    onClick = {
                                        controller.navigate(destination.route) {
                                            popUpTo("vault") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = destination != Destination.VAULT
                                        }
                                    },
                                    colors =
                                        NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor =
                                                MaterialTheme.colorScheme.primary.copy(
                                                    alpha = 0.10f,
                                                ),
                                            unselectedIconColor =
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor =
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                        ),
                                    label = {
                                        Text(
                                            stringResource(destination.title),
                                            fontSize = 10.sp,
                                            lineHeight = 12.sp,
                                            maxLines = 1,
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            painterResource(destination.icon),
                                            null,
                                            Modifier.size(22.dp),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            controller,
            startDestination = "vault",
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable("vault") {
                StorageRoute(
                    onCategory = { controller.navigate("category/" + it.name) },
                    onAdd = { openEditor(it, null) },
                    onFiles = { controller.navigate("files") },
                )
            }
            composable(
                "category/{kind}",
                arguments = listOf(navArgument("kind") { type = NavType.StringType }),
            ) {
                VaultRoute(
                    onBack = { controller.popBackStack() },
                    onOpen = { kind, entry -> openEditor(kind, entry.id) },
                    onAdd = { openEditor(it, null) },
                )
            }
            composable("generator") { GeneratorRoute() }
            composable("settings") {
                SettingsRoute(
                    onSecurity = { controller.navigate("security") },
                    onSync = { controller.navigate("sync") },
                    onTransfer = { controller.navigate("transfer") },
                    onAppearance = { controller.navigate("appearance") },
                )
            }
            composable("appearance") { AppearanceRoute(onBack = { controller.popBackStack() }) }
            composable("security") {
                SecurityRoute(onBack = { controller.popBackStack() }, onLock = closeVault)
            }
            composable("files") { FilesRoute(onBack = { controller.popBackStack() }) }
            composable("sync") { SyncSettingsRoute(onBack = { controller.popBackStack() }) }
            composable("transfer") { TransferRoute(onBack = { controller.popBackStack() }) }
            composable(
                "edit/{kind}?id={id}",
                arguments =
                    listOf(
                        navArgument("kind") { type = NavType.StringType },
                        navArgument("id") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
            ) {
                EditorRoute(onBack = { controller.popBackStack() })
            }
        }
    }
}
