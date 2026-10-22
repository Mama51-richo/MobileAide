package com.mobileaide

import android.view.View
import android.widget.AdapterView

class SimpleSelectionListener(
    private val onSelectionChanged: (String) -> Unit
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        val selected = parent.getItemAtPosition(position).toString()
        onSelectionChanged(selected)
    }

    override fun onNothingSelected(parent: AdapterView<*>) = Unit
}
