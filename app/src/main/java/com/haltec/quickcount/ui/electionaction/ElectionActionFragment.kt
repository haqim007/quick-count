package com.haltec.quickcount.ui.electionaction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.haltec.quickcount.R
import com.haltec.quickcount.databinding.FragmentElectionActionBinding
import com.haltec.quickcount.ui.BaseFragment
import com.haltec.quickcount.ui.MainViewModel

class ElectionActionFragment : BaseFragment() {
    
    private lateinit var binding: FragmentElectionActionBinding
    private val activityViewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentElectionActionBinding.inflate(layoutInflater, container, false)
        
        val args: ElectionActionFragmentArgs by navArgs()
        val tps = args.tps
        val election = args.election
        
        binding.apply { 
            btnBack.setOnClickListener { 
                findNavController().navigateUp()
            }
            btnLogout.setOnClickListener {
                activityViewModel.requestToLogout()
            }
            tvTpsName.text = tps.name
            tvTpsLocation.text = getString(R.string.tps_location_, tps.village, tps.subdistrict)
            tvElectionName.text = election.title
            btnUploadEvidence.setOnClickListener { 
                findNavController().navigate(
                    ElectionActionFragmentDirections.actionElectionActionFragmentToUploadEvidenceFragment(
                        tps, election
                    )
                )
            }
            btnOpenForm.setOnClickListener { 
                findNavController().navigate(
                    ElectionActionFragmentDirections.actionElectionActionFragmentToVoteFragment(tps, election)
                )
            }
        }
        
        return binding.root
    }

}