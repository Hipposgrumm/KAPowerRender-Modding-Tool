package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.util.types.wrappers.SizeLimitedString;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UByte;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writer designed to assist in writing data back into KAR files.
 */
public class BlockWriter {
    private static final int INCREMENT = 0x8000;
    private final SharedBytearray file;
    private final SharedInt sharedFileSize;
    private int fileSize; // Mirrors sharedFileSize and used to check if it changed
    private boolean littleEndian = false;
    private int pointer;
    private boolean pointerAtEnd = true;
    private final int startpos;

    /// Creates a container for writing the file.
    public BlockWriter() {
        this.file = new SharedBytearray(new byte[INCREMENT]);
        this.sharedFileSize = new SharedInt(0);
        this.fileSize = 0;
        this.pointer = 0;
        this.startpos = 0;
    }

    /// @see #segment()
    private BlockWriter(SharedBytearray file, SharedInt size, int startpos, boolean littleEndian) {
        this.file = file;
        this.sharedFileSize = size;
        this.fileSize = size.value;
        this.littleEndian = littleEndian;
        this.startpos = startpos;
        this.pointer = startpos;
    }

    /// Write all byte data to a file.
    public void writeout(File destination) throws IOException {
        try (FileOutputStream out = new FileOutputStream(destination)) {
            out.write(file.data, 0, fileSize);
        }
    }

    /// @return Whether the writer is currently in little endian mode.
    public boolean isLittleEndian() {
        return littleEndian;
    }

    /// Set whether the writer is using little endian.
    public void setLittleEndian(boolean littleEndian) {
        this.littleEndian = littleEndian;
    }

    /**
     * Set the file read pointer relative to this writer's start position.
     * @param pos Position to go to, relative to start position.
     * @throws IndexOutOfBoundsException If the seek index is negative or outside the file.
     */
    public void seek(int pos) {
        getUpdateSharedFileSize();
        int newPos = startpos+pos;
        if (pos < 0) throw new IndexOutOfBoundsException(String.format("Seek is outside block (%s < %s)", newPos, startpos));
        if (pos > fileSize) throw new IndexOutOfBoundsException(String.format("Seek location is beyond current scope of file (%s > %s)", newPos, fileSize));
        pointer = newPos;
        pointerAtEnd = pointer == fileSize;
    }

    /**
     * Move the file pointer by an offset.
     * @apiNote If the pointer is at the end it will automatically increment if
     * @param amount Amount to move; can be positive or negative.
     * @throws IndexOutOfBoundsException If the resulting location would be out of range of the writer or the file.
     */
    public void move(int amount) {
        getUpdateSharedFileSize();
        int pos = pointer+amount;
        if (pos < startpos) throw new IndexOutOfBoundsException(String.format("Shift location is outside designated area (%s < %s)", pos, startpos));
        if (pos > fileSize) throw new IndexOutOfBoundsException(String.format("Shift location goes beyond current file length (%s, > %s)", pos, fileSize));
        pointer = pos;
        pointerAtEnd = pointer == fileSize;
    }

    /// @return Current pointer location relative to this writer's start position.
    public int getPointer() {
        getUpdateSharedFileSize();
        return pointer-startpos;
    }

    /// @return Pointer current position in entire file.
    public int getTruePointer() {
        getUpdateSharedFileSize();
        return pointer;
    }

    /**
     * Current size of the data in the writer.
     * @apiNote This size is NOT the maximum size, as data can be appended to the file.
     * @return Size of Data
     */
    public int getSize() {
        getUpdateSharedFileSize();
        return fileSize-startpos;
    }

    /// Ensure that the file size is accurate in case it was updated by any sub-writers.
    private void getUpdateSharedFileSize() {
        if (sharedFileSize.value == fileSize) return;
        fileSize = sharedFileSize.value;
        if (pointerAtEnd) pointer = fileSize;
    }

    /// Update the file size based on the pointer.
    private void doUpdateSharedFileSize() {
        if (pointer <= fileSize) return;
        fileSize = pointer;
        sharedFileSize.value = fileSize;
    }

    /// Ensures that there is the requested amount of usable bytes in the array from the pointer.
    private void ensureSize(int count) {
        getUpdateSharedFileSize();
        if (count == 0) return;

        int neededSize = pointer+count;
        int currentSize = file.data.length;
        if (currentSize >= neededSize) return;

        while (currentSize < neededSize) currentSize += INCREMENT;
        byte[] newArray = new byte[currentSize];
        System.arraycopy(file.data, 0, newArray, 0, fileSize);
        file.data = newArray;
    }

    /**
     * Write an array of bytes.
     * @param bytes Bytes
     */
    public void writeBytes(byte[] bytes) {
        ensureSize(bytes.length);
        System.arraycopy(bytes, 0, file.data, pointer, bytes.length);
        pointer += bytes.length;
        doUpdateSharedFileSize();
    }

    /**
     * Write a byte.
     * @param b Byte
     */
    public void writeByte(byte b) {
        ensureSize(1);
        file.data[pointer++] = b;
        doUpdateSharedFileSize();
    }

    /**
     * Write an unsigned byte.
     * @param b UByte
     */
    public void writeUByte(UByte b) {
        writeByte(b.getByte());
    }

