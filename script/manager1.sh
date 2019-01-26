#!/bin/bash
echo "dummy.sh #####################################"
nohup sh dummy.sh > dummy.out

echo "numberTest1.sh #####################################"
nohup sh numberTest1.sh > numberTest1.out

echo "numberTest2.sh #####################################"
nohup sh numberTest2.sh > numberTest2.out

echo "timestampTest1.sh #####################################"
nohup sh timestampTest1.sh > timestampTest1.out

echo "timestampTest2.sh #####################################"
nohup sh timestampTest2.sh > timestampTest2.out

echo "simulationTest1.sh #####################################"
nohup sh simulationTest1.sh > simulationTest1.out

echo "simulationTest2.sh #####################################"
nohup sh simulationTest2.sh > simulationTest2.out

echo "coreTest1.sh #####################################"
nohup sh coreTest1.sh > coreTest1.out

echo "coreTest2.sh #####################################"
nohup sh coreTest2.sh > coreTest2.out