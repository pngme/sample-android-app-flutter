import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // #docregion build
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Pngme',
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Pngme SDK: Sample Flutter App'),
        ),
        body: const BodyWidget(),
      ),
    );
  }
}

class BodyWidget extends StatefulWidget {
  const BodyWidget({Key? key}) : super(key: key);

  @override
  State<BodyWidget> createState() => _BodyWidgetState();
}

class _BodyWidgetState extends State<BodyWidget> {
  bool _buttonEnabled = true;
  static const sdkChannel = MethodChannel('com.flutter.pngme/sdk');

  void _clickButton() {
    openSDK();
    setState(() => _buttonEnabled = false);
  }

  void openSDK() async {
    String value;
    print("opening Pngme SDK...");
    try {
      value = await sdkChannel.invokeMethod("go", <String, dynamic>{
        'sdkToken': 'cc94dacfc4b816fabf9bd0f12e892172d05de9c57209922efe8378707dd846704f6b885beb71228a564190a56e673810',
        'firstName': 'Nico',
        'lastName': 'Flutter 5',
        'email': 'nicorico@pngme.com',
        'phoneNumber': '2348118445005',
        'externalId': '',
        'isKycVerified': false,
        'companyName': 'AcmeInc',
        'hidePngmeDialog': true,
      });
      print(value);
    } catch (e) {
      print(e);
    }
  }

  @override
  Widget build(BuildContext context) {
    final ButtonStyle style =
    ElevatedButton.styleFrom(textStyle: const TextStyle(fontSize: 20));

    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          ElevatedButton(
            style: style,
            onPressed: _buttonEnabled ? _clickButton : null,
            child: const Text('Invoke Pngme SDK'),
          ),
        ],
      ),
    );
  }
}