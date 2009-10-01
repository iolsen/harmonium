//////////////////////////////////////////////////////////////////////
//
//     File: Mp3Helper.java
//
//     Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

/*
 * Created on May 9, 2005
 *
 */
package com.tivo.hme.sdk.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import com.tivo.hme.interfaces.IHmeConstants;
import com.tivo.hme.sdk.io.FastInputStream;
import com.tivo.hme.sdk.io.HmeInputStream;

/**
 * A helper class for manipulating mp3 files.  This class replaces the Mp3Duration helper
 * class and adds additional functionality for seeking into MP3 streams.
 * 
 * Usage:
 * <ul>
 * <li>Create a helper instance for a stream and specify the stream size</li>
 * <li>Call either getMp3Duration() or seek(skiptime)</li>
 * <li>Do NOT re-use the helper instance</li>
 * </ul>
 * 
 * Since getMp3Duration() may read the entire stream, it is best to use the 
 * helper with a stream that won't be needed again - perhaps by creating 
 * a new InputStream from the original source of the MP3.
 * 
 * The InputStream returned by seek(skiptime) has been advanced and is
 * ready to be used for playback starting at the new position.
 * 
 * @author Ken Gidley (based on Mp3Duration by Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin)
*/
public class Mp3Helper {
    
    // if true, spew some debug output
    static boolean DEBUG = false;
    
    //
    // If the frame length is the same for CBR_FRAMES, we conclude that the mp3
    // is CBR. 400 frames is approximately 10 seconds of audio.
    //
    final static int CBR_FRAMES = 400;
    
    // id3 tag magic header
    final static String ID3_MAGIC = "ID3";
    

    //
    // see http://mpgedit.org/mpgedit/mpeg_format/MP3Format.html
    //
    
    // mpeg audio version
    final static int MP3_MPEG_25       = 0;
    final static int MP3_MPEG_RESERVED = 1;
    final static int MP3_MPEG_2        = 2;
    final static int MP3_MPEG_1        = 3;
    
    // mpeg layer
    final static int MP3_LAYER_RESERVED = 0;
    final static int MP3_LAYER_3        = 1;
    final static int MP3_LAYER_2        = 2;
    final static int MP3_LAYER_1        = 3;
    
    // stereo flag
    final static int MP3_STEREO_NORMAL = 0;
    final static int MP3_STEREO_JOINT  = 1;
    final static int MP3_STEREO_DUAL   = 2;
    final static int MP3_STEREO_MONO   = 3;
    
    // bitrate table
    final static int MP3_BITRATES[][] = {
            {   0,   0,   0,   0,   0 },
            {  32,  32,  32,  32,   8 },
            {  64,  48,  40,  48,  16 },
            {  96,  56,  48,  56,  24 },
            { 128,  64,  56,  64,  32 },
            { 160,  80,  64,  80,  40 },
            { 192,  96,  80,  96,  48 },
            { 224, 112,  96, 112,  56 },
            { 256, 128, 112, 128,  64 },
            { 288, 160, 128, 144,  80 },
            { 320, 192, 160, 160,  96 },
            { 352, 224, 192, 176, 112 },
            { 384, 256, 224, 192, 128 },
            { 416, 320, 256, 224, 144 },
            { 448, 384, 320, 256, 160 },
            {   0,   0,   0,   0,   0 }
    };
    
    // sample rate table
    final static int MP3_SAMPLERATES[][] = {
            { 11025, 0, 22050, 44100 },
            { 12000, 0, 24000, 48000 },
            {  8000, 0, 16000, 32000 },
            {     0, 0,     0,     0 },
    };
    
    //
    // information on Xing was found at
    // 
    // http://home.pcisys.net/~melanson/codecs/mp3extensions.txt
    //
    
    // look for xing in the first XING_FRAMES frames
    final static int XING_FRAMES = 10;
    
    // xing magic
    final static String XING_MAGIC = "Xing";
    
    // where to find the xing magic
    final static int XING_OFFSET[] = { 17, 32, 9, 17 };
    
    // this flag indicates that the Xing header contains nframes
    final static int XING_NFRAMES_FLAG = 0x0001;

    final static String VBRI_MAGIC = "VBRI";
    
    private FastInputStream fin = null;
    private HmeInputStream hmeIn = null;
    private long availableBytes = -1;
    private int nframes = 0;
    private byte buf[] = new byte[4];
    private int bitrate = -1;
    private int samplerate = -1;
    private int oldFlen = 0;
    private int version = 0;
    private int mode = 0;
    private int layer = 0;
    private int sindex = 0;
    private boolean cbr = false;
    private boolean vbr = false;
    

    
    private boolean skippedID3 = false;

    private int maxNumFrames = -1;

