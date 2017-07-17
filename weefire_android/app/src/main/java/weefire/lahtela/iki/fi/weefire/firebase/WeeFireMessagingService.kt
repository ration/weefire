package weefire.lahtela.iki.fi.weefire.firebase

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

val MSG_INTENT = "message"
var INTENT_IRC = "IRC"

class WeeFireMessagingService : FirebaseMessagingService() {
    val TAG = "WeeFireMessagingService"
    val broadcaster: LocalBroadcastManager = LocalBroadcastManager.getInstance(this)

    override fun onMessageReceived(msg: RemoteMessage?) {
        if (msg != null) {
            Log.d(TAG, "Message reveived ${msg.data}")
            if (msg.data != null && msg.data.size != 0) {
               // broadcaster.sendBroadcast()
            } else if (msg.notification != null) {
                val intent = Intent(INTENT_IRC)
                intent.putExtra("data", intent.putExtra(MSG_INTENT, msg.notification.body))
                Log.d(TAG, "broadcasting ${msg.notification.body}")
                broadcaster.sendBroadcast(intent)
            }
        }
    }
}