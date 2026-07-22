package com.banglakb.keyboard

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matchers.ViewMatchers.isDisplayed
import androidx.test.espresso.matchers.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.banglakb.keyboard.ui.settings.SettingsActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsActivityTest {

    @Test
    fun enableKeyboardButton_isDisplayedOnLaunch() {
        ActivityScenario.launch(SettingsActivity::class.java).use {
            onView(withId(R.id.btnEnableKeyboard)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun themeToggleGroup_isDisplayedOnLaunch() {
        ActivityScenario.launch(SettingsActivity::class.java).use {
            onView(withId(R.id.themeToggleGroup)).check(matches(isDisplayed()))
        }
    }
}
