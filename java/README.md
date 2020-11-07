# FftTest
A Java test application for the JNI wrapper int shared library ooura (liboura.so/ooura.dll).

JNI header file generated with:
javah -d .. fft.JniFft

Compile with:
javac -d out fft\*.java

Copy shared library ooura (libooura.so/ooura.dll) into out dir.

Run with:
java -cp out -Djava.library.path=out fft.FftTest
