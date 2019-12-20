package com.rad4m.eventdo.ui.neweventpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rad4m.eventdo.R
import com.rad4m.eventdo.databinding.FragmentNewEventBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.ViewModelFactory
import javax.inject.Inject

class NewEventFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: NewEventViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewEventBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.setSelectedEvent(NewEventFragmentArgs.fromBundle(arguments!!).selectedEvent)
        activity!!.window.apply {
            statusBarColor = ContextCompat.getColor(activity!!, R.color.saturated_purple)
        }

        viewModel.startAddingNewEvent.observe(this, Observer {
            if (it) {
                Toast.makeText(activity, "Changes saved!", Toast.LENGTH_SHORT).show()
                viewModel.stopAddingNewEvent()
                findNavController().navigateUp()
            }
        })

        viewModel.cancelAddingNewEvent.observe(this, Observer {
            if (it) {
                findNavController().navigateUp()
                viewModel.stopCancelling()
            }
        })

        return binding.root
    }
}