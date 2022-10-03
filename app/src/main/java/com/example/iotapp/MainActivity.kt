package com.example.iotapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.iotapp.SoundData.currentSoundValMean
import com.example.iotapp.SoundData.isNotify
import com.example.iotapp.databinding.ActivityMainBinding
import com.example.iotapp.ui.home.HomeFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var TAG = "테스트"
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // 스플래시
        createNotificationChannel() // 알림 등록
        super.onCreate(savedInstanceState)

        val db = Firebase.firestore
        clearOldData(db)
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
                        sensorData.add(Gson().fromJson(it, Sensor::class.java).sound.toString())

                        currentSoundValMean = meanOfSound(sensorData).toString()
                        Log.d(TAG, "Current sensor value: $currentSoundValMean")


                    }
                }
                Log.d(TAG, "Current sensor data list: $sensorData")
            }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            if (isNotify) {
                isNotify = false
                Snackbar.make(view, "이제부터 알림을 수신하지 않습니다.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                binding.appBarMain.fab.backgroundTintList = ContextCompat.getColorStateList(this,R.color.notif_button_off)

            }else{
                isNotify = true
                Snackbar.make(view, "알림을 수신합니다.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                binding.appBarMain.fab.backgroundTintList = ContextCompat.getColorStateList(this,R.color.notif_button_on)

            }
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_vod
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun clearOldData(db: FirebaseFirestore) {
        db.collection("timeData")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    if (isOldData(document)) {
                        db.collection("timeData").document(document.id)
                            .delete().addOnSuccessListener {
                                Log.d("오래된", "deleted")
                            }
                        Log.d("오래된", document.id)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

    }

    private fun isOldData(document: QueryDocumentSnapshot): Boolean {
        var year = document.id.substring(0, 4).toInt()
        var month = document.id.substring(5, 7).toInt() - 1
        var day = document.id.substring(8, 10).toInt()

        val beginDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }.timeInMillis

        val year2 = Calendar.getInstance().get(Calendar.YEAR)
        val month2 = Calendar.getInstance().get(Calendar.MONTH)
        val day2 = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance().apply {
            set(Calendar.YEAR, year2)
            set(Calendar.MONTH, month2)
            set(Calendar.DAY_OF_MONTH, day2)
        }.timeInMillis


        val dateDif = getIgnoredTimeDays(today) - getIgnoredTimeDays(beginDay)
        Log.d("오래된", "$year, $month, $day, $year2, $month2, $day2, $beginDay, $today, $dateDif, ")

        return dateDif > 1;
    }

    private fun getIgnoredTimeDays(time: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = time

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
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
        return sum / (list.size)
    }

    class Sensor {
        var sound: String? = null
    }

    var CHANNEL_ID = "My_Notification"

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}