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

class AddCategorySpinnerArrayAdapter(
    context: Context,
    private val options: List<String>,
    private val isCustomView: Boolean
) : ArrayAdapter<String>(context, 0, options) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_selected_item, parent, false)

        val textView = view.findViewById<TextView>(R.id.selectedItemText)
        textView.text = options[position]

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_dropdown_item, parent, false)


        val textView = view.findViewById<TextView>(R.id.spinnerItemText)
        textView.text = options[position]


        if (position == options.size - 1) {
            textView.beGone()
        } else {
            textView.beVisible()
        }

        return view
    }
}

private const val TAG = "AddCategorySpinnerArray"