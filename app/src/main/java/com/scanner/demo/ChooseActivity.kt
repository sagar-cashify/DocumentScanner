package com.scanner.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ChooseActivity : AppCompatActivity() {
lateinit var btn_file : Button
lateinit var btn_cam : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        btn_file.setOnClickListener {


        }


        btn_cam.setOnClickListener {


        }
    }
}
