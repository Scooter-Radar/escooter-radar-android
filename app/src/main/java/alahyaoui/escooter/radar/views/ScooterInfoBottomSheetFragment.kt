package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.ScooterInfoBottomSheetBinding
import alahyaoui.escooter.radar.utils.MapsApiUrls.directionBaseUrl
import alahyaoui.escooter.radar.utils.ScooterApplicationUrls
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ScooterInfoBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: ScooterInfoBottomSheetBinding

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

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
            initRentButton()
            initDirectionButton()
        }

        return binding.root
    }

    private fun initCompanyImage() {
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

    private fun initScooterImage() {
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

    private fun initRentButton(){
        val url =
            when (binding.scooter?.company?.lowercase()) {
                "lime" -> ScooterApplicationUrls.limeUrl
                "bird" -> ScooterApplicationUrls.birdUrl
                "pony" -> ScooterApplicationUrls.ponyUrl
                "spin" -> ScooterApplicationUrls.spinUrl
                else -> ""
            }

        binding.buttonRent.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
            startActivity(intent)
        }
    }

    private fun initDirectionButton(){
        binding.buttonGoTo.setOnClickListener {
            val longitude = binding.scooter?.location?.coordinates?.get(0)
            val latitude = binding.scooter?.location?.coordinates?.get(1)
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("$directionBaseUrl/?api=1&destination=$latitude,$longitude")
            )
            startActivity(intent)
        }
    }
}