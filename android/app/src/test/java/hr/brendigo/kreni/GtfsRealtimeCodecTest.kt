package hr.brendigo.kreni

import com.google.transit.realtime.GtfsRealtime
import org.junit.Assert.*
import org.junit.Test

class GtfsRealtimeCodecTest {
    private fun message(timestamp: Long, vehicleTime: Long): ByteArray {
        val vehicle = GtfsRealtime.VehiclePosition.newBuilder()
            .setPosition(GtfsRealtime.Position.newBuilder()
                .setLatitude(45.81f).setLongitude(15.97f).build())
            .setTimestamp(vehicleTime)
            .setTrip(GtfsRealtime.TripDescriptor.newBuilder().setRouteId("6").build())
            .setVehicle(GtfsRealtime.VehicleDescriptor.newBuilder().setId("vehicle-1").build())
            .build()
        return GtfsRealtime.FeedMessage.newBuilder()
            .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                .setGtfsRealtimeVersion("2.0").setTimestamp(timestamp).build())
            .addEntity(GtfsRealtime.FeedEntity.newBuilder()
                .setId("entity-1").setVehicle(vehicle).build()).build().toByteArray()
    }

    @Test fun realGpsVehicleCanBeDecoded() {
        val data = GtfsRealtimeCodec.parse(message(100000, 100000), "zagreb", "zet", 100000)
        assertEquals(1, data.vehicles.size)
        assertEquals("vehicle-1", data.vehicles.single().id)
        assertEquals("6", data.vehicles.single().route)
        assertEquals("unknown", data.vehicles.single().mode)
        assertTrue(data.arrivals.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun outdatedVehicleFeedRejected() {
        GtfsRealtimeCodec.parse(message(100000, 100000), "zagreb", "zet", 100091)
    }

    @Test fun oldPositionDroppedDespiteFreshFeed() {
        val data = GtfsRealtimeCodec.parse(message(100000, 99900), "zagreb", "zet", 100000)
        assertTrue(data.vehicles.isEmpty())
    }
}
