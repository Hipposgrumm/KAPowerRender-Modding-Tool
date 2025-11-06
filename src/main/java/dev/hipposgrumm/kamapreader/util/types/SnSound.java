package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.util.List;

public class SnSound implements DatingBachelor {
    public byte[] UNKNOWN1;
    public int UNKNOWN2;
    private byte[] DATA;

    public byte[] DATA_DECODED;
    public AudioFormat FORMAT;

    public SnSound(byte[] data) {
        setData(data);
    }

    public byte[] getData() {
        return DATA;
    }

    public void setData(byte[] data) {
        DATA = data;
        // EPresident on StackOverflow
        // https://stackoverflow.com/q/33110772/20170780
        try {
            try (AudioInputStream baseStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(data))) {
                AudioFormat baseFormat = baseStream.getFormat();
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false);
                try (AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, baseStream)) {
                    DATA_DECODED = decodedStream.readAllBytes();
                }
                FORMAT = decodedFormat;
            }
        } catch (Exception ignored) {}
    }

    @Override
    public List<? extends DatingProfileEntry<?>> getDatingProfile() {
        return List.of(new DatingProfileEntry<>("Clip",
                () -> this,
                snd -> {
            //UNKNOWN1 = snd.UNKNOWN1;
            //UNKNOWN2 = snd.UNKNOWN2;
            setData(snd.DATA);
        }));
    }

    @Override
    public String toString() {
        return "sound";
    }
}
