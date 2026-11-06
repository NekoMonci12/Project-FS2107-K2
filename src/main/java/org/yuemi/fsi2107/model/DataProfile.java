package org.yuemi.fsi2107.model;

import java.time.LocalDateTime;

/**
 * Data mentah profil yang dibaca dari penyimpanan.
 *
 * <p>Record ini memisahkan bentuk data di tabel {@code profile} dari kelas
 * profil di domain, sehingga lapisan penyimpanan tidak perlu ikut berubah
 * ketika ada tipe profil baru.</p>
 *
 * @param tipe        diskriminator tipe profil
 * @param id          identitas profil
 * @param nama        nama profil
 * @param catatan     catatan bebas profil
 * @param dibuatPada  waktu profil dibuat
 * @param tarifPph    tarif PPh dalam persen, {@code null} bila tipe profil tidak memakainya
 * @param tarifPpn    tarif PPN dalam persen, {@code null} bila tipe profil tidak memakainya
 */
public record DataProfile(String tipe, long id, String nama, String catatan,
                          LocalDateTime dibuatPada, Double tarifPph, Double tarifPpn) {
}
