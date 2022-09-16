package com.example.iotapp.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
        flag=true
        thread(start = true) {
            while (flag) {
                mainActivity.runOnUiThread {    //Ui에 접근할 수 있음
                    val convertedDecibelValue = (-3 * currentSoundValMean.toInt() / 40) + 95
                    binding.soundValue.text = convertedDecibelValue.toString() + "dB"
                    binding.resultText.text = "수신중..."
                    var hourOfDay = SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()).toString().substring(0, 1).toInt()
//                    var hourOfDay = 7
                    Log.d("테스트",SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()).toString())
                    if (hourOfDay in 6..21) {
                        when {
                            convertedDecibelValue >= 70 -> {
                                binding.resultText.text = "아주 나쁨"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_very_dissatisfied_24)
                            }
                            convertedDecibelValue >= 57 -> {
                                binding.resultText.text = "나쁨"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_dissatisfied_24)
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
                            }
                            convertedDecibelValue >= 52 -> {
                                binding.resultText.text = "나쁨"
                                binding.soundFace.setImageResource(R.drawable.ic_baseline_sentiment_dissatisfied_24)
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
                }
                Thread.sleep(1000)    //1000 == 1초
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        flag=false
        _binding = null
    }

}