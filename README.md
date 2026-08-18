Foldable IoT Crutch 


Smart Foldable Aluminium Axillary Crutch with IoT Integration for Enhanced Mobility and Rehabilitation 

  

What the Project Does 

- Our project introduces a  lightweight, foldable, IoT-enabled smart crutch  designed to improve mobility, safety, posture, and rehabilitation outcomes for users.
- It integrates multiple sensors, fall detection + ML-based fall prediction, health tracking, emergency alerts, and a mobile application for real-time monitoring.
- The device also includes a  built-in foldable chair ,  shock absorber ,  auto-LED , and  Frequency Dependent energy harvesting .



Problem Statement 

Traditional crutches cause:

 - Underarm pain, nerve compression, and wrist strain
 - No real-time feedback about user health or gait
 - No protection against falls
 - No smart features like posture guidance, alerts, or clinical monitoring
 - Inconvenient, bulky designs with no portability

 
 
 Problem Statement ID – 25144 (SIH 2025) 
 Theme – MedTech / HealthTech  

    Solution Overview 

We designed a  Smart Foldable Aluminium Crutch  that solves these issues through:

     🔹 Ergonomic & Foldable Design

  - Aluminium 6061 alloy (lightweight, rustproof, supports >120kg)
  - Cushioned axillary pad
  - Integrated portable folding chair
  - Adjustable height levels


     🔹 Smart Sensors & IoT

  - Weight / stress sensor for posture correction
  - Gyroscope for fall detection & ML fall prediction
  - PPG heart rate module
  - LDR-based auto LED
  - Mobile app (BLE/Wi-Fi)


     🔹 Safety Enhancements

  - 200 lumen LED
  - Reflective radium tail light
  - SOS emergency button


     🔹 Power & Efficiency

  - Frequency Dependent shock absorber generates energy
  - Hybrid system with Li-ion battery


  

    Key Features 

  🔹  1. Fall Detection & ML-Based Fall Prediction 

  Real-time gyro data + trained ML model for early fall alerts.

  🔹  2. Smart Health Monitoring 

  Heart rate tracking & calibration through PPG module.

  🔹  3. Posture Monitoring System 

  Weight sensor detects incorrect leaning and warns user.

  🔹  4. Auto Illumination System 

  LDR-controlled LED for night visibility + red tail light.

  🔹  5. SOS Emergency Trigger 

  Single-button emergency SMS / app alert to caregivers.

  🔹  6. Foldable Ergonomic Design 
 
  Integrated rest chair, cushioned pad, shock absorber.

  🔹  7. Energy Harvesting 

  Frequency Dependent absorber generates energy during walking.

  🔹  8.Workout Games
  Breathing Exercises, Walking Exercise, Posture Analysis With Photo, Indoor & Outdoor Training.

  🔹  9. MediKart Shopping App
  Shopping App for smart crutch & crutch related accessories 

  🔹  10. Voice Recognition System
  For giving commands to the crutch for performing tasks (i.e ., LED , SOS ) 

  🔹  11.Height Adjustment 
  Setting height adjustment -> Suggesting Upper Extension, Lower Extension, Top Level , Bottom Level to the user for adjusting the height

  🔹 12.Developer Mode
  Gives the raw data of the sensors directly to the user

  🔹 13.Reminder on the crutch 
  Gives reminder of taking prescribed medicines to the user.

  🔹  14.Caretaker Dashboard 
  Remote monitoring portal for clinical and family members 

  🔹  15. Data Visualization in health metrics 
  It gives real time data visualization in terms of statistical data 
  
  🔹  16. Multiple Variant with customizable system
  DhruvX, ApeX , ZenX
   


     🛠  Technologies Used 

    🔹 Hardware: 

  - Aluminium 6061 Alloy Frame
  - Gyroscope (Fall Sensor)
  - Weight / Stress Sensor
  - PPG Heart Rate Module
  - Frequency Dependent Shock Absorber
  - ESP32 / Arduino /
  - 200 lumen LED + Tail Light


    🔹 Software: 

   - C / C++  for firmware
   - Python (TensorFlow Lite)  for fall-prediction ML
   - Android (Java/Kotlin)  mobile app
   - Firebase / AWS IoT  for alerts
   - SOS emergency alert  system
   - MQTT / HTTP ,  CLASSIC BLUETOOTH  for communication
   - SQLite / PostgreSQL  for data storage


  

     System Architecture 

The diagram on  Page 3  shows the complete workflow:

  - Sensor Layer → Microcontroller → Wireless Connection
  - Firebase/AWS → Mobile App → User & Doctor Dashboard


  

     Steps to Install & Run the System 

         🔧 1. Firmware Setup (ESP32 / Arduino) 

1. Install  Arduino IDE 
2. Add ESP32 board manager
3. Install required libraries:

     `Adafruit_Sensor`
     `PulseSensor`
     `Wire.h`
     `BLEDevice.h`
4. Flash firmware to ESP32
5. Configure WiFi/MQTT credentials

  

2. Mobile App Setup (Android) 

1. Import project into Android Studio
2. Add dependencies:

    -  Firebase SDK
    -  Classic Bluetooth library
    -  Retrofit/MQTT client
3. Set environment variables & SDK location 
4. Build & Install `.apk`

  

 3. Cloud Configuration 

 If using Firebase: 

  - Create project
  - Enable Realtime DB / Firestore
  - Enable Cloud Messaging for alerts

 If using AWS IoT: 

  - Create IoT Core device
  - Generate certificate
  - Update ESP32 config

  

          4. Run the System 

1. Power the crutch
2. Open the Android app
3. Connect crutch via Bluetooth
4. Begin receiving real-time data in health metrics(HR, weight, fall status)
5. Enable SOS monitoring after clicking trigger button in the crutch

   Android App Overview Through Flowchart:-
   StrideX :-https://whimsical.com/tUmXisT6USV8ZvZq93T1k
   
   MediKart:-https://whimsical.com/V9r2iRES9udtvyjwP84mLd

