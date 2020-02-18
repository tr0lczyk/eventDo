package com.rad4m.eventdo.ui.myaccountfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rad4m.eventdo.R
import com.rad4m.eventdo.databinding.FragmentMyaccountBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.Utilities.Companion.makeStatusBarTransparent
import com.rad4m.eventdo.utils.Utilities.Companion.showDialog
import com.rad4m.eventdo.utils.Utilities.Companion.showInformingDialog
import com.rad4m.eventdo.utils.ViewModelFactory
import javax.inject.Inject

class MyAccountFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MyAccountViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMyaccountBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.backNavigation.observe(this, Observer {
            if (it) {
                this.findNavController().navigateUp()
                viewModel.stopBackNavigation()
            }
        })

        viewModel.navigateToLogin.observe(this, Observer {
            if (it) {
                showInformingDialog(
                    activity!!,
                    getString(R.string.account_deleted_message),
                    getString(R.string.account_deleted_title)
                )
                activity!!.makeStatusBarTransparent()
                findNavController().navigate(MyAccountFragmentDirections.actionMyAccountFragmentToIntroFragment())
                viewModel.navigateToLogin.value = false
            }
        })

        viewModel.showDeleteUserDialog.observe(this, Observer {
            if (it) {
                val deleteUser = { viewModel.deleteUserAccount() }
                showDialog(
                    activity!!,
                    getString(R.string.account_delete_text),
                    getString(R.string.delete_your_account),
                    getString(R.string.yes_button),
                    deleteUser,
                    getString(R.string.no_button)
                )
                viewModel.showDeleteUserDialog.value = false
            }
        })

        return binding.root
    }
}