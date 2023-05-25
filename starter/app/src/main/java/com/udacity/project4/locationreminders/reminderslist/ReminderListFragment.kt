package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    // Use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private val authenticationViewModel: AuthenticationViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    private lateinit var menu: Menu
    private var permissionDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reminders, container, false
        )
        binding.viewModel = _viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))
        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        return binding.root
    }

    private fun observeLifecycleData() {
        authenticationViewModel.authenticationStateEvent.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                handleMenu(it)
            }
        }
    }

    private fun logout() {
        AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
            _viewModel.showSnackBar.value = getString(R.string.logout_msg)
            authenticationViewModel.getLoginState()
        }
    }

    private fun handleMenu(isLogin: Boolean) {
        showMenu(R.id.logout, isLogin)
        showMenu(R.id.login, !isLogin)
    }

    private fun showMenu(menuId: Int, isShow: Boolean) {
        if (::menu.isInitialized) {
            val item = menu.findItem(menuId)
            item?.isVisible = isShow
            requireActivity().invalidateOptionsMenu()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
        startActivity(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        observeLifecycleData()
        binding.addReminderFAB.setOnClickListener {
            if (authenticationViewModel.isLogIn) {
                navigateToAddReminder()
            } else {
                navigateToLogin()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Load the reminders list on the ui
        enableMyLocation()
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        // Use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(ReminderListFragmentDirections.toSaveReminder())
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {}
        // Setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                logout()
            }
            R.id.login -> {
                navigateToLogin()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Display logout as menu item
        this.menu = menu
        inflater.inflate(R.menu.main_menu, menu)
        handleMenu(authenticationViewModel.isLogIn)
    }

    private fun enableMyLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionDialog()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                showPermissionDialog()
            }
        }

    private fun openPermissionSetting() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        requireActivity().startActivity(intent)
    }

    private fun showPermissionDialog() {
        if (permissionDialog == null) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.location_required_error))
            builder.setMessage(getString(R.string.permission_denied_explanation))

            builder.setPositiveButton(R.string.settings) { dialog, _ ->
                openPermissionSetting()
                dialog.dismiss()
            }

            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish()
            }
            permissionDialog = builder.show()
        } else {
            permissionDialog?.show()
        }
    }
}