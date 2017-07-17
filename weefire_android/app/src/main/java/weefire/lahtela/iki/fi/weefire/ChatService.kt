package weefire.lahtela.iki.fi.weefire

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import weefire.lahtela.iki.fi.weefire.firebase.INTENT_IRC
import weefire.lahtela.iki.fi.weefire.firebase.MSG_INTENT


data class ChatMessage(val source: String = "", val text: String = "",
                       val gid: Long = -1, val server: String = "",
                       val timestamp: Long = -1, val channel: String = "")


data class Channel(val name: String = "", val topic: String = "",
                   val server: String = "", val id: String = "")


interface IChatService {
    fun channels(): Observable<Channel>
    fun messages(channel: Channel?): Observable<ChatMessage>
}

class ChatService : IChatService {

    val messageReceiver: BroadcastReceiver
    private val messages: Subject<ChatMessage> = BehaviorSubject.create()
    val json = jacksonObjectMapper()

    constructor(context: Context) {
        messageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val msg: ChatMessage = json.readValue(intent.getStringExtra(MSG_INTENT))
                messages.onNext(msg)
            }
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(messageReceiver,
                IntentFilter(INTENT_IRC))
    }


    override fun messages(channel: Channel?): Observable<ChatMessage> {
        return if (channel == null) {
            messages.hide()
        } else {
            messages.filter({ it.channel == channel.name }).hide()
        }
    }

    override fun channels(): Observable<Channel> {
        TODO("not implemented")
    }

}