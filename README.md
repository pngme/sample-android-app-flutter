<p align="center">
  <img src="https://admin.pngme.com/logo.png" alt="Pngme" width="100" height="100">
  <h3 align="center">Pngme Android (Flutter) Integration & Sample App</h3>
</p>

This documentation covers how to use the Kotlin SDK v2.x with Flutter.

You can find similar documentation for [Expo](https://github.com/pngme/sample-android-app-react-native-expo), [Flutter-Java](https://github.com/pngme/sample-android-app-flutter_java), [Kotlin](https://github.com/pngme/sample-android-app-kotlin) and [React Native](https://github.com/pngme/sample-android-app-react-native).

> Pngme does not currently provide a native Flutter (Dart) SDK but the Kotlin SDK is compatible with Flutter apps using the following steps.

## Setup

1. The SDK supports Android API version 16+
1. The SDK enables your app to:
   1. Register a mobile phone user with Pngme
   1. Request SMS permissions from the user using a [Permission Dialog Flow](.docs/permission_flow.gif)
   1. Periodically send data to Pngme to analyze financial events
1. Using the SDK requires an **SDK Token**
   - [**Sign up for a free Pngme Dashboard account**](https://admin.pngme.com) then access your SDK token from the [Keys page](https://admin.pngme.com/keys)
   - Use the `test` SDK token during development but replace with the `production` SDK token before deploying your app to the Google Play store

<p align="center">
  <img src="https://raw.githubusercontent.com/pngme/sample-android-app-flutter/main/.docs/webconsole_keys.png" width=450 height=300/>
</p>

After integrating the SDK, financial data will be accessible in the [Pngme Dashboard](https://admin.pngme.com/users) and via the [Pngme REST APIs](https://developers.api.pngme.com/reference/).

## Integrating the SDK

This sample app assumes you have Android Studio installed and your local environment is [configured for Flutter development](https://docs.flutter.dev/get-started/install).

### Step 1

Add the JitPack package manager to `/android/build.gradle`.

```groovy
    allprojects {
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }
```

### Step 2

Check that `ext.kotlin_version >= '1.4.32'` in `/android/build.gradle`. You can skip this step if you are using a newer version.

```groovy
buildscript {
    ext.kotlin_version = '1.4.32'  // update version here
    ...
```

### Step 3

Add the following dependencies to `/android/app/build.gradle`.

```groovy
dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    // add from here
    implementation 'com.github.pngme:android-sdk:v3.2.0'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'androidx.multidex:multidex:2.0.1'
    // to here
}
```

### Step 4

Enable multidex support in `/android/app/build.gradle`.

```groovy
android {
    defaultConfig {
        multiDexEnabled = true
    }
}
```

### Step 5

Add the compatibility activity from [/android/app/src/main/kotlin/com/example/sampleflutter/PngmeSDKHelper.kt](/android/app/src/main/kotlin/com/example/sampleflutter/PngmeSDKHelper.kt) by copying the entire file into your project.

⚠️ Modify the top line of the `PngmeSDKHelper.kt` file to match your project path/naming.

```groovy
package com.example.sampleflutter  // update project name here
```

### Step 6

Add the `PngmeSDKHelper` as an activity in [/android/app/src/main/AndroidManifest.xml](/android/app/src/main/AndroidManifest.xml).

⚠️ Modify `com.example.sampleflutter` to match your project name.

```xml
<activity android:name="com.example.sampleflutter.PngmeSDKHelper" android:theme="@style/Theme.AppCompat.NoActionBar" />
<!--                   ^ update project name here                                                                   -->
```

### Step 7

Add the Flutter channel and ensure you override the `configureFlutterEngine` method in your main activity similar to [/android/app/src/main/kotlin/com/example/sampleflutter/MainActivity.kt](/android/app/src/main/kotlin/com/example/sampleflutter/MainActivity.kt).

### Step 8

Call the Pngme SDK via the Flutter channel from your main Flutter app, passing the `go` method in the channel.

```dart
value = await sdkChannel.invokeMethod("go", <String, dynamic>{
        'sdkToken': 'XXXXXXX',
        'firstName': 'Nico',
        'lastName': 'Rico',
        'email': 'nicorico@pngme.com',
        'phoneNumber': '2348118445990',
        'externalId': '',
        'isKycVerified': false,
        'companyName': 'AcmeInc'
        'hidePngmeDialog': boolean; // defaults to false

      });
```

> ⚠️ The SDK Token is sensitive and must be protected. It should be passed to the application at compile time and encrypted. Consider using an encrypted secrets manager (such as [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/)) to store the SDK token.
>
> The SDK Token as an inline string is shown here **for demonstration purposes only**.

| Field           | Description                                                                                             |
| --------------- | ------------------------------------------------------------------------------------------------------- |
| `sdkToken`      | the SDK Token from the [Pngme Dashboard Keys page](https://admin.pngme.com/keys)                        |
| `firstName`     | the mobile phone user's first name                                                                      |
| `lastName`      | the mobile phone user's last name                                                                       |
| `email`         | the mobile phone user's email address                                                                   |
| `phoneNumber`   | the mobile phone user's phone number, example `"23411234567"`                                           |
| `externalId`    | a unique identifier for the user provided by your app; if none available, pass an empty string `""`     |
| `companyName`   | your company's name, used in the display header of the [SMS Permission Flow](.docs/permission_flow.gif) |
| hidePngmeDialog | a boolean, indicating if the Pngme dialog should be hidden in the permissions UI flow                   |

## PngmeSDK API

### `resetPermissionFlow()`

```kotlin
fun resetPermissionFlow(context: Context)
```

| Field   | Description             |
| ------- | ----------------------- |
| context | the current app Context |

The [Permission Dialog Flow](.docs/permission_flow.gif) will only run the first time that the `go` method is invoked.
If your app needs to implement logic to show the Dialog Flow again,
then you can reset the permission flow by calling `resetPermissionFlow`.

### `isPermissionGranted()`

```kotlin
fun isPermissionGranted(context: Context): Boolean
```

| Field   | Description             |
| ------- | ----------------------- |
| context | the current app Context |

This indicates if the user has accepted the SMS permissions request:

- Returns `true` if the user has accepted the SMS permission request.
- Returns `false` if the user has denied the SMS permission request.

## Sample Flutter App

The Pngme SDK is launched in the `openSDK()` method located in the Flutter app entrypoint: [`lib/main.dart`](lib/main.dart). This triggers a demo call to the PngmeSDK using the following arguments:

```dart
String value;
value = await sdkChannel.invokeMethod("go", <String, dynamic>{
        'sdkToken': 'XXXXXXX',
        'firstName': 'Nico',
        'lastName': 'Rico',
        'email': 'nicorico@pngme.com',
        'phoneNumber': '2348118445990',
        'externalId': '',
        'isKycVerified': false,
        'companyName': 'AcmeInc'
      });
```

### Behavior

The sample app demonstrates a basic flow:

1. user creates an account with the app
2. the user goes to apply for a loan, and has the option of selecting to use the Pngme service
3. if the Pngme service is selected, the SDK is invoked, and the [Permission Flow](.docs/permission_flow.gif) is presented (unless the `hidePngmeDialog` flag has been set to `true`)

   <sub>- :warning: _Note that if a user chooses to hide the permissions flow, they will need to design their own information and consent screen compliant with Google Whitelisting requirements. Consult with <support@pngme.com> if you would like assistance with this process._</sub>

4. when the permission flow exits, the user is presented with a fake loan application page

### Sending test data

This can be tested in a sample app running in the local emulator, assuming the emulated app is running with a valid SDK Token.

Android Emulator can simulate incoming SMS messages, and we can use this to test the Pngme SDK locally.

> _If a valid SDK token is used in the `'sdkToken': 'XXXXXXX'` parameter, then the below SMS will be successfully sent to the Pngme system_.

The following text message is of a recognized format for the Stanbic bank sender: `Stanbic`.

```text
Acc:XXXXXX1111
CR:NGN4,000.00
Desc:HELLO WORLD! SAMPLE MESSAGE
Bal:NGN50,000.00
```

You can inject this fake SMS into the emulated phone by following these steps.
It is advisable that you pre-populate the emulated phone with the SMS _before_ running the sample app.

> Once the app gets the permissions form the user it will instantly start sending existing SMS messages to the Pngme system. This results in messages being seen way sooner than SMS received after the app was installed.
>
> The daemon is processing new messages every 30 minutes, so the new feed messages will take at least 30 minutes to appear in the webconsole.

![Inject Fake SMS](.docs/inject_fake_sms.png)

1. Open the `more` window in the emulator settings
2. Navigate to the `phone` section
3. Set the sender to the string `Stanbic` or one of the senders from our [supported institutions](https://developers.api.pngme.com/reference/supported-institutions)
4. Copy/Paste the above same message into the message box
5. Hit `Send Message`

After following the above steps to send a fake SMS, run the sample app.
The fake SMS will be sent to the Pngme system using the SDK token from your Pngme account.
If the sample app runs successfully, the financial data in the text message will be accessible
via the [Pngme REST APIs](https://developers.api.pngme.com/reference/getting-started-with-your-api) or in the [Pngme webconsole](https://admin.pngme.com).

## Next steps

See [Going Live with the SDK](https://developers.api.pngme.com/docs/going-live-with-the-sdk) to learn more about the whitelisting process with the Google Play store.
