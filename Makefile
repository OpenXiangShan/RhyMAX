BUILD_DIR = ./build
TEST_NAME ?= AME.AMETest
SV_NAME ?= PEcube_sv
PYTHON_NAME ?= AMEtestGen.py

init:
	git submodule update --init --recursive --jobs 4

compile:
	mill -i AME.compile | pv -t

testAll:
	mill -i AME.test.test | pv -t

testOnly:
	mill -i AME.test.testOnly ${TEST_NAME} | pv -t

sv:
	mkdir -p $(BUILD_DIR)
	mill -i AME.runMain ${SV_NAME} --target-dir $(BUILD_DIR) | pv -t

pythonGen:
	cd ./python_Gen && python3 ${PYTHON_NAME}


clean:
	rm -rf ./out
