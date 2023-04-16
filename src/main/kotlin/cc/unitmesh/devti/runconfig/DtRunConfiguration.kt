package cc.unitmesh.devti.runconfig

import cc.unitmesh.devti.ai.OpenAIVersion
import cc.unitmesh.devti.language.StoryConfig
import cc.unitmesh.devti.runconfig.config.DevtiCreateStoryConfigure
import cc.unitmesh.devti.runconfig.ui.DtSettingsEditor
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element

class DtRunConfiguration(project: Project, name: String, factory: ConfigurationFactory) :
    RunConfigurationBase<DtRunConfigurationOptions>(project, factory, name) {

    private var storyConfig: DevtiCreateStoryConfigure? = null

    public override fun getOptions(): DtRunConfigurationOptions {
        return super.getOptions() as DtRunConfigurationOptions
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return DtSettingsEditor(project)
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return DtRunState(environment, this, storyConfig)
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)

        element.writeString("githubToken", options.githubToken())
        element.writeString("openAiApiKey", options.openAiApiKey())
        element.writeString("aiEngineVersion", options.aiVersion().toString())
        element.writeString("aiMaxTokens", options.aiMaxTokens().toString())
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)

        element.readString("githubToken")?.let { this.options.setGithubToken(it) }
        element.readString("openAiApiKey")?.let { this.options.setOpenAiApiKey(it) }
        element.readString("aiEngineVersion")?.let { this.options.setAiVersion(it.toInt()) }
        element.readString("aiMaxTokens")?.let { this.options.setAiMaxTokens(it.toInt()) }
    }

    fun setStoryConfig(storyConfig: DevtiCreateStoryConfigure) {
        this.storyConfig = storyConfig
    }

    fun setGithubToken(text: String) {
        this.options.setGithubToken(text)
    }

    fun setOpenAiApiKey(text: String) {
        this.options.setOpenAiApiKey(text)
    }

    fun setAiVersion(fromIndex: OpenAIVersion) {
        this.options.setAiVersion(fromIndex.ordinal)
    }

    fun setAiMaxTokens(openAiMaxTokens: Int) {
        this.options.setAiMaxTokens(openAiMaxTokens)
    }
}

fun Element.writeString(name: String, value: String) {
    val opt = Element("option")
    opt.setAttribute("name", name)
    opt.setAttribute("value", value)
    addContent(opt)
}

fun Element.readString(name: String): String? =
    children
        .find { it.name == "option" && it.getAttributeValue("name") == name }
        ?.getAttributeValue("value")