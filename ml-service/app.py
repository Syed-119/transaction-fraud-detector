from flask import Flask, request, jsonify
import joblib
import numpy as np

app = Flask(__name__)

model = joblib.load("model.pkl")
scaler = joblib.load("scaler.pkl")

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()

    amount = data.get("amount", 0)
    time_val = data.get("time", 0)

    scaled = scaler.transform([[time_val, amount]])
    time_scaled, amount_scaled = scaled[0]

    features = [time_scaled] + data.get("v_features", [0] * 28) + [amount_scaled]
    features = np.array(features).reshape(1, -1)

    prediction = model.predict(features)[0]
    probability = model.predict_proba(features)[0]
    confidence = float(max(probability))

    return jsonify({
        "fraud": bool(prediction),
        "confidence": round(confidence, 4)
    })

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "healthy"})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)