$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\omjai\.maven\apache-maven-3.9.9\bin;$env:PATH"
& "C:\Users\omjai\.maven\apache-maven-3.9.9\bin\mvn.cmd" @args
