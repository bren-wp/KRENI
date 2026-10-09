import 'package:flutter/material.dart';

class KreniTheme {
  static const navy = Color(0xFF071936);
  static const blue = Color(0xFF146BFC);
  static const teal = Color(0xFF02CDBD);
  static const background = Color(0xFFF5F8FC);

  static ThemeData build(bool dark) {
    final scheme = ColorScheme.fromSeed(
      seedColor: blue,
      brightness: dark ? Brightness.dark : Brightness.light,
      primary: dark ? const Color(0xFF74ACFF) : blue,
      surface: dark ? const Color(0xFF10243E) : Colors.white,
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: dark ? navy : background,
      appBarTheme: AppBarTheme(
        backgroundColor: dark ? navy : background,
        foregroundColor: dark ? Colors.white : navy,
        elevation: 0,
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: scheme.surface,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: scheme.surface,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide.none,
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: scheme.surface,
        indicatorColor: blue.withValues(alpha: dark ? 0.4 : 0.13),
      ),
    );
  }
}
