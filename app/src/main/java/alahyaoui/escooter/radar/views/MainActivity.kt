package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.R
import alahyaoui.escooter.radar.databinding.ActivityMainBinding
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        val navigationView: NavigationView = binding.navView

        navigationView.setNavigationItemSelectedListener {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            when (it.itemId) {
                R.id.nav_maps -> navController.navigate(R.id.maps_destination)
                R.id.nav_settings -> navController.navigate(R.id.settings_destination)
            }
            true
        }

        binding.contentView.navDrawerFAB.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}