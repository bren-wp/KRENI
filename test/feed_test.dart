import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:kreni/data/departure_feed.dart';

void main() {
  test('odbija URL bez HTTPS-a', () async {
    final feed = DepartureFeed(Uri.parse('http://example.org/feed.json'));
    expect(feed.fetch(), throwsFormatException);
  });

  test('odbija polaske bez vremenske zone', () {
    expect(() => Departure.fromJson({
      'stop_id': 'zg-glavni',
      'line': '1',
      'destination': 'Zagreb',
      'departure_time': '2026-10-09T12:00:00',
      'source': 'Test',
    }), throwsFormatException);
  });

  test('validira i sortira potvrđene JSON zapise', () async {
    final client = MockClient((_) async => http.Response(
      '{"departures":['
      '{"stop_id":"zg-glavni","line":"2","destination":"B","departure_time":"2026-10-11T13:00:00+02:00","source":"Test"},'
      '{"stop_id":"zg-glavni","line":"1","destination":"A","departure_time":"2026-10-11T12:00:00+02:00","source":"Test"}'
      ']}', 200,
    ));
    final rows = await DepartureFeed(Uri.parse('https://example.org/feed.json'))
      .fetch(client: client);
    expect(rows.length, 2);
    expect(rows.first.line, '1');
    client.close();
  });
}
