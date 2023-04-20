package com.example.contentproviderdemo

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainAct : AppCompatActivity() {
    private val myPermissionRequest: Int = 101
    private lateinit var tvGetContactList: TextView
    private lateinit var tvGetImageList: TextView
    private lateinit var tvGetSmsList: TextView

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
                arrayOf(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_SMS),
                myPermissionRequest)
        }
    }

    private fun initViews() {
        tvGetContactList = findViewById(R.id.tvGetContactList)
        tvGetImageList = findViewById(R.id.tvGetImageList)
        tvGetSmsList = findViewById(R.id.tvGetSmsList)
    }

    private fun initEvent() {
        tvGetContactList.setOnClickListener {
            getContactList()
        }

        tvGetImageList.setOnClickListener {
            getImageList()
        }

        tvGetSmsList.setOnClickListener {
            getSmsList()
        }
    }

    private fun getSmsList() {
        val sms = Uri.parse("content://sms/")
        val cr = contentResolver
        val cur = cr.query(sms, null, null, null, null)
        Log.e("Logger", "Query Count Sms = ${cur!!.count}")

        if (cur.moveToFirst()) {
            do {
                val displayName = cur.getString(cur.getColumnIndexOrThrow("_id"))
                val address = cur.getString(cur.getColumnIndexOrThrow("address"))
                val msg = cur.getString(cur.getColumnIndexOrThrow("body"))

                Log.e("Logger", "displayName: $displayName, address: $address, message: $msg ")
            } while (cur.moveToNext())
        }
    }

    // https://stackoverflow.com/questions/4195660/get-list-of-photo-galleries-on-android
    private fun getImageList() {
        // which image properties are we querying
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN
        )

        // content:// style URI for the "primary" external storage volume
        val images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cur = managedQuery(images, projection, null, null, null)
        Log.i("Logger","Query Count Image = " + cur.count)

        if (cur.moveToFirst()) {
            var bucket: String
            var id: String
            val bucketColumn: Int = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val idColumn: Int = cur.getColumnIndex(MediaStore.Images.Media._ID)
            do {
                // Get the field values
                bucket = cur.getString(bucketColumn)
                id = cur.getString(idColumn)

                // Do something with the values.
                Log.i("Logger", " Bucket=$bucket, Uri=$id")
            } while (cur.moveToNext())
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