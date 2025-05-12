package com.example.my.project.authenticator.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import coil.load
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.ExitNativeAd
import com.example.my.project.authenticator.databinding.CreateNewGroupBinding
import com.example.my.project.authenticator.databinding.DeleteGroupBinding
import com.example.my.project.authenticator.databinding.DialogCustomBinding
import com.example.my.project.authenticator.databinding.DialogDeleteAccountBinding
import com.example.my.project.authenticator.databinding.DialogEditAccountBinding
import com.example.my.project.authenticator.databinding.DialogLogoutBinding
import com.example.my.project.authenticator.databinding.DialogPasswordGenerationBinding
import com.example.my.project.authenticator.databinding.DialogPasswordOptionsBinding
import com.example.my.project.authenticator.databinding.DialogReplaceAccountBinding
import com.example.my.project.authenticator.databinding.EditGroupBinding
import com.example.my.project.authenticator.databinding.ExitDialogBinding
import com.example.my.project.authenticator.databinding.GntLanguagesBinding
import com.example.my.project.authenticator.otp.domain.model.GuideItem
import com.example.my.project.authenticator.otp.domain.model.LanguagesModel
import com.example.my.project.authenticator.otp.domain.model.Password
import com.example.my.project.authenticator.ui.viewModel.HomeViewModel
import com.example.my.project.authenticator.utils.OnSingleClickListener
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.TotpCardState
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.analytics.FirebaseAnalytics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedDate(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(this))
}

fun View.showKeyboard() {
    if (requestFocus()) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
}

fun Context.browse(url: String, newTask: Boolean = false): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = url.toUri()
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: Exception) {
        false
    }
}

fun TextView.formatFreeTrialFooter(
    onPrivacyPolicyClicked: (() -> Unit)? = null,
    onTermsAndConditionsClicked: (() -> Unit)? = null,
    clickableTextColor: Int = ContextCompat.getColor(context, R.color.black)
) {
    val fullText = context.getString(R.string.terms_of_use_privacy_policy)
    val privacyPolicyText = context.getString(R.string.privacy_policy)
    val termsAndConditionsText = context.getString(R.string.terms_of_use)
    val spannableString = SpannableString(fullText)

    val privacyPolicyStart = fullText.indexOf(privacyPolicyText)
    val termsAndConditionsStart = fullText.indexOf(termsAndConditionsText)

    if (privacyPolicyStart != -1) {
        val privacyPolicyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onPrivacyPolicyClicked?.invoke()
            }
        }

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            privacyPolicyStart,
            privacyPolicyStart + privacyPolicyText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            privacyPolicyClickableSpan,
            privacyPolicyStart,
            privacyPolicyStart + privacyPolicyText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(clickableTextColor),
            privacyPolicyStart,
            privacyPolicyStart + privacyPolicyText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    if (termsAndConditionsStart != -1) {
        val termsAndConditionsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onTermsAndConditionsClicked?.invoke()
            }
        }

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            termsAndConditionsStart,
            termsAndConditionsStart + termsAndConditionsText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            termsAndConditionsClickableSpan,
            termsAndConditionsStart,
            termsAndConditionsStart + termsAndConditionsText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(clickableTextColor),
            termsAndConditionsStart,
            termsAndConditionsStart + termsAndConditionsText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    text = spannableString
    movementMethod = LinkMovementMethod.getInstance()
}

fun View.singleClick(onClick: () -> Unit) {
    this.setOnClickListener(object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            onClick.invoke()
        }
    })
}

fun ViewGroup.safeAddView(adView: View) {
    // Check if the ad view already has a parent
    if (adView.parent != null) {
        // Remove the ad view from its previous parent
        (adView.parent as ViewGroup).removeView(adView)
    }
    // Add the ad view to the new parent
    this.addView(adView)
}

inline fun <reified A : Activity> Activity.startActivityWithAnimation() {
    val intent = Intent(this, A::class.java)
    val options =
        ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
    this.startActivity(intent, options.toBundle())
}

