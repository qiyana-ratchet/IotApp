package com.example.iotapp.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.resources.Compatibility.Api18Impl.setAutoCancel
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.iotapp.MainActivity
import com.example.iotapp.R
import com.example.iotapp.SoundData.currentSoundValMean
import com.example.iotapp.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class HomeFragment : Fragment() {
    ///
    private var flag = true
    lateinit var mainActivity: MainActivity
    var CHANNEL_ID = "My_Notification"
    private var notifCooltime = 999

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }
    ///

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        flag = true
        thread(start = true) {
            while (flag) {
                mainActivity.runOnUiThread {    //Ui에 접근할 수 있음
                    notifCooltime+=1
                    val convertedDecibelValue = (-3 * currentSoundValMean.toInt() / 40) + 95
                    binding.soundValue.text = convertedDecibelValue.toString() + "dB"
                    binding.resultText.text = "수신중..."
                    var hourOfDay =
                        SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()).toString()
                            .substring(0, 1).toInt()
//                    var hourOfDay = 7
                    Log.d(
                        "테스트",
                        SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()).toString()
                    )

                    if (hourOfDay in 6..21) {
                        when {
                            convertedDecibelValue >= 70 -> {
                                binding.resultText.text = "아주 나쁨"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_very_dissatisfied_24)
                                notifyNoise()
                            }
                            convertedDecibelValue >= 57 -> {
                                binding.resultText.text = "나쁨"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_dissatisfied_24)
                                notifyNoise()
                            }
                            convertedDecibelValue >= 30 -> {
                                binding.resultText.text = "보통"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_satisfied_alt_24)
                            }
                            else -> {
                                binding.resultText.text = "좋음"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_satisfied_24)

                            }
                        }
                    } else {
                        when {
                            convertedDecibelValue >= 65 -> {
                                binding.resultText.text = "아주 나쁨"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_very_dissatisfied_24)
                                notifyNoise()
                            }
                            convertedDecibelValue >= 52 -> {
                                binding.resultText.text = "나쁨"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_dissatisfied_24)
                                notifyNoise()
                            }
                            convertedDecibelValue >= 30 -> {
                                binding.resultText.text = "보통"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_satisfied_alt_24)
                            }
                            else -> {
                                binding.resultText.text = "좋음"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_satisfied_24)

                            }
                        }
                    }

                    //
                    // Create an explicit intent for an Activity in your app
//                    val intent = Intent(this, HomeFragment::class.java).apply {
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    }
//                    val pendingIntent: PendingIntent = PendingIntent.getActivity(mainActivity, 0, intent,
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
                    if (convertedDecibelValue>52) {

                    }

                }
                //
                Thread.sleep(1000)    //1000 == 1초
            }
        }
    }

    private fun notifyNoise() {
        if(notifCooltime>=60) {
            var builder = NotificationCompat.Builder(mainActivity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_yoga_mat)
                .setColor(Color.valueOf(0.14118F, 0.75686F, 1.00000F).toArgb())
                .setContentTitle("IotApp")
                .setContentText("소음 발생이 감지되었습니다.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("아래층에 소음이 전해지고 있어요 :(")
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

    override fun onDestroyView() {
        super.onDestroyView()
        flag = false
        _binding = null
    }

}