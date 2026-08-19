package com.maxxcodebug.maxxequalizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CustomPresetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.maxxcodebug.maxxequalizer.ui.AmoledThemeHelper.applyIfNeeded(this)
        setContentView(R.layout.activity_custom_presets)

        findViewById<android.widget.ImageButton>(R.id.presetsBackButton).setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}
