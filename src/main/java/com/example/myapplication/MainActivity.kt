package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import android.app.AlertDialog
import android.content.DialogInterface

class MainActivity : ComponentActivity() {
    private var toLanguage = "ru"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layer)
        val languageButton = findViewById<Button>(R.id.languageButton)
        val GoogleTranslate = findViewById<TextView>(R.id.textView4)
        val MicrosoftTranslate = findViewById<TextView>(R.id.textView6)
        val TextToTranslate = findViewById<EditText>(R.id.textEdit)
        val ButtonTranslate = findViewById<Button>(R.id.button2)

        languageButton.setOnClickListener {
            showLanguageDialog()
        }

        ButtonTranslate.setOnClickListener{
            GoogleTrans("${TextToTranslate.text}", GoogleTranslate)
            MicrosoftTrans("${TextToTranslate.text}", MicrosoftTranslate)
        }
    }
    private fun showLanguageDialog() {
        val languages = arrayOf("auto", "en", "es", "ru","it","ko")
        val checkedItem = if (toLanguage == "auto") 0 else languages.indexOf(toLanguage)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите язык")
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                toLanguage = if (which == 0) "auto" else languages[which]
                dialog.dismiss()
            }
            .setPositiveButton("OK", null)
            .setNegativeButton("Отмена", null)

        val dialog = builder.create()
        dialog.show()
    }
    fun GoogleTrans(str :String, message :TextView) {

        val client = OkHttpClient()

        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = "from=auto&to=${toLanguage}&text=${str}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://google-translate113.p.rapidapi.com/api/v1/translator/text")
            .post(body)
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("X-RapidAPI-Key", "c9243977d4mshfff3a15ce629830p17e625jsnef09086249a0")
            .addHeader("X-RapidAPI-Host", "google-translate113.p.rapidapi.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException(
                            "Запрос к серверу не был успешен:" +
                                    " ${response.code} ${response.message}"
                        )
                    }
                    // вывод тела ответа
                    var trans = response.body!!.string()
                    val res = Gson().fromJson(trans, ResultGoogle::class.java)
                    message.text = res.trans
                }
            }
        })
    }

    fun MicrosoftTrans(str :String, message :TextView) {
        val client = OkHttpClient()

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, "[\r\n    {\r\n        \"Text\": \"${str}\"\r\n    }\r\n]")
        val request = Request.Builder()
            .url("https://microsoft-translator-text.p.rapidapi.com/translate?to%5B0%5D=${toLanguage}&api-version=3.0&profanityAction=NoAction&textType=plain")
            .post(body)
            .addHeader("content-type", "application/json")
            .addHeader("X-RapidAPI-Key", "c9243977d4mshfff3a15ce629830p17e625jsnef09086249a0")
            .addHeader("X-RapidAPI-Host", "microsoft-translator-text.p.rapidapi.com")
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException(
                            "Запрос к серверу не был успешен:" +
                                    " ${response.code} ${response.message}"
                        )
                    }
                    // вывод тела ответа
                    var trans = response.body!!.string()
                    message.text = trans.substring(trans.indexOf("\"text\":") + 8 until trans.indexOf("\"to\":") - 2)
                }
            }
        })
    }

    class ResultGoogle {
        var trans: String? = null

        override fun toString(): String {
            return "trans=$trans)"
        }
    }
}