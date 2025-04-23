package com.example.my.project.authenticator.utils

const val PREFS_NAME = "my_prefs"
const val IS_FIRST_TIME = "FirstTime"
const val USER_EMAIL = "userEmail"
const val USER_PASSWORD = "userPassword"
const val USER_THEME = "userThemes"
const val LIGHT = "Light"
const val DARK = "Dark"
const val SYSTEM_DEFAULT = "System"
const val BACKUP = "backUp"
const val FIRST_TIME_MAIN = "firstTimeMain"
const val BACK_UP_GONE = "backUpGone"
const val FINGERPRINT_ENABLED = "isFingerprintEnabled"
const val APP_THEME = "app_theme"
const val IS_PREMIUM = "is_premium"
const val IS_LANGUAGE_SHOWN = "IS_LANGUAGE_SHOWN"
const val IS_ONBOARDING_SHOWN = "IS_ONBOARDING_SHOWN"
const val LAST_PREMIUM_SHOWN_TIME = "lastPremiumShownTime"
const val IS_APP_RATED = "is_app_rated"

var isInterstitialShowing = false
var shouldShowInterstitialAd = false
var isAnySystemDialogShown = false

// IAP
const val WEEKLY = "weekly"
const val YEARLY = "yearly"
var splashIAPExperiment = YEARLY
const val perMonth = "/month"
const val perYear = "/year"

const val PRIVACY_POLICY_URL = "https://galixo.ai/authenticator/privacy-policy"
const val TERMS_CONDITIONS_URL = "https://galixo.ai/authenticator/terms-and-conditions"