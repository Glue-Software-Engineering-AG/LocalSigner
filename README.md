
# LocalSigner

## Über LocalSigner
Der LocalSigner ermöglicht das Anbringen von elektronischen Signaturen auf PDF-Dokumente. Dazu können sowohl private Schlüssel der Swiss Government PKI als auch SuisseIDs und Soft-Tokens genutzt werden. Signaturen können mit Zeitstempeln versehen werden, um als vollqualifizierte elektronische Signatur gemäss ZertES als Unterschrift verwendet zu werden.
Mit dem eingebauten Validator können elektronische Signaturen ausserdem auf ihre Gültigkeit geprüft werden.

Darüber hinaus können mittels LocalSigner auch diverse einfache Änderungen an PDF-Dateien vorgenommen werden. Die Kompatibilität zum PDF/A-Standard wird automatisch geprüft und nicht PDF/A-konforme Dokumente können direkt nach PDF/A konvertiert werden.


## LocalSigner als Open Source
Die aktuelle Version des LocalSigners (v 4.2.10) wird als sogenannte Closed-Source dem Publikum ausserhalb der Bundesverwaltung zur kostenlosen Nutzung zur Verfügung gestellt. Das ISB als Auftraggeber hat beschlossen, die Weiterentwicklung von LocalSigner spätestens in Jahr 2021 einzustellen. Auf Anfrage verschiedener Interessengruppen hat das ISB im August 2020 beschlossen, LocalSigner als Open Source in kompilierbarer Form freizugeben.


## Build
Das Projekt LocalSigner wird mit _ant_ gebaut (und mit OpenJDK 8).

Die folgenden Bibliotheken des lizenzpflichtigen Produktes BOF PDF-Viewer wurden aus dem Projekt entfernt:
* bfopdf-2.24.2.jar
* bfopdf-jj2000.jar
* bfopdf-license.jar

Damit man das Projekt mit ant bauen kann, muss man (zumindest) die Demoversion des Produktes herunterladen (https://bfo.com/download/) und im Verzeichnis lib wie folgt speichern:
* lib/bfopdf-2.24.2.jar
* lib/bfopdf-jj2000.jar

Diese BFO-Demoversion fügt beim LocalSigner jedem Dokument einen "DEMO"-Stempel hinzu, der anzeigt, dass es mit einer Demoversion erstellt wurde.

LocalSigner kann dann zwar aus dem Quellcode gebaut, aber nicht mit allen Features ausgeführt werden, da die folgenden proprietären Bibliotheken durch Stubs-Varianten ersetzt wurden:
* lib/glue-security-tools-3.0.2.jar
* lib/ltvdetector-1.0.4.jar
* lib/Proxylibrary-Core-1.0.1.jar
* lib/Proxylibrary-GUI-Common-1.0.1.jar
* lib/Proxylibrary-GUI-SWT-1.0.1.jar


### Targets
Mit diversen Targets konnen verschiedene Ausprägungen des LocalSigners gebaut werden.

* `all`: build all versions
* `windows`: build for Microsoft Windows 64bit without JRE
* `linux`: build for Linux

Die fertigen Builds werden unter /deploy abgelegt.


## Starten unter Linux

Voraussetzung ist eine installierte Java Runtime Environment (JRE) in Version 8. Es wird empfohlen, OpenJDK 8 zu nutzen. 

Unter Linux kann LocaSigner wie folgt gestartet werden:
* `cd deploy/linux/`
* `tar -vxzf localsigner_4.2.10_linux.tar.gz `
* `cd LocalSigner/`
* `./localsigner.sh`
  


## Starten unter Windows

Voraussetzung ist eine installierte Java Runtime Environment (JRE) in Version 8. Es wird empfohlen, OpenJDK 8 zu nutzen. 
Zum Beispiel von hier: `https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot`

Unter Windows (64bit) kann LocaSigner wie folgt gestartet werden:
* Die Zip-Datei `deploy/win64/localsigner_4.2.10_windows_64bit.zip` auspacken
* ins Verzeichnis `LocalSigner` wechseln 
* und `Open eGov Localsigner.cmd` ausführen


