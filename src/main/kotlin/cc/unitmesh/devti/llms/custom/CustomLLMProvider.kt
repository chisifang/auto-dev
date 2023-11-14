package cc.unitmesh.devti.llms.custom

import cc.unitmesh.devti.custom.CustomPromptConfig
import cc.unitmesh.devti.gui.chat.ChatActionType
import cc.unitmesh.devti.gui.chat.ChatRole
import cc.unitmesh.devti.llms.LLMProvider
import cc.unitmesh.devti.settings.AutoDevSettingsStateNew
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.service.SSE
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.time.Duration

@Serializable
data class Message(val role: String, val message: String)

@Serializable
data class CustomRequest(val messages: List<Message>)

@Service(Service.Level.PROJECT)
class CustomLLMProvider(val project: Project) : LLMProvider {
    private val autoDevSettingsStateNew = AutoDevSettingsStateNew.getInstance()
    private val url get() = autoDevSettingsStateNew.customEngineServer
    private val key get() = autoDevSettingsStateNew.customEngineToken
    private val engineFormat get() = autoDevSettingsStateNew.customEngineResponseFormat
    private val customPromptConfig: CustomPromptConfig?
        get() {
            val prompts = autoDevSettingsStateNew.customPrompts
            return CustomPromptConfig.tryParse(prompts)
        }
    private var client = OkHttpClient()
    private val timeout = Duration.ofSeconds(600)
    private val messages: MutableList<Message> = ArrayList()

    private val logger = logger<CustomLLMProvider>()

    override fun clearMessage() {
        messages.clear()
    }

    override fun appendLocalMessage(msg: String, role: ChatRole) {
        messages += Message(role.roleName(), msg)
    }

    override fun prompt(promptText: String): String {
        return this.prompt(promptText, "")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun stream(promptText: String, systemPrompt: String, action: ChatActionType): Flow<String> {
        // 不做多轮对话，清空历史对话
        this.clearMessage();
        messages += Message("user", promptText)

        val customRequest = CustomRequest(messages)
        val requestContent = Json.encodeToString<CustomRequest>(customRequest)

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), requestContent)
        logger.info("Requesting from $body")

        val builder = Request.Builder()
        if (key.isNotEmpty()) {
            builder.addHeader("Authorization", "Bearer $key")
        }

        client = client.newBuilder().readTimeout(timeout).build()
        val request = builder.url(this.getUrl(action)).post(body).build()

        val call = client.newCall(request)
        val emitDone = false

        val sseFlowable = Flowable
            .create({ emitter: FlowableEmitter<SSE> ->
                call.enqueue(cc.unitmesh.devti.llms.azure.ResponseBodyCallback(emitter, emitDone))
            }, BackpressureStrategy.BUFFER)

        try {
            logger.info("Starting to stream:")
            return callbackFlow {
                withContext(Dispatchers.IO) {
                    sseFlowable
                        .doOnError(Throwable::printStackTrace)
                        .blockingForEach { sse ->
                            if (engineFormat.isNotEmpty()) {
                                val chunk: String = JsonPath.parse(sse!!.data)?.read<String>(engineFormat)
                                    ?: throw Exception("Failed to parse chunk: ${sse.data}, format: $engineFormat")
                                trySend(chunk)
                            } else {
                                val result: ChatCompletionResult =
                                    ObjectMapper().readValue(sse!!.data, ChatCompletionResult::class.java)

                                val completion = result.choices[0].message
                                if (completion != null && completion.content != null) {
                                    trySend(completion.content)
                                }
                            }
                        }

                    close()
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to stream", e)
            return callbackFlow {
                close()
            }
        }
    }

    private fun getUrl(action: ChatActionType): String {
        var path = "/generate"
        when (action!!) {
            ChatActionType.EXPLAIN -> {
                path = "/api/explain"
            }
            ChatActionType.CODE_COMPLETE -> {
                path = "/api/complete"
            }
            ChatActionType.GENERATE_TEST -> {
                path = "/api/test"
            }
            ChatActionType.REFACTOR -> {
                path = "/api/generate"
            }
            ChatActionType.CHAT -> {
                path = "/api/generate"
            }
            ChatActionType.FIX_ISSUE -> {}
            ChatActionType.GEN_COMMIT_MESSAGE -> {}
            ChatActionType.CREATE_CHANGELOG -> {}
            ChatActionType.CUSTOM_COMPLETE -> {}
            ChatActionType.CUSTOM_ACTION -> {}
            ChatActionType.COUNIT -> {}
        }

        return url + path
    }

    fun prompt(instruction: String, input: String): String {
        messages += Message("user", instruction)
        val customRequest = CustomRequest(messages)
        val requestContent = Json.encodeToString<CustomRequest>(customRequest)

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), requestContent)

        logger.info("Requesting from $body")
        val builder = Request.Builder()
        if (key.isNotEmpty()) {
            builder.addHeader("Authorization", "Bearer $key")
        }

        try {
            client = client.newBuilder().readTimeout(timeout).build()

            val request = builder.url(url).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                logger.error("$response")
                return ""
            }

            return response.body?.string() ?: ""
        } catch (e: IllegalArgumentException) {
            logger.error("Failed to set timeout", e)
            return ""
        }
    }
}