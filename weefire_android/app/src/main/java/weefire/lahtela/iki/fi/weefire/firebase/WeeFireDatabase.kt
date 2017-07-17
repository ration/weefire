package weefire.lahtela.iki.fi.weefire.firebase

import android.util.Log
import com.google.firebase.database.*
import io.reactivex.Observable
import weefire.lahtela.iki.fi.weefire.Channel
import weefire.lahtela.iki.fi.weefire.ChatMessage


class WeeFireDatabase {

    val TAG = "WeeFireDatabase"

    private val USER = "1"

    fun messages(id: String): Observable<ChatMessage> {
        return observeChildren(own_root().child("channels").child(id))
    }

    fun channels(): Observable<List<Channel>> {
        Log.d(TAG, "Reading channel list")
        return observeAsList(own_root().child("channel_list"))
    }

    private fun own_root() = FirebaseDatabase.getInstance().reference.child("users").child(USER)

    inline private fun <reified T : Any> observeAsList(dbRef: DatabaseReference): Observable<List<T>> {
        return Observable.create { subscriber ->
            run {
                dbRef.addValueEventListener(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChildren()) {
                                    val list: List<T> = snapshot.children.
                                            map { it.getValue(T::class.java) }.toList().filterNotNull()
                                    subscriber.onNext(list)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                subscriber.onError(error.toException())
                            }
                        })
            }
        }
    }

    inline private fun <reified T : Any> observeChildren(dbRef: DatabaseReference): Observable<T> {
        return Observable.create { subscriber ->
            run {
                dbRef.addChildEventListener(
                        object : ChildEventListener {
                            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                            }

                            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                            }

                            override fun onChildRemoved(p0: DataSnapshot?) {
                            }

                            override fun onChildAdded(snapshot: DataSnapshot, prevKey: String) {
                                if (snapshot.value != null) {
                                    val data: T? = snapshot.getValue(T::class.java)
                                    if (data != null) {
                                        subscriber.onNext(data)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                subscriber.onError(error.toException())
                            }
                        })
            }
        }
    }
}