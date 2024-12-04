javac src/Server.java 
javac src/Client.java
kill -9 `ps -ef | grep java | awk '{ print $2 }'`