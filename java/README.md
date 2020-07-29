# FftTest
A Java test application for the JNI wrapper int shared library ooura (liboura.so/ooura.dll).

Compile with: javac -d out fft\\*.java

Copy shared library ooura (libooura.so/ooura.dll) into out dir.

Run with: java -cp out -Djava.library.path=out fft.FftTest
