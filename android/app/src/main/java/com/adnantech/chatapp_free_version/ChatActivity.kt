package com.adnantech.chatapp_free_version

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.adnantech.chatapp.utils.FetchImageFromInternet
import com.adnantech.chatapp_free_version.adapters.ChatAdapter
import com.adnantech.chatapp_free_version.models.FetchMessagesResponse
import com.adnantech.chatapp_free_version.models.Message
import com.adnantech.chatapp_free_version.models.SendMessageResponse
import com.adnantech.chatapp_free_version.models.User
import com.adnantech.chatapp_free_version.services.VideocallService
import com.adnantech.chatapp_free_version.utils.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.secret.wc_call.models.IceCandidateModel
import com.secret.wc_call.models.MessageModel
import com.secret.wc_call.utils.NewMessageInterface
import com.secret.wc_call.utils.PeerConnectionObserver
import com.secret.wc_call.utils.RTCAudioManager
import io.socket.client.IO
import io.socket.client.Socket
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() , NewMessageInterface {
    public  lateinit  var mediaPlayer: MediaPlayer
    lateinit  var gintent: Intent
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private var message: MessageModel?=null
    private  var isActivated: Boolean = false
//    lateinit var context: Context
    var base64 : String=""
    lateinit var imgAttachment:ImageView
    var attachmentName :  String =""
    lateinit var  layoutMyMessageInfo:RelativeLayout
    var exxtension : String = ""
    private var _id: String = ""
    private var name: String = ""
    private var phone: String = ""
    private var reciever_name:  String=""
    private val mySharedPreference: MySharedPreference = MySharedPreference()
    private lateinit var socket: Socket
    private lateinit var user: User
    private lateinit var receiver: User
    lateinit var image: ImageView
    lateinit var username : String
    lateinit var titleTv: TextView
    lateinit var loadingDialog: LoadingDialog
    var page: Int = 1
    var isLoaded: Boolean = false
    private lateinit var mediaRecorder : MediaRecorder

    var tempMediaOutput: String = ""

    var mediaState :  Boolean =  false

    private val BROADCAST_ACTION = "com.secret.wc_call.BROADCAST"

    private var socketRepository: SocketRepository?=null
    private var socketRepositorysec: SocketRepositorysec?=null

    private var rtcClient : RTCClient?=null
    private var rtcClientsec : RTCClientsec?=null

    private val TAG = "CallActivity"
    private var target:String = ""
    private val gson = Gson()
    private var isMute = false
    private var isCameraPause = false
    private val rtcAudioManager by lazy { RTCAudioManager.create(this) }
    private var isSpeakerMode = true
    val NOTIFICATION_ID = 0
    val CHANNEL_ID = "chanelId"
    val Chanel_name = "ChanelName"
    lateinit var remoteView: SurfaceViewRenderer
    lateinit var switchCameraButton:ImageView
    lateinit var targetUserNameEt : EditText
    lateinit var micButton:ImageView
    lateinit var videoButton:ImageView
    lateinit var callBtn : Button
    lateinit var  audioOutputButton : ImageView
    lateinit var endCallButton : ImageView
    lateinit var incomingCallLayout: LinearLayout
    lateinit var callLayout : RelativeLayout
    lateinit var whoToCallLayout : RelativeLayout
    lateinit var  localView : SurfaceViewRenderer
    lateinit var remoteViewLoading : ProgressBar
    lateinit var  incomingNameTV :  TextView
    lateinit var acceptButton :  ImageView
    lateinit var  rejectButton : ImageView
    private var data_sevice:Any?=""


    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("InflateParams", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.hide()
        loadingDialog = LoadingDialog(this)
        imgAttachment = findViewById(R.id.imageAttachment)
        imgAttachment.setOnClickListener{
            val intenet = Intent(Intent.ACTION_PICK)
            intenet.type="*/*"
            startActivityForResult(intenet,565)
            btnSend.setImageResource(R.drawable.ic_send)
            btnSend.tag = R.drawable.ic_send

        }

        titleTv = findViewById(R.id.title)
        image = findViewById(R.id.image)




        val backBtn: ImageView = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))

            finish()
        }

        etMessage = findViewById(R.id.etMessage)

        etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun afterTextChanged(p0: Editable?) {
                if (etMessage.text.isEmpty()) {
                    btnSend.setImageResource(R.drawable.ic_mic)
                    btnSend.tag = R.drawable.ic_mic
                } else {
                    btnSend.setImageResource(R.drawable.ic_send)
                    btnSend.tag = R.drawable.ic_send
                }
            }
        })
