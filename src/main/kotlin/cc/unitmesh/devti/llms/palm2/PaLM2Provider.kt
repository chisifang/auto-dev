package cc.unitmesh.devti.llms.palm2

import cc.unitmesh.devti.llms.LLMProvider
import cc.unitmesh.devti.settings.AutoDevSettingsStateNew
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.serialization.Serializable

@Serializable
data class PaLM2Request(val prompt: String, val input: String)

@Service(Service.Level.PROJECT)
class PaLM2Provider(val project: Project) : LLMProvider {
    private val key: String
        get() {
            return AutoDevSettingsStateNew.getInstance().openAiKey
        }
    override fun prompt(input: String): String {
//        val requestContent = Json.encodeToString(CustomRequest(input, input))
//        val body = requestContent.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
//        val builder = Request.Builder()
//            .url("https://generativelanguage.googleapis.com/v1beta2/models/text-bison-001:generateText?key=$key")
//            .post(body)
//        OkHttpClient().newCall(builder.build()).execute().use { response ->
//            if (!response.isSuccessful) throw Exception("Unexpected code $response")
//            return response.body!!.string()
//        }
        TODO()
    }
}
