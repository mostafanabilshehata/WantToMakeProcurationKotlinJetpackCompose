package com.informatique.tawsekmisr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.informatique.tawsekmisr.common.dataStores.LanguageDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LocaleViewModel @Inject constructor(
    private val languageDataStore: LanguageDataStore
) : ViewModel() {

    private val _locale = MutableStateFlow(Locale.forLanguageTag(""))
    val locale: StateFlow<Locale> = _locale

    init {
        viewModelScope.launch {
            languageDataStore.languageFlow.collectLatest { langCode ->
                _locale.value = Locale.forLanguageTag(langCode)
            }
        }
    }

    fun changeLanguage(langCode: String) {
        viewModelScope.launch {
            languageDataStore.saveLanguage(langCode)
            _locale.value = Locale.forLanguageTag(langCode)
        }
    }
}

