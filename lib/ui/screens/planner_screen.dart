import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';
import '../../models/transit.dart';
import '../theme.dart';

class PlannerScreen extends StatefulWidget {
  const PlannerScreen({super.key});
  @override
  State<PlannerScreen> createState() => _PlannerScreenState();
}

class _PlannerScreenState extends State<PlannerScreen> {
  StopPoint? origin;
  StopPoint? destination;
  bool requested = false;

  @override
  Widget build(BuildContext context) {
    final distance = origin == null || destination == null ? null :
      const Distance().as(LengthUnit.Kilometer, origin!.location, destination!.location);
    return Scaffold(
      appBar: AppBar(title: const Text('Planer putovanja')),
      body: ListView(padding: const EdgeInsets.all(20), children: [
        const Text('Od grada do otoka', style: TextStyle(
          fontSize: 25, fontWeight: FontWeight.w900, color: KreniTheme.blue)),
        const SizedBox(height: 7),
        const Text('Odaberi polazište i odredište. Za potvrđene veze potreban je službeni vozni red.'),
        const SizedBox(height: 22),
        DropdownButtonFormField<StopPoint>(
          isExpanded: true,
          decoration: const InputDecoration(labelText: 'Odakle?'),
          value: origin,
          items: [
            for (final stop in StopCatalog.stops)
              DropdownMenuItem(value: stop,
                child: Text(StopCatalog.city(stop.cityId).name + ' · ' + stop.name,
                  overflow: TextOverflow.ellipsis)),
          ],
          onChanged: (stop) => setState(() { origin = stop; requested = false; }),
        ),
        const SizedBox(height: 14),
        IconButton(
          tooltip: 'Zamijeni polazište i odredište',
          onPressed: () => setState(() {
            final old = origin;
            origin = destination;
            destination = old;
            requested = false;
          }),
          icon: const Icon(Icons.swap_vert_rounded),
        ),
        DropdownButtonFormField<StopPoint>(
          isExpanded: true,
          decoration: const InputDecoration(labelText: 'Kamo?'),
          value: destination,
          items: [
            for (final stop in StopCatalog.stops)
              DropdownMenuItem(value: stop,
                child: Text(StopCatalog.city(stop.cityId).name + ' · ' + stop.name,
                  overflow: TextOverflow.ellipsis)),
          ],
          onChanged: (stop) => setState(() { destination = stop; requested = false; }),
        ),
        const SizedBox(height: 16),
        FilledButton.icon(
          onPressed: origin == null || destination == null ||
            origin!.id == destination!.id ? null : () => setState(() => requested = true),
          icon: const Icon(Icons.alt_route_rounded),
          label: const Text('Pregledaj putovanje'),
        ),
        if (requested && distance != null) ...[
          const SizedBox(height: 20),
          Card(child: Padding(padding: const EdgeInsets.all(20),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              const Icon(Icons.route_rounded, color: KreniTheme.blue, size: 38),
              const SizedBox(height: 10),
              Text('Zračna udaljenost: ' + distance.toStringAsFixed(1) + ' km',
                style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 12),
              const Text('Ovo NIJE prometna ruta ni trajanje putovanja. Povezivanje autobusa, tramvaja, vlakova i brodova bit će omogućeno kada budu dostupni provjereni vozni redovi.'),
              const SizedBox(height: 10),
              const Text('Provjerite stvarni polazak kod prijevoznika.',
                style: TextStyle(fontWeight: FontWeight.w700)),
            ]),
          ))),
        ],
      ]),
    );
  }
}
