package src.rfcbb_tml.com;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Vector;

import xoc.dta.datatypes.dsp.MultiSiteSpectrum;
import xoc.dta.datatypes.dsp.WaveDouble;

/**
 * This class provide the utility to read waveform data from directory "projPath/waste" and write waveform data to "projPath/waste" file. <br>
 *
 * @since  1.0.0
 * @author 308770
 *
 */

public class AnalogMath {


    /**
     * provide interface to read data from file.
     *
     * @param fileName   file which stores data
     * @return doubleArray of waveform data
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static double[] readAsciiFileDoubleFromWasteDir(String fileName) throws IOException
    {
        Path path = Paths.get(fileName);
        Scanner scanner = new Scanner(path);
        Vector<Double> data = new Vector<Double>();
        while (scanner.hasNext())
        {
            data.add(scanner.nextDouble());
        }
        scanner.close();
        int size = data.size();
        Double[] arrayD = new Double[size];
        data.toArray(arrayD);
        double[] array= new double[size];
        for (int i=0;i<size;i++)
        {
            array[i]=arrayD[i];
        }
        return array;
    }

    /**
     * provide interface to write MultiSiteSpectrum to file
     *
     *
     * @param fileName          destination to write waveform
     * @param spectrum_watts    spectrum data
     * @return  int value of 1
     */
    @SuppressWarnings("resource")
    public static int writeAsciiFileSpectrumToWasteDir(String fileName, MultiSiteSpectrum spectrum_watts)
    {
        for(int iSite=1;iSite<=spectrum_watts.getNumberOfConfiguredSites();iSite++) {
            String sFileName = fileName + ".Site:" + iSite + ".txt";
            try {
                PrintWriter fout= new PrintWriter(sFileName);
                for (int xx= 0; xx <= spectrum_watts.getSize().get(1)-1; xx++ ) {
                    fout.printf("%.15f\n", spectrum_watts.getValue(iSite, xx));
        }
                fout.close();
                System.out.println("File created: " + sFileName);
            } catch (IOException e){
                System.out.println("For this routine to work SmarTest must be started from the ST8 program directory.  " );
                System.out.println("IOException in routine: writeAsciiFileSpectrumToWasteDir:  " + e);
            }
    }
        return(1);
    }


    /**
     * provide interface to write WaveDouble to file
     *
     *
     * @param fileName          destination to write waveform
     * @param spectrum_watts    wavefrom data
     * @return  int value of 1
     */
    @SuppressWarnings("resource")
    public static int writeAsciiFileWaveDoubleToWasteDir(String fileName, WaveDouble raw_waveforms, int iSite)
    {
        String sFileName = fileName + ".Site:" + iSite + ".txt";
        try {
            PrintWriter fout= new PrintWriter(sFileName);
            for (int xx= 0; xx <= raw_waveforms.getSize()-1; xx++ ) {
                fout.printf("%.15f\n", raw_waveforms.getValue(xx));
        }
            fout.close();
            System.out.println("File created: " + sFileName);
        } catch (IOException e){
            System.out.println("For this routine to work SmarTest must be started from the ST8 program directory.  " );
            System.out.println("IOException in routine: writeAsciiFileWaveDoubleToWasteDir:  " + e);
        }
        return(1);
    }
}

