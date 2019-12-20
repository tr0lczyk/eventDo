package com.rad4m.eventdo.ui.myaccountfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rad4m.eventdo.databinding.FragmentMyaccountBinding

class MyAccountFragment : Fragment() {

    private val viewModel: MyAccountViewModel by lazy {
        ViewModelProviders.of(this).get(MyAccountViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMyaccountBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.backNavigation.observe(this, Observer {
            if (it) {
                this.findNavController().navigateUp()
                viewModel.stopBackNavigation()
            }
        })

        return binding.root
    }
}