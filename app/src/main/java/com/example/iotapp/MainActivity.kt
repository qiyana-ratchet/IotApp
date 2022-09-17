package com.example.iotapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.iotapp.SoundData.currentSoundValMean
import com.example.iotapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var TAG = "테스트"
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() //스플래시
        super.onCreate(savedInstanceState)
        //
        val db = Firebase.firestore
        db.collection("data")
            .whereEqualTo("TagSnapshot", "1")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val sensorData = ArrayList<String>()
                for (doc in value!!) {
                    doc.getString("value")?.let {
                        sensorData.add(Gson().fromJson(it,Sensor::class.java).sound.toString())

                        currentSoundValMean = meanOfSound(sensorData).toString()
                        Log.d(TAG, "Current currentSoundValMean: $currentSoundValMean")


                    }
                }
                Log.d(TAG, "Current Sensor Sound Data: $sensorData")
            }

        //
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ///

        ///

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    fun meanOfSound(list: ArrayList<String>): Int {
        var sum = 0
        for (num in list) {
            sum += num.toInt()
        }
        return sum/(list.size)
    }
    class Sensor {
        var sound: String? = null
    }
}