import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';
import '../models/transit.dart';

/// Lokalna JSON datoteka; nije potreban backend ni baza podataka.
class AppStore extends ChangeNotifier {
  String _cityId = 'zagreb';
  bool _darkMode = false;
  final Set<String> _favourites = <String>{};
  bool _loaded = false;

  String get cityId => _cityId;
  bool get darkMode => _darkMode;
  bool get loaded => _loaded;
  Set<String> get favourites => Set.unmodifiable(_favourites);
  bool isFavourite(String id) => _favourites.contains(id);

  Future<File> _file() async {
    final directory = await getApplicationSupportDirectory();
    return File(directory.path + '/kreni_preferences.json');
  }

  Future<void> load() async {
    try {
      final file = await _file();
      if (await file.exists()) {
        final value = jsonDecode(await file.readAsString());
        if (value is Map<String, dynamic>) {
          final city = value['cityId'];
          if (city is String && StopCatalog.cities.any((c) => c.id == city)) {
            _cityId = city;
          }
          _darkMode = value['darkMode'] == true;
          final ids = value['favourites'];
          if (ids is List) {
            final known = StopCatalog.stops.map((s) => s.id).toSet();
            _favourites.addAll(ids.whereType<String>().where(known.contains));
          }
        }
      }
    } on FileSystemException {
      // Fallback na zadane postavke.
    } on FormatException {
      // Oštećena JSON datoteka ne smije srušiti aplikaciju.
    } on TypeError {
      // Nepodržana shema ne smije srušiti aplikaciju.
    } finally {
      _loaded = true;
      notifyListeners();
    }
  }

  Future<void> _save() async {
    try {
      final file = await _file();
      final tmp = File(file.path + '.tmp');
      await tmp.writeAsString(jsonEncode({
        'schemaVersion': 1,
        'cityId': _cityId,
        'darkMode': _darkMode,
        'favourites': _favourites.toList()..sort(),
      }), flush: true);
      await tmp.rename(file.path);
    } on FileSystemException {
      // UI ostaje responzivan ako pisanje nije moguće.
    }
  }

  void selectCity(String id) {
    if (id == _cityId || !StopCatalog.cities.any((c) => c.id == id)) return;
    _cityId = id;
    notifyListeners();
    _save();
  }

  void setDarkMode(bool value) {
    if (_darkMode == value) return;
    _darkMode = value;
    notifyListeners();
    _save();
  }

  void toggleFavourite(String id) {
    if (!StopCatalog.stops.any((s) => s.id == id)) return;
    if (!_favourites.add(id)) _favourites.remove(id);
    notifyListeners();
    _save();
  }
}
