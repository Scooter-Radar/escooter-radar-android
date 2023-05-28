package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.MainActivityBinding
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            when (it.itemId) {
                R.id.nav_maps -> navController.navigate(R.id.maps_destination)
                R.id.nav_settings -> navController.navigate(R.id.settings_destination)
            }
            true
        }

        drawerLayout = binding.drawerLayout
        binding.navDrawerFAB.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        drawLayout()
        binding.tryAgainButton.setOnClickListener {
            drawLayout()
        }
    }

    fun drawLayout() {
        if (isNetworkAvailable()) {
            binding.navView.visibility = View.VISIBLE
            binding.navDrawerFAB.visibility = View.VISIBLE
            binding.navHostFragmentContentMain.visibility = View.VISIBLE
            binding.noInternetLayout.visibility = View.GONE
        } else {
            binding.navView.visibility = View.GONE
            binding.navDrawerFAB.visibility = View.GONE
            binding.navHostFragmentContentMain.visibility = View.GONE
            binding.noInternetLayout.visibility = View.VISIBLE

        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null && capabilities.hasCapability(NET_CAPABILITY_INTERNET)
    }
}