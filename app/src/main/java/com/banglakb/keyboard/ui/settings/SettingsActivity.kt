package com.banglakb.keyboard.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.banglakb.keyboard.R
import com.banglakb.keyboard.databinding.ActivitySettingsBinding
import com.banglakb.keyboard.util.AppTheme

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupSetupCard()
        setupThemeToggle()
        setupSwitches()
        setupHeightSlider()
        setupDictionarySection()
    }

    private fun setupSetupCard() {
        binding.btnEnableKeyboard.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        binding.btnSwitchKeyboard.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }
    }

    private fun setupThemeToggle() {
        val checkedId = when (viewModel.getTheme()) {
            AppTheme.LIGHT -> R.id.btnThemeLight
            AppTheme.DARK -> R.id.btnThemeDark
            AppTheme.AMOLED -> R.id.btnThemeAmoled
        }
        binding.themeToggleGroup.check(checkedId)
        binding.themeToggleGroup.addOnButtonCheckedListener { _, id, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            viewModel.setTheme(
                when (id) {
                    R.id.btnThemeDark -> AppTheme.DARK
                    R.id.btnThemeAmoled -> AppTheme.AMOLED
                    else -> AppTheme.LIGHT
                }
            )
        }
    }

    private fun setupSwitches() = with(binding) {
        switchAutoCap.isChecked = viewModel.isAutoCapitalizeEnabled()
        switchAutoCap.setOnCheckedChangeListener { _, checked -> viewModel.setAutoCapitalizeEnabled(checked) }

        switchDoubleSpace.isChecked = viewModel.isDoubleSpacePeriodEnabled()
        switchDoubleSpace.setOnCheckedChangeListener { _, checked -> viewModel.setDoubleSpacePeriodEnabled(checked) }

        switchSuggestions.isChecked = viewModel.isWordSuggestionsEnabled()
        switchSuggestions.setOnCheckedChangeListener { _, checked -> viewModel.setWordSuggestionsEnabled(checked) }

        switchSound.isChecked = viewModel.isSoundEnabled()
        switchSound.setOnCheckedChangeListener { _, checked -> viewModel.setSoundEnabled(checked) }

        switchHaptic.isChecked = viewModel.isHapticEnabled()
        switchHaptic.setOnCheckedChangeListener { _, checked -> viewModel.setHapticEnabled(checked) }

        switchPopup.isChecked = viewModel.isKeyPopupEnabled()
        switchPopup.setOnCheckedChangeListener { _, checked -> viewModel.setKeyPopupEnabled(checked) }

        switchOneHanded.isChecked = viewModel.isOneHandedMode()
        switchOneHanded.setOnCheckedChangeListener { _, checked -> viewModel.setOneHandedMode(checked) }
    }

    private fun setupHeightSlider() {
        binding.sliderHeight.value = viewModel.getKeyboardHeightPercent().toFloat()
        binding.sliderHeight.addOnChangeListener { _, value, _ ->
            viewModel.setKeyboardHeightPercent(value.toInt())
        }
    }

    private fun setupDictionarySection() {
        viewModel.personalDictionary.observe(this) { words ->
            binding.txtDictionaryCount.text =
                getString(R.string.dictionary_count_format, words.size)
        }
        binding.btnExportDictionary.setOnClickListener {
            viewModel.exportDictionary { content ->
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content)
                }
                startActivity(Intent.createChooser(sendIntent, getString(R.string.export_dictionary)))
            }
        }
    }
}
