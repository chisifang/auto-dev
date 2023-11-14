package cc.unitmesh.devti.llms

import cc.unitmesh.devti.llms.custom.CustomLLMProvider
import cc.unitmesh.devti.settings.AIEngines
import cc.unitmesh.devti.settings.AutoDevSettingsStateNew
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service
class LlmProviderFactory {
    private val aiEngine: AIEngines
        get() = AIEngines.values()
            .find { it.name.lowercase() == AutoDevSettingsStateNew.getInstance().aiEngine.lowercase() } ?: AIEngines.Custom

    fun connector(project: Project): LLMProvider {
        return when (aiEngine) {
//            AIEngines.OpenAI -> project.getService(OpenAIProvider::class.java)
            AIEngines.Custom -> project.getService(CustomLLMProvider::class.java)
//            AIEngines.Azure -> project.getService(AzureOpenAIProvider::class.java)
//            AIEngines.XingHuo -> project.getService(XingHuoProvider::class.java)
        }
    }
}
