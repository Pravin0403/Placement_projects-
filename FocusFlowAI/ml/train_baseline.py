#!/usr/bin/env python3
"""Generate a synthetic focus-risk dataset, train baselines, and export artifacts.

Feature order matches FeatureVector.java:
  0 app_switch_count
  1 screen_unlock_count
  2 focus_session_duration_min
  3 session_elapsed_minutes
  4 interaction_frequency
  5 previous_break_duration_min
  6 time_of_day
  7 day_of_week
  8 notification_interruption_count
  9 risk_score_previous_window
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    f1_score,
    precision_score,
    recall_score,
    roc_auc_score,
)
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

# Add XGBoost support
try:
    import xgboost as xgb
    XGBOOST_AVAILABLE = True
except ImportError:
    XGBOOST_AVAILABLE = False

FEATURE_NAMES = [
    "app_switch_count",
    "screen_unlock_count",
    "focus_session_duration_min",
    "session_elapsed_minutes",
    "interaction_frequency",
    "previous_break_duration_min",
    "time_of_day",
    "day_of_week",
    "notification_interruption_count",
    "risk_score_previous_window",
]

# Matches FeatureScalerImpl baked-in statistics.
MEAN = np.array([4.2, 1.8, 28.0, 28.0, 0.35, 4.0, 14.5, 4.0, 1.1, 0.32], dtype=np.float64)
STD = np.array([3.4, 1.6, 18.0, 18.0, 0.28, 5.0, 5.2, 2.0, 1.4, 0.22], dtype=np.float64)
WEIGHTS = np.array([0.55, 0.48, 0.12, 0.18, 0.42, -0.22, 0.16, 0.05, 0.36, 0.40], dtype=np.float64)
BIAS = -0.15


def generate_dataset(n: int, rng: np.random.Generator) -> tuple[np.ndarray, np.ndarray]:
    x = rng.normal(MEAN, STD, size=(n, len(FEATURE_NAMES)))
    x[:, 0] = np.clip(x[:, 0], 0, 40)
    x[:, 1] = np.clip(x[:, 1], 0, 20)
    x[:, 2] = np.clip(x[:, 2], 1, 180)
    x[:, 3] = np.clip(x[:, 3], 1, 180)
    x[:, 4] = np.clip(x[:, 4], 0, 2)
    x[:, 5] = np.clip(x[:, 5], 0, 60)
    x[:, 6] = np.clip(x[:, 6], 0, 23.99)
    x[:, 7] = np.clip(np.round(x[:, 7]), 1, 7)
    x[:, 8] = np.clip(x[:, 8], 0, 15)
    x[:, 9] = np.clip(x[:, 9], 0, 1)

    scaled = (x - MEAN) / STD
    logit = BIAS + scaled @ WEIGHTS + rng.normal(0, 0.35, size=n)
    prob = 1.0 / (1.0 + np.exp(-logit))
    y = (prob > 0.5).astype(np.int32)
    return x.astype(np.float32), y


def load_real_data(csv_path: str) -> tuple[np.ndarray, np.ndarray]:
    """Load real user data from CSV file."""
    df = pd.read_csv(csv_path)
    
    # Extract features and labels (assuming CSV has specific structure)
    feature_cols = [col for col in df.columns if col in FEATURE_NAMES]
    x = df[feature_cols].values.astype(np.float32)
    
    # Assuming label column is named 'distraction_in_next_15_minutes'
    if 'distraction_in_next_15_minutes' in df.columns:
        y = df['distraction_in_next_15_minutes'].values.astype(np.int32)
    else:
        raise ValueError("CSV must contain 'distraction_in_next_15_minutes' column")
    
    return x, y


def metrics(y_true: np.ndarray, y_pred: np.ndarray, y_prob: np.ndarray) -> dict[str, float]:
    return {
        "accuracy": float(accuracy_score(y_true, y_pred)),
        "precision": float(precision_score(y_true, y_pred, zero_division=0)),
        "recall": float(recall_score(y_true, y_pred, zero_division=0)),
        "f1": float(f1_score(y_true, y_pred, zero_division=0)),
        "roc_auc": float(roc_auc_score(y_true, y_prob)),
    }


def maybe_export_tflite(output_dir: Path, scaler: StandardScaler, n_features: int) -> None:
    try:
        import tensorflow as tf
    except ImportError:
        print("TensorFlow not installed; skipping TFLite export.")
        return

    model = tf.keras.Sequential(
        [
            tf.keras.layers.Input(shape=(n_features,)),
            tf.keras.layers.Dense(8, activation="relu"),
            tf.keras.layers.Dense(1, activation="sigmoid"),
        ]
    )
    model.compile(optimizer="adam", loss="binary_crossentropy")
    rng = np.random.default_rng(7)
    dummy_x, dummy_y = generate_dataset(512, rng)
    dummy_x = scaler.transform(dummy_x)
    model.fit(dummy_x, dummy_y, epochs=3, verbose=0, batch_size=32)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    path = output_dir / "focus_risk.tflite"
    path.write_bytes(tflite_model)
    print(f"Wrote {path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Train FocusFlow baseline models")
    parser.add_argument("--samples", type=int, default=5000)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--tflite", action="store_true", help="Train a tiny Keras net and export TFLite")
    parser.add_argument("--xgboost", action="store_true", help="Train XGBoost model")
    parser.add_argument("--source", type=str, default="synthetic", choices=["synthetic", "csv"], help="Data source")
    parser.add_argument("--csv-path", type=str, help="Path to CSV file for real data")
    parser.add_argument("--out", type=Path, default=Path(__file__).resolve().parent / "artifacts")
    args = parser.parse_args()

    if args.source == "csv" and args.csv_path:
        # Load real data from CSV
        x, y = load_real_data(args.csv_path)
    else:
        # Generate synthetic data
        rng = np.random.default_rng(args.seed)
        x, y = generate_dataset(args.samples, rng)
    x_train, x_test, y_train, y_test = train_test_split(
        x, y, test_size=0.2, random_state=args.seed, stratify=y
    )

    scaler = StandardScaler()
    x_train_s = scaler.fit_transform(x_train)
    x_test_s = scaler.transform(x_test)

    logistic = LogisticRegression(max_iter=400)
    logistic.fit(x_train_s, y_train)
    rf = RandomForestClassifier(n_estimators=80, max_depth=8, random_state=args.seed)
    rf.fit(x_train_s, y_train)

    log_prob = logistic.predict_proba(x_test_s)[:, 1]
    rf_prob = rf.predict_proba(x_test_s)[:, 1]
    
    report = {
        "logistic": metrics(y_test, (log_prob >= 0.5).astype(int), log_prob),
        "random_forest": metrics(y_test, (rf_prob >= 0.5).astype(int), rf_prob),
        "feature_names": FEATURE_NAMES,
    }

    # Add XGBoost if requested and available
    if args.xgboost and XGBOOST_AVAILABLE:
        xgb_model = xgb.XGBClassifier(
            n_estimators=100,
            max_depth=6,
            learning_rate=0.1,
            objective='binary:logistic',
            random_state=args.seed
        )
        xgb_model.fit(x_train_s, y_train)
        xgb_prob = xgb_model.predict_proba(x_test_s)[:, 1]
        report["xgboost"] = metrics(y_test, (xgb_prob >= 0.5).astype(int), xgb_prob)

    args.out.mkdir(parents=True, exist_ok=True)
    (args.out / "metrics.json").write_text(json.dumps(report, indent=2), encoding="utf-8")
    (args.out / "scaler.json").write_text(
        json.dumps(
            {
                "feature_names": FEATURE_NAMES,
                "mean": scaler.mean_.tolist(),
                "std": scaler.scale_.tolist(),
            },
            indent=2,
        ),
        encoding="utf-8",
    )
    (args.out / "logistic_weights.json").write_text(
        json.dumps(
            {
                "bias": float(logistic.intercept_[0]),
                "weights": logistic.coef_[0].tolist(),
                "feature_names": FEATURE_NAMES,
            },
            indent=2,
        ),
        encoding="utf-8",
    )

    print(json.dumps(report, indent=2))
    print(f"Wrote artifacts to {args.out}")
    if args.tflite:
        maybe_export_tflite(args.out, scaler, len(FEATURE_NAMES))


if __name__ == "__main__":
    main()
