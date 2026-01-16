package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.blocks.Block;
import dev.hipposgrumm.kamapreader.util.types.wrappers.SizeLimitedString;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UByte;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Writer designed to assist in writing data back into KAR files.
 */
public class BlockWriter {
    private final RandomAccessFile writer;
    private boolean littleEndian;
    private final long startpos;

    /**
     * Creates a container for writing the file.
     * @param writer File to write data to.
     * @param useLittleEndian Whether the writer starts in little endian.
     * @throws IOException If there is an IO error.
     */
    public BlockWriter(RandomAccessFile writer, boolean useLittleEndian) throws IOException {
        this.writer = writer;
        this.littleEndian = useLittleEndian;
        this.startpos = writer.getFilePointer();
    }

    /**
     * @return Whether the writer is currently in little endian mode.
     */
    public boolean isLittleEndian() {
        return littleEndian;
    }

    /**
     * Set whether the writer is using little endian.
     */
    public void setLittleEndian(boolean littleEndian) {
        this.littleEndian = littleEndian;
    }

    /**
     * Set the file read pointer relative to this writer's start position.
     * @param pos Position to go to, relative to start position.
     * @throws IOException If there is an IO error.
     * @throws IndexOutOfBoundsException If the seek index is negative or outside of the file.
     */
    public void seek(int pos) throws IOException {
        if (pos < 0) throw new IndexOutOfBoundsException("Seek location cannot be negative.");
        long newPos = startpos+pos;
        if (newPos > writer.length()) throw new IndexOutOfBoundsException("Seek location is beyond current scope of file.");
        this.writer.seek(newPos);
    }

    /**
     * Move the file pointer by an offset.
     * @param amount Amount to move; can be positive or negative.
     * @throws IOException If there is an IO error.
     * @throws IndexOutOfBoundsException If the resulting location would be out of range of the writer or the file.
     */
    public void move(int amount) throws IOException {
        long pos = this.writer.getFilePointer()+amount;
        if (pos < startpos) throw new IndexOutOfBoundsException("Shift location is outside designated area.");
        if (pos > writer.length()) throw new IndexOutOfBoundsException("Shift location goes beyond current file length.");
        this.writer.seek(pos);
    }

    /**
     * @return Current pointer location relative to this writer's start position.
     * @throws IOException If there is an IO error.
     */
    public int getPointer() throws IOException {
        return (int) (writer.getFilePointer()-startpos);
    }

    /**
     * @return Pointer current position in entire file.
     * @throws IOException If there is an IO error.
     */
    public int getTruePointer() throws IOException {
        return (int) writer.getFilePointer();
    }

    /**
     * Current size of the data in the writer.
     * @implNote This size is NOT the maximum size, as data can be appended to the file.
     * @return Size of Data
     * @throws IOException If there is an IO error.
     */
    public int getSize() throws IOException {
        return (int) (writer.length()-startpos);
    }

    /**
     * Write an array of bytes.
     * @param bytes Bytes
     * @throws IOException If there is a write error.
     */
    public void writeBytes(byte[] bytes) throws IOException {
        writer.write(bytes);
    }

    /**
     * Write a byte.
     * @param b Byte
     * @throws IOException If there is a write error.
     */
    public void writeByte(byte b) throws IOException {
        writer.writeByte(b);
    }

    /**
     * Write an unsigned byte.
     * @param b UByte
     * @throws IOException If there is a write error.
     */
    public void writeUByte(UByte b) throws IOException {
        writeByte(b.getByte());
    }

    /**
     * Write a short.
     * @param s Short
     * @throws IOException If there is a write error.
     */
    public void writeShort(short s) throws IOException {
        if (littleEndian) s = Short.reverseBytes(s);
        writer.writeShort(s);
    }

    /**
     * Writes an unsigned short.
     * @param s UShort
     * @throws IOException If there is a write error.
     */
    public void writeUShort(UShort s) throws IOException {
        writeShort(s.getShort());
    }

