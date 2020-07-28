package fft;

public class JniFft {
    
    static {
        System.loadLibrary("ooura");
    }

    public native void internalCalculate(double[] inputData, double[] outputData);
}
