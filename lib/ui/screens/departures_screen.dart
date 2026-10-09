import 'package:flutter/material.dart';
import '../../core/app_store.dart';
import '../../data/departure_feed.dart';
import '../../models/transit.dart';
import '../widgets.dart';

const _feedUrl = String.fromEnvironment('KRENI_FEED_URL');

class DeparturesScreen extends StatefulWidget {
  const DeparturesScreen({super.key, required this.store});
  final AppStore store;
  @override
  State<DeparturesScreen> createState() => _DeparturesScreenState();
}

class _DeparturesScreenState extends State<DeparturesScreen> {
  Future<List<Departure>>? future;
  @override
  void initState() {
    super.initState();
    refresh();
  }

  void refresh() {
    if (_feedUrl.isEmpty) return;
    final url = Uri.tryParse(_feedUrl);
    if (url == null) {
      setState(() => future = Future.error(const FormatException('Neispravan URL.')));
      return;
    }
    setState(() => future = DepartureFeed(url).fetch());
  }

  @override
  Widget build(BuildContext context) {
    final ids = StopCatalog.inCity(widget.store.cityId).map((s) => s.id).toSet();
    return ListView(padding: const EdgeInsets.all(18), children: [
      const BrandHeader(compact: true),
      const SizedBox(height: 22),
      Text('Polasci', style: Theme.of(context).textTheme.headlineMedium),
      const SizedBox(height: 7),
      Row(children: [
        const Text('Grad: '),
        CityPicker(store: widget.store),
        const Spacer(),
        IconButton(onPressed: _feedUrl.isEmpty ? null : refresh,
          tooltip: 'Osvježi podatke', icon: const Icon(Icons.refresh_rounded)),
      ]),
      const SizedBox(height: 12),
      const DataNotice(),
      const SizedBox(height: 15),
      if (_feedUrl.isEmpty)
        const Card(child: Padding(padding: EdgeInsets.all(20), child: Column(
          children: [
            Icon(Icons.schedule_rounded, size: 44),
            SizedBox(height: 9),
            Text('Vozni redovi još nisu povezani.',
              style: TextStyle(fontWeight: FontWeight.bold)),
            SizedBox(height: 8),
            Text('Nakon potvrde licence i dostupnosti podataka moguće je uključiti službeni HTTPS/JSON feed.'),
          ],
        )))
      else FutureBuilder<List<Departure>>(
        future: future,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: Padding(padding: EdgeInsets.all(24),
              child: CircularProgressIndicator()));
          }
          if (snapshot.hasError) {
            return Card(child: Padding(padding: const EdgeInsets.all(20),
              child: Text('Izvor polazaka nije dostupan. Provjerite internetsku vezu i podatke prijevoznika.')));
          }
          final results = (snapshot.data ?? [])
            .where((d) => ids.contains(d.stopId) && d.time.isAfter(DateTime.now()))
            .toList();
          if (results.isEmpty) {
            return const Card(child: Padding(
              padding: EdgeInsets.all(20), child: Text('Za odabrani grad nema potvrđenih budućih polazaka.')));
          }
          return Column(children: [
            for (final departure in results)
              Card(child: ListTile(
                leading: const Icon(Icons.schedule_rounded),
                title: Text(departure.line + ' · ' + departure.destination),
                subtitle: Text('Izvor: ' + departure.source),
                trailing: Text(
                  departure.time.toLocal().hour.toString().padLeft(2, '0') +
                  ':' + departure.time.toLocal().minute.toString().padLeft(2, '0'),
                  style: const TextStyle(fontWeight: FontWeight.w700),
                ),
              )),
          ]);
        },
      ),
    ]);
  }
}
