import XCTest
@testable import KRENI

final class LiveTests: XCTestCase {
    func testPredictedMinutes() {
        let arrival = TrackedArrival(id:"a",stopId:"zet:1",route:"6",mode:"tram",
                                     scheduledAt:nil,predictedAt:100121,updatedAt:100000)
        XCTAssertEqual(arrival.minutes(now:100000), 3)
        XCTAssertTrue(arrival.predicted(now:100000))
    }
    func testExpiredVehicleIsRemoved() {
        let vehicle = TrackedVehicle(id:"v",route:"6",mode:"tram",
                                      lat:45.81,lon:15.97,observedAt:99900)
        let snapshot = LiveSnapshot(schemaVersion:1,city:"zagreb",provider:"zet",
                                     generatedAt:100000,vehicles:[vehicle],arrivals:[])
        let checked = snapshot.verified(city:"zagreb",provider:"zet",now:100000)
        XCTAssertTrue(checked?.vehicles.isEmpty == true)
        XCTAssertNil(snapshot.verified(city:"zagreb",provider:"zet",now:100091))
    }
}
