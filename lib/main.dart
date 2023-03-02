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

    // Put here your SDK token
    // You can find it in the webconsole at admin.pngme.com under Settings > Keys
    // If you are just checking the sample app, it is best to use the Test token
    // Then, you can browse any sent data in the webconsole under Dashboard > Explore Data > (Select Test Environment on the right)
    try {
      value = await sdkChannel.invokeMethod("go", <String, dynamic>{
        'sdkToken': 'XXXXXXX',
        'firstName': 'Nico',
        'lastName': 'Rico',
        'email': 'nicorico@pngme.com',
        'phoneNumber': '2348118445990',
        'externalId': '',
        'companyName': 'AcmeInc',
        'hidePngmeDialog': false
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
