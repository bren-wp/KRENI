import XCTest
@testable import KRENI

final class CatalogTests: XCTestCase {
    func testCatalogRejectsRepeatedStopIdentifiers() {
        let city = City(id: "zagreb", name: "Zagreb", lat: 45.81, lon: 15.97, trams: true)
        let stop = Stop(id: "one", city: "zagreb", name: "Stanica",
                        lat: 45.81, lon: 15.97, modes: ["tram"])
        let catalog = Catalog(schemaVersion: 1, cities: [city], stops: [stop, stop])
        XCTAssertThrowsError(try catalog.validate())
    }

    func testCatalogAcceptsValidStops() {
        let city = City(id: "rijeka", name: "Rijeka", lat: 45.3, lon: 14.4, trams: false)
        let stop = Stop(id: "one", city: "rijeka", name: "Žabica",
                        lat: 45.3, lon: 14.4, modes: ["bus"])
        XCTAssertNoThrow(try Catalog(schemaVersion: 1, cities: [city], stops: [stop]).validate())
    }
}
