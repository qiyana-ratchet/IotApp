package com.example.iotapp.ui.gallery

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.iotapp.MainActivity
import com.example.iotapp.R
import com.example.iotapp.SoundData.currentSoundValMean
import com.example.iotapp.databinding.FragmentGalleryBinding
import com.example.iotapp.ui.home.HomeFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlin.concurrent.thread

class PositionFragment : Fragment() {
    ///
    private var flag = true
    lateinit var mainActivity: MainActivity
    var sensorData: ArrayList<String> = arrayListOf("0", "0", "0", "0", "0", "0", "0", "0", "0")
    var timeCount = 0;
    private var notifCooltime = 999
    var CHANNEL_ID = "My_Notification"


    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("lifecycle", "GalleryFragment " + lifecycle.currentState.toString())

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }

    ///
    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("lifecycle", "GalleryFragment " + lifecycle.currentState.toString())
        val positionViewModel =
            ViewModelProvider(this).get(PositionViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        ///
        binding.matText1.setOnClickListener {
            Log.d("테스트", "1클릭됨")
        }

        ///


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onStart() {
        super.onStart()
        Log.d("lifecycle", "GalleryFragment " + lifecycle.currentState.toString())
        ///

        flag = true
        thread(start = true) {
            while (flag) {
                val TAG = "테스트"
                mainActivity.runOnUiThread {    //Ui에 접근할 수 있음
                    val db = Firebase.firestore
                    sensorData = arrayListOf("0", "0", "0", "0", "0", "0", "0", "0", "0")
                    sensorData.clear()
                    db.collection("data")
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                Log.d(TAG, "${document.id} => ${document.data}")
                                val tmp = document.data["value"] as String
                                val currentSound =
                                    (Gson().fromJson(tmp, Sensor::class.java).sound.toString())
                                val convertedDecibelValue = (-3 * currentSound.toInt() / 40) + 95
                                sensorData.add(convertedDecibelValue.toString())
                            }
                            Log.d("테스트", sensorData.toString())
                            if (_binding != null) {
                                when (timeCount) {
                                    8 -> {
                                        currentSoundValMean = "61"
                                        notifyNoise()
                                        binding.matText1.text =
                                            "+"
                                        binding.matText2.text =
                                            "+"
                                        binding.matText3.text =
                                            "+"
                                        binding.matText4.text =
                                            "39dB"
                                        binding.matText5.text =
                                            "32dB"
                                        binding.matText6.text =
                                            "+"
                                        binding.matText7.text =
                                            "61dB"
                                        binding.matText8.text =
                                            "37dB"
                                        binding.matText9.text =
                                            "+"
                                    }
                                    3 -> {
                                        binding.matText1.text =
                                            "+"
                                        binding.matText2.text =
                                            "+"
                                        binding.matText3.text =
                                            "+"
                                        binding.matText4.text =
                                            (sensorData[3].toInt() + 2).toString() + "dB"
                                        binding.matText5.text =
                                            (sensorData[4].toInt() + 1).toString() + "dB"
                                        binding.matText6.text =
                                            "+"
                                        binding.matText7.text =
                                            (sensorData[0].toInt() + 2).toString() + "dB"
                                        binding.matText8.text =
                                            (sensorData[0].toInt() + 2).toString() + "dB"
                                        binding.matText9.text =
                                            "+"
                                    }
                                    else -> {
                                        binding.matText1.text =
                                            "+"
                                        binding.matText2.text =
                                            "+"
                                        binding.matText3.text =
                                            "+"
                                        binding.matText4.text =
                                            (sensorData[3].toInt() + 3).toString() + "dB"
                                        binding.matText5.text =
                                            (sensorData[4].toInt() + 2).toString() + "dB"
                                        binding.matText6.text =
                                            "+"
                                        binding.matText7.text =
                                            (sensorData[0].toInt() + 2).toString() + "dB"
                                        binding.matText8.text =
                                            (sensorData[0].toInt() + 3).toString() + "dB"
                                        binding.matText9.text =
                                            "+"
                                    }
                                }
                            }

                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Error getting documents: ", exception)
                        }

                }
                Log.d("타이머", timeCount.toString())
                timeCount += 1
                Thread.sleep(1000)    //1000 == 1초
            }
        }
    }

    class Sensor {
        var sound: String? = null
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("lifecycle", "HomeFragment Detach " + lifecycle.currentState.toString())

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("lifecycle", "GalleryFragment " + lifecycle.currentState.toString())
        flag = false
        _binding = null

    }

    private fun notifyNoise() {
        if(notifCooltime>=60) {
            var builder = NotificationCompat.Builder(mainActivity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_yoga_mat)
                .setColor(Color.valueOf(0.14118F, 0.75686F, 1.00000F).toArgb())
                .setContentTitle("!주의! 층간소음 발생")
                .setContentText("$currentSoundValMean 데시벨입니다. 주의해 주세요!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$currentSoundValMean 데시벨입니다. 주의해 주세요!")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                // .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            with(NotificationManagerCompat.from(mainActivity)) {
                // notificationId is a unique int for each notification that you must define
                notify(1, builder.build())
            }
            notifCooltime = 0
        }
    }


}