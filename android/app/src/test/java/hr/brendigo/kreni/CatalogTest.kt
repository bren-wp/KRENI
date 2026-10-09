package hr.brendigo.kreni

import org.junit.Assert.*
import org.junit.Test

class CatalogTest {
    private val sample = """{
      "schemaVersion":1,
      "cities":[{"id":"zagreb","name":"Zagreb","lat":45.8,"lon":15.9,"trams":true}],
      "stops":[{"id":"a","city":"zagreb","name":"Kolodvor","lat":45.8,"lon":15.9,"modes":["train"]}]
    }"""

    @Test fun parsesKnownCityAndStop() {
        val catalog = CatalogReader.parse(sample)
        assertEquals(1, catalog.cities.size)
        assertEquals("Kolodvor", catalog.stopsIn("zagreb").single().name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateStopId() {
        val duplicate = """{"id":"a","city":"zagreb","name":"Duplikat","lat":45.8,"lon":15.9,"modes":["bus"]}"""
        val bad = sample.replace("}]}", "}," + duplicate + "]}")
        CatalogReader.parse(bad)
    }
}
