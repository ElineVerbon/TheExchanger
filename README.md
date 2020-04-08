# TheExchanger

To connect to the pi:
- Connect the pi to a charger and a network cable
- Start a terminal, type 'ssh pi@nu-pi-stefan'. Fill in the password.

To start the server running on the pi:
- In a terminal window, navigate to the folder where you cloned the project (../TheExchanger/theExchanger)
- Type './gradlew deploy' to start the server

To start the client on the computer:
- In the same terminal window, type 'java -cp build/libs/NUM2-0.0.1.jar com.nedap.university.eline.exchanger.client.Client'
- Now answer the prompts!