package dev.hipposgrumm.kamapreader.reader;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A safe reader designed to assist in reading the blocks
 * for KAR files, making it easy to switch between endianness
 * while avoiding difficult-to-debug overrun-related issues.
 */
public class BlockReader {
    private final RandomAccessFile reader;
    private boolean littleEndian;
    private final long startpos;
    private final long endpos;
    private final int size;

    /**
     * Static function to read 4 bytes as string.
     * @param reader File Reader to read the data from.
     * @return 4-character string, intended to depict a block type.
     * @throws IOException if there is a read error.
     * @see #readBlockType()
     */
    public static String readBlockType(RandomAccessFile reader) throws IOException {
        byte[] b = new byte[4];
        reader.readFully(b);
        return new String(b);
    }

    /**
     * Creates a safe container for reading the file.
     * @apiNote Any actions performed by the reader affect the original reader given.
     * @param reader File reader to read data from.
     * @param useLittleEndian Whether the reader starts as little endian.
     * @param size Size to allocate for the reader.
     * @throws IOException If there is an IO error.
     * @throws IndexOutOfBoundsException If file not large enough for the allocated size.
     */
    public BlockReader(RandomAccessFile reader, boolean useLittleEndian, int size) throws IOException {
        this.reader = reader;
        this.littleEndian = useLittleEndian;
        this.size = size;
        this.startpos = reader.getFilePointer();
        this.endpos = startpos + size;
        if (this.endpos > reader.length()) throw new IndexOutOfBoundsException("Block extends outside of file size.");
    }

    /**
     * @return Whether the reader is currently in little endian mode.
     */
    public boolean isLittleEndian() {
        return littleEndian;
    }

    /**
     * Set whether the reader is using little endian.
     */
    public void setLittleEndian(boolean littleEndian) {
        this.littleEndian = littleEndian;
    }

    /**
     * Set the file read pointer relative to this reader's start position.
     * @param pos Position to go to, relative to start position.
     * @throws IOException If there is an IO error.
     * @throws IndexOutOfBoundsException If the seek index is negative or above the size of the block.
     */
    public void seek(int pos) throws IOException {
        if (pos < 0 || pos > size) throw new IndexOutOfBoundsException("Seek location is outside block.");
        this.reader.seek(startpos+pos);
    }

    /**
     * Move the file pointer by an offset.
     * @param amount Amount to move; can be positive or negative.
     * @throws IOException If there is an IO error.
     * @throws IndexOutOfBoundsException If the resulting location would be out of range of the reader.
     */
    public void move(int amount) throws IOException {
        long pos = this.reader.getFilePointer()+amount;
        if (pos < startpos || pos > endpos) throw new IndexOutOfBoundsException("Shift location moves outside block.");
        this.reader.seek(pos);
    }

    /**
     * @return Current pointer location relative to this reader's start position.
     * @throws IOException If there is an IO error.
     */
    public int getPointer() throws IOException {
        return (int) (reader.getFilePointer()-startpos);
    }

    /**
     * @return Size of the allocated reader.
     */
    public int getSize() {
        return size;
    }

    /**
     * @return Amount of bytes left available to this reader.
     * @throws IOException If there is an IO error.
     */
    public int getRemaining() throws IOException {
        return (int) (endpos-reader.getFilePointer());
    }

    /// Checks if there is enough bytes left to read the requested amount.
    private void assertSize(int count) throws IOException {
        if (count == 0) return;
        if (reader.getFilePointer()+count > endpos) throw new IndexOutOfBoundsException(String.format("Remaining bytes in block (%s) is less than required amount (%s). At 0x%08X", getRemaining(), count, reader.getFilePointer()));
    }

    /**
     * Reads an array of bytes.
     * @param count Size of the array.
     * @return An array of bytes.
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there are not enough bytes in the array to return a complete array.
     */
    public byte[] readBytes(int count) throws IOException {
        assertSize(count);
        byte[] bytes = new byte[count];
        reader.read(bytes);
        return bytes;
    }

    /**
     * Reads a byte.
     * @return Byte
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 1 byte of data to read.
     */
    public byte readByte() throws IOException {
        assertSize(1);
        return reader.readByte();
    }

