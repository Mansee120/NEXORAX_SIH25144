
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
import tensorflow as tf
from tensorflow.keras import layers, callbacks
import pickle
import os

# ============================
# CONFIG
# ============================
DATA_FILE = "height_segments_with_weight.xlsx"   # Put this file in same folder as this script
MODEL_DIR = "models"
os.makedirs(MODEL_DIR, exist_ok=True)

# ============================
# 1. LOAD DATASET
# ============================
print("[INFO] Loading dataset from:", DATA_FILE)
df = pd.read_excel(DATA_FILE, engine='openpyxl')

expected_cols = [
    "UserHeight_cm",
    "ArmpitToWrist_cm",
    "WristToFoot_cm",
    "UserWeight_kg",
    "RecommendedCrutchHeight_cm"
]

for col in expected_cols:
    if col not in df.columns:
        raise ValueError(f"Missing required column: {col}")

# Drop rows with any missing values
df = df.dropna().reset_index(drop=True)
print("[INFO] Dataset shape after dropping NaNs:", df.shape)

# ============================
# 2. SPLIT FEATURES & TARGET
# ============================
X = df[[
    "UserHeight_cm",
    "ArmpitToWrist_cm",
    "WristToFoot_cm",
    "UserWeight_kg"
]].values.astype("float32")

y = df["RecommendedCrutchHeight_cm"].values.astype("float32")

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

print("[INFO] Train size:", X_train.shape, "Test size:", X_test.shape)

# ============================
# 3. SCALE FEATURES
# ============================
scaler = MinMaxScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled = scaler.transform(X_test)

# Save scaler for later (ESP32/Android preprocessing)
scaler_path = os.path.join(MODEL_DIR, "scaler.pkl")
with open(scaler_path, "wb") as f:
    pickle.dump(scaler, f)
print(f"[INFO] Saved scaler to {scaler_path}")

# ============================
# 4. BUILD MODEL (REGRESSION)
# ============================
model = tf.keras.Sequential([
    layers.Input(shape=(4,)),
    layers.Dense(32, activation="relu"),
    layers.Dense(16, activation="relu"),
    layers.Dense(1)  # output: RecommendedCrutchHeight_cm
])

model.compile(
    optimizer="adam",
    loss="mse",
    metrics=["mae"]
)

model.summary()

# ============================
# 5. TRAIN MODEL
# ============================
es = callbacks.EarlyStopping(
    monitor="val_loss",
    patience=10,
    restore_best_weights=True
)

history = model.fit(
    X_train_scaled, y_train,
    epochs=100,
    batch_size=16,
    validation_split=0.2,
    callbacks=[es],
    verbose=1
)

# ============================
# 6. EVALUATE MODEL
# ============================
test_loss, test_mae = model.evaluate(X_test_scaled, y_test, verbose=0)
print(f"[RESULT] Test MAE (cm): {test_mae:.3f}")

# Show a few example predictions
preds = model.predict(X_test_scaled[:10]).flatten()
print("\n[EXAMPLES] True vs Predicted crutch height (first 10 samples):")
for true_val, pred_val in zip(y_test[:10], preds):
    print(f"  True: {true_val:.1f} cm   Pred: {pred_val:.1f} cm   Diff: {pred_val-true_val:.1f} cm")

# ============================
# 7. SAVE KERAS & TFLITE MODEL
# ============================
keras_path = os.path.join(MODEL_DIR, "crutch_height_model.keras")
model.save(keras_path)
print(f"[INFO] Saved Keras model to {keras_path}")

# Convert to TFLite
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

tflite_path = os.path.join(MODEL_DIR, "crutch_height_model.tflite")
with open(tflite_path, "wb") as f:
    f.write(tflite_model)
print(f"[INFO] Saved TFLite model to {tflite_path}")

print("\n[DONE] Training and export complete.")
