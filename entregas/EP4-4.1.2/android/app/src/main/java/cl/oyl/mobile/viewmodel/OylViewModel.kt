package cl.oyl.mobile.viewmodel
import android.app.Application

/** Sólo estado transversal de la aplicación; no administra productos, compras ni perfiles. */
class OylViewModel(app: Application) : BaseOylViewModel(app) {
    val notice = graph.notice
    val busy = graph.busy
    fun showNotice(message: String) = graph.showNotice(message)
    fun dismissNotice() = graph.dismissNotice()
}