inline fun <reified A : Activity> Activity.startActivityWithAnimationAndClearStack() {
    val intent = Intent(this, A::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val options =
        ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
    this.startActivity(intent, options.toBundle())
}


fun Activity.finishWithAnimation() {
    finish()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

fun Activity.showCustomDialog(callback: (result: String) -> Unit) {
    val dialog = BottomSheetDialog(this)
    val binding = DialogCustomBinding.inflate(LayoutInflater.from(this))

    dialog.setContentView(binding.root)

    binding.apply {
        ivScanQR.setOnClickListener {
            callback.invoke("ivScanQR")
            dialog.dismiss()
        }

        ivEnterKey.setOnClickListener {
            callback.invoke("ivEnterKey")
            dialog.dismiss()
        }
    }

    dialog.setOnDismissListener {
        callback.invoke("dismiss")
    }

    dialog.show()

    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    dialog.window?.setGravity(Gravity.BOTTOM)
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun MaterialCardView.changeCardStorkColor(color: Int, theme: Resources.Theme) {
    strokeColor = ResourcesCompat.getColor(resources, color, theme)
}

fun getPlatformList(): ArrayList<GuideItem> {
    val platforms = ArrayList<GuideItem>()
    platforms.add(
        GuideItem(
            "Facebook",
            "https://galixo.ai/authenticator/assets/guide_facebook.pdf",
            R.drawable.ic_fb
        )
    )
    platforms.add(
        GuideItem(
            "Instagram",
            "https://galixo.ai/authenticator/assets/guide_instagram.pdf",
            R.drawable.ic_insta
        )
    )
    platforms.add(
        GuideItem(
            "Tiktok",
            "https://galixo.ai/authenticator/assets/guide_tiktok.pdf",
            R.drawable.ic_tiktok
        )
    )
    platforms.add(
        GuideItem(
            "Google",
            "https://galixo.ai/authenticator/assets/guide_google.pdf",
            R.drawable.ic_google
        )
    )
    platforms.add(
        GuideItem(
            "LinkedIn",
            "https://galixo.ai/authenticator/assets/guide_linkedin.pdf",
            R.drawable.ic_linkedin
        )
    )
    platforms.add(
        GuideItem(
            "Youtube",
            "https://galixo.ai/authenticator/assets/guide_youtube.pdf",
            R.drawable.ic_yt
        )
    )
    platforms.add(
        GuideItem(
            "Dropbox",
            "https://galixo.ai/authenticator/assets/guide_dropbox.pdf",
            R.drawable.ic_dropbox
        )
    )

    return platforms
}

fun getLanguageList(): ArrayList<LanguagesModel> {
    val languagesList = ArrayList<LanguagesModel>()
    languagesList.add(LanguagesModel("English", "English", "en"))
    languagesList.add(LanguagesModel("Afrikaans", "Afrikaans", "af"))
    languagesList.add(LanguagesModel("Arabic", "عربي", "ar"))
    languagesList.add(LanguagesModel("Chinese", "汉语", "zh"))
    languagesList.add(LanguagesModel("Czech", "čeština", "cs"))
    languagesList.add(LanguagesModel("Danish", "dansk", "da"))
    languagesList.add(LanguagesModel("Dutch", "Nederland", "nl"))
    languagesList.add(LanguagesModel("German", "Deutsch", "de"))
    languagesList.add(LanguagesModel("Greek", "ελληνικά", "el"))
    languagesList.add(LanguagesModel("Hindi", "हिन्दी", "hi"))
    languagesList.add(LanguagesModel("Indonesian", "Bahasa Indonesia", "in"))
    languagesList.add(LanguagesModel("Italian", "italiano", "it"))
    languagesList.add(LanguagesModel("Japanese", "日本語", "ja"))
    languagesList.add(LanguagesModel("Malay", "məˈlā", "ms"))
    languagesList.add(LanguagesModel("Korean", "한국인", "ko"))
    languagesList.add(LanguagesModel("Norwegian", "norsk", "no"))
    languagesList.add(LanguagesModel("Persian", "فارسی", "fa"))
    languagesList.add(LanguagesModel("Portuguese", "Português", "pt"))
    languagesList.add(LanguagesModel("Russian", "російський", "ru"))
    languagesList.add(LanguagesModel("Spanish", "español", "es"))
    languagesList.add(LanguagesModel("Thai", "ไทย", "th"))
    languagesList.add(LanguagesModel("Turkish", "Türk", "tr"))
    languagesList.add(LanguagesModel("Vietnamese", "Tiếng Việt", "vi"))
    return languagesList
}

fun View.clickWithExtraDebounce(debounceTime: Long = 5000L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun String.formatCode(): String {
    if (isEmpty()) return ""

    return when (this.length) {
        5 -> if (this.toIntOrNull() == null) take(5) else "${take(3)} ${takeLast(2)}"
        6 -> "${take(3)} ${takeLast(3)}"
        7 -> "${take(4)} ${takeLast(3)}"
        8 -> "${take(4)} ${takeLast(4)}"
        else -> this
    }
}

fun ImageView.loadIssuerLogo(issuer: String) {
    val logoUrl = "https://logo.clearbit.com/${issuer.lowercase()}.com"

    // NOTE: if the above url is not working use following one
//    val logoUrl = "https://img.logo.dev/${issuer.lowercase()}.com"

    // Use Coil to load the image into the ImageView
    load(logoUrl) {
        placeholder(R.drawable.ic_profile_placeholder)  // Placeholder while loading
        error(R.drawable.ic_profile_placeholder)  // Error image if the logo fails to load
    }
}

fun TextView.setProfileImage(accountName: String) {
    val letterColors = mapOf(
        'A' to "#4285F4".toColorInt(),
        'B' to "#DB4437".toColorInt(),
        'C' to "#0F9D58".toColorInt(),
        'D' to "#F4B400".toColorInt(),
        'E' to "#AB47BC".toColorInt(),
        'F' to "#FB8C00".toColorInt(),
        'G' to "#00ACC1".toColorInt(),
        'H' to "#039BE5".toColorInt(),
        'I' to "#1E88E5".toColorInt(),
        'J' to "#E91E63".toColorInt(),
        'K' to "#FFC107".toColorInt(),
        'L' to "#795548".toColorInt(),
        'M' to "#4285F4".toColorInt(),
        'N' to "#DB4437".toColorInt(),
        'O' to "#0F9D58".toColorInt(),
        'P' to "#F4B400".toColorInt(),
        'Q' to "#AB47BC".toColorInt(),
        'R' to "#FB8C00".toColorInt(),
        'S' to "#00ACC1".toColorInt(),
        'T' to "#039BE5".toColorInt(),
        'U' to "#1E88E5".toColorInt(),
        'V' to "#E91E63".toColorInt(),
        'W' to "#FFC107".toColorInt(),
        'X' to "#795548".toColorInt(),
        'Y' to "#4285F4".toColorInt(),
        'Z' to "#DB4437".toColorInt()
    )

    val firstLetter = accountName.firstOrNull()?.uppercaseChar() ?: 'A'
    this.text = firstLetter.toString()
    val color = letterColors[firstLetter] ?: Color.GRAY

    val background = this.background as? GradientDrawable ?: GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    background.setColor(color)

    this.background = background
}


fun Context.showAskPasswordDialog(onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.ask_password_dialog, null)

    val dialogBuilder = AlertDialog.Builder(this, R.style.TransparentDialog)
        .setView(dialogView)
        .setCancelable(false)

    val alertDialog = dialogBuilder.create()

    val passwordEditText = dialogView.findViewById<EditText>(R.id.passwordEditText)
    val okButton = dialogView.findViewById<Button>(R.id.okButton)
    val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

    okButton.setOnClickListener {
        val password = passwordEditText.text.toString().trim()
        onSuccess(password)
        alertDialog.dismiss()
    }

    cancelButton.setOnClickListener {
        onDismiss()
        alertDialog.dismiss()
    }

    alertDialog.setOnShowListener {
        val window = alertDialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window?.attributes)
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        window?.attributes = layoutParams
    }

    alertDialog.show()
}

fun Context.logFirebaseEvent(eventName: String, params: Map<String, String> = emptyMap()) {
    val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    val bundle = Bundle().apply {
        params.forEach { (key, value) ->
            putString(key, value)
        }
    }
    firebaseAnalytics.logEvent(eventName, bundle)
}

fun View.setOnDebouncedClickListener(debounceTime: Long = 2000L, action: (View) -> Unit) {
    var lastClickTime = 0L

    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            action(view)
        }
    }
}

