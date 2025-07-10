from flask import Flask, request, jsonify
from dotenv import load_dotenv
import os

from health_care_ai import get_health_care_assistance

load_dotenv()
app = Flask(__name__)

@app.route("/health", methods=["POST"])
def career():
    data = request.get_json()
    if not data or "user_input" not in data:
        return jsonify({"error": "Provide 'user_input' in JSON body"}), 400

    advice = get_health_care_assistance(data["user_input"])
    return jsonify({"advice": advice}), 200

if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    app.run(host="0.0.0.0", port=port)
