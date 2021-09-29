/**
 * Reed-Solomon Coding over 8-bit values.
 *
 * Copyright 2015, Backblaze, Inc.
 */

package securestorage;


/**
 * Reed-Solomon Coding over 8-bit values.
 */
public class ReedSolomon {

    private final int dataShardCount;
    private final int parityShardCount;
    private final int totalShardCount;
    private final Matrix matrix;

    
    private final byte [] [] parityRows;

    /**
     * Initializes a new encoder/decoder.
     */
    public ReedSolomon(int dataShardCount, int parityShardCount) {
        this.dataShardCount = dataShardCount;
        this.parityShardCount = parityShardCount;
        this.totalShardCount = dataShardCount + parityShardCount;
        matrix = buildMatrix(dataShardCount, this.totalShardCount);
        parityRows = new byte [parityShardCount] [];
        for (int i = 0; i < parityShardCount; i++) {
            parityRows[i] = matrix.getRow(dataShardCount + i);
        }
    }

    /**
     * Returns the number of data shards.
     */
    public int getDataShardCount() {
        return dataShardCount;
    }

    /**
     * Returns the number of parity shards.
     */
    public int getParityShardCount() {
        return parityShardCount;
    }

    /**
     * Returns the total number of shards.
     */
    public int getTotalShardCount() {
        return totalShardCount;
    }

   
    public void encodeParity(byte[][] shards, int offset, int byteCount) {
        // Check arguments.
        checkBuffersAndSizes(shards, offset, byteCount);

        // Build the array of output buffers.
        byte [] [] outputs = new byte [parityShardCount] [];
        for (int i = 0; i < parityShardCount; i++) {
            outputs[i] = shards[dataShardCount + i];
        }

        // Do the coding.
        codeSomeShards(parityRows, shards, outputs, parityShardCount,
                offset, byteCount);
    }

   
    public boolean isParityCorrect(byte[][] shards, int firstByte, int byteCount) {
        // Check arguments.
        checkBuffersAndSizes(shards, firstByte, byteCount);

        // Build the array of buffers being checked.
        byte [] [] toCheck = new byte [parityShardCount] [];
        for (int i = 0; i < parityShardCount; i++) {
            toCheck[i] = shards[dataShardCount + i];
        }

        // Do the checking.
        return checkSomeShards(parityRows, shards, toCheck, parityShardCount,
                firstByte, byteCount);
    }


    public void decodeMissing(byte [] [] shards,
                              boolean [] shardPresent,
                              final int offset,
                              final int byteCount) {
        // Check arguments.
        checkBuffersAndSizes(shards, offset, byteCount);

        // Quick check: are all of the shards present?  If so, there's
        // nothing to do.
        int numberPresent = 0;
        for (int i = 0; i < totalShardCount; i++) {
            if (shardPresent[i]) {
                numberPresent += 1;
            }
        }
        if (numberPresent == totalShardCount) {
            // Cool.  All of the shards data data.  We don't
            // need to do anything.
            return;
        }

        // More complete sanity check
        if (numberPresent < dataShardCount) {
            throw new IllegalArgumentException("Not enough shards present");
        }

       
        Matrix subMatrix = new Matrix(dataShardCount, dataShardCount);
        byte [] [] subShards = new byte [dataShardCount] [];
        {
            int subMatrixRow = 0;
            for (int matrixRow = 0; matrixRow < totalShardCount && subMatrixRow < dataShardCount; matrixRow++) {
                if (shardPresent[matrixRow]) {
                    for (int c = 0; c < dataShardCount; c++) {
                        subMatrix.set(subMatrixRow, c, matrix.get(matrixRow, c));
                    }
                    subShards[subMatrixRow] = shards[matrixRow];
                    subMatrixRow += 1;
                }
            }
        }

        Matrix dataDecodeMatrix = subMatrix.invert();

       
        byte [] [] outputs = new byte [parityShardCount] [];
        byte [] [] matrixRows = new byte [parityShardCount] [];
        int outputCount = 0;
        for (int iShard = 0; iShard < dataShardCount; iShard++) {
            if (!shardPresent[iShard]) {
                outputs[outputCount] = shards[iShard];
                matrixRows[outputCount] = dataDecodeMatrix.getRow(iShard);
                outputCount += 1;
            }
        }
        codeSomeShards(matrixRows, subShards, outputs, outputCount, offset, byteCount);

      
        outputCount = 0;
        for (int iShard = dataShardCount; iShard < totalShardCount; iShard++) {
            if (!shardPresent[iShard]) {
                outputs[outputCount] = shards[iShard];
                matrixRows[outputCount] = parityRows[iShard - dataShardCount];
                outputCount += 1;
            }
        }
        codeSomeShards(matrixRows, shards, outputs, outputCount, offset, byteCount);
    }