fun Fragment.showReplaceAccountDialog(onReplace: () -> Unit, onKeep: () -> Unit) {
    val binding = DialogReplaceAccountBinding.inflate(layoutInflater)


    val builder = AlertDialog.Builder(requireContext(), R.style.TransparentDialog)
    builder.setView(binding.root)
        .setCancelable(true)

    val alert = builder.create()
    alert.show()


    alert.setOnShowListener {
        val window = alert.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window?.attributes)
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        window?.attributes = layoutParams
    }

    binding.buttonReplace.setOnClickListener {
        alert.dismiss()
        onReplace()
    }

    binding.buttonKeep.setOnClickListener {
        alert.dismiss()
        onKeep()
    }
}

fun Context.copyTextToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}

fun String.getFirstCharacter(): Char? {
    return if (this.isNotEmpty()) {
        this[0]
    } else {
        null
    }
}

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        networkInfo != null && networkInfo.isConnected
    }
}


fun Context.sendEmail(recipient: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    startActivity(intent)
}


fun Fragment.sendEmail(recipient: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    startActivity(intent)

}


fun Context.openAppInPlayStore() {
    val appPackageName = this.packageName
    try {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

@SuppressLint("ClickableViewAccessibility")
fun Activity.showEditAccountBottomSheet(
    viewLifecycleOwner: LifecycleOwner,
    homeViewModel: HomeViewModel,
    account: TotpCardState,
    onNameChanged: (String) -> Unit,
    onDelete: (TotpCardState) -> Unit
) {
    val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentDialog).apply {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
    val binding = DialogEditAccountBinding.inflate(LayoutInflater.from(this))
    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()

    // Observe the homeState from homeViewModel (LiveData)
    homeViewModel.homeState.observe(viewLifecycleOwner, Observer { homeState ->
        // Look for the specific account in homeState.totpList and update the dialog UI
        val accountInHomeState = homeState.totpList.find { it.id == account.id }
        accountInHomeState?.let { mAccount ->
            // Update OTP and progress bar every time the account data changes
            Log.d("TAG888", "showEditAccountBottomSheet: otp= ${mAccount.oneTimeCode}")
            binding.apply {
                tvCode.text = mAccount.oneTimeCode.toString().padStart(6, '0').formatCode()
                circularProgress.progress = mAccount.secondsLeft.toFloat()
                circularProgress.text = mAccount.secondsLeft.toString()

                icCopy.setOnClickListener {
                    copyTextToClipboard(mAccount.oneTimeCode.toString())
                    bottomSheetDialog.dismiss()
                }

                btnCopy.setOnClickListener {
                    copyTextToClipboard(mAccount.oneTimeCode.toString())
                    bottomSheetDialog.dismiss()
                }
            }
        }
    })

    binding.apply {
        etAccountName.setText(account.name)

        etAccountName.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(
                p0: View?,
                event: MotionEvent?
            ): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN && icEdit.isVisible) {
                    icEdit.hide()
                    btnApply.show()
                }
                return false
            }
        })

        icEdit.setOnClickListener {
            icEdit.hide()
            etAccountName.requestFocus()
            // move the cursor to end of the text
            if (etAccountName.getText().toString().isNotEmpty()) etAccountName.setSelection(
                etAccountName.text!!.length
            )
            etAccountName.showKeyboard()
            btnApply.show()
        }

        btnApply.setOnClickListener {
            if (etAccountName.getText().toString() == "") {
                toast(getString(R.string.field_should_not_empty))
            } else {
                onNameChanged.invoke(etAccountName.getText().toString())
                bottomSheetDialog.dismiss()
            }
        }

        icDelete.setOnClickListener {
            onDelete(account)
            bottomSheetDialog.dismiss()
        }
    }
}