    /**
     * Create a helper for an InputStream representing an MP3 stream 
     * with the specified size.  The helper will read the stream to
     * perform its functions. In the case of the getMp3Duration() method,
     * the entire stream may be read and therefore it is recommended 
     * that the stream be closed after calling the getMp3Duration() method.
     * The seek method will advance into the stream to the specified
     * point and return a stream that can then be used to begin play
     * from the specified point.
     * 
     */
    public Mp3Helper(InputStream in, long available) 
    {
    	fin = new FastInputStream(in, IHmeConstants.TCP_BUFFER_SIZE);
        hmeIn = new HmeInputStream(fin);
        nframes = 0;
        availableBytes = available;
    }

    private long skipID3Header(long available) throws IOException
    {
        // see if we've already read the id3 header
        if (skippedID3)
            return available;
        
        //
        // skip ID3
        //
        hmeIn.mark(16);
        hmeIn.readFully(buf, 0, 3);
        if (new String(buf, 0, 3).equals(ID3_MAGIC)) {
            /*int version = */hmeIn.readShort();
            /*int flags =*/ hmeIn.read();
            int id3size = (hmeIn.read() << 21) + (hmeIn.read() << 14) + (hmeIn.read() << 7) + (hmeIn.read());
            if (DEBUG) {
                System.out.println("ID3 found, size=" + id3size);
            }
            hmeIn.skip(id3size);
            available -= id3size + 10;
        } else {
        	hmeIn.reset();
        }
        return available;
    }

    private void findSync(boolean markHeader) throws IOException 
    {
        int ch = hmeIn.read();
        int ch2;
        while (true) {
            ch2 = hmeIn.read();
            if (ch2 == -1) {
                throw new EOFException();
            }
            if (ch == 0xff && (ch2 & 0xe0) == 0xe0) {
                break;
            }
            ch = ch2;
        }
        ++nframes;

        // set the mark to preserve the header to allow 'unreading' it
        if (markHeader) {
        	hmeIn.mark(4);
        }
        //
        // read frame header
        //
        buf[0] = (byte)ch;
        buf[1] = (byte)ch2;
        hmeIn.readFully(buf, 2, 2);
    }

    private int readFrame() throws IOException
    {
        int flen = 0;

        // 
        // find the next frame sync point
        //
        findSync(false);

        //
        // calculate frame length
        //
        
        version = getBits(buf, 19, 20);
        layer   = getBits(buf, 17, 18);
        int bindex  = getBits(buf, 12, 15);
        sindex  = getBits(buf, 10, 11);
        int padding = getBits(buf,  9,  9);
        mode    = getBits(buf,  6,  7);
        
        int rateindex = 0;
        switch (version) {
        case MP3_MPEG_1:
            switch (layer) {
            case MP3_LAYER_1: rateindex = 0; break;
            case MP3_LAYER_2: rateindex = 1; break;
            case MP3_LAYER_3: rateindex = 2; break;
            }
            break;
        case MP3_MPEG_2:
        case MP3_MPEG_25:
            switch (layer) {
            case MP3_LAYER_1: rateindex = 3; break;
            case MP3_LAYER_2:
            case MP3_LAYER_3: rateindex = 4; break;
            }
            break;
        }
        
        bitrate = MP3_BITRATES[bindex][rateindex];
        samplerate = MP3_SAMPLERATES[sindex][version];
        if (bitrate == 0 || samplerate == 0) {
            // invalid frame - resync
            flen = -1;
            return flen;
        }
        
        if (layer == MP3_LAYER_1) {
            flen =  12 * bitrate * 1000 / samplerate + padding;
            flen *= 4;
        } else {
            flen = 144 * bitrate * 1000 / samplerate;
            if (version != MP3_MPEG_1) {
                flen /= 2;
            }
            flen += padding;
        }
        flen -= 4;
        
        return flen;
    }
    
    private int checkForVBR(int flen) throws IOException
    {
    	boolean xing = false;
    	fin.mark(128);
        long pos = fin.getCount();
        int xindex = (((version == MP3_MPEG_1) ? 0 : 1) * 2 +
                ((mode == MP3_STEREO_MONO) ? 0 : 1));
        hmeIn.skip(XING_OFFSET[xindex]);
        hmeIn.readFully(buf, 0, 4);
        if (new String(buf).equals(XING_MAGIC)) {
            int flags = hmeIn.readInt();
            if ((flags & XING_NFRAMES_FLAG) != 0) {
                maxNumFrames = hmeIn.readInt();
                vbr = true;
                xing = true;
                if (DEBUG) {
                    System.out.println("Xing: maxNumFrames=" + maxNumFrames);
                }
            }
        }
        
        if (!xing)
        {
        	// check for VBRI - logic from javazoom library
        	fin.reset();
        	pos = fin.getCount();
        	hmeIn.skip(32);
        	hmeIn.readFully(buf, 0, 4);
        	if (new String(buf).equals(VBRI_MAGIC)) {
        		hmeIn.skip(10);
        		maxNumFrames = hmeIn.readInt();
        		vbr = true;
        		if (DEBUG) {
        			System.out.println("VBRI: maxNumFrames=" + maxNumFrames);
        		}
        	}
        }
        flen -= fin.getCount() - pos;
        return flen;
    }

    
    private boolean checkForCBR(int flen)
    {
        //
        // is this still a CBR?
        //
        
        if (oldFlen >= 0) {
            if (nframes > CBR_FRAMES) {
                cbr = true;
            }
            if (oldFlen > 0 && Math.abs(flen - oldFlen) > 4) {
                if (DEBUG) {
                    System.out.println("VBR: at " + nframes + " " +
                            flen + " != " + oldFlen);
                }
                vbr = true;
                oldFlen = -1;
            }
            // --- begin old code --- 
            // oldFlen = flen;
            // --- end old code ---
            // --- begin new code ---
            else // KPG - added missing 'else'
            {
                oldFlen = flen;
            }
            // --- end new code ---
        }
        return cbr;
    }
    
