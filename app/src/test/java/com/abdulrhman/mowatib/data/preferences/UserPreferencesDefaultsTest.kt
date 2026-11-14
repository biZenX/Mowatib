package com.abdulrhman.mowatib.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesDefaultsTest {
    @Test
    fun defaultForegroundNotificationStatusModeKeepsNotificationSimple() {
        assertEquals(
            ForegroundNotificationStatusMode.DEFAULT,
            UserPreferences().foregroundNotificationStatusMode
        )
    }

    @Test
    fun defaultAppLanguageIsArabic() {
        assertEquals("ar", UserPreferences().appLanguage)
    }
}
