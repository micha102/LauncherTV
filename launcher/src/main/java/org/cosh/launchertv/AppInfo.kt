package org.cosh.launchertv

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Log
import org.cosh.launchertv.fragments.ApplicationFragment.Companion.TAG

class AppInfo(packageManager: PackageManager, resolveInfo: ResolveInfo) {
    var mIcon: Drawable
    var mPackageName: String
    var mName: String

    init {
        val activityInfo = resolveInfo.activityInfo

        // If activityInfo is null, the packageName cannot be accessed safely
        mPackageName = activityInfo?.packageName ?: "UnknownPackage"

        mName = try {
            resolveInfo.loadLabel(packageManager).toString()
        } catch (e: Exception) {
            mPackageName
        }

        // Safely load the icon with null checks
        mIcon = try {
            resolveInfo.loadIcon(packageManager)
        } catch (e: Exception) {
            // Fallback icon if loading the icon fails
            packageManager.getDefaultActivityIcon()
        }
    }

    // Constructor that accepts ApplicationInfo directly
    constructor(packageManager: PackageManager, applicationInfo: ApplicationInfo) : this(
        packageManager,
        ResolveInfo().apply {
            activityInfo = android.content.pm.ActivityInfo() // Ensure activityInfo is initialized
            activityInfo.packageName = applicationInfo.packageName
        }
    ) {
        mPackageName = applicationInfo.packageName
        mName = try {
            applicationInfo.loadLabel(packageManager).toString()
        } catch (e: Exception) {
            mPackageName
        }

        // Safely load the icon with null checks
        mIcon = try {
            applicationInfo.loadIcon(packageManager)
        } catch (e: Exception) {
            // Fallback icon if loading the icon fails
            packageManager.getDefaultActivityIcon()
        }
    }

    fun getName(): String = mName

    fun getIcon(): Drawable = mIcon

    fun getPackageName(): String = mPackageName
}