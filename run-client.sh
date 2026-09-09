#!/usr/bin/env bash
MYSQL_JAR="src/mysql-connector-j-9.7.0/mysql-connector-j-9.7.0.jar"
if [ ! -d "bin" ] || [ ! -f "bin/com/bookbridge/client/Main.class" ]; then
    echo "⚙️ Binaries not found, running build.sh..."
    bash build.sh
fi

echo "🌐 Starting BookBridge Client & Web UI (Port 8081)..."
java -cp "bin:$MYSQL_JAR" com.bookbridge.client.Main "$@"
