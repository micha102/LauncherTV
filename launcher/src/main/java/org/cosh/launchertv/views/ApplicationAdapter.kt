package org.cosh.launchertv.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import org.cosh.launchertv.AppInfo
import org.cosh.launchertv.R

class ApplicationAdapter(
    context: Context,
    private val mResource: Int,
    items: Array<AppInfo>
) : ArrayAdapter<AppInfo>(context, R.layout.list_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: View.inflate(context, mResource, null)

        val packageImage: ImageView = view.findViewById(R.id.application_icon)
        val packageName: TextView = view.findViewById(R.id.application_name)
        val appInfo = getItem(position)

        appInfo?.let { info ->
            view.tag = info // Explicitly setting the tag to `AppInfo` type
            packageName.text = info.getName()

            // Handling nullable `icon` field in AppInfo class
            info.getIcon().let { icon ->
                packageImage.setImageDrawable(icon) // Setting the icon if not null
            }
        }

        return view
    }
}