import numpy as np
import os

class MatrixGenerator:
    def __init__(self):
        # 硬件参数初始化
        # self.tileM = 32
        # self.tileN = 32
        # self.tileK = 64

        # self.m = 8
        # self.n = 8
        # self.k = 2

        self.tileM = 64
        self.tileN = 64
        self.tileK = 256

        self.m = 32
        self.n = 32
        self.k = 8


        # 用户配置矩阵尺寸
        self.mtilem = 32
        self.mtilen = 32
        self.mtilek = 64

        self.numM = self.tileM // self.m
        self.numN = self.tileN // self.n
        self.numK = self.tileK // self.k

        

        # 生成原始矩阵
        self.A0 = np.random.randint(-128, 127, (self.tileM, self.tileK), dtype=np.int8)
        self.B0 = np.random.randint(-128, 127, (self.tileK, self.tileN), dtype=np.int8)
        self.Ctmp0 = np.random.randint(-2**31, 2**31-1, (self.tileM, self.tileN), dtype=np.int32)

        # 进行区域保留，其余填 0
        self.A0 = self._crop_and_zero(self.A0, self.mtilem, self.mtilek)
        self.B0 = self._crop_and_zero(self.B0, self.mtilek, self.mtilen)
        self.Ctmp0 = self._crop_and_zero(self.Ctmp0, self.mtilem, self.mtilen)


        self.C0 = np.dot(self.A0.astype(np.int32), self.B0.astype(np.int32)).astype(np.int32) + self.Ctmp0.astype(np.int32)

        
        # self.C0 = self._crop_and_zero(C0, self.mtilem, self.mtilen)

        # 转换为内部存储格式
        self.A = self._convert_A()
        self.B = self._convert_B()
        self.Ctmp = self._convert_C(self.Ctmp0)
        self.C = self._convert_C(self.C0)

    def _crop_and_zero(self, matrix, max_row, max_col):
        """保留左上角 [0:max_row, 0:max_col]，其余置 0"""
        result = np.zeros_like(matrix)
        result[:max_row, :max_col] = matrix[:max_row, :max_col]
        return result

    def _convert_A(self):
        A = [[0] * (self.numK * self.numM) for _ in range(self.m)]
        for i in range(self.numM):
            for j in range(self.m):
                for p in range(self.numK):
                    hex_str = ""
                    for q in range(self.k):
                        elem = self.A0[self.m * i + j][p * self.k + q]
                        hex_str += f"{elem & 0xFF:02x}"
                    A[j][i * self.numK + p] = hex_str
        return A

    def _convert_B(self):
        B = [[0] * (self.numK * self.numN) for _ in range(self.n)]
        for i in range(self.numN):
            for j in range(self.n):
                for p in range(self.numK):
                    hex_str = ""
                    for q in range(self.k):
                        elem = self.B0[p * self.k + q][self.n * i + j]
                        hex_str += f"{elem & 0xFF:02x}"
                    B[j][i * self.numK + p] = hex_str
        return B

    def _convert_C(self, src_matrix):
        C = [[0] * (self.numM * self.numN * self.m) for _ in range(self.n // 4)]
        for i in range(self.numM):
            for j in range(self.numN):
                for p in range(self.n // 4):
                    for q in range(self.m):
                        hex_str = ""
                        for r in range(4):
                            elem = src_matrix[i * self.m + q][j * self.n + p * 4 + r]
                            hex_str += f"{elem & 0xFFFFFFFF:08x}"
                        C[p][i * self.numN * self.m + j * self.m + q] = hex_str
        return C

    def save_to_file(self):
        """保存为完整的 Scala 文件"""
        output_dir = "../../test/src/main/scala/AME/TestData"
        os.makedirs(output_dir, exist_ok=True)
        output_file = os.path.join(output_dir, "AME_TestData.scala")

        with open(output_file, "w") as f:
            f.write("package AME\n\n")
            f.write("import chisel3._\n\n")
            f.write("object AMETestData {\n")

            f.write("  val A = Seq(\n")
            for row in self.A:
                f.write("    Seq(\n")
                f.write("      " + ",\n      ".join(f"\"h{v}\".U" for v in row) + "\n")
                f.write("    ),\n")
            f.write("  )\n\n")

            f.write("  val B = Seq(\n")
            for row in self.B:
                f.write("    Seq(\n")
                f.write("      " + ",\n      ".join(f"\"h{v}\".U" for v in row) + "\n")
                f.write("    ),\n")
            f.write("  )\n\n")

            f.write("  val Ctmp = Seq(\n")
            for row in self.Ctmp:
                f.write("    Seq(\n")
                f.write("      " + ",\n      ".join(f"\"h{v}\".U" for v in row) + "\n")
                f.write("    ),\n")
            f.write("  )\n\n")

            f.write("  val C = Seq(\n")
            for row in self.C:
                f.write("    Seq(\n")
                f.write("      " + ",\n      ".join(f"\"h{v}\".U" for v in row) + "\n")
                f.write("    ),\n")
            f.write("  )\n")

            f.write("}\n")

if __name__ == "__main__":
    gen = MatrixGenerator()
    gen.save_to_file()