    /**
     * Write a short.
     * @param s Short
     */
    public void writeShort(short s) {
        if (littleEndian) writeShortLittle(s);
        else writeShortBig(s);
    }

    /**
     * Writes an unsigned short.
     * @param s UShort
     */
    public void writeUShort(UShort s) {
        writeShort(s.getShort());
    }

    /**
     * Writes an integer.
     * @param i Integer
     */
    public void writeInt(int i) {
        if (littleEndian) writeIntLittle(i);
        else writeIntBig(i);
    }

    /**
     * Writes an unsigned integer.
     * @param i UInteger
     */
    public void writeUInt(UInteger i) {
        writeInt(i.getInt());
    }

    /**
     * Writes a float.
     * @param f Float
     */
    public void writeFloat(float f) {
        writeInt(Float.floatToIntBits(f));
    }

    /**
     * Write a short, always in little endian.
     * @param s Short (Signed)
     */
    public void writeShortLittle(short s) {
        ensureSize(2);
        file.data[pointer++] = (byte) (s & 0xFF);
        file.data[pointer++] = (byte) (s >> 8);
        doUpdateSharedFileSize();
    }

    /**
     * Writes an unsigned short, always in little endian.
     * @param s Short (Unsigned)
     */
    public void writeUShortLittle(UShort s) {
        writeShortLittle(s.getShort());
    }

    /**
     * Writes an integer, always in little endian.
     * @param i Integer (Signed)
     */
    public void writeIntLittle(int i) {
        ensureSize(4);
        file.data[pointer++] = (byte) (i & 0xFF);
        file.data[pointer++] = (byte) ((i >> 8) & 0xFF);
        file.data[pointer++] = (byte) ((i >> 16) & 0xFF);
        file.data[pointer++] = (byte) (i >> 24);
        doUpdateSharedFileSize();
    }

    /**
     * Writes an unsigned integer, always in little endian.
     * @param i Integer (Unsigned)
     */
    public void writeUIntLittle(UInteger i) {
        writeIntLittle(i.getInt());
    }

    /**
     * Writes a float, always in little endian.
     * @param f Float
     */
    public void writeFloatLittle(float f) {
        writeIntLittle(Float.floatToIntBits(f));
    }

    /**
     * Write a short, always in big endian.
     * @param s Short (Signed)
     */
    public void writeShortBig(short s) {
        ensureSize(2);
        file.data[pointer++] = (byte) (s >> 8);
        file.data[pointer++] = (byte) (s & 0xFF);
        doUpdateSharedFileSize();
    }

    /**
     * Writes an unsigned short, always in big endian.
     * @param s Short (Unsigned)
     */
    public void writeUShortBig(UShort s) {
        writeShortBig(s.getShort());
    }

    /**
     * Writes an integer, always in big endian.
     * @param i Integer (Signed)
     */
    public void writeIntBig(int i) {
        ensureSize(4);
        file.data[pointer++] = (byte) (i >> 24);
        file.data[pointer++] = (byte) ((i >> 16) & 0xFF);
        file.data[pointer++] = (byte) ((i >> 8) & 0xFF);
        file.data[pointer++] = (byte) (i & 0xFF);
        doUpdateSharedFileSize();
    }

    /**
     * Writes an unsigned integer, always in big endian.
     * @param i Integer (Unsigned)
     */
    public void writeUIntBig(UInteger i) {
        writeIntBig(i.getInt());
    }

    /**
     * Writes a float, always in big endian.
     * @param f Float
     */
    public void writeFloatBig(float f) {
        writeIntBig(Float.floatToIntBits(f));
    }

    /**
     * Write string as bytes.
     * @param str Bytes as string.
     */
    public void writeRawString(String str) {
        writeBytes(str.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Write a string with a terminating character (NUL) at the end.
     * @param str String
     */
    public void writeTerminatedString(String str) {
        writeRawString(str);
        writeByte((byte)'\0');
    }

    /**
     * Write a string with a terminating character (NUL) at the end, but with a specific size anyway.
     * @param str String
     * @throws IndexOutOfBoundsException If the string is longer than the allotted space.
     */
    public void writeTerminatedStringFixed(SizeLimitedString str) {
        writeRawString(str.toString());
        int stringLen = str.toString().length();
        int extraBytes = (str.getSize()-stringLen)+1;
        writeBytes(new byte[extraBytes]);
    }

    /**
     * Write a string prefixed with the size.
     * @param str String
     */
    public void writeSizedString(String str) {
        writeInt(str.length());
        writeRawString(str);
    }

    /**
     * Write a string prefixed with the size always in little endian.
     * @param str String
     */
    public void writeSizedStringLittle(String str) {
        writeIntLittle(str.length());
        writeRawString(str);
    }

    /// Create a sub-writer based on this writer.
    public BlockWriter segment() {
        getUpdateSharedFileSize();
        return new BlockWriter(file, sharedFileSize, pointer, littleEndian);
    }

    private static final class SharedInt {
        public int value;

        public SharedInt(int i) {
            this.value = i;
        }
    }

    private static final class SharedBytearray {
        public byte[] data;

        public SharedBytearray(byte[] start) {
            this.data = start;
        }
    }
}
