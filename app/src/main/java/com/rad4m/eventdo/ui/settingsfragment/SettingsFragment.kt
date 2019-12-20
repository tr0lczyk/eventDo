package com.rad4m.eventdo.ui.settingsfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rad4m.eventdo.databinding.FragmentSettingsBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.Utilities.Companion.AUTO_ADD_EVENT
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import com.rad4m.eventdo.utils.ViewModelFactory
import javax.inject.Inject

class SettingsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: SettingsViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSettingsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.backNavigation.observe(this, Observer {
            if (it) {
                this.findNavController().navigateUp()
                viewModel.stopBackNavigation()
            }
        })

        viewModel.isNewEventPageOn.observe(this, Observer {
            viewModel.sharedPrefs.save(NEW_EVENT_PAGE, it)
        })

        viewModel.isAutoAddEventOn.observe(this, Observer {
            viewModel.sharedPrefs.save(AUTO_ADD_EVENT, it)
        })
        return binding.root
    }
}