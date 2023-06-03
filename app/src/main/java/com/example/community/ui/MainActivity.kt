package com.example.community.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.community.R
import com.example.community.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity :
    AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener
{
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavi()
        setExpandableList()
    }

    private fun initNavi(){

        val navHostFragment =   // FragmentContainerView
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navBar.setupWithNavController(navController)
        binding.navBar.background = null   // 바텀네비게이션 배경 없애기

        val navView=binding.drawerNav
        val toolbar=binding.toolbar

        setSupportActionBar(toolbar)  // toolbar setting
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_menu) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게
        navView.setNavigationItemSelectedListener(this)

    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val drawerLayout=binding.drawerLayout

        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when(item.itemId){
            android.R.id.home->{
                // 햄버거 버튼 클릭시 네비게이션 드로어 열기
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /* ExpandableListView 설정 */
    private fun setExpandableList() {
        val parentList = mutableListOf("울산광역시", "부산광역시")
        val childList = mutableListOf(
            mutableListOf("남구", "동구","울주군","중구","북구"),
            mutableListOf("중구","남구","동래구","북구","수영구","해운대구")
        )

        val expandableAdapter =
            ExpandableListAdapter(this, parentList, childList)
        val menu=binding.expandedMenu
        menu.setAdapter(expandableAdapter)

        menu.setOnGroupClickListener { parent, v, groupPosition, id ->
            /* todo : parent 클릭 이벤트 설정 */
            false
        }
        menu.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            /* todo : child 클릭 이벤트 설정 */
            false
        }
    }

    // drawer menu item click
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.ulsan ->{
                Toast.makeText(this,"ulsan",Toast.LENGTH_SHORT).show()
            }
            R.id.busan ->{
                Toast.makeText(this,"busan",Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }
    
}