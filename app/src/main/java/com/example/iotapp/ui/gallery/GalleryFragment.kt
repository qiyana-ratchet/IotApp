package com.example.iotapp.ui.gallery

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.example.iotapp.MainActivity
import com.example.iotapp.R
import com.example.iotapp.databinding.FragmentGalleryBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlin.concurrent.thread

class GalleryFragment : Fragment() {
    ///
    private var flag = true
    lateinit var mainActivity: MainActivity
    var sensorData: ArrayList<String> = arrayListOf("0", "0", "0", "0", "0", "0", "0", "0", "0")

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
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

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
                                binding.matText1.text =
                                    (sensorData[0].toInt() + 1).toString() + "dB"
                                binding.matText2.text =
                                    (sensorData[1].toInt() - 2).toString() + "dB"
                                binding.matText3.text =
                                    (sensorData[2].toInt() - 4).toString() + "dB"
                                binding.matText4.text =
                                    (sensorData[3].toInt() + 3).toString() + "dB"
                                binding.matText5.text =
                                    (sensorData[4].toInt() + 2).toString() + "dB"
                                binding.matText6.text =
                                    (sensorData[5].toInt() + 1).toString() + "dB"
                                binding.matText7.text =
                                    (sensorData[0].toInt() + 2).toString() + "dB"
                                binding.matText8.text =
                                    (sensorData[0].toInt() + 3).toString() + "dB"
                                binding.matText9.text =
                                    (sensorData[0].toInt() - 1).toString() + "dB"
                            }

                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Error getting documents: ", exception)
                        }

                }
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
}