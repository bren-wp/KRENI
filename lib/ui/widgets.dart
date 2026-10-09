import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../core/app_store.dart';
import '../models/transit.dart';
import 'theme.dart';

class BrandHeader extends StatelessWidget {
  const BrandHeader({super.key, this.compact = false});
  final bool compact;
  @override
  Widget build(BuildContext context) => Row(children: [
    SvgPicture.asset('assets/brand/mark.svg',
      width: compact ? 38 : 50, height: compact ? 38 : 50),
    const SizedBox(width: 12),
    Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text('KRENI', style: TextStyle(
        fontWeight: FontWeight.w900,
        fontSize: compact ? 24 : 30, letterSpacing: -1.2,
      )),
      if (!compact) Text('Javni prijevoz Hrvatske',
        style: Theme.of(context).textTheme.labelSmall),
    ]),
  ]);
}

class CityPicker extends StatelessWidget {
  const CityPicker({super.key, required this.store});
  final AppStore store;
  @override
  Widget build(BuildContext context) => DropdownButton<String>(
    value: store.cityId,
    underline: const SizedBox.shrink(),
    icon: const Icon(Icons.keyboard_arrow_down_rounded),
    items: StopCatalog.cities.map((city) => DropdownMenuItem<String>(
      value: city.id, child: Text(city.name),
    )).toList(),
    onChanged: (value) {
      if (value != null) store.selectCity(value);
    },
  );
}

class StopTile extends StatelessWidget {
  const StopTile({super.key, required this.stop, required this.store, this.onOpen});
  final StopPoint stop;
  final AppStore store;
  final VoidCallback? onOpen;

  @override
  Widget build(BuildContext context) {
    final mode = stop.modes.first;
    final favourite = store.isFavourite(stop.id);
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
        child: Row(children: [
          CircleAvatar(
            backgroundColor: mode.color.withValues(alpha: .13),
            foregroundColor: mode.color,
            child: Icon(mode.icon),
          ),
          const SizedBox(width: 12),
          Expanded(child: InkWell(
            onTap: onOpen,
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 10),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(stop.name, style: const TextStyle(fontWeight: FontWeight.w700)),
                  const SizedBox(height: 3),
                  Text(stop.modes.map((m) => m.label).join(' · '),
                    style: Theme.of(context).textTheme.bodySmall),
                ],
              ),
            ),
          )),
          IconButton(
            tooltip: favourite ? 'Ukloni iz omiljenih' : 'Dodaj u omiljene',
            onPressed: () => store.toggleFavourite(stop.id),
            icon: Icon(favourite ? Icons.star_rounded : Icons.star_outline_rounded,
              color: favourite ? KreniTheme.blue : null),
          ),
          if (onOpen != null) IconButton(
            tooltip: 'Prikaži na karti',
            onPressed: onOpen,
            icon: const Icon(Icons.chevron_right_rounded),
          ),
        ]),
      ),
    );
  }
}

class DataNotice extends StatelessWidget {
  const DataNotice({super.key, this.short = false});
  final bool short;
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(12),
    decoration: BoxDecoration(
      color: KreniTheme.blue.withValues(alpha: .09),
      borderRadius: BorderRadius.circular(14),
    ),
    child: Row(children: [
      const Icon(Icons.info_outline_rounded, color: KreniTheme.blue),
      const SizedBox(width: 8),
      Expanded(child: Text(
        short ? 'Informativne lokacije · bez praćenja uživo' :
          'Lokacije su orijentacijske. Vozni redovi i GPS pozicije uživo nisu povezani sa službenim izvorima.',
        style: Theme.of(context).textTheme.bodySmall,
      )),
    ]),
  );
}
