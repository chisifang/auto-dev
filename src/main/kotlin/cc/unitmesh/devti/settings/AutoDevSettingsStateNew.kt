package cc.unitmesh.devti.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "cc.unitmesh.devti.settings.DevtiSettingsStateNew", storages = [Storage("DevtiNewSettings.xml")])
class AutoDevSettingsStateNew : PersistentStateComponent<AutoDevSettingsStateNew> {
    var gitType = DEFAULT_GIT_TYPE
    var githubToken = ""
    var gitlabToken = ""
    var gitlabUrl = ""
    var openAiKey = ""
    var openAiModel = ""
    var delaySeconds = ""

    var aiEngine = DEFAULT_AI_ENGINE
    var customOpenAiHost = ""
    var customEngineServer = "http://10.110.30.55:4040"
    var customEngineToken = "123456"
    var customPrompts = ""

    var xingHuoAppId = ""
    var xingHuoApiSecrect = ""
    var xingHuoApiKey = ""

    /**
     * should be a json path
     */
    var customEngineResponseFormat = "\$.result.text"
    var language = DEFAULT_HUMAN_LANGUAGE
    var maxTokenLength = MAX_TOKEN_LENGTH.toString()

    fun fetchMaxTokenLength(): Int {
        return maxTokenLength.toIntOrNull() ?: MAX_TOKEN_LENGTH
    }

    @Synchronized
    override fun getState(): AutoDevSettingsStateNew {
        return this
    }

    @Synchronized
    override fun loadState(state: AutoDevSettingsStateNew) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val maxTokenLength: Int get() = getInstance().fetchMaxTokenLength()

        fun getInstance(): AutoDevSettingsStateNew {
            return ApplicationManager.getApplication().getService(AutoDevSettingsStateNew::class.java).state
        }
    }

}
