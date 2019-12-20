package com.rad4m.eventdo.ui.mainfragment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.rad4m.eventdo.databinding.FragmentMainBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.models.EventModel
import com.rad4m.eventdo.utils.HeaderItemDecoration
import com.rad4m.eventdo.utils.Utilities.Companion.ITEM_VIEW_TYPE_HEADER
import com.rad4m.eventdo.utils.Utilities.Companion.NEW_EVENT_PAGE
import com.rad4m.eventdo.utils.Utilities.Companion.convertStringToDate
import com.rad4m.eventdo.utils.ViewModelFactory
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import javax.inject.Inject

class MainFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(inflater)
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
            com.rad4m.eventdo.R.string.drawer_open,
            com.rad4m.eventdo.R.string.drawer_close
        ) {

        }
        drawerToggle.isDrawerIndicatorEnabled = true
        binding.menuDrawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
        UIUtil.hideKeyboard(activity)

        val adapter = EventsAdapter(EventsAdapter.EventListener {
            if (viewModel.sharedPrefs.getValueBoolean(NEW_EVENT_PAGE) == true) {
                viewModel.navigateToSelectedEvent(it)
            } else {
                saveEventToCalendar(it)
            }
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
            com.rad4m.eventdo.R.id.infoItem -> goToEventDoPage()
            com.rad4m.eventdo.R.id.settingsItem -> this.findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToSettingsFragment()
            )
            com.rad4m.eventdo.R.id.accountItem -> this.findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToMyAccountFragment()
            )
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun goToEventDoPage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(com.rad4m.eventdo.R.string.about_us_link))
        startActivity(intent)
    }

    private fun saveEventToCalendar(event: EventModel) {
        val insertCalendarIntent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, event.title)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
            .putExtra(
                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                convertStringToDate(event.dtStart!!).time
            )
            .putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                convertStringToDate(event.dtEnd!!).time
            )
            .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        startActivity(insertCalendarIntent)
    }
}