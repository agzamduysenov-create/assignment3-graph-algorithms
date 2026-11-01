#!/bin/sh
# Compile everything, run tests, demo and experiments (Linux / macOS / Git Bash)
set -e
rm -rf out
javac -d out $(find src -name "*.java")
java -cp out tests.AllTests
java -cp out app.Main
java -cp out app.Benchmark
