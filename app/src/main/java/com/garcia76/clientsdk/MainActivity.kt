package com.garcia76.clientsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //variables


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
