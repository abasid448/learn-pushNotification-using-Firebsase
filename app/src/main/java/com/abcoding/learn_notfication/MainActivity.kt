package com.abcoding.learn_notfication

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abcoding.learn_notfication.ui.theme.Learn_notficationTheme
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


const val TOPIC = "/topics/myTopic"

class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Learn_notficationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    TextFieldsAndButton { title, message, token ->
                        if (title.isNotEmpty() && message.isNotEmpty()) {
                            PushNotification(
                                NotificationData(title, message),
                                TOPIC
                            ).also { sendNotification(it) }
                        }
                    }
                }
            }
        }
        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            FirebaseService.token = it.result
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(TAG, "Response: ${Gson().toJson(response)} ")
                } else {
                    Log.e(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendNotification: ${e.toString()}")
            }
        }
}

@Composable
fun TextFieldsAndButton(onClick: (String, String, String) -> Unit) {
    var titleText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var tokenText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "title", textAlign = TextAlign.Start)
        TextField(value = titleText, onValueChange = { titleText = it })
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "body", textAlign = TextAlign.Start)
        TextField(value = messageText, onValueChange = { messageText = it })
        Spacer(modifier = Modifier.height(16.dp))



        Button(
            onClick = { onClick(titleText, messageText, tokenText) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Send")
        }
    }
}


