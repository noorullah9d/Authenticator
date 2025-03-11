package com.example.my.project.authenticator.extensions

import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.CreateNewGroupBinding
import com.example.my.project.authenticator.databinding.DeleteGroupBinding
import com.example.my.project.authenticator.databinding.DialogCustomBinding
import com.example.my.project.authenticator.databinding.DialogReplaceAccountBinding
import com.example.my.project.authenticator.databinding.EditGroupBinding
import com.example.my.project.authenticator.databinding.ExitDialogBinding
import com.example.my.project.authenticator.model.GuideItem
import com.example.my.project.authenticator.model.LanguagesModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.firebase.analytics.FirebaseAnalytics


inline fun <reified A : Activity> Activity.startActivityWithAnimation() {
    val intent = Intent(this, A::class.java)
    val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
    this.startActivity(intent, options.toBundle())
}

inline fun <reified A : Activity> Activity.startActivityWithAnimationAndClearStack() {
    val intent = Intent(this, A::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
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
    platforms.add(GuideItem("Facebook", "https://galixo.ai/authenticator/assets/guide_facebook.pdf", R.drawable.ic_fb))
    platforms.add(GuideItem("Instagram", "https://galixo.ai/authenticator/assets/guide_instagram.pdf", R.drawable.ic_insta))
    platforms.add(GuideItem("Tiktok", "https://galixo.ai/authenticator/assets/guide_tiktok.pdf", R.drawable.ic_tiktok))
    platforms.add(GuideItem("Google", "https://galixo.ai/authenticator/assets/guide_google.pdf", R.drawable.ic_google))
    platforms.add(GuideItem("LinkedIn", "https://galixo.ai/authenticator/assets/guide_linkedin.pdf", R.drawable.ic_linkedin))
    platforms.add(GuideItem("Youtube", "https://galixo.ai/authenticator/assets/guide_youtube.pdf", R.drawable.ic_yt))
    platforms.add(GuideItem("Dropbox", "https://galixo.ai/authenticator/assets/guide_dropbox.pdf", R.drawable.ic_dropbox))

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

fun TextView.setProfileImage(accountName: String) {
    val letterColors = mapOf(
        'A' to Color.parseColor("#4285F4"),
        'B' to Color.parseColor("#DB4437"),
        'C' to Color.parseColor("#0F9D58"),
        'D' to Color.parseColor("#F4B400"),
        'E' to Color.parseColor("#AB47BC"),
        'F' to Color.parseColor("#FB8C00"),
        'G' to Color.parseColor("#00ACC1"),
        'H' to Color.parseColor("#039BE5"),
        'I' to Color.parseColor("#1E88E5"),
        'J' to Color.parseColor("#E91E63"),
        'K' to Color.parseColor("#FFC107"),
        'L' to Color.parseColor("#795548"),
        'M' to Color.parseColor("#4285F4"),
        'N' to Color.parseColor("#DB4437"),
        'O' to Color.parseColor("#0F9D58"),
        'P' to Color.parseColor("#F4B400"),
        'Q' to Color.parseColor("#AB47BC"),
        'R' to Color.parseColor("#FB8C00"),
        'S' to Color.parseColor("#00ACC1"),
        'T' to Color.parseColor("#039BE5"),
        'U' to Color.parseColor("#1E88E5"),
        'V' to Color.parseColor("#E91E63"),
        'W' to Color.parseColor("#FFC107"),
        'X' to Color.parseColor("#795548"),
        'Y' to Color.parseColor("#4285F4"),
        'Z' to Color.parseColor("#DB4437")
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

fun Context.privacyPolicy(url: String, newTask: Boolean = false): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}

fun Context.copyTextToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    toast("Text Copied")
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
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        networkInfo != null && networkInfo.isConnected
    }
}


fun Context.sendEmail(recipient: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    startActivity(intent)
}


fun Fragment.sendEmail(recipient: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    startActivity(intent)

}


fun Context.openAppInPlayStore() {
    val appPackageName = this.packageName
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}


fun Fragment.showBottomSheetDialog(onExitClicked: () -> Unit, onCancelClicked: () -> Unit) {
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.TransparentDialog)
    val binding = ExitDialogBinding.inflate(LayoutInflater.from(requireContext()))
    bottomSheetDialog.setContentView(binding.root)
    binding.exit.setOnClickListener {
        onExitClicked()
        bottomSheetDialog.dismiss()
    }
    binding.ratingStars.setOnRatingChangeListener { ratingBar, rating, fromUser ->
        if (ratingBar.rating > 3) {
            openAppInPlayStore()
            bottomSheetDialog.dismiss()
        } else {
            sendEmail("apps@galixo.ai", "", "")
            bottomSheetDialog.dismiss()
        }
    }


    binding.cancel.setOnClickListener {
        onCancelClicked()
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.show()
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
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
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


fun String?.validatePasswordChange(currentPassword: String, newPassword: String, confirmPassword: String): String {
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

