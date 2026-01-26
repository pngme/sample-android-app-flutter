import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const DiagnosticsApp());
}

class DiagnosticsApp extends StatelessWidget {
  const DiagnosticsApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Pngme Diagnostics',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blueGrey),
        useMaterial3: true,
      ),
      home: const DiagnosticsPage(),
    );
  }
}

class DiagnosticsPage extends StatefulWidget {
  const DiagnosticsPage({super.key});

  @override
  State<DiagnosticsPage> createState() => _DiagnosticsPageState();
}

class _DiagnosticsPageState extends State<DiagnosticsPage> {
  static const MethodChannel _channel = MethodChannel('com.pngme/diagnostics');

  final _clientKey = TextEditingController();
  final _firstName = TextEditingController(text: 'Test');
  final _lastName = TextEditingController(text: 'User');
  final _email = TextEditingController(text: 'test.user@example.com');
  final _phoneNumber = TextEditingController(text: '+15551234567');
  final _externalId = TextEditingController(text: 'flutter-diagnostics');
  final _companyName = TextEditingController(text: 'Pngme Diagnostics');

  Map<String, dynamic>? _diagnostics;
  String? _error;
  bool _busy = false;

  @override
  void dispose() {
    _clientKey.dispose();
    _firstName.dispose();
    _lastName.dispose();
    _email.dispose();
    _phoneNumber.dispose();
    _externalId.dispose();
    _companyName.dispose();
    super.dispose();
  }

  Future<void> _withBusy(Future<void> Function() fn) async {
    if (_busy) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await fn();
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _startPngme() async {
    await _channel.invokeMethod('startPngme', <String, dynamic>{
      'clientKey': _clientKey.text.trim(),
      'firstName': _firstName.text.trim(),
      'lastName': _lastName.text.trim(),
      'email': _email.text.trim(),
      'phoneNumber': _phoneNumber.text.trim(),
      'externalId': _externalId.text.trim(),
      'companyName': _companyName.text.trim(),
    });
  }

  Future<void> _scheduleSmsSync() async {
    await _channel.invokeMethod('scheduleSmsSync');
  }

  Future<void> _openAppSettings() async {
    await _channel.invokeMethod('openAppSettings');
  }

  Future<void> _refreshDiagnostics() async {
    final result = await _channel.invokeMethod('getDiagnostics');
    setState(() => _diagnostics = Map<String, dynamic>.from(result));
  }

  @override
  Widget build(BuildContext context) {
    final diagnosticsText = _diagnostics == null
        ? '(no diagnostics yet)'
        : const JsonEncoder.withIndent('  ').convert(_diagnostics);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Pngme Diagnostics'),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            TextField(
              controller: _clientKey,
              decoration: const InputDecoration(
                labelText: 'Client Key (SDK token)',
                hintText: 'Paste your Pngme client key here',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _firstName,
                    decoration: const InputDecoration(
                      labelText: 'First name',
                      border: OutlineInputBorder(),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: TextField(
                    controller: _lastName,
                    decoration: const InputDecoration(
                      labelText: 'Last name',
                      border: OutlineInputBorder(),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _email,
              decoration: const InputDecoration(
                labelText: 'Email',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _phoneNumber,
              decoration: const InputDecoration(
                labelText: 'Phone number',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _externalId,
              decoration: const InputDecoration(
                labelText: 'External ID',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _companyName,
              decoration: const InputDecoration(
                labelText: 'Company name',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                ElevatedButton(
                  onPressed: _busy
                      ? null
                      : () => _withBusy(() async {
                            await _startPngme();
                            await _refreshDiagnostics();
                          }),
                  child: const Text('Start Pngme Flow'),
                ),
                OutlinedButton(
                  onPressed: _busy ? null : () => _withBusy(_refreshDiagnostics),
                  child: const Text('Refresh Diagnostics'),
                ),
                OutlinedButton(
                  onPressed: _busy ? null : () => _withBusy(_scheduleSmsSync),
                  child: const Text('Schedule SMS Sync'),
                ),
                OutlinedButton(
                  onPressed: _busy ? null : () => _withBusy(_openAppSettings),
                  child: const Text('Open App Settings'),
                ),
              ],
            ),
            const SizedBox(height: 16),
            if (_error != null)
              Text(
                _error!,
                style: TextStyle(color: Theme.of(context).colorScheme.error),
              ),
            const SizedBox(height: 8),
            Text(
              diagnosticsText,
              style: const TextStyle(fontFamily: 'monospace'),
            ),
          ],
        ),
      ),
    );
  }
}
