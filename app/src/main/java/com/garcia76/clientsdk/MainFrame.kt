package com.garcia76.clientsdk

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.avaya.clientservices.client.Client
import com.avaya.clientservices.client.ClientConfiguration
import com.avaya.clientservices.credentials.Challenge
import com.avaya.clientservices.credentials.CredentialCompletionHandler
import com.avaya.clientservices.credentials.CredentialProvider
import com.avaya.clientservices.credentials.UserCredential
import com.avaya.clientservices.user.UserConfiguration
import java.util.*


class MainFrame : AppCompatActivity() {
    var clientConfiguration: ClientConfiguration? = null
    var useruuid:String? = null
    val myPreferences = "myPrefs"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_frame)

        SIPCredentialProvider.setContext(this);
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

        var client = Client(clientConfiguration, application, ApplicationClientListener())
        var userConfiguration = UserConfiguration()
        var sipConfiguration = userConfiguration.sipUserConfiguration
        sipConfiguration.isEnabled = true
        sipConfiguration.userId = "5255552782823"



    }

    abstract class SIPCredentialProvider: CredentialProvider {
        companion object {

            private lateinit var context: Context

            fun setContext(con: Context) {
                context=con
            }
        }

        override fun onAuthenticationChallengeCancelled(p0: Challenge?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        }
    }

}
