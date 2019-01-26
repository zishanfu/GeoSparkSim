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