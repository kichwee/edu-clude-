package com.example.educloud.theme

import androidx.compose.ui.graphics.Color

// Stitch-derived Android visual language: warm paper, leaf green, mango orange,
// lake blue and high-contrast charcoal. Kept intentionally calm for low-end phones.
val EduCloudPaper = Color(0xFFFAF9F6)
val EduCloudSurface = Color(0xFFFFFFFF)
val EduCloudInk = Color(0xFF1A1C1A)
val EduCloudMutedInk = Color(0xFF42493C)
val EduCloudOrange = Color(0xFFFE9B4E)
val EduCloudOrangeLight = Color(0xFFFFDCC5)
val EduCloudLake = Color(0xFF00658E)
val EduCloudLakeLight = Color(0xFFC7E7FF)
val EduCloudSun = Color(0xFFFCA35D)
val EduCloudLeaf = Color(0xFF3B6A1B)
val EduCloudLeafLight = Color(0xFFBAF293)
val EduCloudLine = Color(0xFFC2C9B8)

// Additional Stitch tokens
val EduCloudError = Color(0xFFBA1A1A)
val EduCloudErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)   // on-error-container
val EduCloudWarning = Color(0xFF904F00)
val EduCloudWarningContainer = Color(0xFFFFDDB3)
val EduCloudSuccess = Color(0xFF146E2E)
val EduCloudSuccessContainer = Color(0xFF9DF8A7)
val EduCloudInfo = Color(0xFF00658E)
val EduCloudInfoContainer = Color(0xFFC7E7FF)

val EduBlue80 = Color(0xFF92CAFF)
val EduBlueGrey80 = Color(0xFFB1C8FF)
val EduGreen80 = Color(0xFF6DD58C)

val EduBlue40 = EduCloudLake
val EduBlueGrey40 = EduCloudInk
val EduGreen40 = Color(0xFF2E7D32)

// Extended palette
val EduBlue = EduCloudLake
val EduGreen = Color(0xFF2E7D32)
val EduAmber = Color(0xFFF57F17)
val EduRed = Color(0xFFC62828)

// Subject colors
val SubjectMath = EduCloudLeaf
val SubjectScience = Color(0xFF2E7D32)   // Green – Sayansi
val SubjectKiswahili = Color(0xFFAD1457) // Pink – Kiswahili
val SubjectEnglish = Color(0xFF6A1B9A)   // Purple – English

// Streak / gamification
val StreakOrange = Color(0xFFE65100)
val StreakGold = Color(0xFFFFD600)

// Neutral
val Surface = EduCloudPaper
val SurfaceDark = Color(0xFF121212)
val OnSurfaceDark = Color(0xFFE0E0E0)

// ── Stitch Material surface/container tokens ──────────────────────────────────
val SurfaceContainerLowest  = Color(0xFFFFFFFF)   // surface-container-lowest
val SurfaceContainerLow     = Color(0xFFF4F3F1)   // surface-container-low
val SurfaceContainer        = Color(0xFFEFEEEB)   // surface-container
val SurfaceContainerHigh    = Color(0xFFE9E8E5)   // surface-container-high
val SurfaceContainerHighest = Color(0xFFE3E2E0)   // surface-container-highest
val SurfaceDimToken         = Color(0xFFDBDAD7)   // surface-dim
val SurfaceBright           = Color(0xFFFAF9F6)   // surface-bright
val InverseSurface          = Color(0xFF2F312F)   // inverse-surface
val InverseOnSurface        = Color(0xFFF2F1EE)   // inverse-on-surface

// Primary / container tokens ───────────────────────────────────────────────────
val PrimaryContainer        = Color(0xFF6DA04B)   // primary-container
val OnPrimaryContainer      = Color(0xFF133200)   // on-primary-container
val PrimaryFixed            = Color(0xFFBAF293)   // primary-fixed
val PrimaryFixedDim         = Color(0xFF9FD67A)   // primary-fixed-dim
val InversePrimary          = Color(0xFF9FD67A)   // inverse-primary
val OnPrimaryFixed          = Color(0xFF0A2100)   // on-primary-fixed
val OnPrimaryFixedVariant   = Color(0xFF235102)   // on-primary-fixed-variant

// Secondary / container tokens ─────────────────────────────────────────────────
val SecondaryFixed          = Color(0xFFFFDCC5)   // secondary-fixed  (peach)
val SecondaryFixedDim       = Color(0xFFFFB783)   // secondary-fixed-dim
val SecondaryContainer      = Color(0xFFFE9B4E)   // secondary-container (mango)
val OnSecondaryContainer    = Color(0xFF6E3600)   // on-secondary-container
val OnSecondaryFixed        = Color(0xFF301400)   // on-secondary-fixed
val OnSecondaryFixedVariant = Color(0xFF703700)   // on-secondary-fixed-variant

// Tertiary / container tokens ──────────────────────────────────────────────────
val TertiaryContainer       = Color(0xFF4F9AC7)   // tertiary-container
val TertiaryFixed           = Color(0xFFC7E7FF)   // tertiary-fixed
val TertiaryFixedDim        = Color(0xFF86CFFE)   // tertiary-fixed-dim
val OnTertiaryContainer     = Color(0xFF002F45)   // on-tertiary-container
val OnTertiaryFixed         = Color(0xFF001E2E)   // on-tertiary-fixed
val OnTertiaryFixedVariant  = Color(0xFF004C6C)   // on-tertiary-fixed-variant

// Outline tokens ───────────────────────────────────────────────────────────────
val Outline                 = Color(0xFF72796A)   // outline
val OutlineVariant          = Color(0xFFC2C9B8)   // outline-variant
