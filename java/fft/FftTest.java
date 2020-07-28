package fft;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FftTest {
	static final int LENGTH = 0x4000;
	static final int CUT_OFF = 0x80;

    public static void main (String[] args) {		
		double[] input = new double[LENGTH];
		double[] output = new double[LENGTH + 1];
		
        for (int x = 0; x < LENGTH; x++)
        {
            input[x] = (x < LENGTH / 2 ? 1 : 0) + Math.sin(x * (double)2 * Math.PI / (double)LENGTH);
        }
		
		JniFft jniFft = new JniFft();
		jniFft.internalCalculate(input, output);
		
		try
		{
			StringBuilder builder = new StringBuilder();		
			for (int x = 0; x < LENGTH; x++)
			{
				builder.append(input[x] + ";");

				if (x * 2 < CUT_OFF)
					builder.append(Math.sqrt(output[x * 2] * output[x * 2] + output[x * 2 + 1] * output[x * 2 + 1]) * 2 / LENGTH + "\n");
				else
					builder.append("\n");
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("result.csv"));
			writer.write(builder.toString());
			writer.close();
		}
		catch (IOException ex) {
			System.out.println(ex);
		}
    }
}