package com.appventurez.bmwms.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class FlexibleMessageDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): String {
        return if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            // If the message is an object like {"email": "The Email field is required"}, 
            // extract the first entry values or join them together.
            val sb = StringBuilder()
            for (key in jsonObject.keySet()) {
                val element = jsonObject.get(key)
                if (element.isJsonArray) {
                    val arr = element.asJsonArray
                    for (i in 0 until arr.size()) {
                        sb.append(arr[i].asString).append(" ")
                    }
                } else if (element.isJsonPrimitive) {
                    sb.append(element.asString).append(" ")
                }
            }
            sb.toString().trim()
        } else if (json.isJsonArray) {
            val arr = json.asJsonArray
            val sb = StringBuilder()
            for (i in 0 until arr.size()) {
                sb.append(arr[i].asString).append(" ")
            }
            sb.toString().trim()
        } else {
            json.asString
        }
    }
}
