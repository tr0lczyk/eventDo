package com.rad4m.eventdo.ui.mainfragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.R
import com.rad4m.eventdo.R.id.*
import com.rad4m.eventdo.databinding.FragmentMainBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.utils.HeaderItemDecoration
import com.rad4m.eventdo.utils.Utilities.Companion.EVENT_ID_NOTIFICATION
import com.rad4m.eventdo.utils.Utilities.Companion.ITEM_VIEW_TYPE_HEADER
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import com.rad4m.eventdo.utils.Utilities.Companion.TODAY_APP_START
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_ID
import com.rad4m.eventdo.utils.Utilities.Companion.makeStatusBarNotTransparent
import com.rad4m.eventdo.utils.Utilities.Companion.showDialog
import com.rad4m.eventdo.utils.Utilities.Companion.toastMessage
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.deleteCalendarEntry
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.openCalendar
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.saveCalEventContentResolver
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.saveEventToCalendar
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.verifyIfEventDeleted
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.verifyLastIntentEvent
import com.rad4m.eventdo.utils.ViewModelFactory
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import timber.log.Timber
import javax.inject.Inject

class MainFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private lateinit var drawer: DrawerLayout

    lateinit var binding: FragmentMainBinding

    private val RECORD_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        activity!!.makeStatusBarNotTransparent()
        drawer = binding.menuDrawer

        UIUtil.hideKeyboard(activity)
        val drawerToggle = object : ActionBarDrawerToggle(
            activity,
            binding.menuDrawer,
            binding.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ) {

        }
        drawerToggle.isDrawerIndicatorEnabled = true
        binding.menuDrawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
        UIUtil.hideKeyboard(activity)
        binding.recyclerEvents.itemAnimator = null

        val adapter = EventsAdapter(EventsAdapter.EventListener {
            onEventClick(it)
        })

        binding.recyclerEvents.adapter = adapter
        binding.recyclerEvents.addItemDecoration(HeaderItemDecoration(binding.recyclerEvents) {
            adapter.getItemViewType(it) == ITEM_VIEW_TYPE_HEADER
        })

        viewModel.eventList.observe(viewLifecycleOwner, Observer {
            if (viewModel.showUpcomingEvents.value!!) {
                viewModel.convertEventsToDataItems(it.sortedBy { event -> event.dtStart })
            } else {
                viewModel.convertEventsToDataItemsBackwards(it.sortedByDescending { event -> event.dtStart })
            }
        })

        viewModel.dataItemList.observe(this, Observer {
            adapter.submitList(it)
        })

        viewModel.showSnackBarFromNotification.observe(this, Observer { event ->
            if (event != null) {
                Snackbar.make(
                    binding.menuDrawer,
                    getString(R.string.events_added_to_calendar),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.show_calendar) { openCalendar(requireActivity(), event) }
                    .show()
                viewModel.showSnackBarFromNotification.value = null
            }
        })

        viewModel.permissionGranted.observe(this, Observer {
            if (it) {
                if (setupPermissions()) {
                    viewModel.downloadEvents()
                }
                viewModel.permissionGranted.value = false
                viewModel.swipeRefreshing.value = false
            }
        })
        return binding.root
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            infoItem -> goToEventDoPage()
            settingsItem -> this.findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToSettingsFragment()
            )
            accountItem -> this.findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToMyAccountFragment()
            )
            rateItem -> goToEventDoPage()
            shareItem -> shareAppContent()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun goToEventDoPage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(R.string.about_us_link))
        startActivity(intent)
    }

    private fun shareAppContent() {
        val chooseIntent = Intent(Intent.ACTION_SEND)
        chooseIntent.type = "text/plain"
        chooseIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.chooser_link))
        startActivity(Intent.createChooser(chooseIntent, getString(R.string.send_with)))
    }

    fun checkIfEventsDeleted() {
        if (setupPermissions()) {
            verifyIfEventDeleted()
        }
    }

    fun saveEventLocally(
        event: EventModel,
        activity: FragmentActivity
    ) {
        if (setupPermissions()) {
            showDialog(
                activity,
                getString(R.string.add_main_fragment) + event.title + getString(R.string.to_cal_main),
                getString(R.string.save_event_main_fragment),
                getString(R.string.add_main_fragment),
                {
                    saveAndshowSnackBar(
                        event,
                        activity
                    )
                },
                getString(R.string.cancel_main_fragment)
            )
        }
    }

    fun ifEventExist(
        event: EventModel,
        activity: FragmentActivity
    ) {
        if (setupPermissions()) {
            val deleteEntry = { deleteCalendarEntry(activity, event) }
            showDialog(
                activity,
                "${getString(R.string.event)} ${getString(R.string.already_there)}",
                "${event.title}", getString(R.string.delete_main),
                deleteEntry,
                getString(R.string.cancel_main_fragment)
            )
        }
    }

    private fun saveAndshowSnackBar(
        event: EventModel,
        activity: FragmentActivity
    ) {
        saveCalEventContentResolver(
            event,
            activity
        )
        Snackbar.make(
            binding.menuDrawer,
            getString(R.string.events_added_to_calendar),
            Snackbar.LENGTH_LONG
        )
            .setAction(
                R.string.show_calendar
            ) {
                openCalendar(activity, event)
            }.show()
    }

    fun saveEventToCalendarExternal(
        event: EventModel,
        activity: FragmentActivity
    ) {
        if (setupPermissions()) {
            saveEventToCalendar(
                event, activity,
                viewModel.sharedPrefs.getValueString(
                    USER_MAIN_CALENDAR_ID
                )
            )
        }
    }

    private fun eventNotSaved(event: EventModel) {
        if (viewModel.sharedPrefs.getValueBoolean(NEW_EVENT_PAGE) == true) {
            saveEventToCalendarExternal(
                event,
                activity!!
            )
        } else {
            saveEventLocally(
                event,
                activity!!
            )
        }
    }

    fun startService(){
        requireActivity().startService(
            Intent(
                EventDoApplication.instance,
                NetworkService::class.java
            )
        )
    }

    fun onEventClick(event: EventModel) {
        if (setupPermissions()) {
            when (event.localEventId) {
                null -> eventNotSaved(event)
                else -> ifEventExist(event, activity!!)
            }
        }
    }


    fun verifyLastIntent() {
        if (setupPermissions()) {
            verifyLastIntentEvent(activity!!)
        }
    }

    override fun onStart() {
        super.onStart()
        verifyLastIntent()
        checkIfEventsDeleted()
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.sharedPrefs.getValueString(EVENT_ID_NOTIFICATION).isNullOrBlank()) {
            viewModel.executrWorkStart(
                viewModel.sharedPrefs.getValueString(EVENT_ID_NOTIFICATION)!!.toInt()
            ).invokeOnCompletion {
                viewModel.downloadEvents()
            }
            viewModel.sharedPrefs.removeValue(EVENT_ID_NOTIFICATION)
        } else {
            if (setupPermissions()) {
                startService()
            }
        }
    }

    private fun setupPermissions(): Boolean {
        val permissions = mutableListOf<Int>()
        permissions.add(
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_CALENDAR
            )
        )

        permissions.add(
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CALENDAR
            )
        )
        for (i in permissions) {
            if (i != PackageManager.PERMISSION_GRANTED) {
                Timber.i("Permission to record denied")
                makeRequest()
                return false
            }
        }
        return true
    }

    private fun daniel() {
        toastMessage(requireContext(), R.string.calendar_access_denied)
    }

    private fun makeRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR),
            RECORD_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (permissions.isNotEmpty() && grantResults.get(0) != 0 && grantResults.get(1) != 0) {
            daniel()
        } else if (permissions.isNotEmpty() && grantResults.get(0) == 0 && grantResults.get(1) == 0) {
            startService()
            this.findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToSettingsFragment("openPopUp")
            )
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().stopService(
            Intent(
                EventDoApplication.instance,
                NetworkService::class.java
            )
        )
    }
}