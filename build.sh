 #!/bin/bash

mvn clean install -Dmaven.test.skip=true
cp ./target/service-graph-platform-0.0.1-SNAPSHOT.jar app.jar
