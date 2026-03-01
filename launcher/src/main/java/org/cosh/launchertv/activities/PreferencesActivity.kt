package org.cosh.launchertv.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.cosh.launchertv.fragments.PreferencesFragment

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, PreferencesFragment())
            .commit()
    }

    override fun onDestroy() {
        setResult(RESULT_OK)
        super.onDestroy()
    }
}