package com.rad4m.eventdo.ui.neweventpage

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rad4m.eventdo.R
import com.rad4m.eventdo.databinding.FragmentNewEventBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_NAME
import com.rad4m.eventdo.utils.ViewModelFactory
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@RuntimePermissions
class NewEventFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: NewEventViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmentNewEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewEventBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        downloadCalWithPermissionCheck()
        showAsSpinner()
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

    @NeedsPermission(Manifest.permission.READ_CALENDAR)
    fun downloadCal() {
        val adapter =
            ArrayAdapter(
                activity!!,
                android.R.layout.simple_spinner_item,
                viewModel.getCalendarsIds(activity!!)
            )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.calendarNewEventSpinner.adapter = adapter
        binding.calendarNewEventSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val calName = adapter.getItem(position)
                }
            }
        if (viewModel.sharedPrefs.getValueString(USER_MAIN_CALENDAR_NAME) != null) {
            val position =
                adapter.getPosition(viewModel.sharedPrefs.getValueString(USER_MAIN_CALENDAR_NAME))
            binding.calendarNewEventSpinner.setSelection(
                position
            )
        }
    }

    fun showAsSpinner() {
        val adapter =
            ArrayAdapter(
                activity!!,
                android.R.layout.simple_spinner_item,
                listOf("Busy", "Free")
            )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.showAsContentSpinner.adapter = adapter
        binding.showAsContentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.eventShowAs.value = adapter.getItem(position)
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
}