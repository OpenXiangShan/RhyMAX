# AME

This is used to accelerate matrix operations.

Branch "LS" is for adding LS instructions.

## Quick start

```
make init
make compile
```

## Generate SystemVerilog

```
make sv  #default is PEcube_sv
make sv SV_NAME=xxx_sv  #specific target
```

## ChiselTest

```
make testAll  #run all tests
make testOnly  #default is AME.AMETest, consider it a smoke test
make testOnly TEST_NAME=packageName.testName  #specific target
```

## Generate ramdom testdata by Python

```
make pythonGen  #default is MMAUtestGen.py
make pythonGen PYTHON_NAME=xxx.py  #specific target
```

## Clean mill cache

```
make clean
```
