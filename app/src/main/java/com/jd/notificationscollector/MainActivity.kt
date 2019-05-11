package com.jd.notificationscollector

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.jd.notificationscollector.model.Notification

class MainActivity : AppCompatActivity() {

    companion object {
        private const val NUMBER_OF_NOTIFICATIONS_PER_PAGE = 50
        private const val INITIAL_NUMBER_OF_NOTIFICATIONS = 100
    }

    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var db: NotificationsCollectorDatabase

    private var dataset: MutableList<Notification> = mutableListOf()
    private var notificationsCount = INITIAL_NUMBER_OF_NOTIFICATIONS

    private val onLoadMoreClick = View.OnClickListener {
        loadMoreNotifications()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = NotificationsCollectorDatabase(applicationContext)

        swipeContainer = findViewById(R.id.notifications_swipe_container)
        swipeContainer.setOnRefreshListener {
            refresh()
            swipeContainer.isRefreshing = false
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = NotificationsRecyclerAdapter(dataset, onLoadMoreClick, this)
        recyclerView = findViewById<RecyclerView>(R.id.notifications_recycler).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.btn_clear -> {
            AlertDialog.Builder(this)
                .setTitle(R.string.clear_confirm_title)
                .setMessage(R.string.clear_confirm_message)
                .setPositiveButton(R.string.confirm_positive) { _, _ ->
                    clearNotifications()
                }
                .setNegativeButton(R.string.confirm_negative, null)
                .show()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun refresh() {
        notificationsCount = INITIAL_NUMBER_OF_NOTIFICATIONS
        dataset.clear()
        dataset.addAll(db.findNotifications(notificationsCount))
        viewAdapter.notifyDataSetChanged()
    }

    private fun clearNotifications() {
        db.clearNotificationsTable()
        refresh()
    }

    private fun loadMoreNotifications() {
        notificationsCount += NUMBER_OF_NOTIFICATIONS_PER_PAGE
        dataset.clear()
        dataset.addAll(db.findNotifications(notificationsCount))
        viewAdapter.notifyDataSetChanged()
    }

}
