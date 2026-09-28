package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.Exportable;
import dev.hipposgrumm.kamapreader.util.control.display.SoundDisplay;
import javazoom.spi.vorbis.sampled.convert.DecodedVorbisAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SnSound implements DatingBachelor, Exportable {
    public byte[] UNKNOWN1;
    public int UNKNOWN2;
    private byte[] DATA;

    public byte[] DATA_DECODED;
    public AudioFormat FORMAT;

    public final String exportFileName;
    private String fileExtension;

    public SnSound(String exportFileName, byte[] data) {
        this.exportFileName = exportFileName;
        setData(data);
    }

    public byte[] getData() {
        return DATA;
    }

    public void setData(byte[] data) {
        DATA = data;
        try {
            fileExtension = AudioSystem.getAudioFileFormat(new ByteArrayInputStream(data)).getType().getExtension();
        } catch (Exception e) {
            fileExtension = "unknown_please_report";
            e.printStackTrace();
        }
        // EPresident on StackOverflow
        // https://stackoverflow.com/q/33110772/20170780
        try {
            VorbisSPIWorkaroundStream stream = new VorbisSPIWorkaroundStream(data);
            try (AudioInputStream baseStream = AudioSystem.getAudioInputStream(stream)) {
                AudioFormat baseFormat = baseStream.getFormat();
                float sampleRate = baseFormat.getSampleRate();
                int channels = baseFormat.getChannels();
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        sampleRate, 16, channels, channels * 2, sampleRate, false
                );
                try (AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, baseStream)) {
                    if (decodedStream instanceof DecodedVorbisAudioInputStream) // Only apply workaround for the VorbisSPI one, since it can cause an infinite loop otherwise.
                        stream.applyWorkaround = data.length < 0x1800; // size of data read for header
                    DATA_DECODED = decodedStream.readAllBytes();
                }
                FORMAT = decodedFormat;
            }
        } catch (Exception ignored) {}
    }

    @Override
    public List<? extends DatingProfileEntry> getDatingProfile() {
        return List.of(new DatingProfileEntry("Clip", false, new SoundDisplay(this)));
    }

    public void copyFrom(SnSound snd) {
        //UNKNOWN1 = snd.UNKNOWN1;
        //UNKNOWN2 = snd.UNKNOWN2;
        setData(snd.DATA);
    }

    @Override
    public ContextMenuOption[] getContextMenu() {
        return new ContextMenuOption[] {
                new ContextMenuOption("Export", Exportable::massExport)
        };
    }

    @Override
    public String getFileName() {
        return exportFileName;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public void writeExportData(FileOutputStream outputStream) throws IOException {
        outputStream.write(getData());
    }

    @Override
    public String toString() {
        return exportFileName+"."+fileExtension;
    }



    /// There is a bug in VorbisSPI where if a file is small enough, it won't properly
    ///     process any of the audio data. This is because it exhausts the inputted buffer
    ///     reading the header.
    /// This workaround will effectively choke/bottleneck the reader so that it will
    ///     read the data in smaller chunks. This increases the likelihood of
    ///     there being any data left for it to read when it comes time to read
    ///     the actual audio data.
    private static class VorbisSPIWorkaroundStream extends ByteArrayInputStream {
        private static final int PRECISION = 1 << 3;

        public boolean applyWorkaround = false;

        public VorbisSPIWorkaroundStream(byte[] buf) {
            super(buf);
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) {
            if (applyWorkaround) len /= PRECISION;
            return super.read(b, off, len);
        }
    }
}
