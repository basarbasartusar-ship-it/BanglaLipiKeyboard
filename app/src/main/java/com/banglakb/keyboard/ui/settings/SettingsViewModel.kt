package com.banglakb.keyboard.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.banglakb.keyboard.db.AppDatabase
import com.banglakb.keyboard.db.WordEntity
import com.banglakb.keyboard.repository.DictionaryRepository
import com.banglakb.keyboard.util.AppTheme
import com.banglakb.keyboard.util.Prefs
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = Prefs(application)
    private val repository = DictionaryRepository(AppDatabase.getInstance(application).wordDao())

    val personalDictionary: LiveData<List<WordEntity>> = repository.personalDictionary().asLiveData()

    fun getTheme(): AppTheme = prefs.theme
    fun setTheme(theme: AppTheme) { prefs.theme = theme }

    fun isSoundEnabled() = prefs.soundEnabled
    fun setSoundEnabled(enabled: Boolean) { prefs.soundEnabled = enabled }

    fun isHapticEnabled() = prefs.hapticEnabled
    fun setHapticEnabled(enabled: Boolean) { prefs.hapticEnabled = enabled }

    fun isKeyPopupEnabled() = prefs.keyPopupEnabled
    fun setKeyPopupEnabled(enabled: Boolean) { prefs.keyPopupEnabled = enabled }

    fun isAutoCapitalizeEnabled() = prefs.autoCapitalize
    fun setAutoCapitalizeEnabled(enabled: Boolean) { prefs.autoCapitalize = enabled }

    fun isDoubleSpacePeriodEnabled() = prefs.doubleSpacePeriod
    fun setDoubleSpacePeriodEnabled(enabled: Boolean) { prefs.doubleSpacePeriod = enabled }

    fun isWordSuggestionsEnabled() = prefs.wordSuggestions
    fun setWordSuggestionsEnabled(enabled: Boolean) { prefs.wordSuggestions = enabled }

    fun getKeyboardHeightPercent() = prefs.keyboardHeightPercent
    fun setKeyboardHeightPercent(pct: Int) { prefs.keyboardHeightPercent = pct }

    fun isOneHandedMode() = prefs.oneHandedMode
    fun setOneHandedMode(enabled: Boolean) { prefs.oneHandedMode = enabled }

    fun addWord(word: String, language: String) = viewModelScope.launch {
        repository.addToPersonalDictionary(word, language)
    }

    fun removeWord(word: WordEntity) = viewModelScope.launch {
        repository.removeWord(word)
    }

    fun exportDictionary(onResult: (String) -> Unit) = viewModelScope.launch {
        onResult(repository.exportDictionary())
    }

    fun importDictionary(content: String, onResult: (Int) -> Unit) = viewModelScope.launch {
        onResult(repository.importDictionary(content))
    }
}
