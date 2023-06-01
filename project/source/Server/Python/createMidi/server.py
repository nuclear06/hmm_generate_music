import io
import os

import xlwt
from flask import Flask, request, send_file, make_response

import bass
import dataProcess
import drum
import hmm
import core
from CONSTANT import PREFIX_PATH
from merge import merge, convertMidiToMp3, remove
from mido import bpm2tempo

app = Flask(__name__)


@app.route('/')
@app.route('/main', methods=['post', 'get'])
def main():
    workbook = xlwt.Workbook(encoding='utf-8')
    booksheet = workbook.add_sheet('Sheet 1', cell_overwrite_ok=True)

    s = request.args.get('str')  # 音高序列
    a = request.args.get('a')  # 音调
    choose = request.args.get('c')  # 选择的乐器
    speed = bpm2tempo(int(request.args.get('speed')))  # 速度传入为bpm,转化为tempo(us)

    # speed = request.args.get('speed')  # 速度传入为bpm,转化为tempo(us)

    print("speed(tempo)为:", speed)
    happy = request.args.get('happy')  # 风格参数(sad-happy)

    ma = dataProcess.input_data(s, a)  # 返回处理好的数据

    [h, l] = ma.shape
    for i in range(h):
        for j in range(l):
            booksheet.write(i, j, ma[i, j])
    workbook.save('melody.xls')  # 将矩阵数据转存到xls文件

    strma = ["C", "#C", "D", "#D", "E", "F", "#F", "G", "#G", "A", "#A", "B"]
    colours = []

    # hmm.py文件:实现了HMM模型和维特比算法
    ma = hmm.mmm(float(happy))  # 返回解码结果

    # 加入五种三和弦
    # strma = ["C", "#C", "D", "#D", "E", "F", "#F", "G", "#G", "A", "#A", "B"]
    for i in ma:
        if i <= 11:
            colours.append(strma[i])
        elif i <= 23:
            colours.append(strma[i - 12] + "m")  # major大三和弦
        elif i <= 35:
            colours.append(strma[i - 24] + "aug")  # augmented增强三和弦
        elif i <= 47:
            colours.append(strma[i - 36] + "im")  # diminished减弱三和弦
        else:
            colours.append(strma[i - 48] + "sus")  # suspended挂留和弦

    # colours = ["#C", "Am", "Dm", "Em", "Fsus"]

    # c,选用的乐器
    if int(choose[0]) == 1:
        core.createOneChord(colours, int(speed), '钢琴分解.mid', 1, 0)
    if int(choose[1]) == 1:
        core.createOneChord(colours, int(speed), '钢琴柱式.mid', 0, 0)
    if int(choose[2]) == 1:
        core.createOneChord(colours, int(speed), '吉他分解.mid', 1, 1)
    if int(choose[3]) == 1:
        core.createOneChord(colours, int(speed), '吉他柱式.mid', 0, 1)
    if int(choose[4]) == 1:
        drum.drum(len(colours), int(speed))
    if int(choose[5]) == 1:
        bass.create_bass(colours, int(speed))

    if merge(delete=True):
        # convertMidiToMp3(delete=True)
        convertMidiToMp3(delete=True)

    data = get_resp_bytes(delete=True)
    if data is None:
        resp = make_response("")
        resp.headers['Content-Length'] = 0
        return resp
    return send_file(
        io.BytesIO(data),
        mimetype='audio/mpeg',
        as_attachment=True,
        download_name="generate-music.mp3"
    )


def get_resp_bytes(delete=True):
    """
    读取生成的音频文件并返回二进制数据.

    :param delete:读取后是否删除(默认删除)
    :return: 音频的二进制数据
    """
    file_name = PREFIX_PATH + "merged.mp3"
    if not os.path.exists(file_name):
        return None
    f = open(file_name, "rb")
    data = f.read()
    f.close()
    if delete:
        remove(file_name)
    return data


# app.run(host='0.0.0.0', port=5000, debug=True)


app.run(host='127.0.0.1', port=5000, debug=True)
# http://127.0.0.1:5000/main?str=4,4,4,4,4,4,4,4,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7/7,7,7,7,7,7,7,7,4,4,4,4,4,4,4,4,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,11,11,11,11,11,11,11,11,9,9,9,9,9,9,9,9,9,9,9,9,9,7,9,9,9,9,9,9,9/9,9,9,9,9,9,9,9,9,7,7,7,7,7,7,7,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,9,9,9,9,9,9/9,0,0,0,0,0,0,0,0,9,9,9,9,9,9,9,9,9,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7/4,4,4,4,4,4,4,4,4,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7/7,7,7,7,7,7,7,4,4,4,4,4,4,4,4,4,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,11,11,11,11,11,11,11,11,11,9,9,9,9,9,9,9,9,9,9,9,9,9,7,9,9,9,9,9,9,9,9/9,9,9,9,9,9,9,9,9,9,9,7,7,7,7,7,7,7,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,9,9,9,9,9,9,9/9,9,9,9,9,9,9,9,9,0,0,0,0,0,0,0,0,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,7,7,10,2/7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,2,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7/7,4,4,4,4,4,4,4,4,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,0,0,0,0,0,0,0/0,9,9,9,9,9,9,9,9,9,0,0,0,0,0,0,0,0,0,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0/0,0,4,4,4,4,4,4,4,4,4,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,6,2,2,2,2,2/4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,2,2,2,2,2,2,2/2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,2,2,2,2,2,2,2,0,0,0,0,0,0,0/0,0,9,9,9,9,9,9,9,9,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,9,9,9,9,9,9,9,9,9,7,7,7,7,7,7,7/7,2,2,2,2,2,2,2,2,4,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0&a=C&speed=125&c=011000&happy=1
