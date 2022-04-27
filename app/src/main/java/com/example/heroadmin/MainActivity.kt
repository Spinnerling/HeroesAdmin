package com.example.heroadmin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.heroadmin.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    // Initialize the binding object
    private lateinit var binding: ActivityMainBinding
    lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
    }
}