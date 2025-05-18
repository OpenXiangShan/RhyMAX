# AME  

This is to support FP8.

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
make testOnly  #default is MMAU.MMAUTestExpect
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
