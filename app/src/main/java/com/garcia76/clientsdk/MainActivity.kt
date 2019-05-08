package com.garcia76.clientsdk

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avaya.clientservices.client.Client
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //variables


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.MEDIA_CONTENT_CONTROL,
                Manifest.permission.READ_PHONE_STATE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {/* ... */
                }

                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {/* ... */
                }
            }).check()
        setContentView(R.layout.activity_main)
        csdkversion_lbl.text = Client.getVersion()

        login_btn.setOnClickListener {

            val intent = Intent(this, MainFrame::class.java)
            // Pasar Valores entre Actividades
            val myPreferences = "myPrefs"
            val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("username", user_txt.text.toString())
            editor.putString("password", pass_txt.text.toString())
            editor.apply()
            startActivity(intent)
        }

        server_btn.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            // Pasar Valores entre Actividades
            startActivity(intent)
        }

    }

}
