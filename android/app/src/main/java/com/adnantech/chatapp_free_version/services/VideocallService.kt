package com.adnantech .chatapp_free_version.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.adnantech.chatapp_free_version.ChatActivity
import com.adnantech.chatapp_free_version.HomeActivity
import com.adnantech.chatapp_free_version.R
import com.adnantech.chatapp_free_version.SocketRepository
import com.secret.wc_call.models.MessageModel
import com.secret.wc_call.utils.NewMessageInterface


class VideocallService: Service(){

    private val NOTIFICATION_CHANEL_ID = "VIDEOCALLID"
    private val NOTIFICATION_CHANEL_NAME = "VIDEOCALLNAME"
   private var message: String? = null

    private var _id:String = ""
    private var  answer: String = ""
    private var data_service :  String = ""
    private var offer : String =""


    @SuppressLint("NotificationId0")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val broadcastIntent = Intent("my_custom_action")
        broadcastIntent.putExtra("some_key", "some_data")
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)

        Toast.makeText(this, "Service is working", Toast.LENGTH_SHORT).show()
//        Log.i("myService", "TEst is working")

        message = intent?.getStringExtra("message")


        val submitIntent = Intent(this, ChatActivity::class.java)
        _id= intent?.getStringExtra("_id").toString()

        data_service =  intent?.getStringExtra("data_service").toString()

         offer =  intent?.getStringExtra("offer_recieved").toString()


        submitIntent.putExtra("_id", _id)

        submitIntent.putExtra("data_service",data_service)
        submitIntent.putExtra("name_service",message)
        submitIntent.putExtra("offer_recieved",offer)
        submitIntent.putExtra("request_id",false)
//        submitIntent.action = "accept_action"


        val submitPendingIntent = PendingIntent.getActivity(this, 0, submitIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cancelIntent = Intent(this, HomeActivity::class.java)

        val cancelPendingIntent = PendingIntent.getActivity(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        if (message==null){


            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
                .setContentTitle("Video Call Service")
                .setContentText("messege is null")
                .setSmallIcon(R.drawable.ic_action_name)
                .setAutoCancel(true)
                .build()

            startForeground(1234, notification)
        }else
        {
            showHeadsUpNotification(this, "Heads-Up Notification", message.toString()+" is calling you")

            val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
                .setSmallIcon(R.drawable.ic_action_name)
                .setContentTitle("Some one is calling you  ")
                .setContentText(message.toString()+" is calling you")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_accept, "Accept", submitPendingIntent) // Add the Submit action
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1,notification)

            startForeground(1234, notification)

        }

        // Start the service as a foreground service with the notification.

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }


    private fun createNotificationChannel() {
        // Create the notification channel for Android Oreo and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANEL_ID,
                NOTIFICATION_CHANEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }
    private fun showHeadsUpNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "MyChannelId"
            val channelName = "My Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, "MyChannelId")
            .setSmallIcon(R.drawable.ic_action_name)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }




}