package no.tornado.tornadofx.idea.facet

import com.intellij.facet.FacetConfiguration
import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetValidatorsManager
import com.intellij.openapi.Disposable
import org.jdom.Element

class TornadoFXFacetConfiguration : FacetConfiguration, Disposable {

    override fun createEditorTabs(editorContext: FacetEditorContext, validatorsManager: FacetValidatorsManager) =
        arrayOf(TornadoFXFacetEditorTab(editorContext))

    @Deprecated("Deprecated in Java")
    override fun readExternal(element: Element?) {

    }

    @Deprecated("Deprecated in Java")
    override fun writeExternal(element: Element?) {
    }

    override fun dispose() {
    }
}