package com.example.contentproviderdemo

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainAct : AppCompatActivity() {
    private val myPermissionRequest: Int = 101
    private lateinit var tvGetContactList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)

        grantedPermission()

        initViews()

        initEvent()
    }

    private fun grantedPermission() {
        // Check Permission
        if (ContextCompat.checkSelfPermission(this , android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS),
                myPermissionRequest)
        }
    }

    private fun initViews() {
        tvGetContactList = findViewById(R.id.tvGetContactList)
    }

    private fun initEvent() {
        tvGetContactList.setOnClickListener {
            getContactList()
        }
    }

    //https://stackoverflow.com/questions/12562151/android-get-all-contacts
    @SuppressLint("Range")
    private fun getContactList() {
        val cr = contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id: String = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name: String = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur: Cursor? = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                    while (pCur!!.moveToNext()) {
                        val phoneNo: String = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Log.i("Logger", "Name: $name")
                        Log.i("Logger", "Phone Number: $phoneNo")
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
    }

}