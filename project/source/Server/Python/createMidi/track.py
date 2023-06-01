import mido


class Track:
    """
    用于创建一个MIDI音轨，并将音符和和弦等信息添加到这个音轨中
    """

    def __init__(self, speed):
        self.speed = 50000
        self.track = self.createTrack()
        self.mid = mido.MidiFile()
        self.track.append(mido.MetaMessage('set_tempo', tempo=speed, time=0))

    @staticmethod
    def createTrack():
        """创建一个空的MIDI音轨

        :return: mido.MidiTrack()
        """
        track = mido.MidiTrack()
        return track

    def inputChord(self, note, velocity, broken_chord, guitar):
        """向音轨中添加和弦

        :param note:三个音符
        :param velocity:三个音符的音量
        :param broken_chord:是否要分别播放三个音符
        :param guitar:是否为吉他
        :return:
        """
        if guitar == 1:
            self.track.append(mido.Message('program_change', program=28, time=0))
        else:
            self.track.append(mido.Message('program_change', program=0, time=0))

        if broken_chord == 0:
            self.track.append(mido.Message('note_on', note=note[0], velocity=velocity[0], time=0))
            self.track.append(mido.Message('note_on', note=note[1], velocity=velocity[1], time=0))
            self.track.append(mido.Message('note_on', note=note[2], velocity=velocity[2], time=0))
            self.track.append(mido.Message('note_off', note=note[0], velocity=velocity[0], time=480))
            self.track.append(mido.Message('note_off', note=note[1], velocity=velocity[1], time=0))
            self.track.append(mido.Message('note_off', note=note[2], velocity=velocity[2], time=0))
        elif broken_chord == 1:
            self.track.append(mido.Message('note_on', note=note[0], velocity=velocity[0], time=0))

            self.track.append(mido.Message('note_on', note=note[1], velocity=velocity[1], time=120))

            self.track.append(mido.Message('note_on', note=note[2], velocity=velocity[2], time=120))

            self.track.append(mido.Message('note_off', note=note[0], velocity=velocity[0], time=240))
            self.track.append(mido.Message('note_off', note=note[1], velocity=velocity[1], time=0))
            self.track.append(mido.Message('note_off', note=note[2], velocity=velocity[2], time=0))

    def save_mid(self, name):
        """保存midi数据至mid文件

        :param name:文件名
        :return:
        """
        self.mid.tracks.append(self.track)
        self.mid.save(name)
        return
