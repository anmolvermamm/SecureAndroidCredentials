package com.googlyandroid.myapplication

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.googlyandroid.myapplication.internal.android.crypto.SyncCrypto
import com.googlyandroid.myapplication.internal.android.crypto.SyncCryptoFactory
import kotlinx.android.synthetic.main.activity_login.listView
import kotlinx.android.synthetic.main.activity_main_header.aliasText
import kotlinx.android.synthetic.main.activity_main_header.decryptedText
import kotlinx.android.synthetic.main.activity_main_header.encryptedText
import kotlinx.android.synthetic.main.activity_main_header.generateKeyPair
import kotlinx.android.synthetic.main.activity_main_header.startText
import java.security.KeyStoreException

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

  var keyAliases: ArrayList<String> = ArrayList()
  var listAdapter: KeyRecyclerAdapter? = null

  private var factory: SyncCrypto? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    factory = SyncCryptoFactory.get(this)

    val listHeader = View.inflate(this, R.layout.activity_main_header, null)

    listView.addHeaderView(listHeader)
    listAdapter = KeyRecyclerAdapter(this, R.id.keyAlias)
    listView.adapter = listAdapter

    generateKeyPair.setOnClickListener {
      if (generateKeyPair.text.toString().isNotEmpty()) {
        var alias = aliasText.text.toString()

        factory?.let {
          if (it.is_keystore_unlocked) {
            createKeyForAlias(alias)
          } else {
            it.unlock_keystore()
          }
        }

      }
    }
    refreshKeys()

  }

  private fun createKeyForAlias(alias: String) {
    var dialog = ProgressDialog.show(this@LoginActivity, "Creating...", "alias $alias")


    object : AsyncTask<String, String, String>() {
      override fun onPreExecute() {
        super.onPreExecute()
        dialog.show()
      }

      override fun doInBackground(vararg p0: String?): String {
        factory?.create_key_if_not_available(alias)
        return ""
      }

      override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        dialog.dismiss()
        refreshKeys()
      }
    }.execute()
  }

  private fun refreshKeys() {
    keyAliases = ArrayList()
    encryptedText.setText("")
    decryptedText.setText("")
    startText.setText("")
    try {
      keyAliases!!.addAll(factory!!.aliases.toList())
    } catch (e: Exception) {
    }

    if (listAdapter != null)
      listAdapter!!.notifyDataSetChanged()
  }

  fun deleteKey(alias: String) {
    val alertDialog = AlertDialog.Builder(this)
        .setTitle("Delete Key")
        .setMessage("Do you want to delete the key \"$alias\" from the keystore?")
        .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
          try {
            factory?.let {
              if (it.is_keystore_unlocked) {
                factory?.deleteEntry(alias)
                refreshKeys()
              } else {
                it.unlock_keystore()
              }
            }

          } catch (e: KeyStoreException) {
            Toast.makeText(this@LoginActivity,
                "Exception " + e.message + " occured",
                Toast.LENGTH_LONG).show()
            Log.e(LoginActivity::class.simpleName, Log.getStackTraceString(e))
          }

          dialog.dismiss()
        })
        .setNegativeButton("No",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        .create()
    alertDialog.show()
  }

  inner class KeyRecyclerAdapter(context: Context, textView: Int) :
      ArrayAdapter<String>(context, textView) {

    override fun getCount(): Int {
      return keyAliases!!.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
      val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)

      val keyAlias = itemView.findViewById<View>(R.id.keyAlias) as TextView
      keyAlias.text = keyAliases!!.get(position)
      val encryptButton = itemView.findViewById<View>(R.id.encryptButton) as Button
      encryptButton.setOnClickListener(
          View.OnClickListener {
            try {
              factory?.let {
                if (it.is_keystore_unlocked) {
                  encryptedText.setText(
                      factory?.encrypt(keyAlias.text.toString(), startText.text.toString()))

                } else {
                  it.unlock_keystore()
                }
              }
            } catch (ex: Exception) {
            }

          })
      val decryptButton = itemView.findViewById<View>(R.id.decryptButton) as Button
      decryptButton.setOnClickListener(
          View.OnClickListener {
            factory?.let {
              if (it.is_keystore_unlocked) {
                try {
                  if (encryptedText.text.toString().isNotEmpty()) {
                    decryptedText.setText(factory?.decrypt(
                        keyAlias.text.toString(), encryptedText.text.toString()))
                  }
                } catch (ex: Exception) {

                }

              } else {
                it.unlock_keystore()
              }
            }

          })
      val deleteButton = itemView.findViewById<View>(R.id.deleteButton) as Button
      deleteButton.setOnClickListener(
          View.OnClickListener { deleteKey(keyAlias.text.toString()) })

      return itemView
    }

    override fun getItem(position: Int): String? {
      return keyAliases!![position]
    }

  }

}
