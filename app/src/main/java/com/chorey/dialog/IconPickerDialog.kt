package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.chorey.adapter.IconPickerAdapter
import com.chorey.databinding.DialogIconPickerBinding

class IconPickerDialog : DialogFragment() {

    private var _binding: DialogIconPickerBinding? = null
    private val binding get() = _binding!!

    var iconList: List<Int> = listOf()
    var parentDialog : DialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogIconPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (parentDialog == null) dismiss()

        val adapter = IconPickerAdapter(iconList,
             parentDialog as IconPickerAdapter.IconPickerDialogListener)
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