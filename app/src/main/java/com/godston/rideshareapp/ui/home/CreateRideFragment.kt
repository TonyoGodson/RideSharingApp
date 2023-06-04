package com.godston.rideshareapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.godston.rideshareapp.R
import com.godston.rideshareapp.databinding.LayoutCreateRideBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CreateRideFragment : BottomSheetDialogFragment() {

    private lateinit var _binding: LayoutCreateRideBinding
    private val binding get() = _binding!!
    companion object {
        fun newInstance(): CreateRideFragment {
            return CreateRideFragment()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutCreateRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeRide.setOnClickListener {
            dismiss()
        }
    }
}
