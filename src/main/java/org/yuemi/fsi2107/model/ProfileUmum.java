package org.yuemi.fsi2107.model;

import java.time.LocalDateTime;

/**
 * Profil umum, dipakai untuk pencatatan keuangan pribadi yang tidak
 * membutuhkan setelan pajak khusus.
 */
public final class ProfileUmum extends Profile {

    /** Nilai diskriminator profil umum. */
    public static final String TIPE = "UMUM";

    public ProfileUmum(long id, String nama, String catatan, LocalDateTime dibuatPada) {
        super(id, nama, catatan, dibuatPada);
    }

    @Override
    public String getTipe() {
        return TIPE;
    }
}
