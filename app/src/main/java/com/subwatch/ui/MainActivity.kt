package com.subwatch.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.subwatch.R
import com.subwatch.data.SubWatchDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: SubscriptionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationsIfNeeded()

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler)
        adapter = SubscriptionAdapter(
            onClick = { sub ->
                startActivity(Intent(this, EditSubscriptionActivity::class.java).apply {
                    putExtra(EditSubscriptionActivity.EXTRA_ID, sub.id)
                })
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab)
            .setOnClickListener {
                startActivity(Intent(this, EditSubscriptionActivity::class.java))
            }

        val dao = SubWatchDatabase.get().subscriptionDao()
        lifecycleScope.launch {
            dao.observeAll().collect { list ->
                adapter.submit(list)
            }
        }
    }

    private fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
