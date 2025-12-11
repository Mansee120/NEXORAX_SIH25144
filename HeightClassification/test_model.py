
import numpy as np
import pickle
import os
import tensorflow as tf

# ============================
# CONFIG
# ============================
MODEL_DIR = "models"
KERAS_MODEL_PATH = os.path.join(MODEL_DIR, "crutch_height_model.keras")
TFLITE_MODEL_PATH = os.path.join(MODEL_DIR, "crutch_height_model.tflite")
SCALER_PATH = os.path.join(MODEL_DIR, "scaler.pkl")

# ============================
# LOAD MODEL & SCALER
# ============================
print("[INFO] Loading Keras model...")
model = tf.keras.models.load_model(KERAS_MODEL_PATH)

print("[INFO] Loading scaler...")
with open(SCALER_PATH, "rb") as f:
    scaler = pickle.load(f)

# ============================
# HELPER FUNCTION
# ============================
def predict_crutch_height(user_height_cm, armpit_to_wrist_cm, wrist_to_foot_cm, user_weight_kg):
    """
    Predict recommended crutch height based on user measurements.
    
    Args:
        user_height_cm: User's total height in cm
        armpit_to_wrist_cm: Distance from armpit to wrist in cm
        wrist_to_foot_cm: Distance from wrist to foot in cm
        user_weight_kg: User's weight in kg
    
    Returns:
        Predicted crutch height in cm
    """
    # Create input array
    input_data = np.array([[
        user_height_cm,
        armpit_to_wrist_cm,
        wrist_to_foot_cm,
        user_weight_kg
    ]], dtype="float32")
    
    # Scale the input
    input_scaled = scaler.transform(input_data)
    
    # Make prediction
    prediction = model.predict(input_scaled, verbose=0)
    
    return prediction[0][0]

# ============================
# TEST CASES
# ============================
print("\n" + "="*60)
print("TESTING CRUTCH HEIGHT PREDICTION MODEL")
print("="*60)

test_cases = [
    {
        "name": "Short Person",
        "height": 150,
        "armpit_to_wrist": 35,
        "wrist_to_foot": 45,
        "weight": 55
    },
    {
        "name": "Average Person",
        "height": 170,
        "armpit_to_wrist": 40,
        "wrist_to_foot": 50,
        "weight": 70
    },
    {
        "name": "Tall Person",
        "height": 190,
        "armpit_to_wrist": 45,
        "wrist_to_foot": 55,
        "weight": 85
    },
    {
        "name": "Heavy Person",
        "height": 175,
        "armpit_to_wrist": 42,
        "wrist_to_foot": 52,
        "weight": 95
    },
    {
        "name": "Light Person",
        "height": 160,
        "armpit_to_wrist": 38,
        "wrist_to_foot": 48,
        "weight": 50
    }
]

print("\n[TEST PREDICTIONS]:\n")
for test in test_cases:
    predicted_height = predict_crutch_height(
        test["height"],
        test["armpit_to_wrist"],
        test["wrist_to_foot"],
        test["weight"]
    )
    
    print(f"  {test['name']}:")
    print(f"    Input Measurements:")
    print(f"      - Height: {test['height']} cm")
    print(f"      - Armpit to Wrist: {test['armpit_to_wrist']} cm")
    print(f"      - Wrist to Foot: {test['wrist_to_foot']} cm")
    print(f"      - Weight: {test['weight']} kg")
    print(f"    Predicted Crutch Height: {predicted_height:.1f} cm")
    print()

# ============================
# INTERACTIVE MODE
# ============================
print("\n" + "="*60)
print("INTERACTIVE PREDICTION MODE")
print("="*60)
print("\nEnter your measurements to get a crutch height prediction.")
print("(or press Ctrl+C to exit)\n")

try:
    while True:
        try:
            height = float(input("Enter your height (cm): "))
            armpit_wrist = float(input("Enter armpit to wrist distance (cm): "))
            wrist_foot = float(input("Enter wrist to foot distance (cm): "))
            weight = float(input("Enter your weight (kg): "))
            
            predicted = predict_crutch_height(height, armpit_wrist, wrist_foot, weight)
            
            print(f"\n✓ Recommended crutch height: {predicted:.1f} cm\n")
            
        except ValueError:
            print("Invalid input. Please enter numeric values.\n")
            
except KeyboardInterrupt:
    print("\n\n[INFO] Testing complete. Goodbye!")

print("\n[DONE] Model testing finished.")
