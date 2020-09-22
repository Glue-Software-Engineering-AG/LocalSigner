#!/bin/sh


# Check for Smart Card Daemon
echo "Checking pcscd (PC/SC Smart Card Daemon)"
if  ps -ef | grep -v grep | grep pcscd > /dev/null; then
  echo "   pcscd running"
else
  echo "   pcscd not running"
  echo "Warning: Smartcard will probably not work until pcscd is running!"
fi

# Home Path to installation directory
LS_HOME="$(dirname "$0")"

libs="$LS_HOME/lib/localsigner.jar:\
$LS_HOME/lib/bcprov-jdk15on-1.59.jar:\
$LS_HOME/lib/bcmail-jdk15on-1.59.jar:\
$LS_HOME/lib/bcpkix-jdk15on-1.59.jar"

if [ "$(java -version 2>&1 | grep 64-Bit)" ]; then
  # 64bit JVM
  echo "LocalSigner 64bit mode"
  libs="$libs:$LS_HOME/lib/swt.jar"
else 
  echo "Please use a 64Bit JRE 8"
fi

echo $libs
export SWT_GTK3=0
java -Xmx512m -Dbase=$LS_HOME -classpath $libs ch.admin.localsigner.main.LocalSigner "$@"
