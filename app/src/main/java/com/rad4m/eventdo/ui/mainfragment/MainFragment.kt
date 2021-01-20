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
import com.rad4m.eventdo.EventDoApplication
import com.rad4m.eventdo.R
import com.rad4m.eventdo.R.id.*
import com.rad4m.eventdo.databinding.FragmentMainBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.utils.HeaderItemDecoration
import com.rad4m.eventdo.utils.Utilities
import com.rad4m.eventdo.utils.Utilities.Companion.EVENT_ID_NOTIFICATION
import com.rad4m.eventdo.utils.Utilities.Companion.ITEM_VIEW_TYPE_HEADER
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import com.rad4m.eventdo.utils.Utilities.Companion.TODAY_APP_START
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
import timber.log.Timber
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.sharedPrefs.getValueBoolean(TODAY_APP_START) == false) {
            permissionToReadWithPermissionCheck()
            Handler().postDelayed({
                permissionToWriteWithPermissionCheck()
                viewModel.sharedPrefs.save(TODAY_APP_START, true)
            }, 5000)
        }
//        if (!viewModel.sharedPrefs.getValueString(EVENT_ID_NOTIFICATION).isNullOrBlank()) {
//            viewModel.downloadEvents()
//            Handler(Looper.getMainLooper()).postDelayed({
//                viewModel.viewModelScope.launch {
//                    val eventModel = viewModel.getOneEvent(
//                        viewModel.sharedPrefs.getValueString(
//                            EVENT_ID_NOTIFICATION
//                        )!!
//                    )
//                    onEventClickWithPermissionCheck(eventModel)
//                    viewModel.sharedPrefs.removeValue(EVENT_ID_NOTIFICATION)
//                }
//            }, 3000)
//        }
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
    fun permissionToWrite() {
        Timber.i("WRITE aability")
    }

    @NeedsPermission(
        Manifest.permission.READ_CALENDAR
    )
    fun permissionToRead() {
        Timber.i("READ aability")
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

    @OnPermissionDenied(
        Manifest.permission.WRITE_CALENDAR
    )
    fun onDenied() {
//        Toast.makeText(
//            activity,
//            getString(R.string.denied_calendar_access_text_write),
//            Toast.LENGTH_LONG
//        )
//            .show()
        Timber.i("write calendar toast")
    }

    @OnPermissionDenied(
        Manifest.permission.READ_CALENDAR
    )
    fun onDenied2() {
//        Toast.makeText(
//            activity,
//            getString(R.string.denied_calendar_access_text_read),
//            Toast.LENGTH_LONG
//        )
//            .show()
        Timber.i("read calendar toast")
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var delay = 0L
        if (viewModel.sharedPrefs.getValueBoolean(Utilities.NOT_FIRST_START) == false) {
            delay = 5L
        }
        Handler().postDelayed({
            requireActivity().startService(
                Intent(
                    EventDoApplication.instance,
                    NetworkService::class.java
                )
            )
        }, delay)
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.sharedPrefs.removeValue(TODAY_APP_START)
    }

    override fun onStart() {
        super.onStart()
        verifyLastIntentWithPermissionCheck()
        checkIfEventsDeletedWithPermissionCheck()
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
            viewModel.downloadEventsWorkManager()

        }
    }
}