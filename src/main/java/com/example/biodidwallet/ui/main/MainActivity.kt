package com.example.biodidwallet.ui.main

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.biodidwallet.R
import com.example.biodidwallet.ui.credentials.CredentialListFragment
import com.example.biodidwallet.ui.identity.DIDListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        
        // 默认显示身份列表
        if (savedInstanceState == null) {
            loadFragment(DIDListFragment())
        }
        
        // 观察选中的DID变化
        viewModel.selectedDID.observe(this) { didInfo ->
            val actionBar = supportActionBar
            if (didInfo != null) {
                actionBar?.title = didInfo.name
                actionBar?.subtitle = didInfo.id
            } else {
                actionBar?.title = getString(R.string.app_name)
                actionBar?.subtitle = null
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_identity -> loadFragment(DIDListFragment())
            R.id.nav_credentials -> loadFragment(CredentialListFragment())
            R.id.nav_scan -> {
                // TODO: 实现扫描功能
                true
            }
            R.id.nav_settings -> {
                // TODO: 实现设置界面
                true
            }
            else -> false
        }
    }
} 