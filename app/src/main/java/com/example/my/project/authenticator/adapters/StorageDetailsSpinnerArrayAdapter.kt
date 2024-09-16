package com.example.my.project.authenticator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.extensions.beGone
import com.example.my.project.authenticator.extensions.beVisible

class StorageDetailsSpinnerArrayAdapter(
    context: Context,
    private val spinnerList: List<String>,
    private val isRecommended: Boolean
) : ArrayAdapter<String>(context, 0, spinnerList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.app_themes, parent, false)

        val itemTextView: TextView = view.findViewById(R.id.spinnerItemText)
        val viewSpinner: View = view.findViewById(R.id.view)
        itemTextView.text = spinnerList[position]

        if (position == 2) {
            viewSpinner.beGone()
        } else {

            viewSpinner.beVisible()
        }

        return view
    }

}
