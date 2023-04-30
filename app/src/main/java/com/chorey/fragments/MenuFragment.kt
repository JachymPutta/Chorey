package com.chorey.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.chorey.DUMMY_FIELD
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
import com.chorey.viewmodel.AuthViewModel
import com.chorey.viewmodel.UserViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
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

        // FireStore instance
        firestore = Firebase.firestore

        // Empty query before user is loaded
        query = firestore.collection(HOME_COL).whereEqualTo(DUMMY_FIELD, "")

        mrvAdapter = object : MenuRecyclerAdapter(query, this@MenuFragment) {
            override fun onDataChanged() {
                // Change UI based on the number of homes present
                if (itemCount == 0) {
                    binding.allRoomsRecycler.visibility = View.INVISIBLE
                    binding.menuEmptyRecyclerText.visibility = View.VISIBLE
                } else {
                    binding.allRoomsRecycler.visibility = View.VISIBLE
                    binding.menuEmptyRecyclerText.visibility = View.INVISIBLE
                }
            }
        }

        binding.allRoomsRecycler.adapter = mrvAdapter
        binding.allRoomsRecycler.layoutManager = LinearLayoutManager(view.context)

        binding.addHomeButton.setOnClickListener{ addHomeHandle() }
        binding.authButton.setOnClickListener { launchSignInFlow() }
        binding.menuSettingsButton.setOnClickListener {
            UserDetailDialog().show(childFragmentManager, "UserDetailDialog")
        }

        authViewModel.isAuthed.observe(viewLifecycleOwner) { isAuthed ->
            if (isAuthed) { makeMenuScreen() }
            else { makeWelcomeScreen() }
        }
        makeWelcomeScreen()
        if (userViewModel.user != null) { updateQuery() }
    }
    override fun onStart() {
        super.onStart()
        mrvAdapter.startListening()
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
        val myHomes = userViewModel.user!!.memberOf.values

        if (!myHomes.isEmpty()) {
            query = firestore.collection(HOME_COL).whereIn("homeName", myHomes.toList())
            mrvAdapter.setQuery(query)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                authViewModel.onLoginSuccess()
                checkUserName()
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

    private fun checkUserName() {
        if (userViewModel.user == null) {
            val user = Firebase.auth.currentUser!!

            firestore.collection(USER_COL).document(user.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val ds = task.result
                        if (ds.exists()) {
                            userViewModel.user = ds.toObject<LoggedUserModel>()
                            updateQuery()
                        } else {
                            getUserNameDialog()
                        }

                        binding.menuContentLayout.visibility = View.VISIBLE
//                        binding.menuLoadingText.visibility = View.GONE
                    } else {
                        Log.d(TAG, "Failed with: ${task.exception}")
                    }
                }
        } else {
            binding.menuContentLayout.visibility = View.VISIBLE
//            binding.menuLoadingText.visibility = View.GONE

        }
    }

    private fun updateQuery() {
        val myHomes = userViewModel.user!!.memberOf.values

        if (!myHomes.isEmpty()) {
            query = firestore.collection(HOME_COL).whereIn("homeName", myHomes.toList())
            mrvAdapter.setQuery(query)
        }

    }
    private fun getUserNameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val nameInput = EditText(requireContext())

        //TODO: Custom XML file
        builder.setTitle("Your name:")
            .setView(nameInput)
            .setCancelable(false)
            .setPositiveButton("Ok", null)

        val dialog = builder.create()
        dialog.setIcon(R.mipmap.chorey_logo_foreground)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        fun submitNameHandle() {
            if (nameInput.text.toString().isBlank()) {
                Toast.makeText(requireContext(), "Please input a name.", Toast.LENGTH_SHORT).show()
            } else {
                val loggedUserModel = LoggedUserModel(
                    UID = Firebase.auth.currentUser!!.uid,
                    name = nameInput.text.toString()
                )
                userViewModel.user = loggedUserModel
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