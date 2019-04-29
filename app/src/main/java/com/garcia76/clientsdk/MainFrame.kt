package com.garcia76.clientsdk

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.inflate
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.avaya.clientservices.call.*
import com.avaya.clientservices.client.Client
import com.avaya.clientservices.client.ClientConfiguration
import com.avaya.clientservices.client.CreateUserCompletionHandler
import com.avaya.clientservices.client.UserCreatedException
import com.avaya.clientservices.common.ConnectionPolicy
import com.avaya.clientservices.common.SignalingServer
import com.avaya.clientservices.credentials.Challenge
import com.avaya.clientservices.credentials.CredentialCompletionHandler
import com.avaya.clientservices.credentials.CredentialProvider
import com.avaya.clientservices.credentials.UserCredential
import com.avaya.clientservices.user.User
import com.avaya.clientservices.user.UserConfiguration
import kotlinx.android.synthetic.main.activity_main_frame.*
import java.util.*
import com.avaya.clientservices.call.CallService
import com.garcia76.clientsdk.MainFrame.AppCallServiceHandler
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import com.tapadoo.alerter.Alerter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "Speech"



class MainFrame : AppCompatActivity() {
    companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
    var clientConfiguration: ClientConfiguration? = null
    var useruuid: String? = null
    val myPreferences = "myPrefs"
    private var mPermissionToRecord = true
    private var mAudioEmitter: AudioEmitter? = null
    private lateinit var mTextView: TextSwitcher
    private val mSpeechClient by lazy {
        applicationContext.resources.openRawResource(R.raw.credential).use {
            SpeechClient.create(SpeechSettings.newBuilder()
                .setCredentialsProvider { GoogleCredentials.fromStream(it) }
                .build())
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_frame)

        SIPCredentialProvider.setContext(this)
        AppCallHandler.setContext(this)
        AppCallServiceHandler.setContext(this)

        mTextView = findViewById(R.id.textView14)
        mTextView.setFactory {
            val t = TextView(this)
            t.text = "Escuchando"
            t.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            t.setTextAppearance(android.R.style.TextAppearance_Large)
            t
        }
        mTextView.setInAnimation(applicationContext, android.R.anim.fade_in)
        mTextView.setOutAnimation(applicationContext, android.R.anim.fade_out)


        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        clientConfiguration = ClientConfiguration("/storage/sdcard0/avayaclientservices",
                "Avaya Vantage Basic", "Avaya",
                Build.MODEL, Build.VERSION.RELEASE, "2.0.0", "Avaya")
        useruuid = sharedPreferences.getString("uuid_agent", "")

        if (Funciones().isNullOrEmpty(useruuid)){
            Log.d("CSDK","No se ha encontrado UUID Generando")
            useruuid = UUID.randomUUID().toString()
            Log.d("CSDK","UUID Generado $useruuid")
            val editor = sharedPreferences.edit()
            editor.putString("uuid_agent", useruuid)
            editor.apply()
            Log.d("CSDK","UUID Guardado")

        }
        else
        {
            Log.d("CSDK","UUID encontrado: $useruuid")

        }

        val username = sharedPreferences.getString("username", "")
        val sipcontroller = sharedPreferences.getString("sipcontroller", "sip-na1.avaya.com")
        val sipport = sharedPreferences.getInt("sipport", 5061)
        val sipdomain = sharedPreferences.getString("sipdomain", "net.avaya.com")

        Log.d("CSDK", username)
        Log.d("CSDK", sipcontroller)
        Log.d("CSDK", sipport.toString())
        Log.d("CSDK", sipdomain)

        var client = Client(clientConfiguration, this.application, ApplicationClientListener())
        var userConfiguration = UserConfiguration()
        var sipConfiguration = userConfiguration.sipUserConfiguration
        sipConfiguration.isEnabled = true
        sipConfiguration.userId = username
        sipConfiguration.domain = sipdomain


        val signalingServer = SignalingServer(SignalingServer.TransportType.TLS,
                sipcontroller, // Provided by your administrator
                sipport, // Provided by your administrator
                SignalingServer.FailbackPolicy.AUTOMATIC)                // Allow the Client SDK to manage failback
        sipConfiguration.connectionPolicy = ConnectionPolicy(signalingServer)
        sipConfiguration.credentialProvider = SIPCredentialProvider
        userConfiguration.sipUserConfiguration = sipConfiguration
        client.createUser(userConfiguration, object : CreateUserCompletionHandler {
            override fun onSuccess(user: User) {
                var mUser = user
                mUser.start()
                var callServiceHandler = AppCallServiceHandler
                var callService = mUser.callService
                Log.d("CSDK", "Creado Servicio de llamada")
                callService.addListener(callServiceHandler)

                dialButton.setOnClickListener {
                    var call = callService.createCall()
                    call.remoteAddress = phone_number.text.toString()
                    call.start()
                    deleteButton.setOnClickListener {
                        var callService = mUser!!.callService
                        callService.activeCall.end()
                    }
                }
            }

            override fun onError(error: UserCreatedException) {
                Log.d("CSDK", "Failed to create user", error)
                val dialogBuilder = AlertDialog.Builder(this@MainFrame)
                dialogBuilder.setMessage("Ha ocurrido un eror ${error.failureReason}")
                    .setPositiveButton("Aceptar") { dialog, id -> finish()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("Ha ocurrido un error")
                alert.show()
            }
        })

    }


    override fun onResume() {
        super.onResume()
        if (mPermissionToRecord) {
            val isFirstRequest = AtomicBoolean(true)
            mAudioEmitter = AudioEmitter()
            val requestStream = mSpeechClient.streamingRecognizeCallable()
                .bidiStreamingCall(object : ApiStreamObserver<StreamingRecognizeResponse> {
                    override fun onNext(value: StreamingRecognizeResponse) {
                        runOnUiThread {
                            when {
                                value.resultsCount > 0 -> mTextView.setText(value.getResults(0).getAlternatives(0).transcript)
                                else -> mTextView.setText("error")
                            }
                        }
                    }

                    override fun onError(t: Throwable) {
                        Log.e(TAG, "an error occurred", t)
                    }

                    override fun onCompleted() {
                        Log.d(TAG, "stream closed")
                    }
                })
            mAudioEmitter!!.start { bytes ->
                val builder = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(bytes)
                if (isFirstRequest.getAndSet(false)) {
                    builder.streamingConfig = StreamingRecognitionConfig.newBuilder()
                        .setConfig(RecognitionConfig.newBuilder()
                            .setLanguageCode("es-MX")
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(16000)
                            .build())
                        .setInterimResults(false)
                        .setSingleUtterance(false)
                        .build()
                }
                requestStream.onNext(builder.build())
            }
        } else {
            Log.e(TAG, "No permission to record! Please allow and then relaunch the app!")
        }
    }

    abstract class AppCallHandler:CallListener {

        companion object : CallListener {
            private lateinit var context: Context

            fun setContext(con: Context) {
                context = con
            }

            override fun onCallCapabilitiesChanged(p0: Call?) {


            }

            override fun onCallRemoteAddressChanged(p0: Call?, p1: String?, p2: String?) {


            }

            override fun onCallAudioMuteStatusChanged(p0: Call?, p1: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallRemoteAlerting(p0: Call?, p1: Boolean) {
                //inflate layout
                val view = inflate(context, R.layout.activity_main_frame, null)
                val tv = view.findViewById<AppCompatTextView>(R.id.status_txt)
                tv.post { tv.text = "Llamando"}
            }

            override fun onCallIncomingVideoAddRequestTimedOut(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallHeld(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallServiceUnavailable(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallEnded(p0: Call?, p1: CallEndReason?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallPreempted(p0: Call?, p1: CallPreemptionReason?, p2: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallStarted(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallVideoChannelsUpdated(p0: Call?, p1: MutableList<VideoChannel>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallUnheld(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallUnheldRemotely(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallSpeakerSilenceStatusChanged(p0: Call?, p1: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallRedirected(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallIncomingVideoAddRequestAccepted(p0: Call?, p1: VideoChannel?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallConferenceStatusChanged(p0: Call?, p1: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallJoined(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallDenied(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallAllowedVideoDirectionChanged(p0: Call?, p1: AllowedVideoDirection?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallFailed(p0: Call?, p1: CallException?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallParticipantMatchedContactsChanged(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallDigitCollectionPlayDialTone(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallEstablished(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallQueued(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallDigitCollectionCompleted(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallIncomingVideoAddRequestReceived(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallPrecedenceLevelChanged(p0: Call?, p1: CallPrecedenceLevel?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallExtraPropertiesChanged(p0: Call?, p1: MutableMap<String, String>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallServiceAvailable(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallHeldRemotely(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallIncomingVideoAddRequestDenied(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCallIgnored(p0: Call?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }

    abstract class SIPCredentialProvider: CredentialProvider {
        companion object : CredentialProvider {
            private lateinit var context: Context

            fun setContext(con: Context) {
                context=con
            }
            override fun onAuthenticationChallengeCancelled(p0: Challenge?) {
                Log.d("CSDK", "Chanllenge Recibido: ${p0?.failureCount}")

            }

            override fun onCredentialAccepted(p0: Challenge?) {
                Log.d("CSDK","Credenciuales Aceptadas")


            }

            override fun onAuthenticationChallenge(challenge: Challenge,
                                                   credentialCompletionHandler: CredentialCompletionHandler) {
                Log.d("CSDK", "UserCredentialProvider.onAuthenticationChallenge : Challenge = $challenge")
                var myPreferences = "myPrefs"
                var sharedPreferences = context.getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
                var user = sharedPreferences.getString("username", "")
                var password = sharedPreferences.getString("password", "")
                var domain = sharedPreferences.getString("sipdomain", "")
                val userCredential = UserCredential(user, password, domain)
                credentialCompletionHandler.onCredentialProvided(userCredential)
                Log.d("CSDK", challenge.failureCount.toString())
                if (challenge.failureCount >= 1){
                    val dialogBuilder = AlertDialog.Builder(context)
                    dialogBuilder.setMessage("Credenciales incorrectas")
                        .setPositiveButton("Aceptar") { dialog, id -> MainFrame().finish()
                        }
                    val alert = dialogBuilder.create()
                    alert.setTitle("Ha ocurrido un error")
                    alert.show()
                }
            }
        }
    }

    abstract class AppCallServiceHandler:CallServiceListener {
        companion object : CallServiceListener {
            private lateinit var context: Activity
            fun setContext(con: Activity) {
                context = con
            }

            override fun onActiveCallChanged(p0: CallService?, p1: Call?) {
                Log.d("CSDK-CSListen", "Llamada activa ha cambiado")
                context.status_txt.text = "Llamada Iniciada"
                context.deleteButton.setOnClickListener {
                    p1?.end()
                }
            }

            override fun onCallServiceCapabilityChanged(p0: CallService?) {
                Log.d("CSDK-CSListen", "Capacidad en el servicio de llamada ha cambiado")
                Log.d("CSDK-CSListen", p0?.voIPCallingCapability.toString())

            }

            override fun onCallCreated(p0: CallService?, p1: Call?) {
                Log.d("CSDK-CSListen", "Llamada Creada")

            }

            override fun onIncomingCallUndelivered(p0: CallService?, p1: Call?) {
                Log.d("CSDK-CSListen", "Llamada entrante sin entregar")

            }

            override fun onIncomingCallReceived(p0: CallService?, p1: Call?) {
                Log.d("CSDK-CSListen", "Llamada entrante")
                Log.d("CSDK-CSListen", p1?.remoteNumber.toString())
                Alerter.create(context)
                        .setTitle("Llamada entrante: ${p1?.remoteNumber} ")
                        .setText("Asunto: ${p1?.subject}")
                        .setDuration(10000)
                        .addButton("Audio", R.style.AlertButton, View.OnClickListener {
                            p1?.accept()
                            Alerter.hide()

                        })
                        .addButton("Video", R.style.AlertButton, View.OnClickListener {
                        })
                        .addButton("Colgar", R.style.AlertButton, View.OnClickListener {
                            p1?.ignore()
                            Alerter.hide()
                        })
                        .show()
            }

            override fun onCallRemoved(p0: CallService?, p1: Call?) {
                Log.d("CSDK-CSListen", "Se ha removido la llamada")
                context.status_txt.text = "Llamada Terminada"
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            mPermissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!mPermissionToRecord) {
            finish()
        }
    }

    internal class AudioEmitter {

        private var mAudioRecorder: AudioRecord? = null
        private var mAudioExecutor: ScheduledExecutorService? = null
        private lateinit var mBuffer: ByteArray
        fun start(
            encoding: Int = AudioFormat.ENCODING_PCM_16BIT,
            channel: Int = AudioFormat.CHANNEL_IN_MONO,
            sampleRate: Int = 8000,
            subscriber: (ByteString) -> Unit
        ) {
            mAudioExecutor = Executors.newSingleThreadScheduledExecutor()

            mAudioRecorder = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(encoding)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channel)
                        .build())
                .build()
            mBuffer = ByteArray(2 * AudioRecord.getMinBufferSize(sampleRate, channel, encoding))
            // inicio
            Log.d(TAG, "Recording audio with buffer size of: ${mBuffer.size} bytes")
            mAudioRecorder!!.startRecording()
            mAudioExecutor!!.scheduleAtFixedRate({
                val read = mAudioRecorder!!.read(
                    mBuffer, 0, mBuffer.size, AudioRecord.READ_BLOCKING)

                if (read > 0) {
                    subscriber(ByteString.copyFrom(mBuffer, 0, read))
                }
            }, 0, 10, TimeUnit.MILLISECONDS)
        }

        fun stop() {
            // stop events
            mAudioExecutor?.shutdown()
            mAudioExecutor = null

            // stop recording
            mAudioRecorder?.stop()
            mAudioRecorder?.release()
            mAudioRecorder = null
        }
    }
}
