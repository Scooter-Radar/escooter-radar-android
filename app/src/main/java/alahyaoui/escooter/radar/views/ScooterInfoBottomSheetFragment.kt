package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.ScooterInfoBottomSheetBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScooterInfoBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: ScooterInfoBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScooterInfoBottomSheetBinding.inflate(inflater, container, false)
        binding.scooter = ScooterInfoBottomSheetFragmentArgs.fromBundle(requireArguments()).scooter

        if(binding.scooter != null){
            initCompanyImage()
            initScooterImage()
        }

        return binding.root
    }

    fun initCompanyImage() {
        val image =
            when (binding.scooter?.company?.lowercase()) {
                "lime" -> R.drawable.ic_lime_logo
                "bird" -> R.drawable.ic_bird_logo
                "pony" -> R.drawable.ic_pony_logo
                "spin" -> R.drawable.ic_spin_logo
                else -> R.drawable.ic_baseline_electric_scooter
            }

        binding.imageCompany.setImageResource(image)
    }

    fun initScooterImage() {
        val image =
            when (binding.scooter?.company?.lowercase()) {
                "lime" -> R.drawable.ic_lime_escooter
                "bird" -> R.drawable.ic_bird_escooter
                "pony" -> R.drawable.ic_pony_escooter
                "spin" -> R.drawable.ic_spin_escooter
                else -> R.drawable.ic_baseline_electric_scooter
            }

        binding.imageEscooter.setImageResource(image)
    }
}