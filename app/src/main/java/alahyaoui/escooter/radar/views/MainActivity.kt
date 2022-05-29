package alahyaoui.escooter.radar.views

import alahyaoui.escooter.radar.databinding.ActivityMainBinding
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        binding.contentView.navDrawerFAB.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }
    }
}