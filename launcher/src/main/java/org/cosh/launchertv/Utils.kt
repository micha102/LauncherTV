package org.cosh.launchertv

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import java.util.*

object Utils {

    fun loadApplications(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val intentActivities = packageManager.queryIntentActivities(mainIntent, 0)

        // Only add the CATEGORY_LEANBACK_LAUNCHER if the API level is 21 or higher
        val leanbackIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            }
        } else {
            null
        }

        // Merge the activities
        val allActivities = if (leanbackIntent != null) {
            intentActivities + packageManager.queryIntentActivities(leanbackIntent, 0)
        } else {
            intentActivities
        }

        val knownPackages = mutableSetOf<String>()
        val entries = mutableListOf<AppInfo>()

        for (resolveInfo in allActivities) {
            val packageName = resolveInfo.activityInfo.packageName
            if (context.packageName != packageName && !knownPackages.contains(packageName)) {
                entries.add(AppInfo(packageManager, resolveInfo))
                knownPackages.add(packageName)
            }
        }

        return entries.sortedBy { it.getName().lowercase(Locale.ROOT) }
    }

    fun getPixelFromDp(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), displayMetrics)).toInt()
    }
}