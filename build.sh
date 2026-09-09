#!/usr/bin/env bash
set -e

echo "🔨 Building BookBridge..."
mkdir -p bin

MYSQL_JAR="src/mysql-connector-j-9.7.0/mysql-connector-j-9.7.0.jar"

if [ ! -f "$MYSQL_JAR" ]; then
    # Fallback to any mysql connector jar in project
    MYSQL_JAR=$(find . -name "mysql-connector-j*.jar" | head -n 1)
fi

echo "📦 Classpath dependency: $MYSQL_JAR"

# Find all java source files
SOURCES=$(find src/com/bookbridge -name "*.java")

javac -d bin -cp "$MYSQL_JAR" $SOURCES

echo "✅ Build Successful! Output classes saved in bin/"
