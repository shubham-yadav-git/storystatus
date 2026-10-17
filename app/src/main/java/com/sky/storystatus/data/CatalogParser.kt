package com.sky.storystatus.data

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.sky.storystatus.model.StatusTemplate
import com.sky.storystatus.model.TemplateBackground
import java.lang.reflect.Type

class BackgroundDeserializer : JsonDeserializer<TemplateBackground> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TemplateBackground {
        val jsonObject = json.asJsonObject
        return when {
            jsonObject.has("patternType") || jsonObject.has("pattern") -> {
                context.deserialize(jsonObject, TemplateBackground.Pattern::class.java)
            }
            jsonObject.has("colors") -> {
                context.deserialize(jsonObject, TemplateBackground.Gradient::class.java)
            }
            jsonObject.has("colorValue") -> {
                context.deserialize(jsonObject, TemplateBackground.Color::class.java)
            }
            jsonObject.has("resourceName") -> {
                context.deserialize(jsonObject, TemplateBackground.ImageResource::class.java)
            }
            else -> TemplateBackground.Color(0xFF000000)
        }
    }
}

class CatalogParser(
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(TemplateBackground::class.java, BackgroundDeserializer())
        .create()
) {
    fun parseCatalog(jsonString: String): List<StatusTemplate> {
        return try {
            val listType = object : TypeToken<List<StatusTemplate>>() {}.type
            gson.fromJson(jsonString, listType) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
