package org.yuemi.fsi2107.storage;

/**
 * Kesalahan yang terjadi pada lapisan penyimpanan data.
 */
public class DataStorageException extends RuntimeException {

    public DataStorageException(String pesan) {
        super(pesan);
    }

    public DataStorageException(String pesan, Throwable sebab) {
        super(pesan, sebab);
    }
}
