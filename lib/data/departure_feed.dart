import 'dart:convert';
import 'package:http/http.dart' as http;

/// HTTPS/JSON adapter; nema izmišljanja vremena dolaska ili GPS položaja.
class Departure {
  const Departure(this.stopId, this.line, this.destination, this.time, this.source);
  final String stopId;
  final String line;
  final String destination;
  final DateTime time;
  final String source;

  factory Departure.fromJson(Map<String, dynamic> row) {
    final stop = row['stop_id'];
    final line = row['line'];
    final destination = row['destination'];
    final rawTime = row['departure_time'];
    final source = row['source'];
    if (stop is! String || stop.isEmpty || line is! String || line.isEmpty ||
        destination is! String || destination.isEmpty ||
        source is! String || source.isEmpty || rawTime is! String) {
      throw const FormatException('Neispravan polazak.');
    }
    if (!RegExp(r'(Z|[+-]\d\d:\d\d)$').hasMatch(rawTime)) {
      throw const FormatException('Vremenska zona nije navedena.');
    }
    final parsed = DateTime.tryParse(rawTime);
    if (parsed == null) throw const FormatException('Neispravno vrijeme.');
    return Departure(stop, line, destination, parsed, source);
  }
}

class DepartureFeed {
  const DepartureFeed(this.url);
  final Uri url;

  Future<List<Departure>> fetch({http.Client? client}) async {
    if (url.scheme != 'https' || url.host.isEmpty || url.userInfo.isNotEmpty) {
      throw const FormatException('Dopušten je samo HTTPS.');
    }
    final ownsClient = client == null;
    final c = client ?? http.Client();
    try {
      final response = await c.get(url).timeout(const Duration(seconds: 12));
      if (response.statusCode != 200 || response.bodyBytes.length > 2097152) {
        throw const FormatException('Izvor nije dostupan ili je prevelik.');
      }
      final data = jsonDecode(utf8.decode(response.bodyBytes));
      if (data is! Map<String, dynamic> || data['departures'] is! List) {
        throw const FormatException('Nepodržan JSON format.');
      }
      final entries = data['departures'] as List;
      if (entries.length > 10000) throw const FormatException('Previše zapisa.');
      final result = entries.map((row) {
        if (row is! Map<String, dynamic>) {
          throw const FormatException('Neispravan zapis.');
        }
        return Departure.fromJson(row);
      }).toList();
      result.sort((a, b) => a.time.compareTo(b.time));
      return result;
    } finally {
      if (ownsClient) c.close();
    }
  }
}
