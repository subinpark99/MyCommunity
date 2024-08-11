package com.dev.community.ui.start


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.dev.community.util.Result
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dev.community.R
import com.dev.community.ui.viewModel.UserViewModel
import com.dev.community.databinding.ActivityMainBinding
import com.dev.community.ui.home.adapter.ExpandableListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeUserData()
    }


    // 네비게이션 컨트롤러를 설정하고, 목적지 변경 리스너를 추가하여 DrawerLayout의 아이콘 표시 제어
    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host)
                    as? NavHostFragment
                ?: throw IllegalStateException("NavHostFragment not found")

        drawerLayout = binding.drawerLayout
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment) { // homefragment에서만 drawer 아이콘 보이게
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
    }


    private fun observeUserData() {
        userViewModel.getUser()

        lifecycleScope.launch {
            userViewModel.userState.collect {
                when (it) {
                    is Result.Success -> {
                        val user = it.data
                        initNavi(user.email, user.location)
                        setAge(user.location)

                        userViewModel.updateFcmToken(user.token)
                    }

                    is Result.Error -> Log.e("ERROR", "MainActivity - ${it.message}")
                    is Result.Loading -> Log.d("loading", "loading...")

                }
            }
        }
    }


    private fun initNavi(email: String, location: String) {

        binding.navBar.setupWithNavController(navController)
        binding.navBar.background = null

        val navView = binding.drawerNav
        val headerView = navView.findViewById<ConstraintLayout>(R.id.item_drawer_header)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_menu)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val userEmailView = headerView.findViewById<TextView>(R.id.user_email_tv)
        val userAddressView = headerView.findViewById<TextView>(R.id.user_address_tv)

        userEmailView.text = email
        userAddressView.text = location
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // 사용자 위치에 따라 나이대 리스트를 설정
    private fun setAge(location: String) {
        val province = location.split(" ")[0]
        val parentList = mutableListOf("$province 's")
        val childList = mutableListOf(mutableListOf("10대", "20대", "30대", "40대", "50대", "60대"))

        setExpandableList(parentList, childList)
    }


    private fun setExpandableList(
        parentList: MutableList<String>,
        childList: MutableList<MutableList<String>>
    ) {
        val ageRange = mutableListOf("10대", "20대", "30대", "40대", "50대", "60대")
        val expandableAdapter = ExpandableListAdapter(this, parentList, childList)
        val menu = binding.expandedMenu
        menu.setAdapter(expandableAdapter)

        menu.setOnGroupClickListener { _, _, _, _ -> false }
        menu.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            if (groupPosition == 0 && childPosition in ageRange.indices) {
                val ageValue = ageRange[childPosition]
                val bundle = bundleOf("age" to ageValue)
                navController.navigate(R.id.ageFragment, bundle)
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            } else false
        }
    }

}
