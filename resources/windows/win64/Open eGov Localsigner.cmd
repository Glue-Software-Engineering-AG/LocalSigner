rem Starting LocalSigner x64 with system JRE (see: https://openjdk.java.net/install/, https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot)
cd /D %~dp0

set libs=lib\localsigner.jar;lib\bcprov-jdk15on-1.59.jar;lib\bcmail-jdk15on-1.59.jar;lib\bcpkix-jdk15on-1.59.jar;lib\swt.jar
set nativeDllDir=%~dp0\native
START javaw -Dswt.library.path="%nativeDllDir%" -Xmx512m -classpath %libs% -Djna.boot.library.path="%nativeDllDir%" -Djna.nounpack=true -Djna.noclasspath=true ch.admin.localsigner.main.LocalSigner %*
