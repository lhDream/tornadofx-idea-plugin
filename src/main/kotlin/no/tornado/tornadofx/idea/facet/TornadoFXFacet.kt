package no.tornado.tornadofx.idea.facet

import com.intellij.application.options.CodeStyle
import com.intellij.facet.Facet
import com.intellij.facet.FacetManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.idea.core.formatter.KotlinPackageEntry
import org.jetbrains.kotlin.idea.formatter.kotlinCustomSettings
import org.jetbrains.kotlin.idea.util.projectStructure.allModules

class TornadoFXFacet(
        type: TornadoFXFacetType,
        module: Module,
        name: String,
        configuration: TornadoFXFacetConfiguration,
        underlyingFacet: Facet<*>?
) : Facet<TornadoFXFacetConfiguration>(
        type,
        module,
        name,
        configuration,
        underlyingFacet
) {

    init {
        Disposer.register(this, configuration)
    }

    companion object {
        fun get(project: Project): TornadoFXFacet? {
            for (module in project.allModules()) {
                val facet = FacetManager.getInstance(module)?.getFacetByType(TornadoFXFacetType.ID)
                if (facet != null) return facet
            }
            return null
        }
    }

    override fun initFacet() {
        try {
            val codeStyleSettings = CodeStyle.getDefaultSettings()
            val settings = codeStyleSettings.kotlinCustomSettings
            if (!settings.PACKAGES_TO_USE_STAR_IMPORTS.contains("tornadofx")) {
                // PackageEntry(false, "tornadofx", false)
                val entry = KotlinPackageEntry("tornadofx", false)
                settings.PACKAGES_TO_USE_STAR_IMPORTS.addEntry(entry)
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }
}