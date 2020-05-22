#!/bin/bash
javac *.java

for i in {0..100}
do
	java Main $i $i
done