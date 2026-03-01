package org.cosh.launchertv.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.StateSet
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.cosh.launchertv.R
import org.cosh.launchertv.Setup
import org.cosh.launchertv.PreferencesManager
import java.util.*

class ApplicationView(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private var mMenuClickListener: OnClickListener? = null
    private lateinit var mIcon: ImageView
    private lateinit var mText: TextView
    private var mPackageName: String? = null
    private var mPosition: Int = 0
    private lateinit var preferencesManager: PreferencesManager

    // Public accessors for name, package, and position
    var name: String
        get() = mText.text.toString()
        set(value) {
            mText.text = value
        }

    var packageName: String?
        get() = mPackageName
        set(value) {
            mPackageName = value
        }

    var position: Int
        get() = mPosition
        set(value) {
            mPosition = value
        }

    init {
        initialize(context, attrs, defStyle)
    }

    companion object {
        private const val TAG = "ApplicationView"

        fun getPreferenceKey(appNum: Int): String {
            return String.format(Locale.getDefault(), "application_%02d", appNum)
        }
    }

    private fun createTileShape(backgroundColor: Int, borderColor: Int): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(7f, 7f, 7f, 7f, 0f, 0f, 0f, 0f)
            setColor(backgroundColor)
            setStroke(1, borderColor)
            setBounds(7, 7, 7, 7)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.d(TAG, "keyCode => $keyCode")
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mMenuClickListener?.onClick(this)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun setOnMenuOnClickListener(clickListener: OnClickListener?) {
        mMenuClickListener = clickListener
    }

    private fun setBackgroundStateDrawable(transparency: Float) {
        val stateListDrawable = StateListDrawable()

        val drawableEnabled = createTileShape(
            Color.argb(getTransparency(transparency, 0.0f), 0xF0, 0xF0, 0xF0),
            Color.argb(0xFF, 0x90, 0x90, 0x90)
        )
        val drawableFocused = createTileShape(
            Color.argb(getTransparency(transparency, 0.4f), 0xE0, 0xE0, 0xFF),
            Color.argb(0xFF, 0x90, 0x90, 0x90)
        )
        val drawablePressed = createTileShape(
            Color.argb(getTransparency(transparency, 0.8f), 0xE0, 0xE0, 0xFF),
            Color.argb(0xFF, 0x00, 0x00, 0x00)
        )

        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePressed)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_focused), drawableFocused)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_hovered), drawableFocused)
        stateListDrawable.addState(StateSet.WILD_CARD, drawableEnabled)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            background = stateListDrawable
        } else {
            //noinspection deprecation
            setBackgroundDrawable(stateListDrawable)
        }
    }

    private fun getTransparency(transparency: Float, add: Float): Int {
        val trans = ((transparency + add) * 255.0).toInt()
        return trans.coerceIn(0, 255)
    }

    private fun initialize(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val setup = Setup(context)
        preferencesManager = PreferencesManager(context)

        inflate(context, R.layout.application, this)

        isClickable = true
        isFocusable = true

        if (!setup.isDefaultTransparency()) {
            setBackgroundStateDrawable(setup.getTransparency())
        } else {
            setBackgroundResource(R.drawable.application_selector)
        }

        mIcon = findViewById(R.id.application_icon)
        mText = findViewById(R.id.application_name)
    }

    fun setImageResource(res: Int): ApplicationView {
        mIcon.setImageResource(res)
        return this
    }

    fun setImageDrawable(drawable: Drawable?): ApplicationView {
        mIcon.setImageDrawable(drawable)
        return this
    }

    fun setText(text: CharSequence): ApplicationView {
        mText.text = text
        return this
    }

    fun showName(show: Boolean) {
        mText.visibility = if (show) VISIBLE else GONE
    }

    fun hasPackage(): Boolean {
        return !TextUtils.isEmpty(mPackageName)
    }

}