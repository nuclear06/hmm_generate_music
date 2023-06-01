import numpy

key = {'C': 0, 'bD': 1, 'D': 2, 'bE': 3, 'E': 4, 'F': 5, 'bG': 6, 'G': 7,
       'bA': 8, 'A': 9, 'bB': 10, 'B': 11}


def str_to_arr(string):
    """读取音高序列数据,转化为list

    :param string: 音高序列
    :return: list
    """
    arr = string.split("/")
    for i in range(len(arr)):
        arr[i] = arr[i].split(",")
        arr[i] = list(map(int, arr[i]))

    return arr


def to_C(origin_key, arr):
    """ c调归一化

    :param origin_key: 原始调式
    :param arr: 音高序列
    :return: 归一化后的序列
    """
    temp = key[origin_key]
    for i in range(len(arr)):
        for j in range(len(arr[i])):
            arr[i][j] -= temp
    return arr


def melody_metrix(arr):
    """将数组转为一个矩阵

    :param arr:音高序列
    :return:矩阵格式的数据
    """
    metrix = numpy.zeros((len(arr), 12))
    for i in range(len(arr)):
        count = 0
        for j in range(len(arr[i])):
            metrix[i][arr[i][j]] = metrix[i][arr[i][j]] + 1
            count = count + 1
        for j in range(12):
            metrix[i][j] = metrix[i][j] / count
    return metrix


def input_data(s, a):
    """处理输入的数据

    :param s: 音高序列
    :param a: 音调
    :return: 处理好的音高矩阵
    """
    arr = str_to_arr(s)  # 对音高序列进行分割,转换为列表
    to_C(a, arr)  # c调归一化,通过对乐谱中的音高统一增加或减小一个整体,不改变数据普适性.方便数据处理
    metrix = melody_metrix(arr)  # 频次归一化,转化为概率值
    return metrix
