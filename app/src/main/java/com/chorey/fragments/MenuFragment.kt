package com.chorey.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.Chorey
import com.chorey.DUMMY_FIELD
import com.chorey.HOME_COL
import com.chorey.MAX_HOMES
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.adapter.MenuRecyclerAdapter
import com.chorey.data.DialogState
import com.chorey.data.HomeModel
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.FragmentMenuBinding
import com.chorey.dialog.AddHomeDialog
import com.chorey.dialog.CreateHomeDialog
import com.chorey.dialog.UserDetailDialog
import com.chorey.viewmodel.AuthViewModel
import com.chorey.viewmodel.UserViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.ads.AdRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment(),
    MenuRecyclerAdapter.OnHomeSelectedListener,
    CreateHomeDialog.CreateHomeListener,
    UserDetailDialog.OnIconChangedListener {

    private lateinit var menuRecyclerAdapter: MenuRecyclerAdapter
    private lateinit var binding: FragmentMenuBinding
    private lateinit var query: Query
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    private val userViewModel by activityViewModels<UserViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enable logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Ads
//        val adRequest = AdRequest.Builder().build()
//        binding.adViewMenu.loadAd(adRequest)

        dataLoaded(false)

        // Empty query before user is loaded
        query = Firebase.firestore.collection(HOME_COL).whereEqualTo(DUMMY_FIELD, "")
        menuRecyclerAdapter = initMenuRecyclerAdapter()

        binding.allRoomsRecycler.adapter = menuRecyclerAdapter
        binding.allRoomsRecycler.layoutManager = LinearLayoutManager(view.context)

        binding.addHomeButton.setOnClickListener{ addHomeHandle() }
        binding.authButton.setOnClickListener { launchSignInFlow() }
        binding.menuUserButton.setOnClickListener {
            UserDetailDialog().apply {
                state = DialogState.EDIT
                listener = this@MenuFragment
            }.show(childFragmentManager, "UserDetailDialog")
        }

        authViewModel.isAuthed.observe(viewLifecycleOwner) { isAuthed ->
            if (isAuthed) { makeMenuScreen() }
            else { makeWelcomeScreen() }
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateQuery()
                onIconChanged()
            }
        }

        makeWelcomeScreen()
    }

    override fun onStart() {
        super.onStart()
        menuRecyclerAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        menuRecyclerAdapter.stopListening()
    }

    override fun onHomeSelected(home: DocumentSnapshot) {
        val homeVal = home.toObject<HomeModel>()
        findNavController().navigate(MenuFragmentDirections.actionMenuToHome(homeVal!!))
    }

    override fun onHomeCreated() {
        val myHomes = userViewModel.user.value!!.memberOf.values

        if (!myHomes.isEmpty()) {
            query = Firebase.firestore.collection(HOME_COL).whereIn("homeName", myHomes.toList())
            menuRecyclerAdapter.setQuery(query)
        }
    }

    override fun onIconChanged() {
        binding.menuSettingsButton.setImageResource(userViewModel.user.value!!.icon)
        binding.menuUserName.text = userViewModel.user.value!!.name
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                authViewModel.onLoginSuccess()
                userViewModel.resetUser()
            } else {
                // Sign in failed
                Toast.makeText(context, response?.error?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
//            AuthUI.IdpConfig.FacebookBuilder().build()
            // TODO: Add more Sign-in methods
        )

        // TODO Customize this!
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setLogo(R.mipmap.chorey_logo_foreground)
            .setTheme(R.style.Chorey_AuthUI)
            .build()

        signInLauncher.launch(intent)
    }


    private fun makeWelcomeScreen() {
        binding.menuContentLayout.visibility = View.GONE
        binding.menuWelcomeScreen.visibility = View.VISIBLE
    }

    private fun makeMenuScreen() {
        checkUserName()

        // UI changes
        binding.menuContentLayout.visibility = View.VISIBLE
        binding.menuWelcomeScreen.visibility = View.GONE
    }

    private fun needLogin() = userViewModel.user.value == null || userViewModel.user.value?.UID == ""
    private fun checkUserName() {
        val user = Firebase.auth.currentUser!!

        if (needLogin()) {
            Firebase.firestore.collection(USER_COL).document(user.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val ds = task.result
                        if (ds.exists()) {
                            userViewModel.updateUser(ds.toObject<LoggedUserModel>()!!)
                        } else {
                            UserDetailDialog().apply {
                                listener = this@MenuFragment
                                state = DialogState.CREATE
                                isCancelable = false
                            }.show(childFragmentManager, TAG)
                        }
                        binding.menuContentLayout.visibility = View.VISIBLE
                    } else {
                        Log.e(TAG, "Failed with: ${task.exception}")

                    }
                }
        } else {
            binding.menuContentLayout.visibility = View.VISIBLE
        }
    }

    private fun updateQuery() {
        val myHomes = userViewModel.user.value!!.memberOf.values

        dataLoaded(true)

        if (!myHomes.isEmpty()) {
            query = Firebase.firestore.collection(HOME_COL).whereIn("homeName", myHomes.toList())
            menuRecyclerAdapter.setQuery(query)
        }
    }

    private fun dataLoaded(isLoaded : Boolean) {
        if (isLoaded) {
            binding.loadingSpinner.visibility = View.GONE
            binding.menuEmptyRecyclerText.visibility = View.VISIBLE
        } else {
            binding.loadingSpinner.visibility = View.VISIBLE
            binding.menuEmptyRecyclerText.visibility = View.GONE
        }

        binding.addHomeButton.isEnabled = isLoaded
        binding.menuUserButton.isEnabled = isLoaded
        binding.menuSettingsButton.isEnabled = isLoaded
    }


    private fun addHomeHandle() {
        if (menuRecyclerAdapter.itemCount >= MAX_HOMES) {
            Toast.makeText(activity, "Max number of homes reached!", Toast.LENGTH_SHORT).show()
            return
        }
        AddHomeDialog().show(childFragmentManager, "AddHomeDialog")
    }

    private fun initMenuRecyclerAdapter() : MenuRecyclerAdapter {
        return object : MenuRecyclerAdapter(query, this@MenuFragment) {
            override fun onDataChanged() {
                // Change UI based on the number of homes present
                when(itemCount) {
                    0 -> {
                        binding.allRoomsRecycler.visibility = View.INVISIBLE
                        binding.menuEmptyRecyclerText.visibility = View.VISIBLE
                        binding.menuMainLayout.setOnClickListener { addHomeHandle() }
                    }
                    1 -> {
                        // If logging in and have only one home go directly there
                        val choreyApp = requireActivity().application as Chorey
                        val initLoad = choreyApp.initMenuLoad
                        if (initLoad) {
                            onHomeSelected(getSnapshot(0))
                            choreyApp.initMenuLoad = false
                        }
                        binding.allRoomsRecycler.visibility = View.VISIBLE
                        binding.menuEmptyRecyclerText.visibility = View.INVISIBLE
                        binding.menuMainLayout.setOnClickListener {}
                    }
                    else -> {
                        binding.allRoomsRecycler.visibility = View.VISIBLE
                        binding.menuEmptyRecyclerText.visibility = View.INVISIBLE
                        binding.menuMainLayout.setOnClickListener {}
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "MenuFragment"
    }
}