package dev.hipposgrumm.kamapreader.blocks;

import dev.hipposgrumm.kamapreader.FirstThing;
import dev.hipposgrumm.kamapreader.reader.BlockReader;
import dev.hipposgrumm.kamapreader.reader.BlockWriter;
import dev.hipposgrumm.kamapreader.reader.KARFile;
import dev.hipposgrumm.kamapreader.util.DatingBachelor;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileEntry;
import dev.hipposgrumm.kamapreader.util.control.DatingProfileValue;
import dev.hipposgrumm.kamapreader.util.types.Version;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public abstract class Block implements DatingBachelor {
    private ResourceCheck resourceCheck = null;
    public Version resourceVersion = null;
    // This is some value that affects how something is stored.
    // Depending on the value, and where in loading the block is loaded from,
    //     it will nest the value of something in a pointer into that same spot.
    //     I don't really know what it's doing. I may try to see patterns in
    //     what values are used where or poke it to see what makes the game crash.
    protected UnknownResourceStorageByte unknownResourceStorageByte = null;

    private boolean useArrayCheck = false;
    private int arckUnknown1;
    private short arckUnknown2;

    private boolean successful = false;
    protected boolean littleEndian;

    public final void readFull(BlockReader reader, KARFile.ProgressUpdater progress) {
        int blockpos = reader.getTruePointer();
        try {
            littleEndian = reader.isLittleEndian();
            {
                if (reader.readBlockType().equals("RsCk")) {
                    resourceCheck = new ResourceCheck(reader);
                    if (reader.readBlockType().equals("ArCk")) {
                        useArrayCheck = true;
                        int arrayDataSize = reader.readInt()+8;
                        reader.move(-8);
                        int dataOffset = (unknownResourceStorageByte.encapsulateFullBlock()
                                ? 0
                                : reader.getTruePointer()
                        ) - reader.getPointer();
                        BlockReader arrayDataReader = reader.segment(arrayDataSize);
                        arrayDataReader.move(8);
                        if (resourceVersion.major() >= 1) { // This is an assumption, there's no real way of knowing for sure.
                            arckUnknown1 = arrayDataReader.readInt();
                        }
                        arckUnknown2 = arrayDataReader.readShort();
                        if (arckUnknown2 != 4) System.out.println("NOTICE: Mystery value 2 in ArCk is different from expected. Please notify developer with the file you opened.");
                        int count = arrayDataReader.readUShortDirect();
                        progress.updateWith(reader);
                        readFromArrayBegin(count, progress);
                        for (int i=0;i<count;i++) {
                            int offset = arrayDataReader.readInt();
                            if (offset >= 0) {
                                offset -= dataOffset;
                                reader.seek(offset);
                                progress.updateWith(reader);
                                readPartFromArray(reader.segment(reader.getRemaining()), i, progress);
                            } else readPlaceholderFromArray(i);
                        }
                        readFromArrayEnd(progress);
                        progress.updateWith(reader);
                    } else {
                        reader.move(-4);
                        readWithAll(reader, progress);
                    }
                } else {
                    reader.move(-4);
                    readWithAll(reader, progress);
                }
            }
            successful = true;
        } catch (Exception e) {
            // TODO: Maybe the block needs some indicator that something went wrong.
            System.err.printf("Error reading %s at 0x%02X:%n", getBlockType(), blockpos);
            e.printStackTrace();
        }
    }

    public final void writeFull(BlockWriter writer, KARFile.ProgressUpdater progress) {
        resourceCheck.writeStart(writer);
        if (useArrayCheck) {
            int dataStart = unknownResourceStorageByte.encapsulateFullBlock()
                    ? writer.getTruePointer()
                    : 0;
            BlockWriter arrayDataWriter = writer.segment();
            arrayDataWriter.writeRawString("ArCk");

            int count = writeForArrayBegin(progress);
            if (count > 0xFFFF) throw new IndexOutOfBoundsException("Size is larger than supported value.");
            boolean hasUnknown1 = resourceVersion.major() >= 1; // This is an assumption, there's no real way of knowing for sure.
            arrayDataWriter.writeInt(
                    (count*4) +
                    (hasUnknown1 ? 8 : 4)
            );

            if (hasUnknown1) {
                arrayDataWriter.writeInt(arckUnknown1);
            }
            arrayDataWriter.writeShort(arckUnknown2);
            arrayDataWriter.writeUShortLittleDirect(count);

            byte[] fillerArrayData = new byte[count*4];
            arrayDataWriter.writeBytes(fillerArrayData);
            arrayDataWriter.move(-fillerArrayData.length);
            if (!unknownResourceStorageByte.encapsulateFullBlock()) resourceCheck.writeFinish(writer);
            for (int i=0;i<count;i++) {
                int pointer = writer.getTruePointer();
                boolean wroteData = writePartForArray(writer.segment(), i, progress);
                arrayDataWriter.writeInt(wroteData
                        ? pointer - dataStart
                        : -1
                );
            }
            writeForArrayEnd(writer.segment(), progress);
            if (unknownResourceStorageByte.encapsulateFullBlock()) resourceCheck.writeFinish(writer);
        } else if (unknownResourceStorageByte.encapsulateFullBlock()) {
            writeWithAll(writer, progress);
            resourceCheck.writeFinish(writer);
        } else {
            resourceCheck.writeFinish(writer);
            writeWithAll(writer, progress);
        }
    }

    public final boolean successfullyRead() {
        return successful;
    }

    public final boolean isLittleEndian() {
        return littleEndian;
    }

    /**
     * Read all data from block at once.
     * @param reader Reader
     * @param progress ProgressUpdater
     * @implNote Only called in absence of ArrayCheck.
     */
    protected abstract void readWithAll(BlockReader reader, KARFile.ProgressUpdater progress);

    /**
     * Called before reading data using ArrayCheck.
     * Use this to initialize arrays to receive data.
     * @param count Number of elements to expect.
     * @param progress ProgressUpdater
     * @implNote Only called when ArrayCheck is present.
     */
    protected void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {}

    /**
     * Read a non-placeholder entry in ArrayCheck.
     * @param reader Reader
     * @param index Index of Entry
     * @param progress ProgressUpdater
     * @implNote Only called when ArrayCheck is present.
     */
    protected abstract void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress);

    /**
     * Called when a placeholder entry is found in ArrayCheck.
     * @param index Index of Entry
     * @implNote Only called when ArrayCheck is present.
     */
    protected abstract void readPlaceholderFromArray(int index);

    /**
     * Called after all data has been read by ArrayCheck.
     * Use this to finalize array data into lists that can be modified by the program.
     * @param progress ProgressUpdater
     * @implNote Only called when ArrayCheck is present.
     */
    protected void readFromArrayEnd(KARFile.ProgressUpdater progress) {}

    /**
     * Write all data to block at once.
     * @param writer Writer
     * @param progress ProgressUpdater
     * @implNote Only called in absence of ArrayCheck.
     */
    protected abstract void writeWithAll(BlockWriter writer, KARFile.ProgressUpdater progress);

    /**
     * Called before writing data into ArrayCheck.
     * @param progress ProgressUpdater
     * @return This should return the number of elements that will be written.
     * @implNote Only called when ArrayCheck is present.
     */
    protected abstract int writeForArrayBegin(KARFile.ProgressUpdater progress);

    /**
     * Process an entry in ArrayCheck.
     * @param writer Writer
     * @param index Index of Entry
     * @param progress ProgressUpdater
     * @return True if any data was written. Should return false if it's a placeholder.
     * @implNote Only called when ArrayCheck is present.
     */
    protected abstract boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress);

    /**
     * Called after all data has been written into ArrayCheck.
     * This is generally to add padding if it is necessary.
     * @param progress ProgressUpdater
     * @implNote Only called when ArrayCheck is present.
     */
    protected void writeForArrayEnd(BlockWriter writer, KARFile.ProgressUpdater progress) {}

    public abstract String getBlockType();

    public abstract String toString();

    protected final DatingProfileEntry getMetadataDatingProfileEntry() {
        return (resourceCheck != null)
                ? new DatingProfileEntry("Metadata", resourceCheck)
                : DatingProfileEntry.simple("Metadata", (Object)null);
    }

    public static abstract class Singular extends Block {
        protected final void readFromArrayBegin(int count, KARFile.ProgressUpdater progress) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be read as an array.");
        }

        protected final void readPartFromArray(BlockReader reader, int index, KARFile.ProgressUpdater progress) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be read as an array.");
        }

        protected final void readPlaceholderFromArray(int index) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be read as an array.");
        }

        protected final void readFromArrayEnd(KARFile.ProgressUpdater progress) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be written as an array.");
        }

        protected final int writeForArrayBegin(KARFile.ProgressUpdater progress) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be written as an array.");
        }

        protected final boolean writePartForArray(BlockWriter writer, int index, KARFile.ProgressUpdater progress) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be written as an array.");
        }

        protected final void writeForArrayEnd(BlockWriter writer, KARFile.ProgressUpdater progress) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be written as an array.");
        }
    }

    public static abstract class ArrayChecked extends Block {
        protected final void readWithAll(BlockReader reader, KARFile.ProgressUpdater progress) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be read all at once.");
        }

        protected final void writeWithAll(BlockWriter writer, KARFile.ProgressUpdater progress) {
            throw new BlockDataProcessingMethodException("Block type "+getBlockType()+" is not supposed to be written all at once.");
        }
    }

    private static final class BlockDataProcessingMethodException extends RuntimeException {
        public BlockDataProcessingMethodException(String message) {
            super(message);
        }
    }

    protected record UnknownResourceStorageByte(byte b) {
        protected UnknownResourceStorageByte(byte b) {
            this.b = b;
            switch (b) {
                case 0,1,2,4 -> {}
                default -> System.err.println("WARN: Unknown storage byte type: "+b);
            }
        }

        public boolean encapsulateFullBlock() {
            return switch (b) {
                case 0,1 -> true;
                case 2,4 -> false;
                default -> {
                    System.out.println("WARN: Unknown value for storage byte: "+b);
                    yield true;
                }
            };
        }

        @Override
        public String toString() {
            return String.valueOf(b);
        }
    }

    private class ResourceCheck implements DatingProfileValue {
        private ResourceCheck(BlockReader reader) {
            reader = reader.segment(reader.readInt());

            int version = reader.readInt();
            Block.this.resourceVersion = new Version(
                    (version & 0xF0000000) >> 28,
                    (version & 0x0000FF00) >>  8,
                    (version & 0x000000FF)
            );

            Block.this.unknownResourceStorageByte = new UnknownResourceStorageByte((byte) (reader.readInt() & 0xF));

        }

        public ResourceCheck(
                int major, int minor, int patch,
                byte storage
        ) {
            if ((major & 0x0F) != major) throw new IllegalArgumentException(String.format("Major version exceeds highest possible value (%s > %s)", major, 0x0F));
            if ((minor & 0xFF) != minor) throw new IllegalArgumentException(String.format("Minor version exceeds highest possible value (%s > %s)", minor, 0xFF));
            if ((patch % 0xFF) != patch) throw new IllegalArgumentException(String.format("Patch version exceeds highest possible value (%s > %s)", patch, 0xFF));
            if ((storage % 0xF) != storage) throw new IllegalArgumentException(String.format("Storage byte exceeds highest possible value (%s > %s)", storage, 0xF));

            Block.this.resourceVersion = new Version(major, minor, patch);
            Block.this.unknownResourceStorageByte = new UnknownResourceStorageByte(storage);
        }

        private BlockWriter writer;
        private void writeStart(BlockWriter writer) {
            if (this.writer != null) System.out.println("WARNING: Writer for RsCk never finished before starting another (somehow).");
            this.writer = writer.segment();
            this.writer.writeRawString("RsCk");
            this.writer.writeInt(0xC); // size of self; always going to be this

            this.writer.writeInt(
                    (Block.this.resourceVersion.major() << 28) |
                    (Block.this.resourceVersion.minor() << 8) |
                    Block.this.resourceVersion.patch());
            this.writer.writeInt(Block.this.unknownResourceStorageByte.b());

            this.writer.writeInt(-1); // placeholder for size
            this.writer.disconnectPointer(); // now wait dormant for finish
        }

        private void writeFinish(BlockWriter endingWriterRef) {
            if (this.writer == null) throw new IllegalStateException("Writing for RsCk was never started.");
            // remember we're still at the end of RsCk on this writer
            int size = endingWriterRef.getTruePointer() - this.writer.getTruePointer();
            this.writer.move(-4);
            this.writer.writeInt(size);
            this.writer = null; // finish up, not that it should matter
        }

        @Override
        public Node createDisplay(FirstThing controller, Runnable onChanged, boolean readonly) {
            GridPane gridPane = new GridPane();
            gridPane.setVgap(5);
            gridPane.setHgap(5);

            Label key1 = new Label("Version: ");
            Label val1 = new Label(Block.this.resourceVersion.toString());
            Label key2 = new Label("StorageByte: ");
            Label val2 = new Label(String.valueOf(Block.this.unknownResourceStorageByte));

            gridPane.add(key1, 0, 0);
            gridPane.add(val1, 1, 0);
            gridPane.add(key2, 0, 1);
            gridPane.add(val2, 1, 1);

            return gridPane;
        }

        @Override
        public boolean isModified() {
            return false;
        }
    }
}