    /**
     * Reads the MP3 InputStream (possibly the entire stream) to determine
     * its duration in milliseconds.
     * 
     * <p>Since getMp3Duration() may read the entire stream, it is best to use the 
     * helper with a stream that won't be needed again - perhaps by creating 
     * a new InputStream from the original source of the MP3.
     * 
     * @return the duration of the InputStream in milliseconds.
     */
    public long getMp3Duration()
    {
        long duration = -1;
        try
        {
            availableBytes = skipID3Header(availableBytes);

            //
            // read frames
            //
            oldFlen = 0;
            int flen = -1;
            while (duration == -1) 
            {
                // skip bad frames
                while ((flen = readFrame()) < 0)
                    ;
                
                if (checkForCBR(flen))
                {
                    duration = (availableBytes * 8L) / bitrate;
                    if (DEBUG) {
                        System.out.println("CBR : bytes=" + availableBytes +
                                " bitrate=" + bitrate +
                                "kbps duration=" + duration + "ms");
                    }                                        
                }
                
                // check for Xing frame (type of VBR file)
                if (nframes < XING_FRAMES) 
                {
                    flen = checkForVBR(flen);
                    if (maxNumFrames != -1)
                    {
                        duration = framesToDuration(maxNumFrames);
                        break;
                    }
                }

                // skip the rest of the frame
                hmeIn.skip(flen);
            }
        }
        catch (EOFException e) 
        {
            // walked the whole file, nframes is total frames in file
            return framesToDuration(nframes);
        } 
        catch (IOException e) 
        {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        finally
        {
            try {
                hmeIn.close();
            } catch (IOException e1) 
            {
                // ignore on close
            }
        }

        return duration;
    }

    /**
     * Skips <code>timeToSkip</code> milliseconds into the associated MP3 InputStream,
     * and returns an InputStream that is ready to playback from the new position. 
     * 
     * @return An InputStream for the MP3 that has been advanced to the specified point.
     * @throws IOException If error occurs trying to seek into stream.
     */
    public InputStream seek(long timeToSkip) throws IOException
    {
        long timeSkipped = 0L;
        
        availableBytes = skipID3Header(availableBytes);
        
        //
        // read frames
        //
        oldFlen = 0;
        int flen = -1;
        while (timeSkipped < timeToSkip) 
        {
            // skip any bad frames and/or bad sync bytes
            while ((flen = readFrame()) < 0)
                ;
            
            if (checkForCBR(flen))
            {
                // it's CBR, skip the right amount of bytes
                flen = (int)(timeToSkip*bitrate/8L);
                hmeIn.skip(flen);
                break;
            }
            
            // check for Xing frame (type of VBR file)
            if (nframes < XING_FRAMES) 
            {
                flen = checkForVBR(flen);
            }
            
            timeSkipped += ms_per_frame();
                
            // skip the rest of the frame
            hmeIn.skip(flen);
        }
        if (timeSkipped < timeToSkip)
        {
            // CBR file, must find next frame, then back up
            findSync(true);  // true means set the mark on the header before reading it
            hmeIn.reset();   // now 'unread' the header
        }
        return hmeIn;
    }
    
    /**
     * Pull some bits range out of a byte buffer.
     */
    private int getBits(byte buf[], int from, int to)
    {
        int result = 0;
        for (int i = from; i <= to; ++i) {
            int off = buf.length - (i / 8) - 1;
            int bit = i % 8;
            if ((buf[off] & (1 << bit)) > 0) {
                result += (1 << (i - from));
            }
        }
        return result;
    }
    
    /**
     * Convert nframes to duration.  Calculates the duration assuming
     * the samplerate is constant for all the frames - the usual case,
     * but not always 100% accurate.
     */
    private long framesToDuration(int nframes)
    {
        long tmpSampleRate = samplerate;
       
        if (tmpSampleRate <= 0L)
        {
            if (DEBUG) {
                System.out.println("samplerate = " + samplerate + ", tmpSamplerate = " + tmpSampleRate);
            }
            // bad sample rate, assume 44100
            tmpSampleRate = 44100L;
        }
        if (DEBUG) {
            long dur = nframes * 1152L * 1000L / tmpSampleRate;
            System.out.println("bitrate = " + bitrate + ", samplerate = " + tmpSampleRate + ", nframes = " + nframes + ", duration = " + dur +" ms");
        }                                        
        return nframes * 1152L * 1000L / tmpSampleRate;
    }

    /**
     * Returns ms/frame.  Modified from JavaLayer by JavaZoom.
     * @return milliseconds per frame
     */
    private float ms_per_frame()
    {
        float retVal = 0.0f;
        double[] h_vbr_time_per_frame = {-1, 384, 1152, 1152};

        if (vbr == true)
        {           
            double tpf = h_vbr_time_per_frame[4-layer] / samplerate;
            if (version != MP3_MPEG_1) tpf /= 2;
            retVal = ((float) (tpf * 1000));
        }
        else
        {
            float ms_per_frame_array[][] = {{8.707483f,  8.0f, 12.0f},
                                            {26.12245f, 24.0f, 36.0f},
                                            {26.12245f, 24.0f, 36.0f}};
            retVal = (ms_per_frame_array[(4-layer)-1][sindex]);
        }
        if (DEBUG) {
            System.out.println("nframe = " + nframes + ", ms_per_frame = " + retVal);
        }                                        
        return retVal;
    }

    /**
     * For testing.
     */
    private static void printFileInfo(File file) throws IOException 
    {
        InputStream in = new FileInputStream(file);
        try {
            Mp3Helper mp3Util = new Mp3Helper(new FastInputStream(in, 4096), file.length());
            String shortName = file.toString().substring(file.toString().lastIndexOf("\\")+1);
            long startTime = System.currentTimeMillis();
            long lenMS = mp3Util.getMp3Duration();
            long stopTime = System.currentTimeMillis();
            int mins = (int)(lenMS/1000/60);
            System.out.println(shortName + ", length (ms) = " + lenMS + ", mm:ss = " + mins + ":" + (int)(((lenMS/1000.0f/60.0f)-mins)*60) +", took " + (stopTime-startTime) + " ms to read Mp3\n" );
            System.out.flush();
        } finally {
            in.close();
        }
    }

    /**
     * For testing.
     */
    private static void printStreamInfo(String stream) throws IOException 
    {
//        InputStream in = new FileInputStream(file);
		URL mp3URL = new URL(stream);
        URLConnection conn = mp3URL.openConnection();
        InputStream in = conn.getInputStream();
        int contentLen = conn.getContentLength();

//        BitStream bitstream = new BitStream(in);
//        Header header = bitstream.readFrame();
//        Decoder decoder = new Decoder(header, bitstream);
//        SampleBuffer sample = (SampleBuffer)decoder.decodeFrame();
//        
//        System.out.println("jlme: header.bitrate_string="+header.bitrate_string() + ", header.layer_str=" + header.layer_string() + ", header.mode_string=" + 
//        		header.mode_string() + "\n" + header.toString() + "\n\n");
        
        try {
            Mp3Helper mp3Util = new Mp3Helper(new FastInputStream(in, 4096), contentLen);
//            String shortName = file.toString().substring(file.toString().lastIndexOf("\\")+1);
            long startTime = System.currentTimeMillis();
            long lenMS = mp3Util.getMp3Duration();
            long stopTime = System.currentTimeMillis();
            int mins = (int)(lenMS/1000/60);
            System.out.println(stream + ",\nlength (ms) = " + lenMS + ", mm:ss = " + mins + ":" + (int)(((lenMS/1000.0f/60.0f)-mins)*60) +", took " + (stopTime-startTime) + " ms to read Mp3 stream\n" );
            System.out.flush();
        } finally {
            in.close();
        }
    }

    /**
     * For testing.
     */
    public static void main(String args[]) throws IOException
    {
        String debugStr = System.getProperty("debug");
        if (debugStr != null && debugStr.equalsIgnoreCase("true"))
        {
            DEBUG = true;
        }
        
        for (int j = 0; j < args.length; ++j) {
        	if (args[j].startsWith("http://")) {
        		printStreamInfo(args[j]);
        	}
        	else {
        		File file = new File(args[j]);
        		if (file.isDirectory()) {
        			String list[] = file.list();
        			Arrays.sort(list);                
        			for (int i = 0; i < list.length; i++) {
        				printFileInfo(new File(file, list[i]));
        			}
        		}
        		else
        		{
        			printFileInfo(file);
        		}
        	}
        }
    }
}
