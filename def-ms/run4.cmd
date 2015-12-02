set JAVA_HOME=C:\java\jdk1.8.0_25
set PATH=%JAVA_HOME%\bin;C:\apache-maven-3.3.3\bin;C:\gradle-2.4\bin;
mvn -DskipTests -Dserver.port=7780 -Dakka.remote.netty.tcp.port=2553  spring-boot:run
