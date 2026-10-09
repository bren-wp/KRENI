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
        val bad = """{
          "schemaVersion":1,
          "cities":[{"id":"zagreb","name":"Zagreb","lat":45.8,"lon":15.9,"trams":true}],
          "stops":[
            {"id":"a","city":"zagreb","name":"Prva","lat":45.8,"lon":15.9,"modes":["train"]},
            {"id":"a","city":"zagreb","name":"Druga","lat":45.8,"lon":15.9,"modes":["bus"]}
          ]
        }"""
        CatalogReader.parse(bad)
    }
}
