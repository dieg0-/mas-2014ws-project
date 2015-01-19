mas-2014ws-project
==================

This repository stores the team project for the Multiagent and Agent Systems course (Winter Semester 2014) at HBRS.


#In order to run this project
Please make sure you read this file before running the project.

1. Make sure the JADE environment variables are correctly set. This would of course imply that JAVA is also running correctly.
  - EG. Your .bashrc file  should containg something similar to:
    - export JADE_LIB=/home/JohnyCash/JADE/jade/lib
    - export CLASSPATH='.:${JADE_LIB}/jade.jar:{JADE_LIB}/commons-codec/commons-codec-1.3.jar:{JADE_LIB}/jadeExamples.jar:/home/JohnyCash/JADE/jade/classes'

  NOTE: If this is the case, you should be able to open a terminal in any location and correctly run:
   java jade.Boot -gui
2. From a terminal, run the dna_maas_2014ws.sh:
   ./dna_maas_2014ws.sh
3. The JADE gui should open.

#Communication Tests

##Shelf Test

1. Write click on the Main Container and select "Start New Agent".
2. Create an agent of class shelf.SimPickerAgent. Give it any name.
3. Create an agent (or several) of the class shelf.ShelfAgent. Give it any name.
4. Look at the console:
5. Right now, requests are fixed. The Picker asks periodically for an amount of some specific piece. The Shelves that have such piece answer. The amount decreases in the Shelf's inventory.


##Robot Test

1. Write click on the Main Container and select "Start New Agent".
2. Create an agent of class station.PickerAgent. Give it any name.
3. Create an agent (or several) of class station.RobotAgent. Give it any name.
4. Right now, requests are fixed. The Picker asks periodically for active robots. Active robots answer with their current location
   (x, y).
