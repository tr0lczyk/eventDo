package com.rad4m.eventdo.ui.settingsfragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rad4m.eventdo.R
import com.rad4m.eventdo.databinding.FragmentSettingsBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.Utilities.Companion.AUTO_ADD_EVENT
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_NAME
import com.rad4m.eventdo.utils.ViewModelFactory
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@RuntimePermissions
class SettingsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: SettingsViewModel by viewModels { viewModelFactory }

    lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        downloadCalWithPermissionCheck()
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

    @NeedsPermission(Manifest.permission.READ_CALENDAR)
    fun downloadCal() {
        val adapter =
            ArrayAdapter(
                activity!!,
                android.R.layout.simple_spinner_item,
                viewModel.getCalendarsIds(activity!!)
            )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.calendarSpinner.adapter = adapter
        binding.calendarSpinner.onItemSelectedListener =
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
                    viewModel.saveMainCalendar(viewModel.searchForCal(calName!!))
                }
            }
        if (viewModel.sharedPrefs.getValueString(USER_MAIN_CALENDAR_NAME) != null) {
            val position =
                adapter.getPosition(viewModel.sharedPrefs.getValueString(USER_MAIN_CALENDAR_NAME))
            binding.calendarSpinner.setSelection(
                position
            )
        }
    }

    @OnPermissionDenied(
        Manifest.permission.READ_CALENDAR
    )
    fun onDenied() {
        Toast.makeText(
            activity,
            getString(R.string.denied_calendar_access_text),
            Toast.LENGTH_LONG
        )
            .show()
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