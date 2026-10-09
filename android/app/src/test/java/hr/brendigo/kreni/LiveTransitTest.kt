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
    @Test fun hostileExtremeTimestampsAreNotFresh() {
        val expired = Arrival("a", "zet:1", "6", "tram", 100120, 100121,
            Long.MIN_VALUE, "zet")
        assertNull(expired.minutes(100000))
        val feed = LiveData("zagreb", "zet", Long.MIN_VALUE, emptyList(), emptyList())
        assertFalse(feed.freshFor("zagreb", 100000))
    }
    @Test fun expiredArrivalCannotKeepShowingMinutes() {
        val data = LiveCodec.parse(json,"zagreb","zet",100000)
        val arrival = data.arrivals.single()
        assertEquals(3, arrival.minutes(100000))
        assertNull(arrival.minutes(100091))
        assertTrue(data.freshFor("zagreb",100090))
        assertFalse(data.freshFor("zagreb",100091))
        assertFalse(data.freshFor("rijeka",100000))
        assertTrue(data.filtered(100091).vehicles.isEmpty())
        assertTrue(data.filtered(100091).arrivals.isEmpty())
    }
    @Test fun missingProductionApprovalDisablesValidHttpsSource() {
        val data = LiveCodec.providers("""{"schemaVersion":1,"providers":[{"id":"zet",
        "city":"zagreb","enabled":true,"endpoint":"https://example.org/rt.json"}]}""")
        assertFalse(data.single().enabled)
    }
    @Test fun productionApprovalRequiresExplicitLicenseUrl() {
        val data = LiveCodec.providers("""{"schemaVersion":1,"providers":[{"id":"zet",
        "city":"zagreb","enabled":true,"approvedForProduction":true,
        "endpoint":"https://example.org/rt.json"}]}""")
        assertFalse(data.single().enabled)
    }
    @Test fun hostileEndpointIsDisabled() {
        val data = LiveCodec.providers("""{"schemaVersion":1,"providers":[{"id":"zet",
        "city":"zagreb","enabled":true,"endpoint":"https://user@example.com"}]}""")
        assertFalse(data.single().enabled)
    }
    @Test fun nonHttpsIsDisabled() {
        val data = LiveCodec.providers("""{"schemaVersion":1,"providers":[{"id":"zet",
        "city":"zagreb","enabled":true,"endpoint":"http://example.com"}]}""")
        assertFalse(data.single().enabled)
    }
}
