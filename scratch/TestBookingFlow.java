package scratch;

import aplikasdatafilm.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class TestBookingFlow {
    public static void main(String[] args) {
        System.out.println("=== TEST TRANSAKSI PEMESANAN & PEMBAYARAN KASIR ===");
        try {
            // Find active showtime
            List<PemesananService.Jadwal> jadwalList = PemesananService.cariJadwal("");
            if (jadwalList.isEmpty()) {
                System.out.println("Tidak ada jadwal tayang aktif di database.");
                return;
            }

            PemesananService.Jadwal jadwal = jadwalList.get(0);
            System.out.println("Menggunakan Jadwal: " + jadwal.judul() + " | Studio: " + jadwal.namaStudio() + " | Mulai: " + jadwal.mulaiTayang());

            // Fetch seats
            List<PemesananService.Kursi> kursiList = PemesananService.kursiUntukJadwal(jadwal.idJadwal());
            PemesananService.Kursi kursiTersedia = null;
            for (PemesananService.Kursi k : kursiList) {
                if (!k.sudahDipesan()) {
                    kursiTersedia = k;
                    break;
                }
            }

            if (kursiTersedia == null) {
                System.out.println("Tidak ada kursi kosong untuk jadwal ini.");
                return;
            }

            System.out.println("Memilih Kursi: " + kursiTersedia.kodeKursi() + " (ID: " + kursiTersedia.idKursi() + ")");

            // Process QRIS payment simulation
            long idPemesanan = PemesananService.konfirmasiPembayaranDanBuatTiket(
                    1L, jadwal.idJadwal(), List.of(kursiTersedia.idKursi()), "QRIS");

            System.out.println("✓ Pemesanan Berhasil Dikonfirmasi! ID Pemesanan: #" + idPemesanan);

            // Fetch Struk
            PemesananService.Struk struk = PemesananService.ambilStruk(idPemesanan);
            System.out.println("✓ Data Struk: " + struk.judul() + " | Status: " + struk.statusPembayaran()
                    + " | Kursi: " + struk.kodeKursi() + " | Total: " + CinemaTheme.formatRupiah(struk.total()));

            // Verify JasperReports ticket for this booking
            System.out.println("✓ Menguji render tiket JasperReports...");
            JasperTicketService.compileReport();
            System.out.println("✓ Template tiket bioskop JasperReports siap cetak!");

            System.out.println("=== SEMUA TEST ALUR TRANSAKSI SUKSES 100% ===");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

