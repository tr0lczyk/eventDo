package com.rad4m.eventdo.ui.signupfragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rad4m.eventdo.R
import com.rad4m.eventdo.databinding.FragmentSignUpBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.ViewModelFactory
import javax.inject.Inject

class SignUpFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: SignUpViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSignUpBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.prefix.value = binding.ccp.selectedCountryCodeWithPlus
        binding.ccp.setOnCountryChangeListener {
            viewModel.prefix.value = binding.ccp.selectedCountryCodeWithPlus
        }

        viewModel.termsStarted.observe(this, Observer {
            if (it) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.terms_link))
                startActivity(intent)
                viewModel.termsStarted.value = false
            }
        })

        viewModel.policyStarted.observe(this, Observer {
            if (it) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(getString(R.string.privacy_policy_link))
                startActivity(intent)
                viewModel.policyStarted.value = false
            }
        })

        viewModel.navigateToVerification.observe(this, Observer {
            if (it) {
                this.findNavController()
                    .navigate(SignUpFragmentDirections.actionSignUpFragmentToVerificationFragment())
                viewModel.navigateToVerification.value = false
            }
        })

        return binding.root
    }
}