//        val sharedPreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
//        username = sharedPreference.getString("USERNAME","").toString()
//        Log.i("mylog_sender",username)
        recyclerView = findViewById(R.id.recyclerView)
        // this creates a vertical layout Manager
        recyclerView.layoutManager = LinearLayoutManager(this)
        tempMediaOutput =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath+"/tempRecording"+
                "MychatApp"+"_"+Date().day+"+"+Date().month+"_"+Date().year+".mp3"
        // This will pass the ArrayList to our Adapter
        adapter = ChatAdapter(this, ArrayList())

        // Setting the Adapter with the recyclerview
        recyclerView.adapter = adapter

        btnSend = findViewById(R.id.btnSend)
        btnSend.tag = R.drawable.ic_mic
        val pullToRefresh: SwipeRefreshLayout = findViewById(R.id.pullToRefresh)
        pullToRefresh.setOnRefreshListener {
            page++
            getData()
            pullToRefresh.isRefreshing = false
        }

        if (intent.hasExtra("_id")) {
            _id = intent.getStringExtra("_id").toString()
            name = intent.getStringExtra("name").toString()
            phone = intent.getStringExtra("phone").toString()
            phone = URLEncoder.encode(phone, "UTF-8")
//            Log.i("myLogisname",name)
            getData()

            val userInfo: RelativeLayout = findViewById(R.id.userInfo)
            userInfo.setOnClickListener {
                val intent: Intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra("_id", _id)
                startActivity(intent)
                finish()
            }

            btnSend.setOnClickListener {
                if(btnSend.tag == R.drawable.ic_mic){
                    Dexter.withContext(this).withPermissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                        .withListener(object: MultiplePermissionsListener{
                            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                                    if (p0?.areAllPermissionsGranted() == true){
                                        try {
                                                // micrafon iconun  deyeilmesi ucundur

                                            btnSend.tag= R.drawable.ic_stop_circle
                                            btnSend.setImageResource(R.drawable.ic_stop_circle)

                                            /// rengin deyisdirilmesi ucundur

                                            btnSend.backgroundTintList =  resources.getColorStateList(R.drawable.stop_recording)


                                            //  media recorder her defe yeni acilana qeder davam edecek recorda
                                            mediaRecorder = MediaRecorder()
                                            mediaRecorder.setOutputFile(tempMediaOutput)
                                            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)

                                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

                                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                                            mediaRecorder.prepare()

                                            mediaRecorder.start()


                                            etMessage.isEnabled=false

                                            mediaState = true
                                            }
                                        catch (e: Exception)
                                        {
                                            e.printStackTrace()
                                        }
                                    }else{
                                        Utility.showAlert(this@ChatActivity,
                                            "Permission denied",
                                            "Kindly allow to  all required  permission."
                                        )

                                    }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                p0: MutableList<PermissionRequest>?,
                                p1: PermissionToken?
                            ) {
                                TODO("Not yet implemented")
                            }
                        }).check()

                }

                else if(btnSend.tag == R.drawable.ic_stop_circle){

                    if(mediaState){
                        Log.i("Mylog",mediaState.toString())

                        mediaState = false


                            etMessage.isEnabled = true


                            btnSend.tag =  R.drawable.ic_mic
                            btnSend.setImageResource(R.drawable.ic_mic)

                            btnSend.backgroundTintList = resources.getColorStateList(R.drawable.default_recording)

                            try {
                                mediaRecorder.stop()


                                mediaRecorder.reset()


                                mediaRecorder.release()

                                Log.i("voice_log",  tempMediaOutput)

                                sendVoiceNote(tempMediaOutput)
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                        }
                }

                else  if(btnSend.tag == R.drawable.ic_send) {
                    saveMessage()
                }

            }
        }

        try {
            socket = IO.socket(mySharedPreference.getAPIURL(this))
            socket.connect()

            socket.on("messageRead") { data ->

                if (data.isNotEmpty()) {
                    runOnUiThread {
                        val message: Message =
                            Gson().fromJson(data[0].toString(), Message::class.java)
                        if (::adapter.isInitialized && ::user.isInitialized && ::recyclerView.isInitialized) {
                            val messages: ArrayList<Message> = adapter.getMessages()
                            for (msg in messages) {
                                if (msg._id == message._id) {
                                    msg.isRead = true
                                }
                            }
                            adapter.setMessages(messages, user)
                        }
                    }
                }
            }

            socket.on("newMessage") { data ->
                if (data.isNotEmpty()) {
                    runOnUiThread {
                        val message: Message =
                            Gson().fromJson(data[0].toString(), Message::class.java)
                        if (::adapter.isInitialized && ::user.isInitialized && ::recyclerView.isInitialized) {
                            adapter.appendMessage(message, user)
                            recyclerView.scrollToPosition(adapter.getMessages().size - 1)

                            val messageObject: Message = Message()
                            messageObject._id = message._id
                            messageObject.message = Utility.decryptMessage(message)
                            messageObject.sender = message.sender
                            messageObject.receiver = message.receiver
                            messageObject.attachment = message.attachment
                            messageObject.attachmentName = message.attachmentName
                            messageObject.createdAt = message.createdAt

                            socket.emit("messageRead", Gson().toJson(message))
                        }
                    }
                }
            }
        } catch (e: URISyntaxException) {
            Log.i("mylog", e.message.toString())
        }

        remoteView  =  findViewById(R.id.remote_view)
        callBtn =  findViewById(R.id.callBtn)
        switchCameraButton =  findViewById(R.id.switch_camera_button)
        micButton =  findViewById(R.id.mic_button)
        audioOutputButton= findViewById(R.id.audio_output_button)
        videoButton = findViewById(R.id.video_button)
        endCallButton =  findViewById(R.id.end_call_button)
        incomingCallLayout = findViewById(R.id.incomingCallLayout)
        callLayout =  findViewById(R.id.callLayout)
        whoToCallLayout =  findViewById(R.id.layoutBottom)
        localView =  findViewById(R.id.local_view)
        remoteViewLoading = findViewById(R.id.remote_view_loading)
        incomingNameTV  = findViewById(R.id.incomingNameTV)
        acceptButton  = findViewById(R.id.acceptButton)
        rejectButton =  findViewById(R.id.rejectButton)
        mediaPlayer =  MediaPlayer.create(this,R.raw.musics)

        val gintent = Intent(this, VideocallService::class.java)
        startService(gintent)

        val  name_service = intent.getStringExtra("name_service").toString()
        val requestSent = intent.getBooleanExtra("request_id", true)
        val data_service =  intent.getStringExtra("data_service").toString()

        val offer =  intent.getStringExtra("offer_recieved").toString()
        val sharedPreference = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        username = sharedPreference.getString("USERNAME","").toString()

        if (!requestSent) {
//            Toast.makeText(this, "if is working", Toast.LENGTH_SHORT).show()
//
//            setIncomingCallLayoutVisible()
//            incomingNameTV.text = "${name_service} is calling you"
//
//           runOnUiThread {
//
//            acceptButton.setOnClickListener {
//
//                Log.i("requestsend",data_service)
//                socketRepository = SocketRepository(this)
//                rtcClient = RTCClient(application,"cavad",socketRepository!!, object : PeerConnectionObserver() {
//                    override fun onIceCandidate(p0: IceCandidate?) {
//                        super.onIceCandidate(p0)
//                        rtcClient?.addIceCandidate(p0)
//                        val candidate = hashMapOf(
//                            "sdpMid" to p0?.sdpMid,
//                            "sdpMLineIndex" to p0?.sdpMLineIndex,
//                            "sdpCandidate" to p0?.sdp
//                        )
//                        socketRepository?.sendMessageToSocket(
//                            MessageModel("ice_candidate","kamran","cavad",candidate)
//                        )
//
//                    }
//                    override fun onAddStream(p0: MediaStream?) {
//                        super.onAddStream(p0)
//                        p0?.videoTracks?.get(0)?.addSink(remoteView)
//                        Log.d(TAG, "onAddStream: $p0")
//
//                    }
//                })
//                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
//                switchCameraButton.setOnClickListener {
//                    rtcClient?.switchCamera()
//                }
//
//                micButton.setOnClickListener {
//                    if (isMute){
//                        isMute = false
//                        micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
//                    }else{
//                        isMute = true
//                        micButton.setImageResource(R.drawable.ic_baseline_mic_24)
//                    }
//                    rtcClient?.toggleAudio(isMute)
//                }
//
//                videoButton.setOnClickListener {
//                    if (isCameraPause){
//                        isCameraPause = false
//                        videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
//                    }else{
//                        isCameraPause = true
//                        videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
//                    }
//                    rtcClient?.toggleCamera(isCameraPause)
//                }
//
//                audioOutputButton.setOnClickListener {
//                    if (isSpeakerMode){
//                        isSpeakerMode = false
//                        audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
//                        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
//                    }else{
//                        isSpeakerMode = true
//                        audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
//                        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
//
//                    }
//
//                }
//                endCallButton.setOnClickListener {
//                    setCallLayoutGone()
//                    setWhoToCallLayoutVisible()
//                    setIncomingCallLayoutGone()
//                    rtcClient?.endCall()
//                    name
//                    username
//                }
//                setIncomingCallLayoutGone()
//                setCallLayoutVisible()
//                setWhoToCallLayoutGone()
//
//                rtcClient?.initializeSurfaceView(localView)
//                rtcClient?.initializeSurfaceView(remoteView)
//                rtcClient?.startLocalVideo(localView)
//
//                val session = SessionDescription(
//                    SessionDescription.Type.OFFER,
//                    data_service
//                )
//                Log.i("onCreate",data_service)
//                rtcClient?.onRemoteSessionReceived(session)
//                rtcClient?.answer(name_service!!)
//                target = name_service!!
//                remoteViewLoading.visibility = View.GONE
//
//            }
//            rejectButton.setOnClickListener {
//                mediaPlayer.stop()
//                setIncomingCallLayoutGone()
//                // playmusic stop
//                // hidden service video call voice
//            }
//
//            Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show()
//
//        }
        }

        else{
            init(username)
        }

    }

    private fun sendVoiceNote(tempMediaOut:String) {
        var encondedStrign: String = ""

        try{
            val file = File(tempMediaOut)

            val bytes:ByteArray? = contentResolver.openInputStream(file.toUri())?.readBytes()
            encondedStrign =  Base64.encodeToString(bytes, Base64.DEFAULT)
            Log.i("myLog_encode",encondedStrign)
        }catch (e: IOException){
            e.printStackTrace()
        }

        val queue: RequestQueue  = Volley.newRequestQueue(this)


        val url = Utility.apiUri+"/chats/sendVoiceNote"

        val requestBody: String = "base64="+encondedStrign+"&userId="+_id
        Log.i("myLog",requestBody)
        val  stringRequest: StringRequest = object :StringRequest(
            Method.POST, url,
            Response.Listener { response ->

                val  sendMessageResponse:SendMessageResponse =  Gson().fromJson(response,SendMessageResponse::class.java)

                if(sendMessageResponse.status == "success"){

                    if (File(tempMediaOutput).exists()){
                        File(tempMediaOutput).delete()
                    }

                    socket.emit("newMessage",Gson().toJson(sendMessageResponse.messageObj))

                    adapter.appendMessage(sendMessageResponse.messageObj,sendMessageResponse.user)

                    recyclerView.scrollToPosition(adapter.getMessages().size-1)
                }
            },
            Response.ErrorListener { error ->
                Log.i("myLog",error.toString())

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String ,  String>()
                headers["Authorization"] =
                    "Bearer " + mySharedPreference.getAccessToken(applicationContext)
                return headers
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charset.defaultCharset())
            }
        }
        stringRequest.retryPolicy =  DefaultRetryPolicy(
            0,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 565){
            val data : Uri?   =  data?.data
            try  {
                val byte = data?.let {
                    contentResolver.openInputStream(it)?.readBytes()

                }
                base64 =  Base64.encodeToString(byte,Base64.URL_SAFE)

            }catch (Exp:IOException){

            }
            data?.let {
                attachmentName  = Utility.getFileName(it,contentResolver)

            }
            data?.let {
                exxtension = Utility.getExxtension(contentResolver, it)
            }

            Log.i("mylog",base64)
            Log.i("mylog file name",attachmentName)
            Log.i("mylog  extension",exxtension)

        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun init(name_service:String){


        val requestSent = intent.getBooleanExtra("request_id", true)

      socketRepository = SocketRepository(this)

            if (username !=null){
                socketRepository?.initSocket(username)

            }


        rtcClient = RTCClient(application,username,socketRepository!!, object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                rtcClient?.addIceCandidate(p0)
                val candidate = hashMapOf(
                    "sdpMid" to p0?.sdpMid,
                    "sdpMLineIndex" to p0?.sdpMLineIndex,
                    "sdpCandidate" to p0?.sdp
                )
                socketRepository?.sendMessageToSocket(
                    MessageModel("ice_candidate",username,name,candidate)
                )

            }
            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(remoteView)
                Log.d(TAG, "onAddStream: $p0")

            }
        })
        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)

        callBtn.setOnClickListener {
            socketRepository?.sendMessageToSocket(
                MessageModel(
                "start_call",username,name,null,"isCall"
            )
            )
            target = username
        }

        switchCameraButton.setOnClickListener {
            rtcClient?.switchCamera()
        }

        micButton.setOnClickListener {
            if (isMute){
                isMute = false
                micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
            }else{
                isMute = true
                micButton.setImageResource(R.drawable.ic_baseline_mic_24)
            }
            rtcClient?.toggleAudio(isMute)
        }

        videoButton.setOnClickListener {
            if (isCameraPause){
                isCameraPause = false
                videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
            }else{
                isCameraPause = true
                videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
            }
            rtcClient?.toggleCamera(isCameraPause)
        }

        audioOutputButton.setOnClickListener {
            if (isSpeakerMode){
                isSpeakerMode = false
                audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
            }else{
                isSpeakerMode = true
                audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)

            }

        }
        endCallButton.setOnClickListener {
            setCallLayoutGone()
            setWhoToCallLayoutVisible()
            setIncomingCallLayoutGone()
            rtcClient?.endCall()
            name
            username
            intent.putExtra("request_id",true)
            startActivity(Intent(applicationContext,ChatActivity::class.java))

        }


    }

    @SuppressLint("SuspiciousIndentation")
    override fun onNewMessage(message: MessageModel) {
        val TAG ="TagType"
        // mesele nedir  bu  activity  icra olundugunda type bilinir hansidir  lakin  servisden geri qayidanda belli olmur ona gore message.type burdan service gonder bunu  type elave et
        Log.e("MyNewMessage",message.type)

        when(message.type){
            "call_response"->{
                if (message.data == "user is not online"){
                    //user is not reachable
                    runOnUiThread {
                        Toast.makeText(this,"user is not reachable",Toast.LENGTH_LONG).show()

                    }
                }else{
                    //we are ready for call, we started a call
                    runOnUiThread {
                        setWhoToCallLayoutGone()
                        setCallLayoutVisible()

                        rtcClient?.initializeSurfaceView(localView)
                        rtcClient?.initializeSurfaceView(remoteView)
                        rtcClient?.startLocalVideo(localView)
                        rtcClient?.call(name)

                    }

                }
            }
            "answer_received" ->{

//                val session = SessionDescription(
//                    SessionDescription.Type.ANSWER,
//                    message.data.toString()
//                )
//                rtcClient?.onRemoteSessionReceived(session)
//                runOnUiThread {
//                    Log.i(TAG,"answer")
//                    playMusic(false)
//                    remoteViewLoading.visibility = View.GONE
//                }
            }

            "offer_received" ->{
                runOnUiThread {
                    Log.i(TAG,message.type)

                    val gintent = Intent(this, VideocallService::class.java)

                    gintent.putExtra("message", message.name.toString())
                    gintent.putExtra("_id",_id)
//                    gintent.putExtra("offer","offer_received")
//
                    data_sevice = message.data
//
                    gintent.putExtra("data_service",data_sevice.toString())
                    val offer :String = "Offer"
                    gintent.putExtra("offer_recieved",offer)
                    startService(gintent)
                    Log.i("Offer3",message.data.toString())

                    setIncomingCallLayoutVisible()
                    mediaPlayer.start()
                    val intent = Intent(this, VideocallService::class.java)

                    incomingNameTV.text = "${message.name.toString()} is calling you"

                        acceptButton.setOnClickListener {
                            Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show()

                            setIncomingCallLayoutGone()
                            setCallLayoutVisible()
                            setWhoToCallLayoutGone()

                            rtcClient?.initializeSurfaceView(localView)
                            rtcClient?.initializeSurfaceView(remoteView)
                            rtcClient?.startLocalVideo(localView)
                            Log.i("Offer1",message.data.toString())

                            val session = SessionDescription(
                                SessionDescription.Type.OFFER,
                                message.data.toString()
                            )
                            Log.i("Offer2",session.toString())

                            rtcClient?.onRemoteSessionReceived(session)
                            rtcClient?.answer(message.name!!)
                            target = message.name!!
                            Log.i("Offer4",target)
                            remoteViewLoading.visibility = View.GONE

                        }
                    rejectButton.setOnClickListener {
                        mediaPlayer.stop()
                        setIncomingCallLayoutGone()
                        // playmusic stop
                       // hidden service video call voice
                    }

                }

            }

            "ice_candidate"->{
                try {
                    Log.i(TAG,message.type)
                    val receivingCandidate = gson.fromJson(gson.toJson(message.data), IceCandidateModel::class.java)
                    Log.i(TAG,message.data.toString())
                    rtcClient?.addIceCandidate(IceCandidate(receivingCandidate.sdpMid,
                        Math.toIntExact(receivingCandidate.sdpMLineIndex.toLong()),
                        receivingCandidate.sdpCandidate))
                }
                catch (e:Exception){
                    e.printStackTrace()
                }
            }


        }
    }


    private fun setIncomingCallLayoutGone(){
        playMusic(false)
        incomingCallLayout.visibility = View.GONE
    }
    private fun setIncomingCallLayoutVisible() {


        incomingCallLayout.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone() {
        callLayout.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        callLayout.visibility = View.VISIBLE
    }

    private fun setWhoToCallLayoutGone() {
        whoToCallLayout.visibility = View.GONE
    }

    private fun setWhoToCallLayoutVisible() {
        whoToCallLayout.visibility = View.VISIBLE
    }


    private fun getData() {
        loadingDialog.show()

        val request: RequestQueue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this) + "/chats/fetch"
        val requestBody: String = "userId=" + _id + "&page=" + page

        val queue: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                val fetchMessagesResponse: FetchMessagesResponse =
                    Gson().fromJson(response, FetchMessagesResponse::class.java)
                if (fetchMessagesResponse.status == "success") {
                    user = fetchMessagesResponse.user
                    receiver = fetchMessagesResponse.receiver

                    if (!isLoaded) {
                        adapter.setReceiver(receiver)
                        titleTv.text = receiver.name
                        reciever_name =  reciever_name
                        socket.emit("connected", user._id)
                        socket.emit("allMessagesRead", receiver._id, user._id)

                        if (receiver.image.isEmpty()) {
                            image.setImageResource(R.drawable.default_profile)
                        } else {
                            FetchImageFromInternet(image).execute(receiver.image)
                        }

                        socket.on("allMessagesRead-" + receiver._id) { data ->

                            if (data.isNotEmpty()) {
                                runOnUiThread {
                                    if (::adapter.isInitialized && ::user.isInitialized && ::recyclerView.isInitialized) {

                                        val messages: ArrayList<Message> = adapter.getMessages()
                                        for (msg in messages) {
                                            msg.isRead = true
                                        }
                                        adapter.setMessages(messages, user)
                                    }
                                }
                            }
                        }
                    }

                    if (isLoaded) {
                        for (message in fetchMessagesResponse.messages.reversed()) {
                            adapter.prependMessage(message)
                        }
                    } else {
                        adapter.setMessages(fetchMessagesResponse.messages, user)
                        recyclerView.scrollToPosition(fetchMessagesResponse.messages.size - 1)
                    }
                    isLoaded = true
                } else {
                    Utility.showAlert(this, "Error", fetchMessagesResponse.message)
                }

                loadingDialog.dismiss()
            },

            Response.ErrorListener { error ->
                Log.i("myLog", "error => " + error)
                loadingDialog.dismiss()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer " + mySharedPreference.getAccessToken(applicationContext)
                return headers
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charset.defaultCharset())
            }
        }
        request.add(queue)
    }

    private fun saveMessage() {
        btnSend.isEnabled = false

        val request: RequestQueue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this)+ "/chats/send"
        val requestBody="message=" + etMessage.text+"&userId="+_id+
                "&base64="+base64+"&attachment="+attachmentName+"&extension="+exxtension

        val stringRequest:StringRequest =
        object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                val sendMessageResponse: SendMessageResponse =
                    Gson().fromJson(response, SendMessageResponse::class.java)

                btnSend.isEnabled = true

                etMessage.setText("")
                base64=""
                exxtension=""
                attachmentName=""

                if (sendMessageResponse.status == "success") {
                    socket.emit("newMessage", Gson().toJson(sendMessageResponse.messageObj))

                    adapter.appendMessage(sendMessageResponse.messageObj, sendMessageResponse.user)
                    recyclerView.scrollToPosition(adapter.getMessages().size - 1)

                    val messageObject: Message = Message()
                    messageObject._id = sendMessageResponse.messageObj._id
                    messageObject.message = etMessage.text.toString()
                    messageObject.sender = sendMessageResponse.messageObj.sender
                    messageObject.receiver = sendMessageResponse.messageObj.receiver
                    messageObject.attachment = sendMessageResponse.messageObj.attachment
                    messageObject.attachmentName = sendMessageResponse.messageObj.attachmentName
                    messageObject.createdAt = sendMessageResponse.messageObj.createdAt
                    messageObject.extension =  sendMessageResponse.messageObj.extension
                    etMessage.setText("")
                } else {
                    Utility.showAlert(this, "Error", sendMessageResponse.message)
                }
            },

            Response.ErrorListener { error ->
                Log.i("myLog", "error => " + error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer " + mySharedPreference.getAccessToken(applicationContext)
                return headers
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charset.defaultCharset())
            }
        }
        request.add(stringRequest)
    }


    fun playMusic(play:Boolean){


        if (play == true){
            mediaPlayer.start()

        }else if ( mediaPlayer.isPlaying){
            mediaPlayer.stop()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        socket.disconnect()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
        return true
    }
}
