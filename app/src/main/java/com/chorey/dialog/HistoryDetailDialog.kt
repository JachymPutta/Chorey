package com.chorey.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.chorey.data.ChoreModel
import com.chorey.databinding.DialogHistoryDetailBinding

class HistoryDetailDialog(private val choreModel: ChoreModel) : DialogFragment()
{
    private var _binding: DialogHistoryDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogHistoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val reward =  choreModel.points.toString() + "points"

        binding.createChoreCancelButton.setOnClickListener { onCancelClicked() }
        binding.createChoreName.text = choreModel.choreName
        binding.choreDetailDoneBy.text = choreModel.curAssignee
        binding.choreDetailMinsToComplete.text = choreModel.timeToComplete.toString()
        binding.choreDetailPoints.text = reward
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {
        const val TAG = "HistoryDialog"
    }
}