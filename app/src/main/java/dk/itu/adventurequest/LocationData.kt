package dk.itu.adventurequest

import com.google.android.gms.maps.model.LatLng

object LocationData {
    val discoveryPoints = listOf(
        PinItem(LatLng(55.7949, 12.5714), "Eremitagen", "Advanced Route - Nature Trails", "Conversation"),
        PinItem(LatLng(55.6811, 12.5404), "Landbohøjskolens Have", "Accessible Route - Flat & Paved", "Conversation"),
        PinItem(LatLng(55.7005, 12.5448), "Superkilen Park", "Accessible Route - Flat & Paved", "Conversation"),
        PinItem(LatLng(55.6418, 12.6543), "Kastrup Søbad", "Accessible Route - Wooden Decking", "Conversation"),
        PinItem(LatLng(55.6534, 12.5932), "Grønjordssøen Udkigspunkt", "Advanced Route - Uneven Ground", "Conversation"),
        PinItem(LatLng(55.6111, 12.5256), "Kalvebod Fælled Tower", "Advanced Route - Nature Trails", "Conversation"),
        PinItem(LatLng(55.6433, 12.5516), "Naturpark Amager", "Accessible Route - Flat Paths", "Conversation")
    )
}