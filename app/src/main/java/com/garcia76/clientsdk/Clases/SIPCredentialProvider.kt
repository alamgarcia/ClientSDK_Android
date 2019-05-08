package com.garcia76.clientsdk.Clases

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import com.avaya.clientservices.credentials.Challenge
import com.avaya.clientservices.credentials.CredentialCompletionHandler
import com.avaya.clientservices.credentials.CredentialProvider
import com.avaya.clientservices.credentials.UserCredential
import com.garcia76.clientsdk.MainFrame

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
                                               credentialCompletionHandler: CredentialCompletionHandler
        ) {
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
