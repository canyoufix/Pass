package com.canyoufix.pass

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupTest {
    @get:Rule(order = 0) val timeout: Timeout = Timeout.seconds(60)

    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchDisplaysSetupOrLogin() {
        val activity = composeRule.activity
        val setup = activity.getString(com.canyoufix.pass.R.string.setup_master_password)
        val login = activity.getString(com.canyoufix.pass.R.string.authorization)
        composeRule.onNode(hasText(setup) or hasText(login)).assertIsDisplayed()
    }
}
