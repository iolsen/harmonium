//////////////////////////////////////////////////////////////////////
//
// File: SimMp3Stream.java
//
// Copyright (c) 2003-2004 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

package com.tivo.hme.sim;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import javazoom.jlme.decoder.BitStream;
import javazoom.jlme.decoder.Decoder;
import javazoom.jlme.decoder.Header;
import javazoom.jlme.decoder.SampleBuffer;

import com.tivo.hme.host.http.client.HttpClient;
import com.tivo.hme.interfaces.IHmeConstants;
import com.tivo.hme.sdk.io.FastInputStream;

/**
 * An mp3 stream that plays using jlme and the java sound API.
 *
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
class SimMp3Stream extends SimStreamResource
{
    SourceDataLine line;

    int nframes;
    long duration;
    
    public SimMp3Stream(SimApp app, int id, String url, boolean play)
    {
        super(app, id, url, play);
    }

    void handle(HttpClient http) throws IOException
    {
        FastInputStream in = getInputStream(http);

        //
        // setup decoder and read the first frame
        //
        
        BitStream bitstream = new BitStream(in);
        Header header = bitstream.readFrame();
        Decoder decoder = new Decoder(header, bitstream);
        SampleBuffer sample = (SampleBuffer)decoder.decodeFrame();

        //
        // figure out the duration. If TIVO_DURATION is present in the http
        // header, obey it. Otherwise use content-length and bitrate to estimate
        // the number of frames (and therefore the duration).

        duration = http.getHeaders().getLong(IHmeConstants.TIVO_DURATION, -1);
        if (duration == -1) {
            long available = http.getHeaders().getInt("content-length", -1);
            available -= in.getCount() - Header.framesize;
            duration = framesToMS(available / Header.framesize);

            // calculate a better number from the bitrate, if possible
            String bitstr = header.bitrate_string();
            int space = bitstr.indexOf(' ');
            if (space != -1) {
                try {
                    int bitrate = Integer.parseInt(bitstr.substring(0, space));
                    duration = (available * 8L) / bitrate;
                } catch (NumberFormatException e) {
                }
            }
        }
        
        //
        // create audio line
        //

        AudioFormat format = new AudioFormat(decoder.getOutputFrequency(), 16,
                                             decoder.getOutputChannels(),
                                             true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("the sound format cannot be played");
            return;
        }

        if (Simulator.SOUND) {
            try {
                line = (SourceDataLine)AudioSystem.getLine(info);
                line.open(format);
                line.start();
            } catch (LineUnavailableException e) {
                Simulator.SOUND = false;
                e.printStackTrace();
                return;
            }
        }
        
        //
        // and play
        //

        setStatus(RSRC_STATUS_READY);
        if (play) {
            speed = 1;
            status = RSRC_STATUS_PLAYING;
        }

        long tm0 = System.currentTimeMillis();
        while (status < RSRC_STATUS_CLOSED) {
            if (speed == 0) {

                //
                // paused - wait for unpause
                //
                
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    return;
                }
            } else if (sample.size() == 0) {

                //
                // done!
                //
                
                close(RSRC_STATUS_COMPLETE);
            } else {

                //
                // play a frame
                //
                
                ++nframes;
                
                if (line != null) {
                    line.write(sample.getBuffer(), 0, sample.size());
                } else {
                    // hm. no sound, sleep to simulate playing the mp3
                    long sleep = tm0 + getPosition() - System.currentTimeMillis();
                    if (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }

                //
                // read another frame
                //
                
                bitstream.closeFrame();
                try {
                    header = bitstream.readFrame();
                    sample = (SampleBuffer)decoder.decodeFrame();
                } catch (RuntimeException e) {
                    if (status < RSRC_STATUS_CLOSED) {
                        error(RSRC_ERROR_BAD_DATA, "while decoding mp3");
                    }
                    break;
                }
            }
        }

        //
        // close audio line.
        //
        
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
            line = null;
        }

        synchronized (this) {
            notify();
        }
    }

    /**
     * Overridden to use ReadFullyStream instead of the normal stream.
     */
    protected FastInputStream getInputStream(HttpClient http) throws IOException
    {
        return new ReadFullyStream(http.getInputStream(), 4096);
    }

    //
    // SimStreamResource overriddes
    //

    synchronized void setSpeed(float speed)
    {
        super.setSpeed(speed);
        notify();
    }

    void setStatus(int status)
    {
        super.setStatus(status);
        // give playback a chance to close before the superclass forces it shut
        // by calling http.close
        if (status >= RSRC_STATUS_CLOSED && line != null) {
            try {
                synchronized (this) {
                    wait(200);
                }
            } catch (InterruptedException e) {
            }
        }
    }
    
    protected long getPosition()
    {
        return framesToMS(nframes);
    }

    protected long getDuration()
    {
        return duration;
    }

    String getIcon()
    {
        return "sound.png";
    }

    /**
     * Helper for converting nframes to duration. mp3 is approx 26.122 ms per
     * frame (1152 samples per frame / 44100 Hz).
     */
    static long framesToMS(long nframes)
    {
        // 
        return nframes * 1152 * 1000 / 44100;
    }

    /**
     * This works around bugs inside jlme. It expects that read works like
     * readFully!
     */
    class ReadFullyStream extends FastInputStream
    {
        ReadFullyStream(InputStream in, int len)
        {
            super(in, len);
        }
        
        public int read(byte buf[], int off, int len) throws IOException
        {
            int total = 0;
            while (total < len) {
                int n = super.read(buf, off + total, len - total);
                if (n < 0) {
                    break;
                }
                total += n;
            }
            return total;
        }
    }
}

