package weefire.lahtela.iki.fi.weefire

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.reactivestreams.Subscription
import weefire.lahtela.iki.fi.weefire.firebase.WeeFireDatabase
import java.security.acl.Group

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val TAG = "MainActivity"

    lateinit var chatService: ChatService
    val subscriptions = CompositeDisposable()
    lateinit var chatLinesAdapter: ArrayAdapter<String>
    var channel_subscription: Disposable? = null
    lateinit var channelAdapter: ArrayAdapter<String>
    val lines = mutableListOf("Chat started")
    val channels: MutableList<Channel> = mutableListOf()
    lateinit var db: WeeFireDatabase
    //lateinit var chatList: ListView

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
        channel_subscription?.dispose()

    }

    private val MAX_BUFFER = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        chatLinesAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, lines)
        //   channelAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, channels)
        //  channel_list.adapter = channelAdapter

        chatService = ChatService(applicationContext)
        chatlines.adapter = chatLinesAdapter
        db = WeeFireDatabase()
        subscriptions.add(db.channels().subscribe({
            Log.d(TAG, "Got $it")
            channels.clear()
            channels.addAll(it)
            channels.forEachIndexed { index, (name) -> nav_view.menu.add(Menu.FIRST, index, 0, name) }
        }))


        fab.setOnClickListener { view ->

            val token = FirebaseInstanceId.getInstance().token
            lines.clear()
            chatLinesAdapter.notifyDataSetChanged()
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        FirebaseMessaging.getInstance().subscribeToTopic("irc")
    }

    private fun setLines(id: String) {
        channel_subscription?.dispose()
        lines.clear()
        chatLinesAdapter.notifyDataSetChanged()

        channel_subscription = db.messages(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (lines.size > MAX_BUFFER) {
                        lines.removeAt(0)
                    }
                    lines.add("<${it.source}> ${it.text}")
                    chatLinesAdapter.notifyDataSetChanged()
                    chatlines.setSelection(lines.size - 1)
                })
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        setLines(channels[item.itemId].id)
        title = item.title

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