    /**
     * Reads a short.
     * @return Short (Signed)
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public short readShort() throws IOException {
        assertSize(2);
        short s = reader.readShort();
        if (!littleEndian) return s;
        return Short.reverseBytes(s);
    }

    /**
     * Reads an unsigned short.
     * @return Short (Unsigned)
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public int readUShort() throws IOException {
        short s = readShort();
        if (s >= 0) return s;
        return s & 0xFFFF;
    }

    /**
     * Reads an integer.
     * @return Integer (Signed)
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public int readInt() throws IOException {
        assertSize(4);
        int i = reader.readInt();
        if (!littleEndian) return i;
        return Integer.reverseBytes(i);
    }

    /**
     * Reads an unsigned integer.
     * @return Integer (Unsigned)
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public long readUInt() throws IOException {
        int i = readInt();
        if (i >= 0) return i;
        return i & 0xFFFFFFFFL; // Casts to long and removes negative bit (and any excess from long cast).
    }

    /**
     * Reads a float.
     * @return Float
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as float.
     */
    public float readFloat() throws IOException {
        int f = readInt();
        return Float.intBitsToFloat(f);
    }

    /**
     * Reads a short, always in little endian.
     * @return Short (Signed)
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public short readShortLittle() throws IOException {
        assertSize(2);
        short s = reader.readShort();
        return Short.reverseBytes(s);
    }

    /**
     * Reads an unsigned short, always in little endian.
     * @return Short (Unsigned)
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 2 bytes of available data to read as short.
     */
    public int readUShortLittle() throws IOException {
        short s = readShortLittle();
        if (s >= 0) return s;
        return s & 0xFFFF;
    }

    /**
     * Reads an integer, always in little endian.
     * @return Integer (Signed)
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public int readIntLittle() throws IOException {
        assertSize(4);
        int i = reader.readInt();
        return Integer.reverseBytes(i);
    }

    /**
     * Reads an unsigned integer, always in little endian.
     * @return Integer (Unsigned)
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as int.
     */
    public long readUIntLittle() throws IOException {
        int i = readIntLittle();
        if (i >= 0) return i;
        return i & 0xFFFFFFFFL; // Casts to long and removes negative bit (and any excess from long cast).
    }

    /**
     * Reads a float, always in little endian.
     * @return Float
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not 4 bytes of available data to read as float.
     */
    public float readFloatLittle() throws IOException {
        int f = readIntLittle();
        return Float.intBitsToFloat(f);
    }

    /**
     * Reads a string until it reaches an end character (NUL) or the end of the available data.
     * @return String
     * @throws IOException If there is a read error.
     */
    public String readString() throws IOException {
        StringBuilder string = new StringBuilder();
        long i = reader.getFilePointer();
        while (i < endpos) {
            char c = (char)reader.read();
            if (c==0) break;
            string.append(c);
            i++;
        }
        return string.toString();
    }

    /**
     * Reads a string of a specified size.
     * @param size Size of the string.
     * @return String
     * @throws IOException If there is a read error.
     * @throws IndexOutOfBoundsException If there is not enough data to read a string of the requested size.
     */
    public String readString(int size) throws IOException {
        assertSize(size);
        StringBuilder string = new StringBuilder();
        while (size > 0) {
            int c = reader.read();
            if (c<0) break;
            string.append((char)c);
            size--;
        }
        return string.toString();
    }

    /**
     * Creates a new sub-reader based on the data from this reader.
     * @param size Size of the data allocated to this sub-reader.
     * @throws IOException If creating the reader results in an IO error.
     * @throws IndexOutOfBoundsException If there is not enough data to create a sub-reader of the requested size.
     */
    public BlockReader segment(int size) throws IOException {
        assertSize(size);
        return new BlockReader(reader, littleEndian, size);
    }

    /**
     * Read 4 bytes as string.
     * @return 4-character string, intended to depict a block type.
     * @throws IOException if there is a read error.
     * @throws IndexOutOfBoundsException If there is not 4 bytes to read for the block type.
     */
    public String readBlockType() throws IOException {
        assertSize(4);
        byte[] b = new byte[4];
        reader.readFully(b);
        return new String(b);
    }
}
