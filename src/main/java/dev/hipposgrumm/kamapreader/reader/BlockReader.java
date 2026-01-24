package dev.hipposgrumm.kamapreader.reader;

import dev.hipposgrumm.kamapreader.util.types.wrappers.SizeLimitedString;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UByte;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UInteger;
import dev.hipposgrumm.kamapreader.util.types.wrappers.UShort;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * A safe reader designed to assist in reading the blocks
 * for KAR files, making it easy to switch between endianness
 * while avoiding difficult-to-debug overrun-related issues.
 */
public class BlockReader {
    private final byte[] file;
    private boolean littleEndian = false;
    private int pointer;
    private final int startpos;
    private final int endpos;
    private final int size;

    /**
     * Creates a safe container for reading the file.
     * @param file File to read data from.
     * @throws IOException If there is an IO error.
     * @throws IndexOutOfBoundsException If file not large enough for the allocated size.
     */
    public BlockReader(File file) throws IOException {
        this.file = Files.readAllBytes(file.toPath());
        this.size = this.file.length;
        this.pointer = 0;
        this.startpos = 0;
        this.endpos = size;
    }

    /// @see #segment(int)
    private BlockReader(byte[] file, boolean littleEndian, int startpos, int size) {
        this.file = file;
        this.littleEndian = littleEndian;
        this.size = size;
        this.pointer = startpos;
        this.startpos = startpos;
        this.endpos = startpos+size;
    }

    /// @return Whether the reader is currently in little endian mode.
    public boolean isLittleEndian() {
        return littleEndian;
    }

    /// Set whether the reader is using little endian.
    public void setLittleEndian(boolean littleEndian) {
        this.littleEndian = littleEndian;
    }

    /**
     * Set the file read pointer relative to this reader's start position.
     * @param pos Position to go to, relative to start position.
     * @throws IndexOutOfBoundsException If the seek index is negative or above the size of the block.
     */
    public void seek(int pos) {
        if (pos < 0) throw new IndexOutOfBoundsException(String.format("Seek location is outside block (%s < 0)", pos));
        if (pos > size) throw new IndexOutOfBoundsException(String.format("Seek location is outside block (%s > %s)", pos, size));
        this.pointer = pos+startpos;
    }

    /**
     * Move the file pointer by an offset.
     * @param amount Amount to move; can be positive or negative.
     * @throws IndexOutOfBoundsException If the resulting location would be out of range of the reader.
     */
    public void move(int amount) {
        int pos = pointer+amount;
        if (pos < startpos) throw new IndexOutOfBoundsException(String.format("Shift location moves outside block (%s < %s)", pos, startpos));
        if (pos > endpos) throw new IndexOutOfBoundsException(String.format("Shift location moves outside block (%s > %s)", pos, endpos));
        this.pointer = pos;
    }

    /// @return Current pointer location relative to this reader's start position.
    public int getPointer() {
        return pointer-startpos;
    }

    /// @return Pointer current position in entire file.
    public int getTruePointer() {
        return pointer;
    }

    /// @return Size of the allocated reader.
    public int getSize() {
        return size;
    }

    /// @return Amount of bytes left available to this reader.
    public int getRemaining() {
        return endpos-pointer;
    }

    /// Checks if there is enough bytes left to read the requested amount.
    private void assertSize(int count) throws IndexOutOfBoundsException {
        if (count == 0) return;
        if (pointer+count > endpos) throw new IndexOutOfBoundsException(String.format("Remaining bytes in block (%s) is less than required amount (%s). At 0x%08X", getRemaining(), count, pointer));
    }

    /**
     * Reads an array of bytes.
     * @param count Size of the array.
     * @return An array of bytes.
     * @throws IndexOutOfBoundsException If there are not enough bytes in the array to return a complete array.
     */
    public byte[] readBytes(int count) {
        assertSize(count);
        byte[] bytes = new byte[count];
        System.arraycopy(file, pointer, bytes, 0, count);
        pointer += count;
        return bytes;
    }

    /**
     * Reads a byte.
     * @return Byte (Signed)
     * @throws IndexOutOfBoundsException If there is not 1 byte of data to read.
     */
    public byte readByte() {
        assertSize(1);
        return file[pointer++];
    }

    /**
     * Reads an unsigned byte.
     * @return Byte (Unsigned)
     * @throws IndexOutOfBoundsException If there is not 1 byte of data to read.
     */
    public UByte readUByte() {
        return new UByte(readByte());
    }

    /**
     * Reads a short.
     * @return Short (Signed)
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public short readShort() {
        return littleEndian ?
                readShortLittle() :
                readShortBig();
    }

    /**
     * Reads an unsigned short.
     * @return Short (Unsigned)
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public UShort readUShort() {
        return new UShort(readShort());
    }

    /**
     * Reads an integer.
     * @return Integer (Signed)
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public int readInt() {
        return littleEndian ?
                readIntLittle() :
                readIntBig();
    }

    /**
     * Reads an unsigned integer.
     * @return Integer (Unsigned)
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public UInteger readUInt() {
        return new UInteger(readInt());
    }

    /**
     * Reads a float.
     * @return Float
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as float.
     */
    public float readFloat() {
        return littleEndian ?
                readFloatLittle() :
                readFloatBig();
    }

