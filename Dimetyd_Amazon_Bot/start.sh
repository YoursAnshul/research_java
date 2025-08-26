#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
export PATH=$JAVA_HOME/bin:$PATH
export DISPLAY=:99
dbus-launch --exit-with-session Xvfb :99 -screen 0 1024x768x24 &
sleep 2 & fluxbox & x11vnc -display :99 -N -forever & chromium-browser --no-sandbox & java -jar /app/dfo.jar $1