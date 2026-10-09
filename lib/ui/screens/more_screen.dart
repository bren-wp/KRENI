import 'package:flutter/material.dart';
import '../../core/app_store.dart';
import '../../models/transit.dart';
import '../widgets.dart';

class MoreScreen extends StatelessWidget {
  const MoreScreen({super.key, required this.store, required this.openStop});
  final AppStore store;
  final ValueChanged<StopPoint> openStop;

  @override
  Widget build(BuildContext context) {
    final favourites = StopCatalog.stops
      .where((stop) => store.isFavourite(stop.id)).toList();
    return ListView(padding: const EdgeInsets.all(18), children: [
      const BrandHeader(),
      const SizedBox(height: 20),
      Text('Omiljeno', style: Theme.of(context).textTheme.headlineSmall),
      const SizedBox(height: 7),
      if (favourites.isEmpty)
        const Card(child: Padding(
          padding: EdgeInsets.all(18),
          child: Text('Još nema spremljenih lokacija. Dodaj zvjezdicu uz stanicu.'),
        )),
      for (final stop in favourites)
        StopTile(stop: stop, store: store, onOpen: () => openStop(stop)),
      const SizedBox(height: 25),
      Text('Postavke', style: Theme.of(context).textTheme.headlineSmall),
      Card(child: Column(children: [
        SwitchListTile.adaptive(
          title: const Text('Tamna tema'),
          subtitle: const Text('Promijeni izgled cijele aplikacije'),
          secondary: const Icon(Icons.dark_mode_outlined),
          value: store.darkMode,
          onChanged: store.setDarkMode,
        ),
        const Divider(height: 1),
        const ListTile(
          leading: Icon(Icons.translate_rounded),
          title: Text('Jezik'),
          subtitle: Text('Hrvatski'),
        ),
        const Divider(height: 1),
        const ListTile(
          leading: Icon(Icons.shield_outlined),
          title: Text('Privatnost'),
          subtitle: Text('Bez računa, vlastite baze podataka i praćenja korisnika'),
        ),
      ])),
      const SizedBox(height: 16),
      const DataNotice(),
      const SizedBox(height: 24),
      Center(child: Text('KRENI · v0.0.1 · Brendigo',
        style: Theme.of(context).textTheme.bodySmall)),
    ]);
  }
}
