package com.rad4m.eventdo.ui.mainfragment

import android.Manifest
import android.content.Intent
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
import com.rad4m.eventdo.utils.Utilities.Companion.makeStatusBarNotTransparent
import com.rad4m.eventdo.utils.Utilities.Companion.showDialog
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.deleteCalendarEntry
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.openCalendar
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.saveCalEventContentResolver
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.saveEventToCalendar
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.verifyIfEventDeleted
import com.rad4m.eventdo.utils.UtilitiesCalendar.Companion.verifyLastIntentEvent
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

        val adapter = EventsAdapter(EventsAdapter.EventListener {
            onEventClickWithPermissionCheck(it)
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
//            Handler().postDelayed({
//                binding.recyclerEvents.layoutManager!!.smoothScrollToPosition(
//                    binding.recyclerEvents,
//                    null,
//                    0
//                )
//            }, 100)
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
    fun checkIfEventsDeleted() {
        verifyIfEventDeleted()
    }

    @NeedsPermission(
        Manifest.permission.WRITE_CALENDAR
    )
    fun saveEventLocally(
        event: EventModel,
        activity: FragmentActivity
    ) {
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

    @NeedsPermission(
        Manifest.permission.WRITE_CALENDAR
    )
    fun ifEventExist(
        event: EventModel,
        activity: FragmentActivity
    ) {
        val deleteEntry = { deleteCalendarEntry(activity, event) }
        showDialog(
            activity,
            "${getString(R.string.event)} ${event.title} ${getString(R.string.already_there)}",
            getString(R.string.app_name), getString(R.string.delete_main),
            deleteEntry,
            getString(R.string.cancel_main_fragment)
        )
    }

    private fun saveAndshowSnackBar(
        event: EventModel,
        activity: FragmentActivity
    ) {
        saveCalEventContentResolver(
            event,
            activity
        )
        Snackbar.make(binding.menuDrawer, "Event is already in calendar", Snackbar.LENGTH_LONG)
            .setAction(
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
        Manifest.permission.WRITE_CALENDAR
    )
    fun saveEventToCalendarExternal(
        event: EventModel,
        activity: FragmentActivity
    ) {
        saveEventToCalendar(
            event, activity,
            viewModel.sharedPrefs.getValueString(
                USER_MAIN_CALENDAR_ID
            )
        )
    }

    private fun eventNotSaved(event: EventModel) {
        if (viewModel.sharedPrefs.getValueBoolean(NEW_EVENT_PAGE) == true) {
            saveEventToCalendarExternalWithPermissionCheck(
                event,
                activity!!
            )
        } else {
            saveEventLocallyWithPermissionCheck(
                event,
                activity!!
            )
        }
    }

    @NeedsPermission(
        Manifest.permission.READ_CALENDAR
    )
    fun onEventClick(event: EventModel) {
        when (event.localEventId) {
            null -> eventNotSaved(event)
            else -> ifEventExistWithPermissionCheck(event, activity!!)
        }
    }

    @NeedsPermission(
        Manifest.permission.READ_CALENDAR
    )
    fun verifyLastIntent() {
        verifyLastIntentEvent(activity!!)
    }

    override fun onStart() {
        super.onStart()
        verifyLastIntentWithPermissionCheck()
        checkIfEventsDeletedWithPermissionCheck()
    }
}