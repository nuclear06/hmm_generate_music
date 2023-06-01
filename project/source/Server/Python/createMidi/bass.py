from core import *


def create_bass(colours, speed):
    """根据音符序列和速度创建贝斯mid文件

    :param colours: 和弦的音符
    :param speed: 速度
    :return:
    """
    mid = mido.MidiFile()
    track = mido.MidiTrack()
    mid.tracks.append(track)
    track.append(mido.Message('program_change', program=33, time=0))  # 这个音轨使用的乐器
    track.append(mido.MetaMessage('set_tempo', tempo=speed, time=0))
    num = 1
    first_chord = 1

    for colour in colours:

        str1 = process_str(colour)
        note0 = chord_note(str1)

        if num == 1:
            if first_chord == 1:
                track.append(mido.Message('note_on', note=note0 - 24, velocity=96, time=0))
                track.append(mido.Message('note_off', note=note0 - 24, velocity=96, time=120))
                num += 1
                first_chord = 0
            else:
                track.append(mido.Message('note_on', note=note0 - 24, velocity=96, time=600))
                track.append(mido.Message('note_off', note=note0 - 24, velocity=96, time=120))
                num += 1
        elif num == 2:
            num += 1
        elif num == 3:
            track.append(mido.Message('note_on', note=note0 - 24, velocity=96, time=720))
            track.append(mido.Message('note_off', note=note0 - 24, velocity=96, time=120))
            num += 1

        elif num == 4:
            track.append(mido.Message('note_on', note=note0 - 24, velocity=96, time=0))
            track.append(mido.Message('note_off', note=note0 - 24, velocity=96, time=360))
            num = 1
    mid.save(PREFIX_PATH + '贝斯.mid')