    /**
     * Checks the consistency of arguments passed to public methods.
     */
    private void checkBuffersAndSizes(byte [] [] shards, int offset, int byteCount) {
        
        if (shards.length != totalShardCount) {
            throw new IllegalArgumentException("wrong number of shards: " + shards.length);
        }

        // All of the shard buffers should be the same length.
        int shardLength = shards[0].length;
        for (int i = 1; i < shards.length; i++) {
            if (shards[i].length != shardLength) {
                throw new IllegalArgumentException("Shards are different sizes");
            }
        }

        // The offset and byteCount must be non-negative and fit in the buffers.
        if (offset < 0) {
            throw new IllegalArgumentException("offset is negative: " + offset);
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount is negative: " + byteCount);
        }
        if (shardLength < offset + byteCount) {
            throw new IllegalArgumentException("buffers to small: " + byteCount + offset);
        }
    }

    /**
     * Multiplies a subset of rows from a coding matrix by a full set of
     * input shards to produce some output shards.
     *
     * @param matrixRows The rows from the matrix to use.
     * @param inputs An array of byte arrays, each of which is one input shard.
     *               The inputs array may have extra buffers after the ones
     *               that are used.  They will be ignored.  The number of
     *               inputs used is determined by the length of the
     *               each matrix row.
     * @param outputs Byte arrays where the computed shards are stored.  The
     *                outputs array may also have extra, unused, elements
     *                at the end.  The number of outputs computed, and the
     *                number of matrix rows used, is determined by
     *                outputCount.
     * @param outputCount The number of outputs to compute.
     * @param offset The index in the inputs and output of the first byte
     *               to process.
     * @param byteCount The number of bytes to process.
     */
    private void codeSomeShards(final byte [] [] matrixRows,
                                final byte [] [] inputs,
                                final byte [] [] outputs,
                                final int outputCount,
                                final int offset,
                                final int byteCount) {

        // This is the inner loop.  It needs to be fast.  Be careful
        

        for (int iByte = offset; iByte < offset + byteCount; iByte++) {
            for (int iRow = 0; iRow < outputCount; iRow++) {
                byte [] matrixRow = matrixRows[iRow];
                int value = 0;
                for (int c = 0; c < dataShardCount; c++) {
                    value ^= Galois.multiply(matrixRow[c], inputs[c][iByte]);
                }
                outputs[iRow][iByte] = (byte) value;
            }
        }
    }

    
    private boolean checkSomeShards(final byte [] [] matrixRows,
                                    final byte [] [] inputs,
                                    final byte [] [] toCheck,
                                    final int checkCount,
                                    final int offset,
                                    final int byteCount) {

      

        for (int iByte = offset; iByte < offset + byteCount; iByte++) {
            for (int iRow = 0; iRow < checkCount; iRow++) {
                byte [] matrixRow = matrixRows[iRow];
                int value = 0;
                for (int c = 0; c < dataShardCount; c++) {
                    value ^= Galois.multiply(matrixRow[c], inputs[c][iByte]);
                }
                if (toCheck[iRow][iByte] != (byte) value) {
                    return false;
                }
            }
        }
        return true;
    }

  
    private static Matrix buildMatrix(int dataShards, int totalShards) {
        // Start with a Vandermonde matrix.  This matrix would work,
        // in theory, but doesn't have the property that the data
        // shards are unchanged after encoding.
        Matrix vandermonde = vandermonde(totalShards, dataShards);

        // Multiple by the inverse of the top square of the matrix.
        // This will make the top square be the identity matrix, but
        // preserve the property that any square subset of rows  is
        // invertible.
        Matrix top = vandermonde.submatrix(0, 0, dataShards, dataShards);
        return vandermonde.times(top.invert());
    }

   
    private static Matrix vandermonde(int rows, int cols) {
        Matrix result = new Matrix(rows, cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                result.set(r, c, Galois.exp((byte) r, c));
            }
        }
        return result;
    }
}
