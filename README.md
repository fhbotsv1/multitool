# MultiTool

An all-in-one Android utility app built with Kotlin and Jetpack Compose.

## Project status

**Stage 1 — Project foundation and dashboard**
- [x] Android Gradle project configuration
- [x] Kotlin + Jetpack Compose + Material 3
- [x] Modern dark dashboard
- [x] Tool catalogue with category navigation
- [x] Measurement, sensor, calculator and phone-tool categories
- [x] Settings screen placeholder
- [x] Dedicated application ID: `com.fhbotsv1.multitool`
- [ ] Build and verify on a physical Android device

## Planned tools

### Measurement
- GPS speedometer
- Bubble level
- Screen ruler with calibration
- Protractor
- AR measurement
- GPS distance meter

### Sensors
- Digital compass
- Magnetic field detector (magnetometer)
- Magnetic metal detector
- Ambient light meter
- Sound level meter
- Barometer / altimeter
- Vibration meter

### Calculators
- Stopwatch
- Countdown timer
- Unit converter

### Phone tools
- Flashlight, strobe and SOS
- QR and barcode scanner
- Camera color detector
- Screen and touch tester

Tool availability depends on the hardware and sensors supported by the Android device.

## Technical stack

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX
- Minimum Android version: Android 8.0 (API 26)
- Compile SDK: 36
- JDK: 17

## Development approach

The app is developed in stages. Each tool will be implemented and tested independently before moving to the next stage. Sensor-based tools will check hardware availability and show a clear unsupported-device state when a required sensor is missing.

## Sensor measurement notes

The magnetic detector uses the phone's magnetometer and reports magnetic flux density in microtesla (µT). It measures magnetic fields and is not a general-purpose RF or radiation detector. Readings are affected by the phone, nearby magnets, metal objects and device calibration.

Sound level readings from a phone microphone are estimates unless the device and microphone are calibrated. AR measurement requires a compatible ARCore device.
