package cc.unitmesh.devti.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent

class AutoDevSettingsConfigurable : Configurable {
    private val component: LLMSettingComponent = LLMSettingComponent(AutoDevSettingsStateNew.getInstance())

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "CodeAssistant"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
    }

    @Nullable
    override fun createComponent(): JComponent {
        return component.panel
    }

    override fun isModified(): Boolean {
        val settings: AutoDevSettingsStateNew = AutoDevSettingsStateNew.getInstance()
        return component.isModified(settings)
    }

    override fun apply() {
        component.exportSettings(AutoDevSettingsStateNew.getInstance())
    }

    override fun reset() {
        component.applySettings(AutoDevSettingsStateNew.getInstance())
    }
}
