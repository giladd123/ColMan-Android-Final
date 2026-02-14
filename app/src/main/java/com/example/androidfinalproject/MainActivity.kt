package com.example.androidfinalproject

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup bottom nav with NavController
        bottomNav.setupWithNavController(navController)

        // Handle nav_add click manually
        // This is a temporary workaround to make the add button inactive
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_add -> {
                    // TODO: Navigate to AddReviewFragment when implemented
                    false
                }
                else -> {
                    // Let NavigationUI handle homeFragment and profileFragment
                    navController.navigate(item.itemId)
                    true
                }
            }
        }

        val authDestinations = setOf(
            R.id.loginFragment,
            R.id.signInFragment,
            R.id.signUpFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = if (destination.id in authDestinations) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}