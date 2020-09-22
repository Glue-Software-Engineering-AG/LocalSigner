REM Used for internal tests with debug output on the console.

cd /D %~dp0

set PATH=jre\bin;%PATH%
set libs=lib\localsigner.jar;lib\bcprov-jdk15-145.jar;lib\bcmail-jdk15-145.jar;lib\bctsp-jdk15-145.jar;lib\swt.jar
java -Xmx512m -classpath %libs% ch.admin.localsigner.main.LocalSigner %*
