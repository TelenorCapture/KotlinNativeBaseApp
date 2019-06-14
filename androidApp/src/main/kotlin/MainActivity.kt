package tsl.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("app", "Platform name: " + tsl.baseLib.platformName())
        Log.d("app", "JSON: " + tsl.baseLib.generateJson())
        tsl.baseLib.useTestDatabase()
    }
}
