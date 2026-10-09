import XCTest
@testable import KRENI

final class LiveTests: XCTestCase {
    func testPredictedMinutes() {
        let arrival = TrackedArrival(id:"a",stopId:"zet:1",route:"6",mode:"tram",
                                     scheduledAt:nil,predictedAt:100121,updatedAt:100000)
        XCTAssertEqual(arrival.minutes(now:100000), 3)
        XCTAssertTrue(arrival.predicted(now:100000))
    }
    func testExtremeTimestampsDoNotCrash() {
        let badArrival = TrackedArrival(id:"a",stopId:"zet:1",route:"6",mode:"tram",
            scheduledAt:100120,predictedAt:nil,updatedAt:Int64.min)
        XCTAssertNil(badArrival.minutes(now:100000))
        let badSnapshot = LiveSnapshot(schemaVersion:1,city:"zagreb",provider:"zet",
            generatedAt:Int64.min,vehicles:[],arrivals:[])
        XCTAssertNil(badSnapshot.verified(city:"zagreb",provider:"zet",now:100000))
    }
    func testExpiredArrivalHidesMinutes() {
        let arrival = TrackedArrival(id:"a",stopId:"zet:1",route:"6",mode:"tram",
                                     scheduledAt:100100,predictedAt:100121,updatedAt:100000)
        XCTAssertEqual(arrival.minutes(now:100000), 3)
        XCTAssertNil(arrival.minutes(now:100091))
    }
    func testWrongCityOrOldSnapshotIsHidden() {
        let snap = LiveSnapshot(schemaVersion:1,city:"zagreb",provider:"zet",
                                generatedAt:100000,vehicles:[],arrivals:[])
        XCTAssertNil(snap.verified(city:"rijeka",provider:"zet",now:100000))
        XCTAssertNil(snap.verified(city:"zagreb",provider:"zet",now:100091))
    }
    func testProviderNeedsDocumentedProductionApproval() {
        let source = LiveSource(id:"zet",city:"zagreb",enabled:true,
            endpoint:"https://example.org/realtime",
            approvedForProduction:nil,licenseUrl:nil)
        XCTAssertFalse(source.usable)
        let approved = LiveSource(id:"zet",city:"zagreb",enabled:true,
            endpoint:"https://example.org/realtime",
            approvedForProduction:true,licenseUrl:"https://example.org/license")
        XCTAssertTrue(approved.usable)
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
