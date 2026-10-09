import 'package:flutter/material.dart';

import 'core/app_store.dart';
import 'models/transit.dart';
import 'ui/theme.dart';
import 'ui/screens/home_screen.dart';
import 'ui/screens/map_screen.dart';
import 'ui/screens/departures_screen.dart';
import 'ui/screens/more_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const KreniApp());
}

class KreniApp extends StatefulWidget {
  const KreniApp({super.key});
  @override
  State<KreniApp> createState() => _KreniAppState();
}

class _KreniAppState extends State<KreniApp> {
  final AppStore store = AppStore();

  @override
  void initState() {
    super.initState();
    store.load();
  }

  @override
  void dispose() {
    store.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: store,
    builder: (context, _) => MaterialApp(
      title: 'KRENI · Javni prijevoz Hrvatske',
      debugShowCheckedModeBanner: false,
      theme: KreniTheme.build(false),
      darkTheme: KreniTheme.build(true),
      themeMode: store.darkMode ? ThemeMode.dark : ThemeMode.light,
      home: KreniShell(store: store),
    ),
  );
}

class KreniShell extends StatefulWidget {
  const KreniShell({super.key, required this.store});
  final AppStore store;
  @override
  State<KreniShell> createState() => _KreniShellState();
}

class _KreniShellState extends State<KreniShell> {
  int tab = 0;
  StopPoint? focusedStop;

  void openStop(StopPoint stop) {
    widget.store.selectCity(stop.cityId);
    setState(() {
      focusedStop = stop;
      tab = 1;
    });
  }

  @override
  Widget build(BuildContext context) {
    final screen = switch (tab) {
      0 => HomeScreen(store: widget.store, openStop: openStop),
      1 => MapScreen(
        key: ValueKey(focusedStop?.id ?? 'map'),
        store: widget.store,
        focused: focusedStop,
      ),
      2 => DeparturesScreen(store: widget.store),
      _ => MoreScreen(store: widget.store, openStop: openStop),
    };

    return Scaffold(
      body: SafeArea(child: screen),
      bottomNavigationBar: NavigationBar(
        selectedIndex: tab,
        onDestinationSelected: (index) => setState(() {
          tab = index;
          if (tab != 1) focusedStop = null;
        }),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home_rounded), label: 'Početna'),
          NavigationDestination(icon: Icon(Icons.map_outlined), selectedIcon: Icon(Icons.map_rounded), label: 'Karta'),
          NavigationDestination(icon: Icon(Icons.schedule_outlined), selectedIcon: Icon(Icons.schedule_rounded), label: 'Polasci'),
          NavigationDestination(icon: Icon(Icons.menu_rounded), selectedIcon: Icon(Icons.person_rounded), label: 'Više'),
        ],
      ),
    );
  }
}
