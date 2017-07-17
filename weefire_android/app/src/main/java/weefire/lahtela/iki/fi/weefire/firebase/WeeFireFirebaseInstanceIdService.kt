package weefire.lahtela.iki.fi.weefire.firebase

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by lahte on 27.6.2017.
 */
class WeeFireFirebaseInstanceIdService : FirebaseInstanceIdService() {
    val TAG = WeeFireFirebaseInstanceIdService::javaClass.name

    override fun onTokenRefresh() {
        val token = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "refreshed token $token")
    }

}

