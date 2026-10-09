import 'package:flutter_test/flutter_test.dart';
import 'package:kreni/models/transit.dart';

void main() {
  test('svaka lokacija ima jedinstveni identifikator i poznati grad', () {
    final ids = StopCatalog.stops.map((s) => s.id).toList();
    expect(ids.toSet().length, ids.length);
    for (final stop in StopCatalog.stops) {
      expect(StopCatalog.cities.any((c) => c.id == stop.cityId), isTrue);
      expect(stop.modes, isNotEmpty);
    }
  });

  test('tramvaji su povezani samo sa Zagrebom i Osijekom', () {
    for (final stop in StopCatalog.stops) {
      if (stop.modes.contains(TransitMode.tram)) {
        expect(StopCatalog.city(stop.cityId).hasTrams, isTrue);
      }
    }
    expect(StopCatalog.city('rijeka').hasTrams, isFalse);
  });

  test('svaki grad ima različit ID', () {
    final ids = StopCatalog.cities.map((c) => c.id);
    expect(ids.toSet().length, StopCatalog.cities.length);
  });
}