    /**
     * Reads a short, always in little endian.
     * @return Short (Signed)
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public short readShortLittle() {
        assertSize(2);
        return (short) (
                (file[pointer++] & 0xFF) |
                ((file[pointer++] & 0xFF) << 8)
        );
    }

    /**
     * Reads an unsigned short, always in little endian.
     * @return Short (Unsigned)
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public UShort readUShortLittle() {
        return new UShort(readShortLittle());
    }

    /**
     * Reads an integer, always in little endian.
     * @return Integer (Signed)
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public int readIntLittle() {
        assertSize(4);
        return (
                (file[pointer++] & 0xFF) |
                ((file[pointer++] & 0xFF) << 8) |
                ((file[pointer++] & 0xFF) << 16) |
                ((file[pointer++] & 0xFF) << 24)
        );
    }

    /**
     * Reads an unsigned integer, always in little endian.
     * @return Integer (Unsigned)
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public UInteger readUIntLittle() {
        return new UInteger(readIntLittle());
    }

    /**
     * Reads a float, always in little endian.
     * @return Float
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as float.
     */
    public float readFloatLittle() {
        return Float.intBitsToFloat(readIntLittle());
    }

    /**
     * Reads a short, always in big endian.
     * @return Short (Signed)
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public short readShortBig() {
        assertSize(2);
        return (short) (
                ((file[pointer++] & 0xFF) << 8) |
                (file[pointer++] & 0xFF)
        );
    }

    /**
     * Reads an unsigned short, always in big endian.
     * @return Short (Unsigned)
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public UShort readUShortBig() {
        return new UShort(readShortBig());
    }

    /**
     * Reads an integer, always in little endian.
     * @return Integer (Signed)
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public int readIntBig() {
        assertSize(4);
        return (((file[pointer++] & 0xFF) << 24) |
                ((file[pointer++] & 0xFF) << 16) |
                ((file[pointer++] & 0xFF) << 8) |
                (file[pointer++] & 0xFF)
        );
    }

    /**
     * Reads an unsigned integer, always in little endian.
     * @return Integer (Unsigned)
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public UInteger readUIntBig() {
        return new UInteger(readIntBig());
    }

    /**
     * Reads a float, always in little endian.
     * @return Float
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as float.
     */
    public float readFloatBig() {
        return Float.intBitsToFloat(readIntBig());
    }

    /**
     * Reads a string until it reaches an end character (NUL) or the end of the available data.
     * @return String
     */
    public String readString() {
        StringBuilder string = new StringBuilder();
        while (pointer < endpos) {
            char c = (char) file[pointer++];
            if (c=='\0') break;
            string.append(c);
        }
        return new String(string.toString().getBytes(), StandardCharsets.US_ASCII);
    }

    /**
     * Reads a string of a specified size.
     * @param size Size of the string.
     * @return String
     * @throws IndexOutOfBoundsException If there is not enough data to read a string of the requested size.
     */
    public String readString(int size) {
        assertSize(size);
        StringBuilder string = new StringBuilder();
        while (size > 0) {
            char c = (char) file[pointer++];
            string.append(c);
            size--;
        }
        return new String(string.toString().getBytes(), StandardCharsets.US_ASCII);
    }

    /**
     * Reads a string of the specified size clipped by an end character (NUL) (if present).
     * @param size Size of the string.
     * @return Sized String
     * @throws IndexOutOfBoundsException If there is not enough data to read a string of the requested size.
     */
    public SizeLimitedString readStringFixed(int size) {
        assertSize(size);
        StringBuilder string = new StringBuilder();
        size--;
        final int fullSize = size;
        while (size > 0) {
            char c = (char) file[pointer++];
            if (c=='\0') break;
            size--;
            string.append(c);
        }
        pointer += size;
        return new SizeLimitedString(
                new String(string.toString().getBytes(), StandardCharsets.US_ASCII),
                fullSize
        );
    }

    /**
     * Creates a new sub-reader based on the data from this reader.
     * @param size Size of the data allocated to this sub-reader.
     * @throws IndexOutOfBoundsException If there is not enough data to create a sub-reader of the requested size.
     */
    public BlockReader segment(int size) {
        if (size < 0) throw new IndexOutOfBoundsException("Requested segment size is negative.");
        assertSize(size);
        BlockReader segment = new BlockReader(file, littleEndian, pointer, size);
        pointer += size;
        return segment;
    }

    /**
     * Read 4 bytes as string.
     * @return 4-character string, intended to depict a block type.
     * @throws IndexOutOfBoundsException If there is not 4 bytes to read for the block type.
     */
    public String readBlockType() {
        return new String(readBytes(4), StandardCharsets.US_ASCII);
    }
}
