import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';

enum TransitMode { bus, tram, train, ferry, catamaran }

extension TransitModeUi on TransitMode {
  String get label => switch (this) {
    TransitMode.bus => 'Autobusi',
    TransitMode.tram => 'Tramvaji',
    TransitMode.train => 'Vlakovi',
    TransitMode.ferry => 'Trajekti',
    TransitMode.catamaran => 'Katamarani',
  };
  IconData get icon => switch (this) {
    TransitMode.bus => Icons.directions_bus_rounded,
    TransitMode.tram => Icons.tram_rounded,
    TransitMode.train => Icons.train_rounded,
    TransitMode.ferry => Icons.directions_boat_filled_rounded,
    TransitMode.catamaran => Icons.sailing_rounded,
  };
  Color get color => switch (this) {
    TransitMode.bus => const Color(0xFF1378FF),
    TransitMode.tram => const Color(0xFF00A7DE),
    TransitMode.train => const Color(0xFF00A889),
    TransitMode.ferry => const Color(0xFF3976CF),
    TransitMode.catamaran => const Color(0xFF00BBAF),
  };
}

class CityInfo {
  const CityInfo(this.id, this.name, this.center, {this.hasTrams = false});
  final String id;
  final String name;
  final LatLng center;
  final bool hasTrams;
}

class StopPoint {
  const StopPoint(this.id, this.cityId, this.name, this.location, this.modes);
  final String id;
  final String cityId;
  final String name;
  final LatLng location;
  final Set<TransitMode> modes;
}

/// Informativne lokacije; nisu službeni GTFS stop_id niti GPS uživo.
class StopCatalog {
  static const cities = <CityInfo>[
    CityInfo('zagreb', 'Zagreb', LatLng(45.8131, 15.9775), hasTrams: true),
    CityInfo('rijeka', 'Rijeka', LatLng(45.3271, 14.4422)),
    CityInfo('split', 'Split', LatLng(43.5081, 16.4402)),
    CityInfo('osijek', 'Osijek', LatLng(45.5550, 18.6955), hasTrams: true),
    CityInfo('zadar', 'Zadar', LatLng(44.1194, 15.2314)),
    CityInfo('dubrovnik', 'Dubrovnik', LatLng(42.6507, 18.0944)),
    CityInfo('pula', 'Pula', LatLng(44.8666, 13.8496)),
    CityInfo('sibenik', 'Šibenik', LatLng(43.7350, 15.8952)),
    CityInfo('varazdin', 'Varaždin', LatLng(46.3057, 16.3366)),
    CityInfo('karlovac', 'Karlovac', LatLng(45.4929, 15.5553)),
  ];
  static const stops = <StopPoint>[
    StopPoint('zg-glavni','zagreb','Glavni kolodvor',LatLng(45.8049,15.9792),{TransitMode.tram,TransitMode.train}),
    StopPoint('zg-trg','zagreb','Trg bana J. Jelačića',LatLng(45.8132,15.9775),{TransitMode.tram}),
    StopPoint('zg-ak','zagreb','Autobusni kolodvor',LatLng(45.8030,15.9930),{TransitMode.bus}),
    StopPoint('ri-zabica','rijeka','Žabica',LatLng(45.3286,14.4360),{TransitMode.bus}),
    StopPoint('ri-hz','rijeka','Željeznički kolodvor',LatLng(45.3302,14.4306),{TransitMode.train}),
    StopPoint('ri-luka','rijeka','Putnička luka Rijeka',LatLng(45.3240,14.4410),{TransitMode.ferry,TransitMode.catamaran}),
    StopPoint('st-ak','split','Autobusni kolodvor',LatLng(43.5043,16.4436),{TransitMode.bus}),
    StopPoint('st-hz','split','Željeznički kolodvor',LatLng(43.5041,16.4434),{TransitMode.train}),
    StopPoint('st-luka','split','Trajektna luka Split',LatLng(43.5030,16.4410),{TransitMode.ferry,TransitMode.catamaran}),
    StopPoint('os-trg','osijek','Trg Ante Starčevića',LatLng(45.5603,18.6756),{TransitMode.tram}),
    StopPoint('os-hz','osijek','Željeznički kolodvor',LatLng(45.5487,18.6800),{TransitMode.train}),
    StopPoint('zd-luka','zadar','Gradska luka Zadar',LatLng(44.1180,15.2270),{TransitMode.ferry,TransitMode.catamaran}),
    StopPoint('zd-ak','zadar','Autobusni kolodvor',LatLng(44.1067,15.2391),{TransitMode.bus}),
    StopPoint('du-luka','dubrovnik','Luka Gruž',LatLng(42.6589,18.0870),{TransitMode.ferry,TransitMode.catamaran}),
    StopPoint('du-ak','dubrovnik','Autobusni kolodvor Gruž',LatLng(42.6600,18.0849),{TransitMode.bus}),
    StopPoint('pu-hz','pula','Željeznički kolodvor',LatLng(44.8753,13.8500),{TransitMode.train}),
    StopPoint('pu-luka','pula','Putnička luka Pula',LatLng(44.8740,13.8470),{TransitMode.ferry,TransitMode.catamaran}),
    StopPoint('si-luka','sibenik','Luka Šibenik',LatLng(43.7331,15.8910),{TransitMode.ferry,TransitMode.catamaran}),
    StopPoint('vz-hz','varazdin','Željeznički kolodvor',LatLng(46.3039,16.3405),{TransitMode.train}),
    StopPoint('ka-hz','karlovac','Željeznički kolodvor',LatLng(45.4927,15.5467),{TransitMode.train}),
  ];

  static CityInfo city(String id) => cities.firstWhere(
    (city) => city.id == id, orElse: () => cities.first,
  );
  static List<StopPoint> inCity(String id) =>
    stops.where((stop) => stop.cityId == id).toList();
}
