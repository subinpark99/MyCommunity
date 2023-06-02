package com.example.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
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
    }

    private fun initNavi(){

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


    // drawer menu item click
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.ulsan->{
                Toast.makeText(this,"ulsan",Toast.LENGTH_SHORT).show()
            }
            R.id.busan->{
                Toast.makeText(this,"busan",Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }


}