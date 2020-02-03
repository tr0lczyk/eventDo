package com.rad4m.eventdo.ui.mainfragment

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.rad4m.eventdo.R
import com.rad4m.eventdo.R.id.accountItem
import com.rad4m.eventdo.R.id.infoItem
import com.rad4m.eventdo.R.id.rateItem
import com.rad4m.eventdo.R.id.settingsItem
import com.rad4m.eventdo.R.id.shareItem
import com.rad4m.eventdo.databinding.FragmentMainBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.utils.HeaderItemDecoration
import com.rad4m.eventdo.utils.Utilities.Companion.ITEM_VIEW_TYPE_HEADER
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import com.rad4m.eventdo.utils.Utilities.Companion.USER_MAIN_CALENDAR_ID
import com.rad4m.eventdo.utils.Utilities.Companion.showDialog
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.deleteCalendarEntry
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.getEventIdList
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.openCalendar
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.saveCalEventContentResolver
import com.rad4m.eventdo.utils.ViewModelFactory
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@RuntimePermissions
class MainFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private lateinit var drawer: DrawerLayout

    lateinit var binding: FragmentMainBinding

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
        activity!!.window.apply {
            statusBarColor = Color.WHITE
        }
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
        downloadALlEventsWithPermissionCheck()
        drawerToggle.isDrawerIndicatorEnabled = true
        binding.menuDrawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
        UIUtil.hideKeyboard(activity)

        val adapter = EventsAdapter(EventsAdapter.EventListener {
            onEventClickWithPermissionCheck(it)
        })

        binding.recyclerEvents.adapter = adapter
        binding.recyclerEvents.addItemDecoration(HeaderItemDecoration(binding.recyclerEvents) {
            adapter.getItemViewType(it) == ITEM_VIEW_TYPE_HEADER
        })

        viewModel.selectedEvent.observe(this, Observer {
            if (it != null) {
                findNavController().navigate(
                    MainFragmentDirections.actionMainFragmentToNewEventFragment(
                        it
                    )
                )
                viewModel.finishSelectedEvent()
            }
        })

        viewModel.eventList.observe(viewLifecycleOwner, Observer {
            viewModel.convertEventsToDataItems(it.sortedBy { event -> event.dtStart })
        })

        viewModel.dataItemList.observe(this, Observer {
            adapter.submitList(it)
            Handler().postDelayed({
                binding.recyclerEvents.layoutManager!!.smoothScrollToPosition(
                    binding.recyclerEvents,
                    null,
                    0
                )
            }, 100)
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

    @NeedsPermission(
        Manifest.permission.READ_CALENDAR
    )
    fun downloadALlEvents() {
        viewModel.saveNewEventIdTitleList(getEventIdList(activity!!))
    }

    @NeedsPermission(
        Manifest.permission.WRITE_CALENDAR
    )
    fun saveEventLocally(
        event: EventModel,
        activity: FragmentActivity,
        calendarId: String?
    ) {
        showDialog(
            activity,
            "Add ${event.title} to the calendar?",
            "Save event",
            "Add",
            {
                saveAndshowSnackBar(
                    event,
                    activity, calendarId
                )
            },
            "Cancel"
        )
    }

    @NeedsPermission(
        Manifest.permission.WRITE_CALENDAR
    )
    fun ifEventExist(
        event: EventModel,
        activity: FragmentActivity
    ) {
        val eventId = viewModel.returnEventId(event.title!!)
        showDialog(
            activity,
            "Event is already in calendar",
            "eventDo", "DELETE",
            { deleteCalendarEntry(activity, eventId) },
            "Cancel"
        )
    }

    private fun saveAndshowSnackBar(
        event: EventModel,
        activity: FragmentActivity,
        calendarId: String?
    ) {
        saveCalEventContentResolver(
            event,
            activity,
            calendarId
        )
        Snackbar.make(binding.menuDrawer, R.string.event_saved, Snackbar.LENGTH_LONG).setAction(
            R.string.show_calendar
        ) {
            openCalendar(activity, event)
        }.show()
    }

    @OnPermissionDenied(
        Manifest.permission.WRITE_CALENDAR
    )
    fun onDenied() {
        Toast.makeText(
            activity,
            getString(R.string.denied_calendar_access_text),
            Toast.LENGTH_LONG
        )
            .show()
    }

    @OnPermissionDenied(
        Manifest.permission.READ_CALENDAR
    )
    fun onDenied2() {
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

    @NeedsPermission(
        Manifest.permission.READ_CALENDAR
    )
    fun onEventClick(event: EventModel) {
        if (viewModel.sharedPrefs.getValueBoolean(NEW_EVENT_PAGE) == true) {
            viewModel.navigateToSelectedEvent(event)
        } else {
            if (viewModel.doesEventExists(event.title!!)) {
                ifEventExistWithPermissionCheck(event, activity!!)
            } else {
                saveEventLocallyWithPermissionCheck(
                    event,
                    activity!!,
                    viewModel.sharedPrefs.getValueString(
                        USER_MAIN_CALENDAR_ID
                    )
                )
            }
        }
    }
}