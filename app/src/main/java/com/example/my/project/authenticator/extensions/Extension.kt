package com.example.my.project.authenticator.extensions

import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.DialogCustomBinding
import com.example.my.project.authenticator.model.LanguagesModel
import com.google.android.material.card.MaterialCardView


inline fun <reified A : Activity> Activity.startActivityWithAnimation() {
    val intent = Intent(this, A::class.java)
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


private const val TAG = "Extension"