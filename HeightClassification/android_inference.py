"""
Android TensorFlow Lite Inference Helper
This file demonstrates how to use the TFLite model in Android
"""

import numpy as np
import pickle
import os

# ============================
# ANDROID MODEL CONFIG
# ============================
TFLITE_MODEL_PATH = "models/crutch_height_model.tflite"
SCALER_PATH = "models/scaler.pkl"

# Model input specifications (for Android implementation)
INPUT_SHAPE = (1, 4)  # Batch size 1, 4 features
INPUT_DTYPE = np.float32

# Input feature order (IMPORTANT for Android):
# 0: UserHeight_cm
# 1: ArmpitToWrist_cm
# 2: WristToFoot_cm
# 3: UserWeight_kg

# ============================
# ANDROID IMPLEMENTATION GUIDE
# ============================
"""
HOW TO USE IN ANDROID:

1. Copy these files to your Android assets folder:
   - crutch_height_model.tflite
   - scaler.pkl (optional, if using same scaler logic)

2. Add TensorFlow Lite dependency to build.gradle:
   dependencies {
       implementation 'org.tensorflow:tensorflow-lite:+'
   }

3. Load and run model in Android (Kotlin example):
   
   // Load model
   val tfliteOptions = Interpreter.Options()
   val interpreter = Interpreter(loadModelFile(context), tfliteOptions)
   
   // Prepare input (4 features in this order)
   val inputData = Array(1) { FloatArray(4) }
   inputData[0][0] = userHeight        // UserHeight_cm
   inputData[0][1] = armpitToWrist     // ArmpitToWrist_cm
   inputData[0][2] = wristToFoot       // WristToFoot_cm
   inputData[0][3] = userWeight        // UserWeight_kg
   
   // Scale input (min-max normalization)
   // Using the min/max values from training scaler
   val minValues = floatArrayOf(...)   // Load from scaler.pkl
   val maxValues = floatArrayOf(...)   // Load from scaler.pkl
   
   for (i in 0..3) {
       inputData[0][i] = (inputData[0][i] - minValues[i]) / (maxValues[i] - minValues[i])
   }
   
   // Run inference
   val output = Array(1) { FloatArray(1) }
   interpreter.run(inputData, output)
   
   // Get prediction
   val crutchHeight = output[0][0]
   
   // Close interpreter
   interpreter.close()

4. Scaler min/max values can be extracted using:
   python get_scaler_values.py
"""

def get_scaler_info():
    """Extract and display scaler min/max values for Android"""
    with open(SCALER_PATH, "rb") as f:
        scaler = pickle.load(f)
    
    print("="*60)
    print("SCALER INFO FOR ANDROID")
    print("="*60)
    print("\nFeature Order:")
    features = ["UserHeight_cm", "ArmpitToWrist_cm", "WristToFoot_cm", "UserWeight_kg"]
    for i, feat in enumerate(features):
        print(f"  {i}: {feat}")
    
    print("\nMin Values (for scaling):")
    print(f"  {scaler.data_min_.tolist()}")
    
    print("\nMax Values (for scaling):")
    print(f"  {scaler.data_max_.tolist()}")
    
    print("\nScale Factor (Max - Min):")
    print(f"  {(scaler.data_max_ - scaler.data_min_).tolist()}")
    
    print("\n" + "="*60)
    print("ANDROID CODE SNIPPET:")
    print("="*60)
    print("""
    // Scaler values for Android
    val minValues = floatArrayOf(
""" + f"        {scaler.data_min_[0]}f, {scaler.data_min_[1]}f, {scaler.data_min_[2]}f, {scaler.data_min_[3]}f" + """
    )
    
    val maxValues = floatArrayOf(
""" + f"        {scaler.data_max_[0]}f, {scaler.data_max_[1]}f, {scaler.data_max_[2]}f, {scaler.data_max_[3]}f" + """
    )
    
    // Scale input
    for (i in 0..3) {
        inputData[0][i] = (inputData[0][i] - minValues[i]) / (maxValues[i] - minValues[i])
    }
    """)

if __name__ == "__main__":
    print("[INFO] TensorFlow Lite Model Info for Android\n")
    print(f"Model path: {TFLITE_MODEL_PATH}")
    print(f"Model size: {os.path.getsize(TFLITE_MODEL_PATH) / 1024:.2f} KB\n")
    
    get_scaler_info()
    
    print("\n[DONE] Android setup info complete.")
