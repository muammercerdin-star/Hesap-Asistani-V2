from flask import Flask, send_file

app = Flask(__name__)

@app.route("/")
def home():
    return send_file("hesap_v3.html")

@app.route("/hesap_v3.html")
def hesap_v3():
    return send_file("hesap_v3.html")

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5050, debug=True)
