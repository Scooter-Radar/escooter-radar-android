package alahyaoui.escooter.radar.utils

import alahyaoui.escooter.radar.R
import android.Manifest
import android.content.Context
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.EasyPermissions

object TrackingUtility {

    fun hasLocationPermissions(context: Context): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    fun hasOnlyCoarseLocationPermissions(context: Context): Boolean {
        return EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && !EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun requestLocationPermissions(fragment: Fragment) {
        EasyPermissions.requestPermissions(
            fragment,
            fragment.getString(R.string.location_permission_rationale_message),
            PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}