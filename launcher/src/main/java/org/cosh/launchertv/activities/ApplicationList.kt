package org.cosh.launchertv.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import kotlinx.coroutines.*
import org.cosh.launchertv.AppInfo
import org.cosh.launchertv.R
import org.cosh.launchertv.Utils
import org.cosh.launchertv.views.ApplicationAdapter

class ApplicationList : Activity(),
    AdapterView.OnItemClickListener,
    View.OnClickListener {

    companion object {
        const val PACKAGE_NAME = "package_name"
        const val APPLICATION_NUMBER = "application"
        const val VIEW_TYPE = "view_type"
        const val DELETE = "delete"
        const val SHOW_DELETE = "show_delete"

        const val VIEW_GRID = 0
        const val VIEW_LIST = 1
    }

    private var applicationIndex: Int = -1
    private var viewType: Int = VIEW_GRID
    private lateinit var listView: AbsListView

    // Coroutine scope for launching background tasks
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the view type and application number from the intent extras
        intent.extras?.let { args ->
            if (args.containsKey(APPLICATION_NUMBER))
                applicationIndex = args.getInt(APPLICATION_NUMBER)

            if (args.containsKey(VIEW_TYPE))
                viewType = args.getInt(VIEW_TYPE)
        }

        // Set the content view based on view type
        setContentView(
            if (viewType == VIEW_LIST)
                R.layout.listview
            else
                R.layout.gridview
        )

        // Initialize the listView
        listView = findViewById(R.id.list)

        // Launch the coroutine to load the applications
        loadApplications()

        // Handle visibility of bottom panel if needed
        intent.extras?.let { args ->
            if (args.containsKey(SHOW_DELETE) && !args.getBoolean(SHOW_DELETE)) {
                findViewById<View?>(R.id.bottom_panel)?.visibility = View.GONE
            }
        }

        // Set click listeners
        findViewById<View?>(R.id.delete)?.setOnClickListener(this)
        findViewById<View?>(R.id.cancel)?.setOnClickListener(this)
    }

    // Coroutine to load applications
    private fun loadApplications() {
        coroutineScope.launch {
            // Run the background work on the IO thread
            val apps = withContext(Dispatchers.IO) {
                Utils.loadApplications(this@ApplicationList).toTypedArray()
            }

            // Once the background task is complete, update the UI on the main thread
            listView.onItemClickListener = this@ApplicationList
            listView.adapter = ApplicationAdapter(
                this@ApplicationList,
                if (viewType == VIEW_LIST) R.layout.list_item else R.layout.grid_item,
                apps
            )
        }
    }

    // Handle item click events
    override fun onItemClick(
        parent: AdapterView<*>,
        view: View,
        position: Int,
        id: Long
    ) {
        val appInfo = view.tag as AppInfo

        val data = Intent().apply {
            putExtra(PACKAGE_NAME, appInfo.getPackageName())
            putExtra(APPLICATION_NUMBER, applicationIndex)
        }

        setResult(RESULT_OK, data)
        finish()
    }

    // Handle button click events
    override fun onClick(v: View) {
        when (v.id) {
            R.id.delete -> {
                val data = Intent().apply {
                    putExtra(DELETE, true)
                    putExtra(APPLICATION_NUMBER, applicationIndex)
                }

                // Set the result for the current activity
                setResult(RESULT_OK, data)

                finish()
            }

            R.id.cancel -> {
                // Set the result for the current activity
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    // Clean up the coroutine when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel any ongoing coroutines
    }
}