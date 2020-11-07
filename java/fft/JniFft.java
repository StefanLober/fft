package fft;

import java.io.File;

public class JniFft {
    static {
        try {
            System.loadLibrary("ooura");
        }
        catch(Throwable t) {
            String OS = System.getProperty("os.name");
            if(OS.toLowerCase().contains("win")) {
                System.load(new File("./ooura.dll").getAbsolutePath());
            }
            else if(OS.toLowerCase().contains("lin")) {
                System.load(new File("./libooura.so").getAbsolutePath());
            }
        }
    }

    private long nativeObjectPointer;

    private native long internalNew(int size);
    private native void internalCalculate(double[] inputData, double[] outputData, int cutOff, long nativeObjectPointer);
    private native void internalDispose(long nativeObjectPointer);

    public JniFft(int length) {
        nativeObjectPointer = internalNew(length);
    }

    public void calculate(double[] inputData, double[] outputData, int cutOff) {
        internalCalculate(inputData, outputData, cutOff, nativeObjectPointer);
    }

    public void dispose() {
        internalDispose(nativeObjectPointer);
    }
}