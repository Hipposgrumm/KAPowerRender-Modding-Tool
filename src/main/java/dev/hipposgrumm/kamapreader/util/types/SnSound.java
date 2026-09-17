package dev.hipposgrumm.kamapreader.util.types;

import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.Exportable;

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

    public SnSound(String exportFileName, byte[] data) {
        this.exportFileName = exportFileName;
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
        return "ogg";
    }

    @Override
    public void writeExportData(FileOutputStream outputStream) throws IOException {
        outputStream.write(getData());
    }

    @Override
    public String toString() {
        return exportFileName;
    }
}