fun Activity.showDeleteAccountBottomSheet(
    onDelete: () -> Unit
) {
    val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentDialog).apply {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
    val binding = DialogDeleteAccountBinding.inflate(LayoutInflater.from(this))
    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()

    binding.apply {
        btnDelete.setOnClickListener {
            onDelete()
            bottomSheetDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }
}

fun Activity.showLogoutBottomSheet(
    onLogout: () -> Unit
) {
    val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentDialog)
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()

    binding.apply {
        btnLogout.setOnClickListener {
            onLogout()
            bottomSheetDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }
}

fun Activity.showExitBottomSheet(onExitClicked: () -> Unit) {
    val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentDialog)
    val binding = ExitDialogBinding.inflate(LayoutInflater.from(this))
    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()

    binding.apply {
        // Show appropriate containers based on whether an ad is loaded
        if (ExitNativeAd.mNativeAd != null) {
            // Show ad container
            ratingContainer.hide()
            adContainer.show()
            showNativeAd(this@showExitBottomSheet, adContainer)
        } else {
            // Show rating container
            ratingContainer.show()
            adContainer.hide()
        }

        ratingStars.setOnRatingChangeListener { ratingBar, rating, fromUser ->
            if (ratingBar.rating > 3) {
                PrefsHelper.isAppRated = true
                openAppInPlayStore()
                bottomSheetDialog.dismiss()
            } else {
                sendEmail("apps@galixo.ai", "", "")
                bottomSheetDialog.dismiss()
            }
        }

        exit.setOnClickListener {
            onExitClicked()
            bottomSheetDialog.dismiss()
        }
    }
}

