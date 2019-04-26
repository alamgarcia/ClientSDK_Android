package com.garcia76.clientsdk

import com.avaya.clientservices.client.Client
import com.avaya.clientservices.client.ClientListener
import com.avaya.clientservices.user.User

class ApplicationClientListener:ClientListener {
    override fun onIdentityCertificateEnrollmentFailed(p0: Client?, p1: Int, p2: String?, p3: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClientUserRemoved(p0: Client?, p1: User?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClientUserCreated(p0: Client?, p1: User?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClientShutdown(p0: Client?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}