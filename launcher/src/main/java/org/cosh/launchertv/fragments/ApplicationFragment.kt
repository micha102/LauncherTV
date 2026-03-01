package org.cosh.launchertv.fragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.cosh.launchertv.AppInfo
import org.cosh.launchertv.R
import org.cosh.launchertv.Setup
import org.cosh.launchertv.Utils
import org.cosh.launchertv.activities.ApplicationList
import org.cosh.launchertv.activities.PreferencesActivity
import org.cosh.launchertv.views.ApplicationView
import java.text.DateFormat
import java.util.*
import android.util.Log
import org.cosh.launchertv.PreferencesManager

class ApplicationFragment : Fragment(), View.OnClickListener, View.OnLongClickListener {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var mClock: TextView
    private lateinit var mDate: TextView

    private lateinit var mTimeFormat: DateFormat
    private lateinit var mDateFormat: DateFormat
    private lateinit var mBatteryLevel: TextView
    private lateinit var mBatteryIcon: ImageView
    private lateinit var mContainer: LinearLayout
    private lateinit var mSettings: View
    private lateinit var mGridView: View
    private lateinit var mSetup: Setup

    private val mHandler = Handler()
    private val mTimerTick: Runnable = Runnable { setClock() }

    private var mGridX = 3
    private var mGridY = 2
    private var mApplications: Array<Array<ApplicationView?>> = arrayOf()
    private var mBatteryChangedReceiverRegistered = false

    companion object {
        const val TAG = "ApplicationFragment"
        private const val PREFERENCES_NAME = "applications"
        private const val REQUEST_CODE_APPLICATION_LIST = 0x1E
        private const val REQUEST_CODE_WALLPAPER = 0x1F
        private const val REQUEST_CODE_APPLICATION_START = 0x20
        private const val REQUEST_CODE_PREFERENCES = 0x21

        @JvmStatic
        fun newInstance(): ApplicationFragment {
            return ApplicationFragment()
        }
    }

