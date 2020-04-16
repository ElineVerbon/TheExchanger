# TheExchanger

To get the program up and running, open a command line terminal.
1. First make a clone of this repository by typing in the following lines in the terminal and hitting enter after each line:

`mkdir ElineFinalAssignment`

`cd ElineFinalAssignment`

`git clone https://github.com/ElineVerbon/TheExchanger.git`

`cd TheExchanger`


To connect to the pi:
- Connect the pi to a charger and a network cable
- Start a terminal, type 'ssh pi@nu-pi-stefan'. Fill in the password.

To start the server running on the pi:
- In a terminal window, navigate to the folder where you cloned the project (../TheExchanger)
- Type './gradlew deploy' to load the jar file and start the server

To start the client on the computer:
- In the same terminal window, type 'java -cp build/libs/NUM2-0.0.1.jar com.nedap.university.eline.exchanger.client.Client'
- Now answer the prompts and have fun!
