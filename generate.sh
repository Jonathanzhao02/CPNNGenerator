#!/bin/bash
javac *.java

for i in {0..1000}
do
	java Main FILE=$i ANIM=false MIN=true
done