package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Birthday
import com.example.model.WishCategory
import com.example.ui.WishYaViewModel
import com.example.ui.components.AddEditBirthdayDialog
import com.example.ui.components.PersonalizeNameDialog
import com.example.ui.screens.BirthdaysScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WishesScreen
import com.example.ui.theme.WishPinkSecondary
import com.example.ui.theme.WishPurplePrimary
import com.example.ui.theme.WishYaTheme

enum class ScreenTab(val route: String, val title: String) {
    HOME("home", "Home"),
    BIRTHDAYS("birthdays", "Birthdays"),
    WISHES("wishes", "Wishes"),
    FAVORITES("favorites", "Favorites"),
    SETTINGS("settings", "Settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: WishYaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if intent launched with extra from notification
        handleNotificationIntent(intent)

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            WishYaTheme(themeMode = settings.themeMode) {
                WishYaMainContent(
                    viewModel = viewModel,
                    onRequestNotificationPermission = { launcher ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                launcher(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val target = intent?.getStringExtra("EXTRA_NAVIGATE_TO")
        val personName = intent?.getStringExtra("EXTRA_PERSON_NAME")

        if (!personName.isNullOrEmpty()) {
            viewModel.setRecipientName(personName)
        }
    }
}

@Composable
fun WishYaMainContent(
    viewModel: WishYaViewModel,
    onRequestNotificationPermission: (launcher: (String) -> Unit) -> Unit
) {
    var currentTab by remember { mutableStateOf(ScreenTab.HOME) }
    var previousTab by remember { mutableStateOf(ScreenTab.HOME) }

    // Dialog states
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingBirthday by remember { mutableStateOf<Birthday?>(null) }
    var showPersonalizeDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission once at startup
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result handled */ }

    LaunchedEffect(Unit) {
        onRequestNotificationPermission { perm -> permissionLauncher.launch(perm) }
    }

    // Collect snackbar messages
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // State observation
    val allBirthdays by viewModel.allBirthdays.collectAsStateWithLifecycle()
    val todayBirthdays by viewModel.todayBirthdays.collectAsStateWithLifecycle()
    val upcomingBirthdays by viewModel.upcomingBirthdays.collectAsStateWithLifecycle()

    val filteredWishes by viewModel.filteredWishes.collectAsStateWithLifecycle()
    val favoriteWishIds by viewModel.favoriteWishIds.collectAsStateWithLifecycle()
    val favoriteWishes by viewModel.favoriteWishes.collectAsStateWithLifecycle()
    val recentlyViewedWishes by viewModel.recentlyViewedWishes.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val recipientName by viewModel.recipientPersonalizeName.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Back handler for sub-screens
    BackHandler(enabled = currentTab != ScreenTab.HOME) {
        currentTab = previousTab.takeIf { it != currentTab } ?: ScreenTab.HOME
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Home
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HOME,
                    onClick = {
                        previousTab = currentTab
                        currentTab = ScreenTab.HOME
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WishPurplePrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // Birthdays
                NavigationBarItem(
                    selected = currentTab == ScreenTab.BIRTHDAYS,
                    onClick = {
                        previousTab = currentTab
                        currentTab = ScreenTab.BIRTHDAYS
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (todayBirthdays.isNotEmpty()) {
                                    Badge(containerColor = WishPinkSecondary) {
                                        Text("${todayBirthdays.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Cake, contentDescription = "Birthdays")
                        }
                    },
                    label = { Text("Birthdays") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WishPurplePrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_birthdays")
                )

                // Wishes
                NavigationBarItem(
                    selected = currentTab == ScreenTab.WISHES,
                    onClick = {
                        previousTab = currentTab
                        currentTab = ScreenTab.WISHES
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Wishes") },
                    label = { Text("Wishes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WishPurplePrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_wishes")
                )

                // Favorites
                NavigationBarItem(
                    selected = currentTab == ScreenTab.FAVORITES,
                    onClick = {
                        previousTab = currentTab
                        currentTab = ScreenTab.FAVORITES
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (favoriteWishIds.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("${favoriteWishIds.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Favorites")
                        }
                    },
                    label = { Text("Favorites") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WishPinkSecondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_favorites")
                )

                // Settings
                NavigationBarItem(
                    selected = currentTab == ScreenTab.SETTINGS,
                    onClick = {
                        previousTab = currentTab
                        currentTab = ScreenTab.SETTINGS
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WishPurplePrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            ScreenTab.HOME -> {
                HomeScreen(
                    todayBirthdays = todayBirthdays,
                    upcomingBirthdays = upcomingBirthdays,
                    favoriteCount = favoriteWishIds.size,
                    recentlyViewedWishes = recentlyViewedWishes,
                    onAddBirthdayClick = {
                        editingBirthday = null
                        showAddEditDialog = true
                    },
                    onCategoryClick = { category ->
                        viewModel.setCategory(category)
                        previousTab = ScreenTab.HOME
                        currentTab = ScreenTab.WISHES
                    },
                    onSearchClick = { query ->
                        viewModel.setSearchQuery(query)
                        previousTab = ScreenTab.HOME
                        currentTab = ScreenTab.WISHES
                    },
                    onFavoritesClick = {
                        previousTab = ScreenTab.HOME
                        currentTab = ScreenTab.FAVORITES
                    },
                    onWishClick = { wish ->
                        viewModel.recordWishViewed(wish.id)
                        viewModel.setCategory(wish.category)
                        previousTab = ScreenTab.HOME
                        currentTab = ScreenTab.WISHES
                    },
                    onBirthdayWishClick = { bday ->
                        // Map relationship to category
                        val category = when (bday.relationship.lowercase()) {
                            "friend", "best friend" -> WishCategory.FRIENDS
                            "love", "girlfriend", "boyfriend" -> WishCategory.LOVE
                            "wife", "husband" -> WishCategory.SPOUSE
                            "mother", "mom", "father", "dad" -> WishCategory.PARENTS
                            "brother", "sister" -> WishCategory.SIBLINGS
                            "boss", "colleague", "teacher" -> WishCategory.PROFESSIONAL
                            "son", "daughter" -> WishCategory.KIDS
                            else -> WishCategory.ISLAMIC_GENERAL
                        }
                        viewModel.setCategory(category)
                        previousTab = ScreenTab.HOME
                        currentTab = ScreenTab.WISHES
                    },
                    onBirthdayClick = { bday ->
                        editingBirthday = bday
                        showAddEditDialog = true
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            ScreenTab.BIRTHDAYS -> {
                BirthdaysScreen(
                    birthdays = allBirthdays,
                    onAddBirthdayClick = {
                        editingBirthday = null
                        showAddEditDialog = true
                    },
                    onEditBirthday = { bday ->
                        editingBirthday = bday
                        showAddEditDialog = true
                    },
                    onDeleteBirthday = { bday ->
                        viewModel.deleteBirthday(bday)
                    },
                    onWishPerson = { bday ->
                        val category = when (bday.relationship.lowercase()) {
                            "friend", "best friend" -> WishCategory.FRIENDS
                            "love", "girlfriend", "boyfriend" -> WishCategory.LOVE
                            "wife", "husband" -> WishCategory.SPOUSE
                            "mother", "mom", "father", "dad" -> WishCategory.PARENTS
                            "brother", "sister" -> WishCategory.SIBLINGS
                            "boss", "colleague", "teacher" -> WishCategory.PROFESSIONAL
                            "son", "daughter" -> WishCategory.KIDS
                            else -> WishCategory.ISLAMIC_GENERAL
                        }
                        viewModel.setCategory(category)
                        previousTab = ScreenTab.BIRTHDAYS
                        currentTab = ScreenTab.WISHES
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            ScreenTab.WISHES -> {
                WishesScreen(
                    wishes = filteredWishes,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    selectedLanguage = selectedLanguage,
                    selectedStyle = selectedStyle,
                    recipientName = recipientName,
                    favoriteWishIds = favoriteWishIds,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onCategoryChange = { viewModel.setCategory(it) },
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onStyleChange = { viewModel.setStyle(it) },
                    onOpenPersonalize = { showPersonalizeDialog = true },
                    onClearPersonalize = { viewModel.setRecipientName("") },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onWishViewed = { viewModel.recordWishViewed(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            ScreenTab.FAVORITES -> {
                FavoritesScreen(
                    favoriteWishes = favoriteWishes,
                    recipientName = "",
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onClearFavorites = { viewModel.clearFavorites() },
                    onOpenPersonalize = { showPersonalizeDialog = true },
                    onWishViewed = { viewModel.recordWishViewed(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            ScreenTab.SETTINGS -> {
                SettingsScreen(
                    settings = settings,
                    totalBirthdaysCount = allBirthdays.size,
                    onUpdateTime = { h, m -> viewModel.updateDefaultReminderTime(h, m) },
                    onUpdateOneDayBefore = { viewModel.updateDefaultOneDayBefore(it) },
                    onUpdateTheme = { viewModel.updateThemeMode(it) },
                    onClearAllBirthdays = { viewModel.clearAllBirthdays() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Add / Edit Birthday Dialog
    if (showAddEditDialog) {
        AddEditBirthdayDialog(
            birthdayToEdit = editingBirthday,
            defaultHour = settings.defaultHour,
            defaultMinute = settings.defaultMinute,
            defaultOneDayBefore = settings.defaultOneDayBefore,
            onDismiss = {
                showAddEditDialog = false
                editingBirthday = null
            },
            onSave = { name, relationship, day, month, year, notes, remindOnDay, remindOneDayBefore, hour, minute ->
                viewModel.saveBirthday(
                    id = editingBirthday?.id ?: 0L,
                    name = name,
                    relationship = relationship,
                    day = day,
                    month = month,
                    year = year,
                    notes = notes,
                    remindOnDay = remindOnDay,
                    remindOneDayBefore = remindOneDayBefore,
                    remindHour = hour,
                    remindMinute = minute
                )
                showAddEditDialog = false
                editingBirthday = null
            }
        )
    }

    // Personalize Name Dialog
    if (showPersonalizeDialog) {
        PersonalizeNameDialog(
            currentName = recipientName,
            savedBirthdays = allBirthdays,
            onDismiss = { showPersonalizeDialog = false },
            onSaveName = { name ->
                viewModel.setRecipientName(name)
                showPersonalizeDialog = false
            }
        )
    }
}
