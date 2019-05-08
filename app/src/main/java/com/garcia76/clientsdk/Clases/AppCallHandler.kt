package com.garcia76.clientsdk.Clases

import android.content.Context
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.avaya.clientservices.call.*
import com.garcia76.clientsdk.R

abstract class AppCallHandler: CallListener {

    companion object : CallListener {
        private lateinit var context: Context

        fun setContext(con: Context) {
            context = con
        }

        override fun onCallCapabilitiesChanged(p0: Call?) {
            Log.d("CSDK","Capacidades se la llamada han cambiado {${p0?.state}}")


        }

        override fun onCallRemoteAddressChanged(p0: Call?, p1: String?, p2: String?) {
            Log.d("CSDK","Dirección Remota ha cambiado {${p0?.state}}")



        }

        override fun onCallAudioMuteStatusChanged(p0: Call?, p1: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onCallRemoteAlerting(p0: Call?, p1: Boolean) {
            //inflate layout
            val view = View.inflate(context, R.layout.activity_main_frame, null)
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