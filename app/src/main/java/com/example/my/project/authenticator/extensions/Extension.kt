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
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.DialogCustomBinding
import com.example.my.project.authenticator.databinding.DialogReplaceAccountBinding
import com.example.my.project.authenticator.model.LanguagesModel
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


fun Activity.showCustomDialog(callback: (result: String) -> Unit) {
    val dialog = AlertDialog.Builder(this, R.style.TransparentDialog).create()
    val binding = DialogCustomBinding.inflate(LayoutInflater.from(this))
    dialog.setView(binding.root)

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

    val window = dialog.window
    window?.setGravity(Gravity.BOTTOM)
    val params = window?.attributes
    params?.y = 200
    window?.attributes = params
    window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

}


fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun View.beInVisible() {
    visibility = View.INVISIBLE
}


fun MaterialCardView.changeCardStorkColor(color: Int, theme: Resources.Theme) {
    strokeColor = ResourcesCompat.getColor(resources, color, theme)
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
        'A' to Color.parseColor("#F44336"),
        'B' to Color.parseColor("#E91E63"),
        'C' to Color.parseColor("#9C27B0"),
        'D' to Color.parseColor("#673AB7"),
        'E' to Color.parseColor("#3F51B5"),
        'F' to Color.parseColor("#2196F3"),
        'G' to Color.parseColor("#03A9F4"),
        'H' to Color.parseColor("#00BCD4"),
        'I' to Color.parseColor("#009688"),
        'J' to Color.parseColor("#4CAF50"),
        'K' to Color.parseColor("#8BC34A"),
        'L' to Color.parseColor("#CDDC39"),
        'M' to Color.parseColor("#FFEB3B"),
        'N' to Color.parseColor("#FFC107"),
        'O' to Color.parseColor("#FF9800"),
        'P' to Color.parseColor("#FF5722"),
        'Q' to Color.parseColor("#795548"),
        'R' to Color.parseColor("#9E9E9E"),
        'S' to Color.parseColor("#607D8B"),
        'T' to Color.parseColor("#FF5722"),
        'U' to Color.parseColor("#9C27B0"),
        'V' to Color.parseColor("#673AB7"),
        'W' to Color.parseColor("#3F51B5"),
        'X' to Color.parseColor("#2196F3"),
        'Y' to Color.parseColor("#03A9F4"),
        'Z' to Color.parseColor("#00BCD4")
    )

    val firstLetter = accountName.firstOrNull()?.uppercaseChar() ?: 'A'
    this.text = firstLetter.toString()

    val color = letterColors[firstLetter] ?: Color.GRAY

    val background = this.background as? GradientDrawable
    background?.setColor(color)
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
        .setCancelable(false)

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
}