fun Activity.showPasswordGenerationBottomSheet(
    onUsePassword: (String) -> Unit
) {
    val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentDialog)
    val binding = DialogPasswordGenerationBinding.inflate(LayoutInflater.from(this))
    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()

    var passwordLength = 16
    var includeLower = true
    var includeUpper = true
    var includeNumbers = false
    var includeSymbols = false

    fun updatePasswordUI(password: String) {
        binding.tvPassword.text = password

        // Evaluate strength
        val strength: String
        val color: Int
        val colorHeader: Int

        val diversityScore =
            listOf(includeLower, includeUpper, includeNumbers, includeSymbols).count { it }

        if (password.length >= 14 && diversityScore >= 3) {
            strength = "Very Strong"
            color = ContextCompat.getColor(this, R.color.green100)
            colorHeader = ContextCompat.getColor(this, R.color.greenLight)
        } else if (password.length >= 8 && diversityScore >= 2) {
            strength = "Average"
            color = ContextCompat.getColor(this, R.color.orange100)
            colorHeader = ContextCompat.getColor(this, R.color.orangeLight)
        } else {
            strength = "Weak"
            color = ContextCompat.getColor(this, R.color.red100)
            colorHeader = ContextCompat.getColor(this, R.color.redLight)
        }

        binding.tvPasswordStrength.text = strength
        binding.tvPasswordStrength.setTextColor(color)

        binding.btnCopy.backgroundTintList = ColorStateList.valueOf(color)
        binding.divider.setBackgroundColor(color)
        binding.header.setBackgroundColor(colorHeader)
    }

    // Generate password logic
    fun generatePassword(): String {
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?/"

        var chars = ""
        if (includeLower) chars += lower
        if (includeUpper) chars += upper
        if (includeNumbers) chars += numbers
        if (includeSymbols) chars += symbols

        if (chars.isEmpty()) return ""

        return (1..passwordLength)
            .map { chars.random() }
            .joinToString("")
    }

    // Set initial values
    binding.tvCharacterCount.text =
        getString(R.string.password_length_characters, passwordLength.toString())
    binding.seekBarPasswordCharacters.progress = passwordLength
    binding.seekBarPasswordCharacters.setOnSeekBarChangeListener(object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            passwordLength = progress.coerceAtLeast(4)
            val password = generatePassword()
            updatePasswordUI(password)
            binding.tvCharacterCount.text =
                getString(R.string.password_length_characters, passwordLength.toString())
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    // Switch listeners
    fun MaterialSwitch.bindToggle(onChange: (Boolean) -> Unit) {
        setOnCheckedChangeListener { _, isChecked -> onChange(isChecked) }
    }

    binding.switchLowercase.bindToggle { includeLower = it }
    binding.switchUppercase.bindToggle { includeUpper = it }
    binding.switchNumbers.bindToggle { includeNumbers = it }
    binding.switchSymbols.bindToggle { includeSymbols = it }

    binding.apply {
        // Generate button (icGeneratePassword)
        icGeneratePassword.setOnClickListener {
            val password = generatePassword()
            updatePasswordUI(password)
        }

        // Copy button
        btnCopy.setOnClickListener {
            val password = binding.tvPassword.text.toString()
            if (password.isNotBlank()) copyTextToClipboard(password)
        }

        // Use password button
        btnUsePassword.setOnClickListener {
            val password = binding.tvPassword.text.toString()
            if (password.isNotBlank()) {
                onUsePassword(password)
                bottomSheetDialog.dismiss()
            } else {
                toast(getString(R.string.generate_password_first))
            }
        }

        // Auto-generate once on open
        val initialPassword = generatePassword()
        updatePasswordUI(initialPassword)
    }
}

