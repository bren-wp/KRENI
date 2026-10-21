package hr.brendigo.kreni

import com.google.transit.realtime.GtfsRealtime

/** Decode genuine GTFS-Realtime protobuf; never invent positions or arrival times. */
object GtfsRealtimeCodec {
    fun parse(bytes: ByteArray, city: String, provider: String, now: Long): LiveData {
        require(bytes.size <= 524288) { "GTFS-RT response too large" }
        val feed = GtfsRealtime.FeedMessage.parseFrom(bytes)
        require(feed.hasHeader() && feed.header.hasTimestamp()) {
            "GTFS-RT feed missing generation timestamp"
        }
        val generatedAt = feed.header.timestamp
        require(generatedAt in (now - 90)..(now + 15)) { "GTFS-RT feed stale" }
        require(feed.entityCount <= 6000) { "GTFS-RT entity limit exceeded" }

        // Without a verified GTFS Static route_type mapping the mode is unknown.
        // Using 'tram' or 'bus' based on line numbers would mislabel real vehicles.
        val vehicles = feed.entityList.mapNotNull { entity ->
            if (entity.isDeleted || !entity.hasVehicle()) return@mapNotNull null
            val v = entity.vehicle
            if (!v.hasPosition() || !v.hasTimestamp()) return@mapNotNull null
            val lat = v.position.latitude.toDouble()
            val lon = v.position.longitude.toDouble()
            val observedAt = v.timestamp
            if (!lat.isFinite() || !lon.isFinite() || lat !in 40.0..47.0 ||
                lon !in 12.0..20.0 || observedAt !in (now - 90)..(now + 15)) {
                return@mapNotNull null
            }
            val id = if (v.hasVehicle() && v.vehicle.hasId()) v.vehicle.id else entity.id
            if (id.isBlank()) return@mapNotNull null
            val route = if (v.hasTrip() && v.trip.hasRouteId()) v.trip.routeId else "?"
            Vehicle(id, route.take(40), "unknown", lat, lon, observedAt, provider)
        }.distinctBy { it.id }.take(1500)

        // TripUpdate stop IDs must first be mapped against real GTFS Static stop_id.
        // Never attach an ETA to the orientation-only demo terminal catalog.
        return LiveData(city, provider, generatedAt, vehicles, emptyList())
    }
}