    private val mBatteryChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            mBatteryLevel.text = getString(R.string.battery_level_text, level)
            val batteryIconId = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBatteryIcon.setImageDrawable(resources.getDrawable(batteryIconId, null))
            } else {
                mBatteryIcon.setImageDrawable(resources.getDrawable(batteryIconId))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_application, container, false)
        preferencesManager = PreferencesManager(requireContext())
        mSetup = Setup(requireContext())
        mContainer = view.findViewById(R.id.container)
        mSettings = view.findViewById(R.id.settings)
        mGridView = view.findViewById(R.id.application_grid)
        mClock = view.findViewById(R.id.clock)
        mDate = view.findViewById(R.id.date)
        val batteryLayout = view.findViewById<LinearLayout>(R.id.battery_layout)
        mBatteryLevel = view.findViewById(R.id.battery_level)
        mBatteryIcon = view.findViewById(R.id.battery_icon)

        mTimeFormat = android.text.format.DateFormat.getTimeFormat(activity)
        mDateFormat = android.text.format.DateFormat.getLongDateFormat(activity)

        if (mSetup.keepScreenOn()) mContainer.keepScreenOn = true
        if (!mSetup.showDate()) mDate.visibility = View.GONE

        if (mSetup.showBattery()) {
            batteryLayout.visibility = View.VISIBLE
            activity?.registerReceiver(mBatteryChangedReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            mBatteryChangedReceiverRegistered = true
        } else {
            batteryLayout.visibility = View.INVISIBLE
            if (mBatteryChangedReceiverRegistered) {
                activity?.unregisterReceiver(mBatteryChangedReceiver)
                mBatteryChangedReceiverRegistered = false
            }
        }

        mSettings.setOnClickListener(this)
        mGridView.setOnClickListener(this)

        createApplications()

        return view
    }

    private fun createApplications() {
        mContainer.removeAllViews()

        mGridX = mSetup.getGridX().coerceAtLeast(2)
        mGridY = mSetup.getGridY().coerceAtLeast(1)

        val marginX = Utils.getPixelFromDp(requireContext(), mSetup.getMarginX())
        val marginY = Utils.getPixelFromDp(requireContext(), mSetup.getMarginY())
        val showNames = mSetup.showNames()

        mApplications = Array(mGridY) { arrayOfNulls<ApplicationView>(mGridX) }

        var position = 0
        for (y in 0 until mGridY) {
            val ll = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                isFocusable = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
            }

            for (x in 0 until mGridX) {
                val av = ApplicationView(requireContext()).apply {
                    setOnClickListener(this@ApplicationFragment)
                    setOnLongClickListener(this@ApplicationFragment)
                    setOnMenuOnClickListener { onLongClick(it) }
                    this.position = position++
                    showName(showNames)
                    id = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        0x00FFFFFF + position
                    } else {
                        View.generateViewId()
                    }
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                        setMargins(marginX, marginY, marginX, marginY)
                    }
                }
                ll.addView(av)
                mApplications[y][x] = av
            }
            mContainer.addView(ll)
        }

        updateApplications()
        setApplicationOrder()

        val firstAppView = (mContainer.getChildAt(0) as LinearLayout).getChildAt(0) as ApplicationView
        firstAppView.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }
    }

    private fun setApplicationOrder() {
        for (y in 0 until mGridY) {
            for (x in 0 until mGridX) {
                val upId = if (y > 0) mApplications[y - 1][x]?.id ?: R.id.application_grid else R.id.application_grid
                val downId = if (y + 1 < mGridY) mApplications[y + 1][x]?.id ?: R.id.settings else R.id.settings
                val leftId = if (x > 0) mApplications[y][x - 1]?.id ?: R.id.application_grid else R.id.application_grid
                val rightId = if (x + 1 < mGridX) mApplications[y][x + 1]?.id ?: R.id.settings else R.id.settings

                mApplications[y][x]?.apply {
                    setNextFocusLeftId(leftId)
                    setNextFocusRightId(rightId)
                    setNextFocusUpId(upId)
                    setNextFocusDownId(downId)
                }
            }
        }

        mGridView.apply {
            setNextFocusLeftId(R.id.settings)
            setNextFocusRightId(mApplications[0][0]?.id ?: R.id.settings)
            setNextFocusUpId(R.id.settings)
            setNextFocusDownId(mApplications[0][0]?.id ?: R.id.settings)
        }

        mSettings.apply {
            setNextFocusLeftId(mApplications[mGridY - 1][mGridX - 1]?.id ?: R.id.application_grid)
            setNextFocusRightId(R.id.application_grid)
            setNextFocusUpId(mApplications[mGridY - 1][mGridX - 1]?.id ?: R.id.application_grid)
            setNextFocusDownId(R.id.application_grid)
        }
    }

    private fun updateApplications() {
        val pm = activity?.packageManager ?: return

        for (y in 0 until mGridY) {
            for (x in 0 until mGridX) {
                val app = mApplications[y][x]
                val key = ApplicationView.getPreferenceKey(app?.position ?: -1)
                val packageName = preferencesManager.getPackageName(key)
                setApplication(pm, app, packageName)
            }
        }
    }
    private fun setApplication(pm: PackageManager, app: ApplicationView?, packageName: String?) {

        try {
            if (!packageName.isNullOrEmpty()) {
                // Try to get the PackageInfo object for the given packageName

                val packageInfo = try {
                    pm.getPackageInfo(packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e(TAG, "Package not found: $packageName", e)
                    null
                }

                // Proceed if the packageInfo is found
                packageInfo?.let {
                    val appInfo = AppInfo(pm, it.applicationInfo!!)  // Pass non-null applicationInfo

                    // Update the UI elements
                    app?.apply {
                        setImageDrawable(appInfo.getIcon())
                        setText(appInfo.getName())
                        this.packageName = appInfo.getPackageName()
                    }
                } ?: run {
                    Log.w(TAG, "PackageInfo is null for package: $packageName")
                }
            } else {
                // Handle the case when the packageName is empty or null
                app?.apply {
                    setImageResource(R.drawable.ic_add)
                    setText("")
                    this.packageName = null
                }
            }
        } catch (e: Exception) {
            // Catch and log any unexpected errors
            Log.e(TAG, "Error setting application", e)
        }
    }

    override fun onStart() {
        super.onStart()
        setClock()
        if (mSetup.showBattery() && !mBatteryChangedReceiverRegistered) {
            activity?.registerReceiver(mBatteryChangedReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            mBatteryChangedReceiverRegistered = true
        }
        mHandler.postDelayed(mTimerTick, 1000)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(mTimerTick)
        if (mBatteryChangedReceiverRegistered) {
            activity?.unregisterReceiver(mBatteryChangedReceiver)
        }
    }

    private fun setClock() {
        val date = Date(System.currentTimeMillis())
        mClock.text = mTimeFormat.format(date)
        mDate.text = mDateFormat.format(date)
        mHandler.postDelayed(mTimerTick, 1000)
    }

    override fun onLongClick(v: View?): Boolean {
        if (v is ApplicationView) {
            val appView = v
            if (appView.hasPackage() && mSetup.iconsLocked()) {
                Toast.makeText(activity, R.string.home_locked, Toast.LENGTH_SHORT).show()
            } else {
                openApplicationList(ApplicationList.VIEW_LIST, appView.position, appView.hasPackage(), REQUEST_CODE_APPLICATION_LIST)
            }
            return true
        }
        return false
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.application_grid -> openApplicationList(ApplicationList.VIEW_GRID, 0, false, REQUEST_CODE_APPLICATION_START)
            R.id.settings -> startActivityForResult(Intent(context, PreferencesActivity::class.java), REQUEST_CODE_PREFERENCES)
            else -> {
                if (v is ApplicationView) {
                    openApplication(v)
                }
            }
        }
    }

    private fun openApplication(v: ApplicationView) {
        if (!v.hasPackage()) {
            openApplicationList(ApplicationList.VIEW_LIST, v.position, false, REQUEST_CODE_APPLICATION_LIST)
            return
        }

        try {
            Toast.makeText(activity, v.name, Toast.LENGTH_SHORT).show()
            startActivity(getLaunchIntentForPackage(v.packageName.toString()))
        } catch (e: Exception) {
            Toast.makeText(activity, "${v.name} : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openApplication(packageName: String) {
        try {
            val startApp = getLaunchIntentForPackage(packageName)
            Toast.makeText(activity, packageName, Toast.LENGTH_SHORT).show()
            startActivity(startApp)
        } catch (e: Exception) {
            Toast.makeText(activity, "$packageName : ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openApplicationList(viewType: Int, appNum: Int, showDelete: Boolean, requestCode: Int) {
        val intent = Intent(activity, ApplicationList::class.java).apply {
            putExtra(ApplicationList.APPLICATION_NUMBER, appNum)
            putExtra(ApplicationList.VIEW_TYPE, viewType)
            putExtra(ApplicationList.SHOW_DELETE, showDelete)
        }
        startActivityForResult(intent, requestCode)
    }

    private fun getLaunchIntentForPackage(packageName: String): Intent {
        val pm = activity?.packageManager ?: return Intent()
        var launchIntent = pm.getLaunchIntentForPackage(packageName)

        if (launchIntent == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            launchIntent = pm.getLeanbackLaunchIntentForPackage(packageName)
        }

        return launchIntent ?: Intent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_CODE_WALLPAPER -> {}
            REQUEST_CODE_PREFERENCES -> restartActivity()
            REQUEST_CODE_APPLICATION_START -> intent?.let {
                openApplication(it.extras?.getString(ApplicationList.PACKAGE_NAME) ?: "")
            }
            REQUEST_CODE_APPLICATION_LIST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val appNum = intent?.extras?.getInt(ApplicationList.APPLICATION_NUMBER) ?: return
                    val delete = intent.extras?.getBoolean(ApplicationList.DELETE) == true
                    val packageName = intent.extras?.getString(ApplicationList.PACKAGE_NAME)

                    if (delete) {
                        writePreferences(appNum, null)
                    } else {
                        writePreferences(appNum, packageName)
                    }
                    updateApplications()
                }
            }
        }
    }

    private fun restartActivity() {
        if (mBatteryChangedReceiverRegistered) {
            requireActivity().unregisterReceiver(mBatteryChangedReceiver)
            mBatteryChangedReceiverRegistered = false
        }
        val intent = requireActivity().getIntent()
        requireActivity().finish()
        startActivity(intent)
    }

    private fun writePreferences(appNum: Int, packageName: String?) {
        // Use PreferencesManager to handle SharedPreferences logic
        val key = ApplicationView.getPreferenceKey(appNum)
        preferencesManager.savePackageName(key, packageName)
    }

}