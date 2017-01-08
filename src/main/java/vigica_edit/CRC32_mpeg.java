/*
 * Copyright (C) 2016 bnabi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vigica_edit;

/**
 *
 * @author bnabi
 */
public class CRC32_mpeg {

    /**
     * The initial value to load the register with.
     */
    private static final int INITIAL_REGISTER_VALUE = 0xffffffff;
    private int register = INITIAL_REGISTER_VALUE;

    /**
     * This routine derives the CRC hash using a simplified CRC generation
     * algorithm.
     *
     * @param value the byte to compute the CRC hash for.
     *
     * @throws java.io.IOException if an I/O error occurs while reading in the
     * bytes of data.
     */
    public void update(byte value) {
        // Create a mask to isolate the highest bit.
        int bitMask = (int) (1 << 31);

        byte element = (byte) value;

        register ^= ((int) element << 24);
        for (int i = 0; i < 8; i++) {
            if ((register & bitMask) != 0) {
                register = (int) ((register << 1) ^ 0x04c11db7);
            } else {
                register <<= 1;
            }
        }
    }

    /**
     * This utility routine outputs the hexa crc.
     *
     * @param number the number to display as a hexadecimal String.
     *
     * @return the hexadecimal string representation of the given crc.
     */
    public String getValue() {

        // XOR the final register value.
        register ^= 0x00000000;

        // Create a mask to isolate only the correct width of bits.
        long fullMask = (((1L << 31) - 1L) << 1) | 1L;
        return Long.toHexString(register & fullMask);
    }
}
