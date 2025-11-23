package com.labactivity.lala.UTILS

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.labactivity.lala.R

/**
 * Utility class for showing consistent AlertDialogs throughout the app
 * Replaces Toast messages with proper modal dialogs
 */
object DialogUtils {

    /**
     * Checks if the context is valid for showing dialogs
     */
    private fun isContextValid(context: Context): Boolean {
        return context is Activity && !context.isFinishing && !context.isDestroyed
    }

    /**
     * Shows an information dialog with a single OK button
     * @param context The context to show the dialog in
     * @param title The dialog title (optional)
     * @param message The dialog message
     * @param onDismiss Optional callback when dialog is dismissed
     */
    fun showInfoDialog(
        context: Context,
        title: String = "Information",
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        if (!isContextValid(context)) return

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Shows an error dialog with a single OK button
     * @param context The context to show the dialog in
     * @param title The dialog title (default: "Error")
     * @param message The error message
     * @param onDismiss Optional callback when dialog is dismissed
     */
    fun showErrorDialog(
        context: Context,
        title: String = "Error",
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        if (!isContextValid(context)) return

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Shows a success dialog with a single OK button
     * @param context The context to show the dialog in
     * @param title The dialog title (default: "Success")
     * @param message The success message
     * @param onDismiss Optional callback when dialog is dismissed
     */
    fun showSuccessDialog(
        context: Context,
        title: String = "Success",
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        if (!isContextValid(context)) return

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Shows a warning dialog with a single OK button
     * @param context The context to show the dialog in
     * @param title The dialog title (default: "Warning")
     * @param message The warning message
     * @param onDismiss Optional callback when dialog is dismissed
     */
    fun showWarningDialog(
        context: Context,
        title: String = "Warning",
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        if (!isContextValid(context)) return

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismiss?.invoke()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Shows a confirmation dialog with Yes/No buttons
     * @param context The context to show the dialog in
     * @param title The dialog title
     * @param message The confirmation message
     * @param onConfirm Callback when user clicks Yes
     * @param onCancel Optional callback when user clicks No
     */
    fun showConfirmDialog(
        context: Context,
        title: String = "Confirmation",
        message: String,
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        if (!isContextValid(context)) return

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                onConfirm()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                onCancel?.invoke()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Shows a hint dialog (used for challenge hints)
     * @param context The context to show the dialog in
     * @param title The dialog title (default: "💡 Hint")
     * @param hint The hint text to display
     */
    fun showHintDialog(
        context: Context,
        title: String = "💡 Hint",
        hint: String
    ) {
        if (!isContextValid(context)) return

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(hint)
            .setPositiveButton("Got it!") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Shows a locked feature dialog
     * @param context The context to show the dialog in
     * @param title The dialog title
     * @param message The lock reason message
     */
    fun showLockedDialog(
        context: Context,
        title: String = "🔒 Locked",
        message: String
    ) {
        if (!isContextValid(context)) return

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}
