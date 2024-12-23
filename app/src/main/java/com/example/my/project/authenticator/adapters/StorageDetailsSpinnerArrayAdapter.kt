package com.example.my.project.authenticator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.appclass.CustomSpinner
import com.example.my.project.authenticator.extensions.beGone
import com.example.my.project.authenticator.extensions.beVisible

class StorageDetailsSpinnerArrayAdapter(
    context: Context,
    private val spinnerList: List<String>,
    private val isRecommended: Boolean,
    private val spinner: CustomSpinner,
    private val onItemSelectedCallback: () -> Unit
) : ArrayAdapter<String>(context, 0, spinnerList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = getCustomView(position, convertView, parent)

        val viewSpinner: View = view.findViewById(R.id.view)
        val llAddNew: ConstraintLayout = view.findViewById(R.id.llAddNew)
        if (isRecommended) {
            if (spinner.isDropdownShown) {
                if (position == spinnerList.size - 1 && spinner.isDropdownShown) {
                    viewSpinner.beGone()
                    llAddNew.beVisible()
                } else {
                    viewSpinner.beVisible()
                    llAddNew.beGone()
                }
            } else if (!spinner.isDropdownShown) {
                viewSpinner.beGone()
                llAddNew.beGone()
            }
        }

        llAddNew.setOnClickListener {
            onItemSelectedCallback.invoke()
        }

        return view
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.app_themes, parent, false)

        val itemTextView: TextView = view.findViewById(R.id.spinnerItemText)
        itemTextView.text = spinnerList[position]

        return view
    }
}

private const val TAG = "StorageDetailsSpinnerAr"
