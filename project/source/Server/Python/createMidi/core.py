from chord import *
from CONSTANT import PREFIX_PATH
from track import *


def chord_note(string):
    """把和弦字符串转换为MIDI音符编号

    :param string: 和弦字符串
    :return: 音符编号
    """
    if string == '#C':
        return 61
    elif string == '#D':
        return 63
    elif string == '#F':
        return 66
    elif string == '#G':
        return 68
    elif string == '#A':
        return 70
    elif string == 'C':
        return 60
    elif string == 'D':
        return 62 - 12
    elif string == 'E':
        return 64 - 12
    elif string == 'F':
        return 65 - 12
    elif string == 'G':
        return 67 - 12
    elif string == 'A':
        return 69 - 12
    elif string == 'B':
        return 71 - 12


def process_str(_s):
    """处理音调字符串，去除#

    :param _s:输入的音调
    :return:处理后的字符串
    """
    copy0 = _s[:1]
    if copy0 == '#':
        return _s[:2]
    else:
        return _s[:1]


def chord_type(str1):
    """得到和弦的类型

    :param str1:字符串形式的和弦
    :return:具体类型
    """
    copy0 = str1[:1]
    if copy0 == '#':
        str0 = str1[1:]
    else:
        str0 = str1
    if str0[1:] == 'm':
        return "smallChord"
    elif str0[1:] == 'im':
        return "decreaseChord"
    elif str0[1:] == '':
        return "bigChord"
    elif str0[1:] == 'aug':
        return "addChordNote"
    else:
        return "susChordNote"


def createOneChord(colours: list, speed: int, filename: process_str, broken_chord: int, guitar: int):
    """

    :param colours: 和弦序列
    :param speed: 速度
    :param filename: 文件名
    :param broken_chord: 是和弦还是柱式,如果传入1则为和弦若为0则为柱式
    :param guitar: 钢琴还是吉他,如果传入1则为钢琴若为0则为吉他
    :return:
    """
    myTrack = Track(speed)
    number = 1

    for colour in colours:
        str1 = process_str(colour)
        chord_type1 = chord_type(colour)
        str1_chord_note0 = chord_note(str1)
        if number == 1:
            first_chord = Chord(str1_chord_note0, 0, chord_type1)
            myTrack.inputChord(first_chord.note, first_chord.velocity, broken_chord, guitar)
        elif number == 2:
            second_chord = Chord(str1_chord_note0, 1, chord_type1)
            myTrack.inputChord(second_chord.note, second_chord.velocity, broken_chord, guitar)
        elif number == 3:
            third_chord = Chord(str1_chord_note0, 2, chord_type1)
            myTrack.inputChord(third_chord.note, third_chord.velocity, broken_chord, guitar)
        elif number == 4:
            fourth_chord = Chord(str1_chord_note0, 3, chord_type1)
            myTrack.inputChord(fourth_chord.note, fourth_chord.velocity, broken_chord, guitar)
            number = 1

        number += 1

    myTrack.save_mid(PREFIX_PATH + filename)
