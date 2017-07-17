package weefire.lahtela.iki.fi.weefire

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class ChatMessageAdapter : ArrayAdapter<ChatMessage> {

    constructor(context: Context,
                resourceId: Int,
                items: List<ChatMessage>) : super(context, resourceId, items)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return super.getView(position, convertView, parent)
    }

}