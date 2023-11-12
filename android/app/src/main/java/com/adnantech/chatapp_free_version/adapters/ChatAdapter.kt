package com.adnantech.chatapp_free_version.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.adnantech.chatapp.utils.FetchImageFromInternet
import com.adnantech.chatapp_free_version.R
import com.adnantech.chatapp_free_version.models.Message
import com.adnantech.chatapp_free_version.models.User
import com.adnantech.chatapp_free_version.utils.Utility
import com.bumptech.glide.Glide
import java.io.IOException
import java.util.*


class ChatAdapter(
    var context: Context,
    private var messages: ArrayList<Message>
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private lateinit var user: User
    private lateinit var receiver: User

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_message, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Message = messages[position]

        val isMyMessage: Boolean = (item.sender._id == user._id)
        if(item.audioPath.isEmpty() && item.attachment.isEmpty()){
            holder.message.isVisible =  true
            holder.otherMessage.isVisible = true
            holder.Attachment.isVisible=false
            holder.otherAttachment.isVisible = false
            holder.myPlayIcon.isVisible =  false
            holder.otherPlayIcon.isVisible =  false
        }
        else if(item.attachment.isEmpty()== false){
            holder.myPlayIcon.isVisible =  false
            holder.otherPlayIcon.isVisible =  false

            FetchImageFromInternet(holder.Attachment).execute(item.attachment)
            FetchImageFromInternet(holder.otherAttachment).execute(item.attachment)
        }
        else {
            holder.message.isVisible = false
            holder.otherMessage.isVisible = false
            holder.Attachment.isVisible=false
            holder.otherAttachment.isVisible = false

            if (isMyMessage) {
                holder.myPlayIcon.isVisible = true
                holder.otherPlayIcon.isVisible = false
            } else {
                holder.myPlayIcon.isVisible = false
                holder.otherPlayIcon.isVisible = true
            }

            val mediaPlayer =  MediaPlayer()

            try{
                mediaPlayer.setDataSource(item.audioPath)
                mediaPlayer.prepare()
                mediaPlayer.setOnCompletionListener {

                    if (isMyMessage){
                        holder.myPlayIcon.setImageResource(R.drawable.ic_play_arrow)
                    }
                    else{
                        holder.otherPlayIcon.setImageResource(R.drawable.ic_play_arrow)
                    }
                }

                if (isMyMessage){
                    holder.myPlayIcon.setOnClickListener{
                        if (mediaPlayer.isPlaying){
                            mediaPlayer.pause()
                            holder.myPlayIcon.setImageResource(R.drawable.ic_play_arrow)
                        }else{
                            mediaPlayer.start()
                            holder.myPlayIcon.setImageResource(R.drawable.ic_pause)
                        }
                    }
                }else{
                    holder.otherPlayIcon.setOnClickListener {
                        if (mediaPlayer.isPlaying){
                            mediaPlayer.pause()
                            holder.otherPlayIcon.setImageResource(R.drawable.ic_play_arrow)
                        }else{
                            mediaPlayer.start()
                            holder.otherPlayIcon.setImageResource(R.drawable.ic_pause)

                        }
                    }
                }
            }catch (e:IOException){
                Log.i("mylog",e.message.toString())}
        }
        holder.message.text = item.message
        holder.otherMessage.text = item.message

        holder.time.text = DateUtils.getRelativeTimeSpanString(
            item.createdAt,
            Date().time,
            0L,
            DateUtils.FORMAT_ABBREV_ALL
        )
        holder.otherTime.text = DateUtils.getRelativeTimeSpanString(
            item.createdAt,
            Date().time,
            0L,
            DateUtils.FORMAT_ABBREV_ALL
        )

        if (isMyMessage) {
            holder.myLayout.isVisible = true
            holder.otherLayout.isVisible = false
        } else {
            holder.myLayout.isVisible = false
            holder.otherLayout.isVisible = true
        }
    }

    fun getMessages(): ArrayList<Message> {
        return this.messages
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(messages: ArrayList<Message>, user: User) {
        this.messages = messages
        this.user = user
        notifyDataSetChanged()
    }

    fun prependMessage(message: Message) {
        this.messages.add(0, message)
        notifyItemInserted(0)
    }

    fun setReceiver(receiver: User) {
        this.receiver = receiver
    }

    @SuppressLint("NotifyDataSetChanged")
    fun appendMessage(message: Message, user: User) {
        this.messages.add(message)
        this.user = user
        notifyItemInserted(this.messages.size)
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return messages.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val message: TextView = itemView.findViewById(R.id.message)
        val otherMessage: TextView = itemView.findViewById(R.id.otherMessage)
        val myLayout: RelativeLayout = itemView.findViewById(R.id.myLayout)
        val otherLayout: RelativeLayout = itemView.findViewById(R.id.otherLayout)
        val time: TextView = itemView.findViewById(R.id.time)
        val otherTime: TextView = itemView.findViewById(R.id.otherTime)
        val otherAttachment: ImageView  = itemView.findViewById(R.id.otherAttachment)
        val Attachment: ImageView =  itemView.findViewById(R.id.attachment)
        val  myPlayIcon : ImageView =   itemView.findViewById(R.id.myPlayIcon)
        val otherPlayIcon:ImageView  = itemView.findViewById(R.id.otherPlayIcon)
    }
}
