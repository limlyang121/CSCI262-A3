Tested on : Ubuntu 20.04 WSL2

javac version
11.0.17

java Version
11.0.17

Compile:
javac IDS.java

Run:
java IDS Events.txt Stats.txt <day>

Compile and Run: (10 Days)
javac IDS.java && java IDS Events.txt Stats.txt 10


Note:
1. Don't set maximum in (Events.txt) too small if the mean and std is large
as the random value i set only from minimum to maximum value.

2. Stats1.txt have large mean and std hence more prob to anomality
while Stats2.txt have smaller mean and std hence change to anomality is very small/None.
