import os

from flask import Flask, jsonify, request, Blueprint
from werkzeug.utils import secure_filename
from predict import predict

methods = ['POST', 'GET']
ALLOWED_EXTENSIONS = {'jpg', 'JPG'}
app = Flask(__name__)
app.config['SERVER_NAME'] = 'lzf.chengguo.plus:5000'


def allowed_file(filename):
    ALLOWED_EXTENSIONS = {'jpg', 'JPG'}
    return '.' in filename and filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS


@app.route('/', methods=methods)
def index():
    return '<h1>hello world!<h1>'


@app.route('/classification', methods=methods)
def handle_request():
    if request.method == 'POST':
        imagefile = request.files['file']
        print('连接成功')
        if not (imagefile and allowed_file(imagefile.filename)):
            return jsonify({"error": 1001, "msg": "图片类型：jpg、JPG"})
        basepath = os.path.dirname(__file__)
        filename = os.path.join(basepath, 'static/images', secure_filename(imagefile.filename))
        print("\nReceived image File name : " + imagefile.filename)
        imagefile.save(filename)
        result = predict(filename)
        return result


if __name__ == '__main__':
    app.run(port=5000, ssl_context=("lzf.pem", "lzf.key"))
