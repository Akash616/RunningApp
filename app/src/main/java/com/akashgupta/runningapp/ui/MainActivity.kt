package com.akashgupta.runningapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.akashgupta.runningapp.R
import com.akashgupta.runningapp.databinding.ActivityMainBinding
import com.akashgupta.runningapp.db.RunDAO
import com.akashgupta.runningapp.others.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        //bottom navigation view connect with navigation components
        //This make sure whenever we click on one item of that bottom navigation view, navigate to that specific fragment.
        //binding.bottomNavigationView.setupWithNavController(binding.navigationHostFragment.findNavController())

        // Set up the NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Ensure navController is initialized before calling navigateToTrackingFragmentIfNeeded
        navigateToTrackingFragmentIfNeeded(intent) //new launch

        // Set up the BottomNavigationView with the NavController
        binding.bottomNavigationView.setupWithNavController(navController)

        //Now we don't want to show bottom navigation view inside of our SetupFragment and TrackingFragment.
        navController.addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        binding.bottomNavigationView.visibility = View.VISIBLE
                    else -> binding.bottomNavigationView.visibility = View.GONE
                }
            }

    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.action_global_trackingFragment)
        }
        /* If our main activity was destroyed but our service is still running and if we then send that
        * pending intent that would mean our main activity will be relaunched and in that case it will go inside
        * of onCreate() again. But if the activity was not destroyed and we use the pending intent to launch it
        * then it won't go inside of onCreate() again instead it will call onNewIntent().*/
    }

    override fun onNewIntent(intent: Intent) { //exists launch
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}