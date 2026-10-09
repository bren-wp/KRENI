package hr.brendigo.kreni
import org.junit.Assert.*
import org.junit.Test
class LiveTransitTest {
    private val json = """{"schemaVersion":1,"city":"zagreb","provider":"zet","generatedAt":100000,
      "vehicles":[{"id":"v1","route":"6","mode":"tram","lat":45.81,"lon":15.97,"observedAt":100000}],
      "arrivals":[{"id":"a","stopId":"zet:1","route":"6","mode":"tram","predictedAt":100121,"updatedAt":100000}]}"""
    @Test fun freshVehicleAndMinuteArrival() {
        val data = LiveCodec.parse(json,"zagreb","zet",100000)
        assertEquals(1,data.vehicles.size)
        assertEquals(3,data.arrivals.single().minutes(100000))
        assertTrue(data.arrivals.single().estimated(100000))
    }
    @Test(expected=IllegalArgumentException::class)
    fun staleFeedIsRejected() { LiveCodec.parse(json,"zagreb","zet",100091) }
    @Test(expected=IllegalArgumentException::class)
    fun mismatchedCityIsRejected() { LiveCodec.parse(json,"rijeka","zet",100000) }
    @Test fun nonHttpsIsDisabled() {
        val data = LiveCodec.providers("""{"schemaVersion":1,"providers":[{"id":"zet",
        "city":"zagreb","enabled":true,"endpoint":"http://example.com"}]}""")
        assertFalse(data.single().enabled)
    }
}
