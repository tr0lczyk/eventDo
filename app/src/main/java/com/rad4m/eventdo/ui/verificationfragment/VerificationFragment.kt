package com.rad4m.eventdo.ui.verificationfragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rad4m.eventdo.databinding.FragmentVerificationBinding
import com.rad4m.eventdo.di.appComponent
import com.rad4m.eventdo.utils.Utilities
import com.rad4m.eventdo.utils.ViewModelFactory
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import javax.inject.Inject

class VerificationFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: VerificationViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentVerificationBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.codeView.requestFocus()
        binding.codeView.setOtpCompletionListener {
            viewModel.code.value = binding.codeView.text.toString()
            viewModel.sendUserCode()
        }
        viewModel.navigateToMain.observe(this, Observer {
            if (it) {
                UIUtil.hideKeyboard(activity)
                this.findNavController()
                    .navigate(VerificationFragmentDirections.actionVerificationFragmentToMainFragment(null))
                viewModel.navigateToMain.value = false
            }
        })

        viewModel.codeIncorrect.observe(this, Observer {
            if (it) {
                binding.codeView.setText("")
                viewModel.codeIncorrect.value = false
            }
        })

        Handler().postDelayed({
            Utilities.showKeyboard(activity!!)
        }, 500)

        return binding.root
    }
}