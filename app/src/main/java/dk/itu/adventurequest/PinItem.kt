package dk.itu.adventurequest

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

// discovery point object
class PinItem(
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val conversationStarter: String
) : ClusterItem {
    override fun getPosition(): LatLng = itemPosition
    override fun getTitle(): String = itemTitle
    override fun getSnippet(): String = itemSnippet
    override fun getZIndex(): Float? = null
}