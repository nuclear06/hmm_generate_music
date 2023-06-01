import mido
import os
import subprocess

from CONSTANT import PREFIX_PATH


def merge(delete=True) -> bool:
    """
    合并mid文件至同一个mid文件,默认删除原文件.
    如果合并的所有mid文件中均未检测出音轨则返回False,否则返回True.

    :param delete: 是否删除原文件
    :return: 合并是否成功
    """
    _all = 0

    files = os.listdir(PREFIX_PATH)
    merged = mido.MidiFile()
    for file in files:
        # print(file)
        if not file.endswith('.mid'):
            continue

        if file in ['吉他分解.mid', '吉他柱式.mid', '钢琴分解.mid', '钢琴柱式.mid', '鼓.mid', '贝斯.mid']:
            mid = mido.MidiFile(PREFIX_PATH + file)
            for track in mid.tracks:
                merged.tracks.append(track)
                _all += 1
            if delete:
                remove(PREFIX_PATH + file)
    merged.save(PREFIX_PATH + 'merged.mid')
    print('生成merged.mid')

    if _all != 0:
        return True
    else:
        return False


def remove(file_path) -> None:
    """删除指定路径的文件,实现了删除时异常的捕捉和信息输出.

    :param file_path: 文件路径
    :return:
    """
    try:
        os.remove(file_path)
        print(f"{file_path} 已被删除")
    except FileNotFoundError:
        print(f"{file_path} 不存在")
    except PermissionError:
        print(f"没有权限删除 {file_path}")
    except OSError:
        print(f"删除 {file_path} 失败")


def convertMidiToMp3(delete=True) -> bool:
    """
    调用timidity++和ffmpeg将mid文件转换为mp3.
    使用本方法要求配置ffmpeg和timidity++环境.

    :param delete:是否删除原文件(默认删除)
    :return:转换是否成功
    """
    try:
        cmd = f'timidity "{PREFIX_PATH}merged.mid" -Ow -o "{PREFIX_PATH}merged.wav"'
        # print(cmd)
        subprocess.run(cmd, timeout=10, stdout=subprocess.DEVNULL)
        print('生成merged.wav')
    except subprocess.TimeoutExpired:
        return False
    else:
        if delete:
            remove(PREFIX_PATH + 'merged.mid')
        try:
            cmd2 = f'ffmpeg -i "{PREFIX_PATH}merged.wav" -filter_complex areverse,silenceremove=1:0:-50dB,areverse \
            "{PREFIX_PATH}merged.mp3" -y -hide_banner -loglevel error'
            # areverse,silenceremove=1:0:-50dB,areverse 删除结尾的静音部分
            subprocess.run(cmd2, timeout=10)
            if delete:
                remove(PREFIX_PATH + 'merged.wav')
        except subprocess.TimeoutExpired:
            return False
        else:
            return True


if __name__ == '__main__':
    # if merge(False):
    #     convertMidiToMp3(False)
    convertMidiToMp3(False)
