package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.ScooterInfoBottomSheetFragmentBinding
import alahyaoui.escooter.radar.utils.MapsApiUrls.directionBaseUrl
import alahyaoui.escooter.radar.utils.ScooterIntentUrls
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ScooterInfoBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: ScooterInfoBottomSheetFragmentBinding

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScooterInfoBottomSheetFragmentBinding.inflate(inflater, container, false)
        binding.scooter = ScooterInfoBottomSheetFragmentArgs.fromBundle(requireArguments()).scooter

        if (binding.scooter != null) {
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

    private fun initRentButton() {
        val url =
            binding.scooter?.rentalUris?.android ?: when (binding.scooter?.company?.lowercase()) {
                "lime" -> ScooterIntentUrls.limeUrl
                "bird" -> ScooterIntentUrls.birdUrl
                "pony" -> ScooterIntentUrls.ponyUrl
                "spin" -> ScooterIntentUrls.spinUrl
                else -> {
                    binding.buttonRent.visibility = View.INVISIBLE
                    return
                }
            }

        binding.buttonRent.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                val text = getString(R.string.provider_unavailable_error_message)
                val duration = Toast.LENGTH_LONG
                Toast.makeText(context, text, duration).show()
            }
        }
    }

    private fun initDirectionButton() {
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