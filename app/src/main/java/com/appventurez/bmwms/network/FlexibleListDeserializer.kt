package com.appventurez.bmwms.network

import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class FlexibleListDeserializer : JsonDeserializer<List<*>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<*> {
        if (json.isJsonArray) {
            val array = json.asJsonArray
            val list = mutableListOf<Any>()
            val typeArguments = (typeOfT as? ParameterizedType)?.actualTypeArguments
            if (typeArguments != null && typeArguments.isNotEmpty()) {
                val itemType = typeArguments[0]
                for (element in array) {
                    val item = context.deserialize<Any>(element, itemType)
                    if (item != null) {
                        list.add(item)
                    }
                }
            }
            return list
        } else {
            // If it's not an array (e.g., a String "" or an error message), return an empty list
            return emptyList<Any>()
        }
    }
}
