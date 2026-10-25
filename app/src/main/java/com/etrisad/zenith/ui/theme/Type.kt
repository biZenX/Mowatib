package com.etrisad.zenith.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font as GoogleFontSource
import com.etrisad.zenith.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val nunitoName = GoogleFont("Nunito")
val NunitoFontFamily = FontFamily(
    GoogleFontSource(googleFont = nunitoName, fontProvider = provider, weight = FontWeight.Normal),
    GoogleFontSource(googleFont = nunitoName, fontProvider = provider, weight = FontWeight.Medium),
    GoogleFontSource(googleFont = nunitoName, fontProvider = provider, weight = FontWeight.SemiBold),
    GoogleFontSource(googleFont = nunitoName, fontProvider = provider, weight = FontWeight.Bold)
)

private fun createBasicTypography(fontFamily: FontFamily): Typography {
    return Typography(
        displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
        displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp),
        displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp),
        headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
        headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
        headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
        titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
        titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
        titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
        bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
        bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
        labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
    )
}

val ThmanyahFontFamily = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.thmanyah_sans_regular, FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.thmanyah_sans_medium, FontWeight.Medium),
    androidx.compose.ui.text.font.Font(R.font.thmanyah_sans_bold, FontWeight.Bold)
)

val IbmPlexSansArabicFontFamily = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.ibm_plex_sans_arabic_medium, FontWeight.Medium),
    androidx.compose.ui.text.font.Font(R.font.ibm_plex_sans_arabic_semibold, FontWeight.SemiBold),
    androidx.compose.ui.text.font.Font(R.font.ibm_plex_sans_arabic_bold, FontWeight.Bold)
)

val NunitoTypography = createBasicTypography(NunitoFontFamily)
val ThmanyahTypography = createBasicTypography(ThmanyahFontFamily)
val IbmPlexSansArabicTypography = createBasicTypography(IbmPlexSansArabicFontFamily)
val SystemTypography = createBasicTypography(FontFamily.Default)

