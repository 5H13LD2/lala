package com.labactivity.lala.FIXBACKBUTTON

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit App")
        builder.setMessage("Do you want to exit app?")

        builder.setPositiveButton("Yes") { _, _ ->
            super.onBackPressed() // dito tatawagin yung default back behavior
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }
}
