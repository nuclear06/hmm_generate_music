import random


class Chord:
    """
    用于生成音乐和弦，提供了不同类型和音量的和弦生成方法
    """

    def __init__(self, note0, velocity_type, chord_type):

        self.velocityType = velocity_type
        self.note0 = note0
        self.note = [0, 0, 0]
        self.note[0] = note0
        self.note[1] = 0
        self.note[2] = 0
        self.velocity = [0, 0, 0]

        if chord_type == "bigChord":
            self.bigChordNote(note0)
        elif chord_type == "smallChord":
            self.smallChordNote(note0)
        elif chord_type == "addChordNote":
            self.addChordNote(note0)
        elif chord_type == "susChordNote":
            self.susChordNote(note0)
        else:
            self.decreaseChordNote(note0)

        if velocity_type == 0:
            self.firstChordVelocity()
        elif velocity_type == 1:
            self.secondChordVelocity()
        elif velocity_type == 2:
            self.thirdChordVelocity()
        elif velocity_type == 3:
            self.fourthChordVelocity()
        return

    def bigChordNote(self, note0):
        """添加大调和弦

        :param note0:
        :return:
        """
        self.note[1] = note0 + 4
        self.note[2] = self.note[1] + 3
        return

    def smallChordNote(self, note0):
        """添加大调和弦

        :param note0:
        :return:
        """
        self.note[1] = note0 + 3
        self.note[2] = self.note[1] + 4
        return

    def addChordNote(self, note0):
        """添加增强和弦

        :param note0:
        :return:
        """
        self.note[1] = note0 + 4
        self.note[2] = self.note[1] + 4
        return

    def decreaseChordNote(self, note0):
        """添加减弱和弦

        :param note0:
        :return:
        """
        self.note[1] = note0 + 3
        self.note[2] = self.note[1] + 3
        return

    def susChordNote(self, note0):
        """添加增强和弦

        :param note0:
        :return:
        """
        self.note[1] = note0 + 2
        self.note[2] = self.note[1] + 5

    def firstChordVelocity(self):
        """设置三个音符的音量

        :return:
        """
        self.velocity[0] = 90 + random.randint(0, 8)
        self.velocity[1] = 80 + random.randint(0, 8)
        self.velocity[2] = 70 + random.randint(0, 8)
        return

    def secondChordVelocity(self):
        """设置三个音符的音量为60/55/45


        :return:
        """
        self.velocity[0] = 60 + random.randint(0, 8)
        self.velocity[1] = 55 + random.randint(0, 8)
        self.velocity[2] = 45 + random.randint(0, 8)
        return

    def thirdChordVelocity(self):
        """设置三个音符的音量为85/78/65

        :return:
        """
        self.velocity[0] = 85 + random.randint(0, 8)
        self.velocity[1] = 78 + random.randint(0, 8)
        self.velocity[2] = 65 + random.randint(0, 8)
        return

    def fourthChordVelocity(self):
        """设置三个音符的音量为56/46/43

        :return:
        """
        self.velocity[0] = 56 + random.randint(0, 8)
        self.velocity[1] = 46 + random.randint(0, 8)
        self.velocity[2] = 43 + random.randint(0, 8)
        return
