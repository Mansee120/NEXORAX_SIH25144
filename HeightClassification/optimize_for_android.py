"""
Optimize TensorFlow Lite Model for Android
Applies quantization and other optimizations
"""

import tensorflow as tf
import os

# ============================
# LOAD AND OPTIMIZE MODEL
# ============================
MODEL_DIR = "models"
KERAS_MODEL_PATH = os.path.join(MODEL_DIR, "crutch_height_model.keras")
TFLITE_OPTIMIZED_PATH = os.path.join(MODEL_DIR, "crutch_height_model_optimized.tflite")

print("[INFO] Loading Keras model...")
model = tf.keras.models.load_model(KERAS_MODEL_PATH)

print("[INFO] Converting to TFLite with quantization...")

# Create converter with dynamic range quantization (good for Android)
converter = tf.lite.TFLiteConverter.from_keras_model(model)

# Enable optimizations
converter.optimizations = [tf.lite.Optimize.DEFAULT]

# Convert
tflite_quant_model = converter.convert()

# Save optimized model
with open(TFLITE_OPTIMIZED_PATH, "wb") as f:
    f.write(tflite_quant_model)

# ============================
# COMPARE SIZES
# ============================
original_size = os.path.getsize(os.path.join(MODEL_DIR, "crutch_height_model.tflite")) / 1024
optimized_size = os.path.getsize(TFLITE_OPTIMIZED_PATH) / 1024

print("\n" + "="*60)
print("MODEL OPTIMIZATION RESULTS")
print("="*60)
print(f"Original TFLite model:  {original_size:.2f} KB")
print(f"Optimized TFLite model: {optimized_size:.2f} KB")
print(f"Size reduction: {((original_size - optimized_size) / original_size * 100):.1f}%")
print("\n[DONE] Optimized model saved to:", TFLITE_OPTIMIZED_PATH)

print("\n[INFO] Model is now ready for Android deployment!")
print("Use 'crutch_height_model_optimized.tflite' in your Android app")
