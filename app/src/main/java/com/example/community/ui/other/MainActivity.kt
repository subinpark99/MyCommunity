package com.example.community.ui.other

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.community.R
import com.example.community.data.MyApplication
import com.example.community.data.entity.User
import com.example.community.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson

class MainActivity :
    AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var user: User
    private val gson: Gson = Gson()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userJson = MyApplication.prefs.getUser("user", "")
        user = gson.fromJson(userJson, User::class.java)

        val navHostFragment =   // FragmentContainerView
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        drawerLayout = binding.drawerLayout
        initNavi()
        setAge()

    }

    private fun initNavi() {

        binding.navBar.setupWithNavController(navController)
        binding.navBar.background = null   // 바텀네비게이션 배경 없애기

        val navView = binding.drawerNav
        val toolbar = binding.toolbar
        val headerView = navView.getHeaderView(0)

        setSupportActionBar(toolbar)  // toolbar setting
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_menu) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        navView.setNavigationItemSelectedListener(this)

        val userEmailView = headerView.findViewById<TextView>(R.id.user_email_tv)
        val userAddressView = headerView.findViewById<TextView>(R.id.user_address_tv)

        userEmailView.text = user.email
        userAddressView.text = user.location

    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when (item.itemId) {
            android.R.id.home -> {
                // 햄버거 버튼 클릭시 네비게이션 드로어 열기
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAge() {

        val province = user.location.split(" ")[0] // 광역시,도
        val parentList = mutableListOf("$province ' s")
        val childList = mutableListOf(mutableListOf("10대", "20대", "30대", "40대", "50대", "60대"))

        setExpandableList(parentList, childList)
    }

    /* ExpandableListView 설정 */
    private fun setExpandableList(
        parentList: MutableList<String>,
        childList: MutableList<MutableList<String>>
    ) {

        val ageRange = mutableListOf("10대", "20대", "30대", "40대", "50대", "60대")
        val expandableAdapter =
            ExpandableListAdapter(this, parentList, childList)

        val menu = binding.expandedMenu
        menu.setAdapter(expandableAdapter)

        menu.setOnGroupClickListener { _, _, _, _ ->
            false
        }
        menu.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->  // 메뉴 아이템 클릭

            when (groupPosition) {
                0 -> {
                    when (childPosition) {
                        0, 1, 2, 3, 4, 5 -> {

                            val replace = when (childPosition) {
                                0, 1, 2, 3, 4, 5 -> {
                                    if (childPosition < ageRange.size) {
                                        val ageValue = ageRange[childPosition]
                                        val bundle =
                                            bundleOf("age" to ageValue)

                                        navController.navigate(R.id.ageFragment, bundle)
                                    } else {
                                        null
                                    }
                                }
                                else -> null
                            }
                            if (replace != null) {
                                drawerLayout.closeDrawer(GravityCompat.START)
                            }
                        }
                    }
                    true
                }
                else -> {
                    Log.d("menuClick", "failed")
                    false
                }
            }
        }
    }

    // drawer menu item click
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.ulsan -> {
                Toast.makeText(this, "ulsan", Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }

}
