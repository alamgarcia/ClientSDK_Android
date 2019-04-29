package com.garcia76.clientsdk

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.inflate
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
import com.tapadoo.alerter.Alerter


class MainFrame : AppCompatActivity() {
    var clientConfiguration: ClientConfiguration? = null
    var useruuid:String? = null
    val myPreferences = "myPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_frame)

        SIPCredentialProvider.setContext(this)
        AppCallHandler.setContext(this)
        AppCallServiceHandler.setContext(this)


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
                // build alert dialog
                val dialogBuilder = AlertDialog.Builder(this@MainFrame)
                // set message of alert dialog
                dialogBuilder.setMessage("Ha ocurrido un eror ${error.failureReason}")
                    // if the dialog is cancelable

                    // positive button text and action
                    .setPositiveButton("Aceptar") { dialog, id -> finish()
                    }


                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Ha ocurrido un error")
                // show alert dialog
                alert.show()

            }
        })

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
                    // build alert dialog
                    val dialogBuilder = AlertDialog.Builder(context)
                    // set message of alert dialog
                    dialogBuilder.setMessage("Credenciales incorrectas")
                        // if the dialog is cancelable

                        // positive button text and action
                        .setPositiveButton("Aceptar") { dialog, id -> MainFrame().finish()
                        }


                    // create dialog box
                    val alert = dialogBuilder.create()
                    // set title for alert dialog box
                    alert.setTitle("Ha ocurrido un error")
                    // show alert dialog
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
}
