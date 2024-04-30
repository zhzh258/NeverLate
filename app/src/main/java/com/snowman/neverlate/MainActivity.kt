package com.snowman.neverlate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.snowman.neverlate.databinding.ActivityMainBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.shared.SharedUserViewModel
import com.snowman.neverlate.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var profilePictureImageView: ImageView
    private lateinit var displayNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var navView: NavigationView
    private val firebaseManager = FirebaseManager.getInstance()
    private val sharedUserViewModel: SharedUserViewModel by viewModels()


    private val TAG = "Main Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = firebaseManager.firebaseAuth()
        firestore = firebaseManager.getFirestore()

        if (auth.currentUser == null) {
            goLoginActivity()
        }

        initViews()
        observeUserData()
        setUpFloatingActionBar()
        setUpSideActionBar()
        loadUserData()
    }

    private fun initViews() {
        navView = binding.navView
        val headerView = navView.getHeaderView(0)
        profilePictureImageView = headerView.findViewById(R.id.profileIV)
        displayNameTextView = headerView.findViewById(R.id.displayNameTV)
        emailTextView = headerView.findViewById(R.id.emailTV)
    }

    private fun setUpFloatingActionBar() {
        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun setUpSideActionBar() {
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_events,
                R.id.nav_history,
                R.id.nav_notification,
                R.id.nav_friends,
                R.id.nav_profile,
                R.id.nav_map,
                R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.menu.findItem(R.id.nav_sign_out).setOnMenuItemClickListener {
            signOut()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun loadUserData() {
        firebaseManager.loadUserData { user ->
            if (user != null) {
                user.updateUserViewModel(sharedUserViewModel)
            } else {
                Log.e("FirebaseManager", "Failed to retrieve user data")
            }
        }
    }

    private fun observeUserData() {
        sharedUserViewModel.userData.observe(this) { user ->
            if (user != null) {
                displayNameTextView.text = user.displayName
                emailTextView.text = user.email
                Glide.with(this)
                    .load(user.photoURL)
                    .circleCrop()
                    .error(R.mipmap.ic_launcher_round)
                    .into(profilePictureImageView)
            } else {
                Log.i(TAG, "User data null")
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        goLoginActivity()
    }

    private fun goLoginActivity() {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        finish()
    }
}