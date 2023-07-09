import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: '使用无障碍'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  var _batteryLevel = '电量未知';
  var serviceState = false;
  static const platform = MethodChannel('samples.flutter.dev/assistant');

  Future<void> _getBatteryLevel() async {
    String batteryLevel;
    try {
      final int result = await platform.invokeMethod('getBatteryLevel');
      batteryLevel = '当前电量为 $result %';
    } on PlatformException catch (e) {
      batteryLevel = "获取电量失败: '${e.message}'";
    }

    setState(() {
      _batteryLevel = batteryLevel;
    });
  }

  _handlerOpen(bool value) {
    if (kDebugMode) {
      print(value);
    }
    platform.invokeMethod('handlerOpen', value);
  }

  @override
  Widget build(BuildContext context) {
    platform.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'onAccessibilityServiceUpdate':
          setState(() {
            serviceState = call.arguments;
          });
          break;
        // 其他方法处理
      }
    });
    return Material(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            ElevatedButton(
              onPressed: _getBatteryLevel,
              child: const Text('获取电量'),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text("无障碍状态"),
                Switch(
                  value: serviceState,
                  onChanged: (value) {
                    _handlerOpen(value);
                  },
                ),
              ],
            ),
            ElevatedButton(
              onPressed: () => platform.invokeMethod('handlerService', "home"),
              child: const Text('返回桌面'),
            ),
            ElevatedButton(
              onPressed: () => platform.invokeMethod('handlerService', "recent"),
              child: const Text('最近任务'),
            ),
            Text(_batteryLevel),
          ],
        ),
      ),
    );
  }
}
