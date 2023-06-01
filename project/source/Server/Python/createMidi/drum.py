import mido

from CONSTANT import PREFIX_PATH


def drum(number, speed) -> None:
    """用于生成鼓的midi信息,保存至文件

    :param number: 鼓的数量
    :param speed: 速度
    :return:
    """
    mid = mido.MidiFile()
    track = mido.MidiTrack()
    mid.tracks.append(track)
    track.append(mido.Message('program_change', program=118, time=0))  # 这个音轨使用的乐器
    track.append(mido.MetaMessage('set_tempo', tempo=speed, time=0))
    num = 1
    i = 1
    first_chord = 1

    while i <= number:
        if num == 1:
            if first_chord == 1:
                track.append(mido.Message('note_on', note=60 - 24, velocity=96, time=0))
                track.append(mido.Message('note_off', note=60 - 24, velocity=96, time=120))
                num += 1
                i += 1
                first_chord = 0
            else:
                track.append(mido.Message('note_on', note=60 - 24, velocity=96, time=360))
                track.append(mido.Message('note_off', note=60 - 24, velocity=96, time=120))
                num += 1
                i += 1
        elif num == 2:
            track.append(mido.Message('note_on', note=60 - 24 + 2, velocity=96, time=360))
            track.append(mido.Message('note_off', note=60 - 24 + 2, velocity=96, time=120))
            num += 1
            i += 1
        elif num == 3:
            track.append(mido.Message('note_on', note=60 - 24, velocity=96, time=240))
            track.append(mido.Message('note_off', note=60 - 24, velocity=96, time=120))
            num += 1
            i += 1
        elif num == 4:
            track.append(mido.Message('note_on', note=60 - 24, velocity=96, time=0))
            track.append(mido.Message('note_off', note=60 - 24, velocity=96, time=120))
            track.append(mido.Message('note_on', note=60 - 24 + 2, velocity=96, time=360))
            track.append(mido.Message('note_off', note=60 - 24 + 2, velocity=96, time=120))
            num = 1
            i += 1

    mid.save(PREFIX_PATH + '鼓.mid')

# 118 合成鼓
