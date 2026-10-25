package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BirthdayRepository
import com.example.data.SettingsManager
import com.example.data.ThemeMode
import com.example.data.UserSettings
import com.example.data.wishes.WishesRepository
import com.example.model.Birthday
import com.example.model.BirthdayWish
import com.example.model.WishCategory
import com.example.model.WishLanguage
import com.example.model.WishStyle
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class WishYaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = BirthdayRepository(db.birthdayDao(), db.wishDao())
    private val settingsManager = SettingsManager.getInstance(application)

    val settings: StateFlow<UserSettings> = settingsManager.settings

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // 1. Birthdays
    val allBirthdays: StateFlow<List<Birthday>> = repository.allBirthdays
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayBirthdays: StateFlow<List<Birthday>> = allBirthdays.map { list ->
        val today = LocalDate.now()
        list.filter { it.isToday(today) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingBirthdays: StateFlow<List<Birthday>> = allBirthdays.map { list ->
        val today = LocalDate.now()
        list.sortedBy { it.daysRemaining(today) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Favorites and Recently Viewed
    val favoriteWishIds: StateFlow<Set<Int>> = repository.favoriteWishIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val favoriteWishes: StateFlow<List<BirthdayWish>> = favoriteWishIds.map { idSet ->
        idSet.mapNotNull { WishesRepository.getWishById(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyViewedWishes: StateFlow<List<BirthdayWish>> = repository.recentlyViewedWishIds.map { ids ->
        ids.mapNotNull { WishesRepository.getWishById(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Search and Filtering
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<WishCategory?>(null)
    val selectedLanguage = MutableStateFlow<WishLanguage?>(null)
    val selectedStyle = MutableStateFlow<WishStyle?>(null)
    val recipientPersonalizeName = MutableStateFlow("")

    val filteredWishes: StateFlow<List<BirthdayWish>> = combine(
        searchQuery,
        selectedCategory,
        selectedLanguage,
        selectedStyle
    ) { query, cat, lang, sty ->
        WishesRepository.searchAndFilter(
            query = query,
            category = cat,
            language = lang,
            style = sty
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WishesRepository.allWishes
    )

    // Actions
    fun saveBirthday(
        id: Long = 0,
        name: String,
        relationship: String,
        day: Int,
        month: Int,
        year: Int?,
        notes: String,
        remindOnDay: Boolean,
        remindOneDayBefore: Boolean,
        remindHour: Int,
        remindMinute: Int
    ) {
        viewModelScope.launch {
            val birthday = Birthday(
                id = id,
                name = name.trim(),
                relationship = relationship.trim(),
                birthDay = day,
                birthMonth = month,
                birthYear = year,
                notes = notes.trim(),
                remindOnDay = remindOnDay,
                remindOneDayBefore = remindOneDayBefore,
                remindHour = remindHour,
                remindMinute = remindMinute
            )

            if (id == 0L) {
                val newId = repository.insertBirthday(birthday)
                val inserted = birthday.copy(id = newId)
                ReminderScheduler.scheduleBirthdayReminders(getApplication(), inserted)
                _userMessage.emit("Birthday for ${birthday.name} added!")
            } else {
                repository.updateBirthday(birthday)
                ReminderScheduler.scheduleBirthdayReminders(getApplication(), birthday)
                _userMessage.emit("Birthday for ${birthday.name} updated!")
            }
        }
    }

    fun deleteBirthday(birthday: Birthday) {
        viewModelScope.launch {
            repository.deleteBirthday(birthday)
            ReminderScheduler.cancelBirthdayReminders(getApplication(), birthday.id)
            _userMessage.emit("Deleted ${birthday.name}'s birthday")
        }
    }

    fun clearAllBirthdays() {
        viewModelScope.launch {
            val list = allBirthdays.value
            list.forEach { ReminderScheduler.cancelBirthdayReminders(getApplication(), it.id) }
            repository.deleteAllBirthdays()
            _userMessage.emit("All birthday records cleared")
        }
    }

    fun toggleFavorite(wishId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(wishId)
            val isNowFav = !favoriteWishIds.value.contains(wishId)
            _userMessage.emit(if (isNowFav) "Added to favorites ❤️" else "Removed from favorites")
        }
    }

    fun recordWishViewed(wishId: Int) {
        viewModelScope.launch {
            repository.recordRecentlyViewed(wishId)
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            repository.clearFavorites()
            _userMessage.emit("Favorites cleared")
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCategory(category: WishCategory?) {
        selectedCategory.value = category
    }

    fun setLanguage(language: WishLanguage?) {
        selectedLanguage.value = language
    }

    fun setStyle(style: WishStyle?) {
        selectedStyle.value = style
    }

    fun setRecipientName(name: String) {
        recipientPersonalizeName.value = name
    }

    fun updateDefaultReminderTime(hour: Int, minute: Int) {
        settingsManager.updateDefaultTime(hour, minute)
        viewModelScope.launch {
            _userMessage.emit("Default reminder time updated")
        }
    }

    fun updateDefaultOneDayBefore(enabled: Boolean) {
        settingsManager.updateOneDayBefore(enabled)
    }

    fun updateThemeMode(mode: ThemeMode) {
        settingsManager.updateThemeMode(mode)
    }

    fun emitMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }
}
