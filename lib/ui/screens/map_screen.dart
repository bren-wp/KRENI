import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../core/app_store.dart';
import '../../models/transit.dart';
import '../theme.dart';
import '../widgets.dart';

class MapScreen extends StatefulWidget {
  const MapScreen({super.key, required this.store, this.focused});
  final AppStore store;
  final StopPoint? focused;
  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  final MapController controller = MapController();
  StopPoint? selected;
  TransitMode? activeMode;
  late String previousCity;

  @override
  void initState() {
    super.initState();
    selected = widget.focused;
    previousCity = widget.store.cityId;
  }

  @override
  void didUpdateWidget(covariant MapScreen oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (previousCity != widget.store.cityId) {
      previousCity = widget.store.cityId;
      selected = null;
      activeMode = null;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) controller.move(StopCatalog.city(previousCity).center, 12);
      });
    }
  }

  @override
  void dispose() { controller.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final city = StopCatalog.city(widget.store.cityId);
    final stops = StopCatalog.inCity(city.id)
      .where((stop) => activeMode == null || stop.modes.contains(activeMode)).toList();
    return Column(children: [
      Padding(
        padding: const EdgeInsets.fromLTRB(16, 14, 16, 8),
        child: Row(children: [
          const Expanded(child: BrandHeader(compact: true)),
          CityPicker(store: widget.store),
        ]),
      ),
      SizedBox(height: 52,
        child: ListView(
          scrollDirection: Axis.horizontal,
          padding: const EdgeInsets.symmetric(horizontal: 14),
          children: [
            Padding(padding: const EdgeInsets.only(right: 7), child:
              ChoiceChip(label: const Text('Sve'), selected: activeMode == null,
                onSelected: (_) => setState(() => activeMode = null))),
            for (final mode in TransitMode.values)
              if (mode != TransitMode.tram || city.hasTrams)
                Padding(padding: const EdgeInsets.only(right: 7), child:
                  ChoiceChip(
                    avatar: Icon(mode.icon, size: 18, color: mode.color),
                    label: Text(mode.label),
                    selected: activeMode == mode,
                    onSelected: (_) => setState(() {
                      activeMode = activeMode == mode ? null : mode;
                      selected = null;
                    }),
                  )),
          ],
        ),
      ),
      Expanded(child: Stack(children: [
        FlutterMap(
          mapController: controller,
          options: MapOptions(
            initialCenter: widget.focused?.location ?? city.center,
            initialZoom: widget.focused == null ? 12 : 15,
          ),
          children: [
            TileLayer(
              urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
              userAgentPackageName: 'hr.brendigo.kreni',
            ),
            MarkerLayer(markers: [
              for (final stop in stops)
                Marker(
                  point: stop.location, width: 48, height: 48,
                  child: Tooltip(
                    message: stop.name,
                    child: GestureDetector(
                      onTap: () => setState(() => selected = stop),
                      child: DecoratedBox(
                        decoration: BoxDecoration(
                          color: selected?.id == stop.id ?
                            KreniTheme.teal : KreniTheme.blue,
                          shape: BoxShape.circle,
                          border: Border.all(color: Colors.white, width: 3),
                        ),
                        child: Icon(stop.modes.first.icon, color: Colors.white),
                      ),
                    ),
                  ),
                ),
            ]),
            RichAttributionWidget(attributions: [
              TextSourceAttribution('© OpenStreetMap contributors',
                onTap: () => launchUrl(Uri.parse('https://www.openstreetmap.org/copyright'))),
            ]),
          ],
        ),
        Positioned(top: 12, left: 12, right: 12, child: const DataNotice(short: true)),
        if (selected != null)
          Positioned(bottom: 12, left: 12, right: 12,
            child: Card(child: Padding(padding: const EdgeInsets.all(14),
              child: Column(mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Row(children: [
                    Expanded(child: Text(selected!.name,
                      style: Theme.of(context).textTheme.titleMedium)),
                    IconButton(
                      onPressed: () => setState(() => selected = null),
                      icon: const Icon(Icons.close_rounded),
                      tooltip: 'Zatvori',
                    ),
                  ]),
                  Text(selected!.modes.map((m) => m.label).join(' · ')),
                  const SizedBox(height: 8),
                  Text('Orijentacijska lokacija. Nema provjerenog vremena dolaska.',
                    style: Theme.of(context).textTheme.bodySmall),
                  TextButton.icon(
                    onPressed: () {
                      if (selected != null) {
                        widget.store.toggleFavourite(selected!.id);
                      }
                    },
                    icon: Icon(widget.store.isFavourite(selected!.id) ?
                      Icons.star_rounded : Icons.star_outline_rounded),
                    label: Text(widget.store.isFavourite(selected!.id) ?
                      'Ukloni iz omiljenih' : 'Spremi u omiljene'),
                  ),
                ]),
            ))),
      ])),
    ]);
  }
}
