@echo off
rem Compile everything, run tests, demo and experiments (Windows)
if exist out rmdir /s /q out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt || exit /b 1
del sources.txt
java -cp out tests.AllTests || exit /b 1
java -cp out app.Main
java -cp out app.Benchmark
