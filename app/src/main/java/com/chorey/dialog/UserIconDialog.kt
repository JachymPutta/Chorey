package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.chorey.R
import com.chorey.adapter.UserIconAdapter
import com.chorey.databinding.DialogUserIconBinding

class UserIconDialog(private val parentDialog : DialogFragment): DialogFragment() {

    private var _binding: DialogUserIconBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUserIconBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val iconList = listOf(
            R.drawable.baseline_home_24,
            R.drawable.baseline_money_24,
            R.drawable.baseline_account_circle_24,
            R.drawable.baseline_note_alt_24
        )

        val adapter = UserIconAdapter(iconList,
             parentDialog as UserIconAdapter.UserIconDialogListener)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    companion object {
        const val TAG = "UserIconDialog"
    }
}