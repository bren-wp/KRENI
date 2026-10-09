import 'package:flutter/material.dart';
import '../../core/app_store.dart';
import '../../models/transit.dart';
import '../theme.dart';
import '../widgets.dart';
import 'planner_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key, required this.store, required this.openStop});
  final AppStore store;
  final ValueChanged<StopPoint> openStop;
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final TextEditingController query = TextEditingController();
  @override
  void dispose() { query.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final store = widget.store;
    final text = query.text.toLowerCase().trim();
    final stops = StopCatalog.inCity(store.cityId)
      .where((stop) => stop.name.toLowerCase().contains(text)).toList();
    final city = StopCatalog.city(store.cityId);

    return ListView(padding: const EdgeInsets.fromLTRB(18, 18, 18, 32), children: [
      const BrandHeader(),
      const SizedBox(height: 18),
      Container(
        padding: const EdgeInsets.all(22),
        decoration: BoxDecoration(
          gradient: const LinearGradient(colors: [KreniTheme.navy, Color(0xFF1157B9)]),
          borderRadius: BorderRadius.circular(26),
        ),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('Krenimo zajedno.', style: TextStyle(
            color: Colors.white, fontWeight: FontWeight.w900, fontSize: 28,
          )),
          const SizedBox(height: 5),
          const Text('Od grada do otoka. Jednostavnije, na jednom mjestu.',
            style: TextStyle(color: Color(0xFFD9E9FF))),
          const SizedBox(height: 17),
          FilledButton.icon(
            onPressed: () => Navigator.of(context).push(MaterialPageRoute<void>(
              builder: (_) => const PlannerScreen(),
            )),
            icon: const Icon(Icons.alt_route_rounded),
            label: const Text('Planiraj put'),
            style: FilledButton.styleFrom(
              backgroundColor: KreniTheme.teal,
              foregroundColor: KreniTheme.navy,
              minimumSize: const Size(double.infinity, 50),
            ),
          ),
        ]),
      ),
      const SizedBox(height: 18),
      Row(children: [
        const Icon(Icons.place_outlined, color: KreniTheme.blue),
        const SizedBox(width: 5),
        Text('Odabrani grad', style: Theme.of(context).textTheme.titleMedium),
        const Spacer(),
        CityPicker(store: store),
      ]),
      const SizedBox(height: 10),
      Wrap(spacing: 8, runSpacing: 8, children: TransitMode.values
        .where((mode) => mode != TransitMode.tram || city.hasTrams)
        .map((mode) => Chip(
          avatar: Icon(mode.icon, color: mode.color, size: 20),
          label: Text(mode.label),
        )).toList()),
      const SizedBox(height: 20),
      TextField(
        controller: query,
        onChanged: (_) => setState(() {}),
        decoration: InputDecoration(
          hintText: 'Pretraži lokacije u gradu',
          prefixIcon: const Icon(Icons.search_rounded),
          suffixIcon: query.text.isEmpty ? null :
            IconButton(
              tooltip: 'Očisti pretraživanje',
              onPressed: () { query.clear(); setState(() {}); },
              icon: const Icon(Icons.close_rounded),
            ),
        ),
      ),
      const SizedBox(height: 12),
      const DataNotice(short: true),
      const SizedBox(height: 18),
      Text('Stanice i terminali', style: Theme.of(context).textTheme.titleLarge),
      const SizedBox(height: 8),
      if (stops.isEmpty) const Padding(
        padding: EdgeInsets.all(16), child: Text('Nema lokacija za traženi pojam.'),
      ),
      for (final stop in stops)
        StopTile(stop: stop, store: store, onOpen: () => widget.openStop(stop)),
      const SizedBox(height: 14),
      Text('KRENI pokriva sve gradove kao koncept; stvarni podaci bit će dodavani prema provjerenim izvorima.',
        style: Theme.of(context).textTheme.bodySmall),
    ]);
  }
}
