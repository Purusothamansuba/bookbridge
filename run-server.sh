#!/usr/bin/env bash
MYSQL_JAR="src/mysql-connector-j-9.7.0/mysql-connector-j-9.7.0.jar"
if [ ! -d "bin" ] || [ ! -f "bin/com/bookbridge/server/LibraryServer.class" ]; then
    echo "⚙️ Binaries not found, running build.sh..."
    bash build.sh
fi

echo "🚀 Starting BookBridge Backend Server (Port 8080)..."
java -cp "bin:$MYSQL_JAR" com.bookbridge.server.LibraryServer
