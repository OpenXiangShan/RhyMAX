import numpy as np
import os

# ==== 参数设定 ====
# L2部分
row = 128
stride = 512
startAddr = 0

# RF尺寸
tileM = 64
tileN = 64
tileK = 256

# 计算单元尺寸
m = 32
n = 32
k = 8

# 用户配置矩阵尺寸
# mtilem = 64
# mtilen = 64
# mtilek = 256

mtilem = 30
mtilen = 64
mtilek = 256


numM = tileM // m
numN = tileN // n
numK = tileK // k

# ==== 断言 ====
assert row >= 64, "row must be >= 64"
assert stride >= 256, "stride must be >= 256"
assert (startAddr % stride) + mtilek <= stride, "(startAddr % stride) + mtilek must be <= stride"
assert (startAddr // stride) + tileM <= row, "(startAddr // stride) + tileM must be <= row"

# ==== 路径设定 ====
output_dir = "../../test/src/main/scala/L2/TestData"
os.makedirs(output_dir, exist_ok=True)

# ==== 生成 L2_Data ====
L2_size = row * stride
L2_Data = np.random.randint(0, 256, size=L2_size, dtype=np.uint8)

# ==== 构造 A0 ====
A0 = np.zeros((tileM, tileK), dtype=np.uint8)
for i in range(mtilem):
    for j in range(mtilek):
        idx = startAddr + stride * i + j
        if idx < L2_size:
            A0[i][j] = L2_Data[idx]

# ==== 构造 B0 ====
B0 = np.zeros((tileN, tileK), dtype=np.uint8)
for i in range(mtilen):
    for j in range(mtilek):
        idx = startAddr + stride * i + j
        if idx < L2_size:
            B0[i][j] = L2_Data[idx]

# ==== 构造 C0 ====
C0 = np.zeros((tileM, tileN), dtype=np.uint32)
for i in range(mtilem):
    for j in range(mtilen):
        idx = startAddr + stride * i + j*4
        if idx + 3 < L2_size:
            C0[i][j] = (L2_Data[idx] << 24) + (L2_Data[idx+1] << 16) + (L2_Data[idx+2] << 8) + L2_Data[idx+3]

# ==== 转换为 RF_Data (A) ====

RF_Data_A = [[0] * (numK * numM) for _ in range(m)]

for i in range(numM):
    for j in range(m):
        for p in range(numK):
            hex_str = ""
            for q in range(k):
                elem = A0[m * i + j][p * k + q]
                hex_str += f"{elem:02x}"
            RF_Data_A[j][i * numK + p] = hex_str

# ==== 转换为 RF_Data (B) ====

RF_Data_B = [[0] * (numK * numN) for _ in range(n)]

for i in range(numN):
    for j in range(n):
        for p in range(numK):
            hex_str = ""
            for q in range(k):
                elem = B0[n * i + j][p * k + q]
                hex_str += f"{elem:02x}"
            RF_Data_B[j][i * numK + p] = hex_str

# ==== 保存 L2_Data 到 Scala 文件 ====
l2_file = os.path.join(output_dir, "L2_TestData.scala")
with open(l2_file, "w") as f:
    f.write("package L2\n\n")
    f.write("import chisel3._\n\n")
    f.write("object L2TestData {\n")

    num_lines = row
    for i in range(num_lines):
        base = i * stride
        line_data = L2_Data[base:base + stride]
        f.write(f"  val L2_Line{i} = Seq(\n")
        for j in range(0, stride, 16):
            segment = line_data[j:j+16]
            line = ", ".join(f"\"h{v:02x}\".U" for v in segment)
            f.write(f"    {line},\n")
        f.write("  )\n\n")

    all_lines = " ++ ".join(f"L2_Line{i}" for i in range(num_lines))
    f.write(f"  val L2_Data = {all_lines}\n")
    f.write("}\n")

# ==== 保存 RF_Data 到 Scala 文件 ====
rf_file = os.path.join(output_dir, "RF_TestData.scala")
with open(rf_file, "w") as f:
    f.write("package L2\n\n")
    f.write("import chisel3._\n\n")
    f.write("object RFTestData {\n")

    # A 数据
    f.write("  val A = Seq(\n")
    for row in RF_Data_A:
        f.write("    Seq(\n")
        f.write("      " + ",\n      ".join(f"\"h{v}\".U" for v in row) + "\n")
        f.write("    ),\n")
    f.write("  )\n\n")

    # B 数据
    f.write("  val B = Seq(\n")
    for row in RF_Data_B:
        f.write("    Seq(\n")
        f.write("      " + ",\n      ".join(f"\"h{v}\".U" for v in row) + "\n")
        f.write("    ),\n")
    f.write("  )\n")

    f.write("}\n")
