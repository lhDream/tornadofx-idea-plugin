package no.tornado.tornadofx.idea.configurations

import com.intellij.diagnostic.logging.LogConfigurationPanel
import com.intellij.execution.ExecutionBundle
import com.intellij.execution.Executor
import com.intellij.execution.JavaRunConfigurationExtensionManager
import com.intellij.execution.application.ApplicationConfiguration
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RuntimeConfigurationWarning
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.util.JavaParametersUtil
import com.intellij.execution.util.ProgramParametersUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.WriteExternalException
import org.jdom.Element

class TornadoFXConfiguration(project: Project, factory: ConfigurationFactory, name: String?) : ApplicationConfiguration(name, project, factory) {
    enum class RunType { App, View }

    @JvmField
    var RUN_TYPE = RunType.App
    @JvmField
    var LIVE_STYLESHEETS: Boolean = false
    @JvmField
    var DUMP_STYLESHEETS: Boolean = false
    @JvmField
    var LIVE_VIEWS: Boolean = false

    var viewClassName: String? = null

    override fun getState(executor: Executor, environment: ExecutionEnvironment) = ViewCommandLineState(environment)

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        val group = SettingsEditorGroup<TornadoFXConfiguration>()
        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), TornadoFXSettingsEditor(project))
        JavaRunConfigurationExtensionManager.instance.appendEditors(this, group)
        group.addEditor(ExecutionBundle.message("logs.tab.title"), LogConfigurationPanel<TornadoFXConfiguration>())
        return group
    }

    override fun checkConfiguration() {
        JavaParametersUtil.checkAlternativeJRE(this)

        if (RUN_TYPE == RunType.App) {
            if (mainClassName.isNullOrBlank()) {
                throw RuntimeConfigurationWarning("No App Class specified")
            } else {
                val psiClass = configurationModule.checkModuleAndClassName(mainClassName, "No App Class specified!")
                if (!TornadoFXSettingsEditor.isAppClass(psiClass))
                    throw RuntimeConfigurationWarning("Specified App Class does not inherit from tornadofx.App")
            }
        } else {
            val psiClass = configurationModule.checkModuleAndClassName(viewClassName, "No View Class specified!")
            if (!TornadoFXSettingsEditor.isViewClass(psiClass))
                throw RuntimeConfigurationWarning("Specified View Class does not inherit from tornadofx.View")
        }

        ProgramParametersUtil.checkWorkingDirectoryExist(this, project, configurationModule.module)
        JavaRunConfigurationExtensionManager.checkConfigurationIsValid(this)
    }

    inner class ViewCommandLineState(environment: ExecutionEnvironment) : ApplicationConfiguration.JavaApplicationCommandLineState<TornadoFXConfiguration>(this, environment) {
        override fun createJavaParameters(): JavaParameters? {
            val params = super.createJavaParameters()!!

            if (RUN_TYPE == RunType.View) {
                params.programParametersList.add("--view-class=$viewClassName")
                ApplicationManager.getApplication().runWriteAction {
                    val isInProdSources = JavaParametersUtil.isClassInProductionSources(viewClassName!!, configurationModule.module!!)!!
                    if (!isInProdSources) {
                        params.configureByModule(configurationModule.module, JavaParameters.JDK_AND_CLASSES_AND_TESTS)
                    }
                }
            }

            if (LIVE_VIEWS)
                params.programParametersList.add("--live-views")

            if (LIVE_STYLESHEETS)
                params.programParametersList.add("--live-stylesheets")

            if (DUMP_STYLESHEETS)
                params.programParametersList.add("--dump-stylesheets")

            return params
        }
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        RUN_TYPE = RunType.valueOf(element.getAttributeValue("run-type"))

        val viewOption = element.content.find { it is Element && it.name == "option" && it.getAttribute("name")?.value == "VIEW_CLASS_NAME" }?.let { it as Element }
        viewOption?.let {
            viewClassName = it.getAttribute("value")?.value
        }

        element.getAttributeValue("live-stylesheets")?.apply {
            LIVE_STYLESHEETS = "true" == this
        }
        element.getAttributeValue("live-views")?.apply {
            LIVE_VIEWS = "true" == this
        }
        element.getAttributeValue("dump-stylesheets")?.apply {
            DUMP_STYLESHEETS = "true" == this
        }
    }

    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        viewClassName?.let {
            element.addContent(Element("option").apply {
                setAttribute("name", "VIEW_CLASS_NAME")
                setAttribute("value", it)
            })
        }
        element.setAttribute("run-type", RUN_TYPE.toString())
        element.setAttribute("live-views", LIVE_VIEWS.toString())
        element.setAttribute("live-stylesheets", LIVE_STYLESHEETS.toString())
        element.setAttribute("dump-stylesheets", DUMP_STYLESHEETS.toString())
    }

}
