package com.example.heroadmin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.heroadmin.databinding.FragmentLevelUpBinding
import com.example.heroadmin.databinding.FragmentMockBinding

class LevelUpFragment : Fragment() {
    private lateinit var binding: FragmentLevelUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_level_up, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }


}