fun Activity.showPasswordOptionsBottomSheet(
    password: Password,
    onOptionSelected: (Int) -> Unit
) {
    val bottomSheetDialog = BottomSheetDialog(this, R.style.TransparentDialog)
    val binding = DialogPasswordOptionsBinding.inflate(LayoutInflater.from(this))
    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.setContentView(binding.root)
    bottomSheetDialog.show()

    binding.apply {
        tvName.text = password.name
        tvLastModified.text =
            getString(R.string.last_modified_time, password.lastModified.toFormattedDate())

        if (password.profileImagePath.isNullOrEmpty()) {
            ivProfileImage.hide()
            tvProfileImage.show()
            tvProfileImage.setProfileImage(password.name)
        } else {
            tvProfileImage.hide()
            ivProfileImage.show()
            ivProfileImage.load(password.profileImagePath.toUri()) {
                placeholder(R.drawable.ic_profile_placeholder)
                error(R.drawable.ic_profile_placeholder)
            }
        }

        tvView.setOnClickListener {
            onOptionSelected(tvView.id)
            bottomSheetDialog.dismiss()
        }

        tvEdit.setOnClickListener {
            onOptionSelected(tvEdit.id)
            bottomSheetDialog.dismiss()
        }

        tvCopyUrl.setOnClickListener {
            onOptionSelected(tvCopyUrl.id)
            bottomSheetDialog.dismiss()
        }

        tvCopyUsername.setOnClickListener {
            onOptionSelected(tvCopyUsername.id)
            bottomSheetDialog.dismiss()
        }

        tvCopyPassword.setOnClickListener {
            onOptionSelected(tvCopyPassword.id)
            bottomSheetDialog.dismiss()
        }

        tvShare.setOnClickListener {
            onOptionSelected(tvShare.id)
            bottomSheetDialog.dismiss()
        }

        tvDelete.setOnClickListener {
            onOptionSelected(tvDelete.id)
            bottomSheetDialog.dismiss()
        }
    }
}

