package ir.kitgroup.distributor.core.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val config = newBase.resources.configuration
        config.setLayoutDirection(Locale.ENGLISH) // همیشه LTR
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}