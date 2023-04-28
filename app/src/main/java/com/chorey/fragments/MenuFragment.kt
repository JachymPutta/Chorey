package com.chorey.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.HOME_COL
import com.chorey.MAX_HOMES
import com.chorey.R
import com.chorey.USER_COL
import com.chorey.adapter.MenuRecyclerAdapter
import com.chorey.data.HomeModel
import com.chorey.data.LoggedUserModel
import com.chorey.databinding.FragmentMenuBinding
import com.chorey.dialog.AddHomeDialog
import com.chorey.dialog.CreateHomeDialog
import com.chorey.dialog.UserDetailDialog
import com.chorey.viewmodel.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment(),
    MenuRecyclerAdapter.OnHomeSelectedListener,
    CreateHomeDialog.CreateHomeListener {

    private lateinit var mrvAdapter: MenuRecyclerAdapter
    private lateinit var binding: FragmentMenuBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var query: Query

    private val viewModel by activityViewModels<LoginViewModel>()

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

        // FireStore instance
        firestore = Firebase.firestore

        query = firestore.collection(HOME_COL).limit(MAX_HOMES.toLong())

        mrvAdapter = object : MenuRecyclerAdapter(query, this@MenuFragment) {
            override fun onDataChanged() {
                // Change UI based on the number of homes present
                if (itemCount == 0) {
                    binding.allRoomsRecycler.visibility = View.GONE
                    binding.menuEmptyRecyclerText.visibility = View.VISIBLE
                    binding.addHomeHint.visibility = View.VISIBLE
                    binding.menuArrow.visibility = View.VISIBLE
                } else {
                    binding.allRoomsRecycler.visibility = View.VISIBLE
                    binding.menuEmptyRecyclerText.visibility = View.GONE
                    binding.addHomeHint.visibility = View.GONE
                    binding.menuArrow.visibility = View.GONE
                }
            }
        }

        binding.allRoomsRecycler.adapter = mrvAdapter
        binding.allRoomsRecycler.layoutManager = LinearLayoutManager(view.context)

        binding.menuContentLayout.visibility = View.GONE

        observeAuthState()

        binding.addHomeButton.setOnClickListener{ addHomeHandle() }
        binding.authButton.setOnClickListener { launchSignInFlow() }
        binding.menuSettingsButton.setOnClickListener {
            UserDetailDialog().show(childFragmentManager, "UserDetailDialog")
        }
    }
    override fun onStart() {
        super.onStart()

        // Start sign in if necessary
        if (needSignIn()) {
            makeWelcomeScreen()
        } else {
            checkUserName()
        }
    }

    override fun onStop() {
        super.onStop()
        mrvAdapter.stopListening()
    }

    override fun onHomeSelected(home: DocumentSnapshot) {
        home.reference.get()
            .addOnSuccessListener { doc ->
                val homeVal = doc.toObject<HomeModel>()
                val action = MenuFragmentDirections.actionMenuToHome(homeVal!!)
                findNavController().navigate(action)
            }.addOnFailureListener{
                e -> Log.d(TAG, "Error fetching home from snap: $e!")
            }
    }

    override fun onHomeCreated() {
        val myHomes = viewModel.user!!.memberOf.values

        if (!myHomes.isEmpty()) {
            query = firestore.collection(HOME_COL).whereIn("homeName", myHomes.toList())
            mrvAdapter.setQuery(query)
        }
    }

    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { authState ->
            when (authState) {
                // Changes for a logged in user
                LoginViewModel.AuthState.AUTHED -> { makeMenuScreen() }
                else -> { makeWelcomeScreen() }
            }
        }
    }

    private fun needSignIn() = Firebase.auth.currentUser == null

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
            // TODO: Add more Sign-in methods
        )

        // TODO Customize this!
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setTheme(R.style.Theme_Chorey_Login)
            .build()

        viewModel.launcher.launch(intent)
    }

    /**
     * Function to change the UI when there is no user logged in
     */
    private fun makeWelcomeScreen() {
        binding.menuContentLayout.visibility = View.VISIBLE
        binding.menuLoadingText.visibility = View.GONE

        binding.allRoomsRecycler.visibility = View.GONE
        binding.addHomeButton.visibility = View.GONE
        binding.menuEmptyRecyclerText.visibility = View.GONE
        binding.menuTitleText.setText(R.string.menu_title_welcome)
        binding.authButton.visibility = View.VISIBLE
        binding.menuSettingsButton.visibility = View.GONE
    }

    /**
     * Function to change the UI when there is a user authenticated
     */
    private fun makeMenuScreen() {
        // UI changes
        binding.menuTitleText.setText(R.string.menu_title_default)
        binding.allRoomsRecycler.visibility = View.VISIBLE
        binding.addHomeButton.visibility = View.VISIBLE
        binding.menuEmptyRecyclerText.visibility = View.VISIBLE
        binding.menuSettingsButton.visibility = View.VISIBLE
        binding.authButton.visibility = View.INVISIBLE
        binding.menuBottomLayout.visibility = View.VISIBLE
    }

    private fun checkUserName() {
        val user = Firebase.auth.currentUser ?: return

        firestore.collection(USER_COL).document(user.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val ds = task.result
                    if (ds.exists()) {
                        viewModel.user = ds.toObject<LoggedUserModel>()
                        updateQuery()
                    } else {
                        getUserNameDialog()
                    }

                    binding.menuContentLayout.visibility = View.VISIBLE
                    binding.menuLoadingText.visibility = View.GONE
                } else {
                    Log.d(TAG, "Failed with: ${task.exception}")
                }
            }
    }

    private fun updateQuery() {
        val myHomes = viewModel.user!!.memberOf.values

        if (!myHomes.isEmpty()) {
            query = firestore.collection(HOME_COL).whereIn("homeName", myHomes.toList())
            mrvAdapter.setQuery(query)
        }

    }
    private fun getUserNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val nameInput = EditText(requireContext())

        nameInput.inputType = InputType.TYPE_CLASS_TEXT
        builder.setTitle("Your name:")
            .setView(nameInput)
            .setCancelable(false)
            .setPositiveButton("Ok", null)

        val dialog = builder.create()

        fun submitNameHandle() {
            if (nameInput.text.toString().isBlank()) {
                Toast.makeText(requireContext(), "Please input a name.", Toast.LENGTH_SHORT).show()
            } else {
                val loggedUserModel = LoggedUserModel(
                    UID = Firebase.auth.currentUser!!.uid,
                    name = nameInput.text.toString()
                )
                viewModel.user = loggedUserModel
                updateQuery()
                firestore.collection(USER_COL).document(loggedUserModel.UID).set(loggedUserModel)
                dialog.dismiss()
            }
        }

        nameInput.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                submitNameHandle()
                true
            } else {
                false
            }
        }
        // Need to override the onShow handle so the dialog doesn't dismiss with invalid inputs
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                // TODO: check if the username exists!!
                submitNameHandle()
            }
        }

        dialog.show()
    }


    /**
     * Handles the addition of a new home, checks whether the max number of homes has been reached
     * and then continues through the dialogs
     */
    private fun addHomeHandle() {
        if (mrvAdapter.itemCount >= MAX_HOMES) {
            Toast.makeText(activity, "Max number of homes reached!", Toast.LENGTH_SHORT).show()
            return
        }
        AddHomeDialog().show(childFragmentManager, "AddHomeDialog")
    }

    companion object {
        const val TAG = "MenuFragment"
    }
}