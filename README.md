Java 17 
Maven 3.6.3

project structure:

src/
 └── main/
     └── java/
         └── com/
             └── your/
                 └── package/
                     └── Main.java
pom.xml

install and run:

mvn clean install 

mvn exec:java -Dexec.mainClass="com.techtest.api.Api"
