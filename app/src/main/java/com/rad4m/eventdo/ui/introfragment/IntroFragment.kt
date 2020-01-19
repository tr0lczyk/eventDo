package com.rad4m.eventdo.ui.introfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.rad4m.eventdo.databinding.FragmentIntroBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.SharedPreferences
import com.rad4m.eventdo.utils.Utilities.Companion.USER_LOGOUT
import com.rad4m.eventdo.utils.Utilities.Companion.makeStatusBarTransparent
import javax.inject.Inject

class IntroFragment : Fragment() {

    private val viewModel: IntroViewModel by lazy {
        ViewModelProviders.of(this).get(IntroViewModel::class.java)
    }

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentIntroBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.viewPager.adapter = IntroAdapter()
        if (sharedPrefs.getValueBoolean(USER_LOGOUT) != null) {
            if (sharedPrefs.getValueBoolean(USER_LOGOUT)!!) {
                activity!!.makeStatusBarTransparent()
                sharedPrefs.save(USER_LOGOUT, false)
            }
        }
        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            }).attach()

        viewModel.navigateToSignUp.observe(this, Observer {
            if (it) {
                this.findNavController()
                    .navigate(IntroFragmentDirections.actionIntroFragmentToSignUpFragment())
                viewModel.navigateToSignUp.value = false
            }
        })
        return binding.root
    }
}