fun Activity.sharePassword(password: Password) {
    val shareText = """
    🔐 Password Info

    Name: ${password.name}
    URL: ${password.url.orEmpty()}
    Username: ${password.emailOrUsername}
    Password: ${password.password}
    Notes: ${password.notes.orEmpty()}""".trimIndent()

    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

private fun showNativeAd(activity: Activity, adContainer: FrameLayout) {
    activity.apply {
        adContainer.show()
        ExitNativeAd.mNativeAd?.let {
            val adView = GntLanguagesBinding.inflate(layoutInflater)
            ExitNativeAd.populateNativeAdView(it, adView)
            adContainer.removeAllViews()
            adContainer.safeAddView(adView.root)
        }
    }
}

fun Fragment.createNewGroupDialog(newGroupName: (String) -> Unit) {
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.TransparentDialog)
    val binding = CreateNewGroupBinding.inflate(LayoutInflater.from(requireContext()))
    bottomSheetDialog.setContentView(binding.root)
    binding.apply {


        groupName.inputType = InputType.TYPE_CLASS_TEXT
        groupName.imeOptions = EditorInfo.IME_ACTION_DONE

        ivEnterKey.setOnClickListener {
            val groupNameInput = groupName.text.toString()
            val groupName = groupNameInput.replace("\n", "")
            if (groupName.isNotEmpty()) {
                newGroupName.invoke(groupName)
            } else {
                toast("Group name should not be empty")
            }
            bottomSheetDialog.dismiss()
        }
    }

    bottomSheetDialog.show()
}

fun Fragment.EditGroupDialog(currentName: String, newGroupName: (String) -> Unit) {
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.TransparentDialog)
    val binding = EditGroupBinding.inflate(LayoutInflater.from(requireContext()))
    bottomSheetDialog.setContentView(binding.root)
    binding.apply {

        groupName.setText(currentName)

        ivEnterKey.setOnClickListener {
            val groupName = groupName.text.toString()
            if (groupName.isNotEmpty()) {
                newGroupName.invoke(groupName)
            } else {
                toast("Group name should not be empty")
            }
            bottomSheetDialog.dismiss()
        }
    }

    bottomSheetDialog.show()
}

fun Fragment.deleteGroupDialog(deleteGroup: () -> Unit) {
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.TransparentDialog)
    val binding = DeleteGroupBinding.inflate(LayoutInflater.from(requireContext()))
    bottomSheetDialog.setContentView(binding.root)
    binding.apply {

        btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
        btnDelete.setOnClickListener {
            deleteGroup.invoke()
            bottomSheetDialog.dismiss()
        }
    }

    bottomSheetDialog.show()
}

fun Fragment.openAppInPlayStore() {
    val appPackageName = requireContext().packageName
    try {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}


fun String.getLanguageName(): String {
    return when (this) {
        "en" -> "English"
        "af" -> "Afrikaans"
        "ar" -> "Arabic"
        "zh" -> "Chinese"
        "cs" -> "Czech"
        "da" -> "Danish"
        "nl" -> "Dutch"
        "de" -> "German"
        "el" -> "Greek"
        "hi" -> "Hindi"
        "in" -> "Indonesian"
        "it" -> "Italian"
        "ja" -> "Japanese"
        "ms" -> "Malay"
        "ko" -> "Korean"
        "no" -> "Norwegian"
        "fa" -> "Persian"
        "pt" -> "Portuguese"
        "ru" -> "Russian"
        "es" -> "Spanish"
        "th" -> "Thai"
        "tr" -> "Turkish"
        "vi" -> "Vietnamese"
        else -> "Unknown Language"
    }
}

fun String.validatePassword(confirmPassword: String): String {
    val minPasswordLength = 4

    return when {
        this.isEmpty() -> "Password cannot be empty"
        this.length < minPasswordLength -> "Password must be at least $minPasswordLength characters long"
        this != confirmPassword -> "Password and confirm password do not match"
        else -> "Password is valid"
    }
}


fun String?.validatePasswordChange(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String
): String {
    if (currentPassword != this) {
        return "not"
    }

    if (newPassword.isEmpty()) {
        return "not"
    }

    if (newPassword != confirmPassword) {
        return "not"
    }

    if (newPassword.length < 4) {
        return "not"
    }

    return "ok"
}

