package com.d201.eyeson.view.angel.setting

import android.content.Intent
import android.widget.ToggleButton
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.d201.eyeson.R
import com.d201.eyeson.base.BaseFragment
import com.d201.eyeson.databinding.FragmentAngelSettingBinding
import com.d201.eyeson.view.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "AngelSettingFragment"
private const val PICK_MIN = 0
private const val PICK_MAX = 24

@AndroidEntryPoint
class AngelSettingFragment :
    BaseFragment<FragmentAngelSettingBinding>(R.layout.fragment_angel_setting) {

    private lateinit var buttonList: List<ToggleButton>
    private val viewModel: AngelSettingViewModel by viewModels()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun init() {
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.apply {
            buttonList =
                listOf(toggleSun, toggleMon, toggleTue, toggleWed, toggleThu, toggleFri, toggleSat)
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
            btnLogout.setOnClickListener {
                logOut()
            }
            btnResign.setOnClickListener {
                viewModel.deleteUser()

            }
            btnSave.setOnClickListener {
                saveAlarmInfo()
            }
            numpickStart.minValue = PICK_MIN
            numpickEnd.minValue = PICK_MIN
            numpickStart.maxValue = PICK_MAX
            numpickEnd.maxValue = PICK_MAX
            for (button in buttonList) {
                button.setOnCheckedChangeListener { view, b ->
                    when (b) {
                        true -> {
                            view.setBackgroundResource(R.drawable.shape_layout_border_blue)
                            view.setTextColor(
                                requireActivity().resources.getColor(
                                    R.color.angel_blue,
                                    null
                                )
                            )
                        }
                        false -> {
                            view.setBackgroundResource(R.drawable.shape_layout_border)
                            view.setTextColor(
                                requireActivity().resources.getColor(
                                    R.color.black,
                                    null
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initViewModel() {
        viewModel.apply {
            getAngelInfo()
            deleteUserEvent.observe(viewLifecycleOwner) {
                if (it) {
                    finishActivity()
                }
            }
            saveSettingEvent.observe(viewLifecycleOwner) {
                if (it) {
                    showToast("저장 되었습니다")
                    findNavController().popBackStack()
                }
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.angelInfo.collectLatest {
                if (it != null) {
                    var alarmDay = it.alarmDay
                    for (button in buttonList.reversed()) {
                        if (alarmDay and 1 == 1) {
                            button.isChecked = true
                        }
                        alarmDay = alarmDay shr 1
                    }
                    binding.apply {
                        numpickStart.value = it.alarmStart
                        numpickEnd.value = it.alarmEnd
                        lifecycleScope.launch(Dispatchers.Main) {
                            switchAlarm.isChecked = it.active
                        }
                    }
                }

            }
        }
    }

    private fun finishActivity() {
        requireActivity().startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun saveAlarmInfo() {
        binding.apply {
            var alarmDay = 0
            var checkedDay = 64
            for (item in buttonList) {
                if (item.isChecked) {
                    alarmDay += checkedDay
                }
                checkedDay = checkedDay shr 1
            }
            viewModel.putAngelInfo(
                numpickStart.value,
                numpickEnd.value,
                alarmDay,
                switchAlarm.isChecked
            )
        }
    }

    private fun logOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        mGoogleSignInClient.signOut().addOnCompleteListener {
            finishActivity()
        }
    }
}