    /**
     * Writes an integer.
     * @param i Integer
     * @throws IOException If there is a write error.
     */
    public void writeInt(int i) throws IOException {
        if (littleEndian) i = Integer.reverseBytes(i);
        writer.writeInt(i);
    }

    /**
     * Writes an unsigned integer.
     * @param i UInteger
     * @throws IOException If there is a write error.
     */
    public void writeUInt(UInteger i) throws IOException {
        writeInt(i.getInt());
    }

    /**
     * Writes a float.
     * @param f Float
     * @throws IOException If there is a write error.
     */
    public void writeFloat(float f) throws IOException {
        writeInt(Float.floatToIntBits(f));
    }

    /**
     * Write a short, always in little endian.
     * @param s Short
     * @throws IOException If there is a write error.
     */
    public void writeShortLittle(short s) throws IOException {
        s = Short.reverseBytes(s);
        writer.writeShort(s);
    }

    /**
     * Writes an unsigned short, always in little endian.
     * @param s Short (Unsigned) -- This value will be casted to short.
     * @throws IOException If there is a write error.
     */
    public void writeUShortLittle(UShort s) throws IOException {
        writeShortLittle(s.getShort());
    }

    /**
     * Writes an integer, always in little endian.
     * @param i Integer
     * @throws IOException If there is a write error.
     */
    public void writeIntLittle(int i) throws IOException {
        i = Integer.reverseBytes(i);
        writer.writeInt(i);
    }

    /**
     * Writes an unsigned integer, always in little endian.
     * @param i Integer (Unsigned) -- This value will be casted to int.
     * @throws IOException If there is a write error.
     */
    public void writeUIntLittle(UInteger i) throws IOException {
        writeIntLittle(i.getInt());
    }

    /**
     * Writes a float, always in little endian.
     * @param f Float
     * @throws IOException If there is a write error.
     */
    public void writeFloatLittle(float f) throws IOException {
        writeIntLittle(Float.floatToIntBits(f));
    }

    /**
     * Write string as bytes.
     * @param str Bytes as string.
     * @throws IOException If there is a write error.
     */
    public void writeRawString(String str) throws IOException {
        writer.writeBytes(str);
    }

    /**
     * Write a string with a terminating character (NUL) at the end.
     * @param str String
     * @throws IOException If there is a write error.
     */
    public void writeTerminatedString(String str) throws IOException {
        writer.writeBytes(str);
        writer.writeByte('\0');
    }

    /**
     * Write a string with a terminating character (NUL) at the end, but with a specific size anyway.
     * @param str String
     * @throws IOException If there is a write error.
     * @throws IndexOutOfBoundsException If the string is longer than the allotted space.
     */
    public void writeTerminatedStringFixed(SizeLimitedString str) throws IOException {
        writer.writeBytes(str.toString());
        writeBytes(new byte[(str.getSize()-str.toString().length())+1]);
    }

    /**
     * Write a string prefixed with the size.
     * @param str String
     * @throws IOException If there is a write error.
     */
    public void writeSizedString(String str) throws IOException {
        writeInt(str.length());
        writer.writeBytes(str);
    }

    /**
     * Write a string prefixed with the size always in little endian.
     * @param str String
     * @throws IOException If there is a write error.
     */
    public void writeSizedStringLittle(String str) throws IOException {
        writeIntLittle(str.length());
        writer.writeBytes(str);
    }

    /**
     * Create a sub-writer based on this writer.
     * @throws IOException If creating the writer results in an IO error.
     */
    public BlockWriter segment() throws IOException {
        return new BlockWriter(writer, littleEndian);
    }

    /**
     * Writes an entire KAR block.
     * @param block Block to write.
     * @throws IOException If there is a write error.
     */
    public void writeBlock(Block block) throws IOException {
        BlockWriter subWriter = segment();
        if (littleEndian) subWriter.writeRawString("RIFF\0\0\0\0");
        else subWriter.writeRawString("FORM\0\0\0\0");
        subWriter.writeRawString(block.getBlockType());

        block.write(subWriter);

        subWriter.seek(4);
        subWriter.writeInt(subWriter.getSize()-8);
        subWriter.seek(subWriter.getSize());
    }
}
