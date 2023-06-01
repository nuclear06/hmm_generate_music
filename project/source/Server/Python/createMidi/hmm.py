# coding=utf8

import xlrd
import xlwt
import numpy as np
import math


# 从表格中读取数据
def read_data_mul(file):
    """
    以ndarry形式返回文件数据

    :param file: 读取的文件
    :return:
    """
    data = xlrd.open_workbook(file)
    table = data.sheets()[0]
    n_rows = table.nrows
    _list = []
    for i in range(n_rows):
        _list.append(table.row_values(i))
    return np.array(_list)


def read_data_single(file):
    """
    从xls文件读取数据，仅返回第一行数据。

    :param file: 文件路径
    :return:
    """
    data = xlrd.open_workbook(file)
    table = data.sheets()[0]
    return table.row_values(0)


class HMM(object):
    def __init__(self, a, b, pi):
        """
        初始化HMM模型

        :param a: 状态转移概率矩阵
        :param b: 输出观察概率矩阵
        :param pi:  初始化状态向量
        """
        self.A = np.array(a)
        # print (A)
        self.B = np.array(b)
        self.pi = np.array(pi)
        self.N = self.A.shape[0]  # 总共状态个数
        # print(self.N)
        self.M = self.B.shape[1]  # 总共观察值个数
        # print(self.M)

    # 将数据写回表格
    @staticmethod
    def data_write_single(file_path, data, n):
        """
        将一维数组写入xls文件

        :param file_path: 文件名
        :param data: 数据
        :param n: 长度
        """
        f = xlwt.Workbook()
        sheet1 = f.add_sheet(u'sheet1', cell_overwrite_ok=True)  # 创建sheet
        for i in range(n):
            sheet1.write(0, i, data[i])
        f.save(file_path)  # 保存文件

    def viterbi(self, observe, state):
        """
        维特比算法，用于解码给定的观察序列并计算在HMM模型中最有可能的状态序列，即最优路径。

        :param observe: 观察序列，每个元素表示在对应时间步长中观察到的状态
        :param state: 隐藏状态序列，每个元素表示在对应时间步长中的隐藏状态
        :return: 在HMM模型中最有可能的状态序列，即最优路径.
        """
        max_p = [[0 for _ in range(self.N)] for _ in range(len(observe))]
        path = [[0 for _ in range(self.N)] for _ in range(len(observe))]
        for i in range(self.N):
            max_p[0][i] = self.pi[i] * self.B[state[i]][observe[0]]
            path[0][i] = i
        for i in range(1, len(observe)):
            max_item = [0 for i in range(self.N)]
            for j in range(self.N):
                item = [0 for i in state]
                for k in range(self.N):
                    p = max_p[i - 1][k] * self.B[state[j]][observe[i]] * self.A[state[k]][state[j]]
                    item[state[k]] = p
                max_item[state[j]] = max(item)
                path[i][state[j]] = item.index(max(item))
            max_p[i] = max_item
        path = []
        p = max_p[len(observe) - 1].index(max(max_p[len(observe) - 1]))
        path.append(p)
        for i in range(len(observe) - 1, 0, -1):
            path.append(path[i][p])
            p = path[i][p]
        path.reverse()
        return path


# 当一首歌除音调外保持一致时,使用大调偏欢乐,使用小调偏悲伤
def mmm(emo):
    """
    使用viterbi算法计算最优序列并返回,序列也会写入out.xls

    :param emo: 情绪值，要处于[0,1]
    :return: 最优序列
    """
    pi = read_data_single('src-开头和弦-pi.xls')
    A_happy = read_data_mul('src-大调和弦矩阵-a.xls')
    A_sad = read_data_mul('src-小调和弦矩阵-a.xls')

    A_happy = np.where(A_happy == 0, 0.0001, A_happy)
    A_sad = np.where(A_sad == 0, 0.0001, A_sad)
    _log10 = np.vectorize(math.log10)
    A = 10 ** (emo * _log10(A_happy) + (1 - emo) * _log10(A_sad))

    s_list1 = read_data_mul('src-大调单音矩阵-b-pre.xls')
    s_list2 = read_data_mul('src-小调单音矩阵-b-pre.xls')
    s_list1 = _log10(np.where(s_list1 == 0, 0.001, s_list1))
    s_list2 = _log10(np.where(s_list2 == 0, 0.001, s_list2))

    melody = xlrd.open_workbook('melody.xls')  # 读取之前传入的数据
    m_table = melody.sheets()[0]  # 获取第一个表
    n_treasure = m_table.nrows  # 得到行数
    m_list = []
    for i in range(n_treasure):
        m_list.append(m_table.row_values(i))
        # 将xls文件数据读取到数组
    mT = np.array(m_list).T  # 矩阵转置

    B1 = 10 ** np.dot(s_list1, mT)  # 大调单音矩阵乘数据矩阵(60*12)x(12*4)=(60*4)
    sum_list1 = np.sum(B1, axis=1)  # 对B1矩阵每行求和,得到60个和,存储到数组中
    B1 = B1 / sum_list1[:, np.newaxis]

    # 以下操作同上
    B2 = 10 ** np.dot(s_list2, mT)
    sum_list2 = np.sum(B2, axis=1)
    B2 = B2 / sum_list2[:, np.newaxis]

    B = 10 ** (emo * _log10(B1) + (1 - emo) * _log10(B2))

    ob = [i for i in range(n_treasure)]
    state_s = [i for i in range(60)]

    hmm = HMM(A, B, pi)  # A隐含状态 B观察序列

    result = hmm.viterbi(ob, state_s)  # 使用维特比算法解码

    hmm.data_write_single('out.xls', result, n_treasure)  # 保存输出到xls
    return result


if __name__ == '__main__':
    print(mmm(0.1))
