# Make sure your JADE ENVIRONMENT VARIABLES are correctly set.
# For example: 
# export JADE_LIB=/home/JohnyCash/JADE/jade/lib
# export CLASSPATH=”.:${JADE_LIB}/jade.jar:{JADE_LIB}/commons-codec/commons-codec-1.3.jar:{JADE_LIB}/jadeExamples.jar:/home/JohnyCash/JADE/jade/classes”

#This variable should point to the correct directory of the project
export MAGIC_PATH=./bin
export CLASSPATH=$CLASSPATH:${MAGIC_PATH}
java jade.Boot -gui WarehouseManager:warehouse.WarehouseAgent
