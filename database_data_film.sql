-- =======================================================
-- SQL Database Dump: data_film
-- Aplikasi Data Film & Pemesanan Tiket Bioskop
-- Termasuk kolom baru: url_poster pada tabel film
-- =======================================================

CREATE DATABASE IF NOT EXISTS `data_film` 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `data_film`;

-- 1. Tabel Genre
CREATE TABLE IF NOT EXISTS `genre` (
  `id_genre` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `nama_genre` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_genre`),
  UNIQUE KEY `nama_genre` (`nama_genre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Tabel Studio
CREATE TABLE IF NOT EXISTS `studio` (
  `id_studio` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  `nama_studio` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_studio`),
  UNIQUE KEY `nama_studio` (`nama_studio`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tabel Film (dengan kolom url_poster)
CREATE TABLE IF NOT EXISTS `film` (
  `id_film` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `judul` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_genre` int(10) unsigned NOT NULL,
  `tahun` smallint(5) unsigned NOT NULL,
  `sutradara` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `durasi_menit` smallint(5) unsigned NOT NULL,
  `rating` decimal(3,1) NOT NULL,
  `url_poster` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_film`),
  KEY `fk_film_genre` (`id_genre`),
  CONSTRAINT `fk_film_genre` FOREIGN KEY (`id_genre`) REFERENCES `genre` (`id_genre`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tabel Jadwal Tayang
CREATE TABLE IF NOT EXISTS `jadwal_tayang` (
  `id_jadwal` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `id_film` int(10) unsigned NOT NULL,
  `id_studio` tinyint(3) unsigned NOT NULL,
  `mulai_tayang` datetime NOT NULL,
  `harga_tiket` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_jadwal`),
  UNIQUE KEY `uk_jadwal_studio_waktu` (`id_studio`,`mulai_tayang`),
  KEY `fk_jadwal_film` (`id_film`),
  CONSTRAINT `fk_jadwal_film` FOREIGN KEY (`id_film`) REFERENCES `film` (`id_film`) ON UPDATE CASCADE,
  CONSTRAINT `fk_jadwal_studio` FOREIGN KEY (`id_studio`) REFERENCES `studio` (`id_studio`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Tabel Kursi
CREATE TABLE IF NOT EXISTS `kursi` (
  `id_kursi` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `id_studio` tinyint(3) unsigned NOT NULL,
  `kode_kursi` varchar(5) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_kursi`),
  UNIQUE KEY `uk_kursi_studio` (`id_studio`,`kode_kursi`),
  CONSTRAINT `fk_kursi_studio` FOREIGN KEY (`id_studio`) REFERENCES `studio` (`id_studio`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Tabel Pengguna / User
CREATE TABLE IF NOT EXISTS `pengguna` (
  `id_pengguna` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `nama` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(254) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMIN','USER') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`id_pengguna`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Tabel Pemesanan
CREATE TABLE IF NOT EXISTS `pemesanan` (
  `id_pemesanan` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `id_pengguna` bigint(20) unsigned NOT NULL,
  `dibuat_pada` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `metode_pembayaran` enum('CASH','QRIS','TRANSFER_BANK') COLLATE utf8mb4_unicode_ci NOT NULL,
  `biaya_layanan` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status_pembayaran` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id_pemesanan`),
  KEY `fk_pemesanan_pengguna` (`id_pengguna`),
  CONSTRAINT `fk_pemesanan_pengguna` FOREIGN KEY (`id_pengguna`) REFERENCES `pengguna` (`id_pengguna`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Tabel Tiket
CREATE TABLE IF NOT EXISTS `tiket` (
  `id_tiket` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `id_pemesanan` bigint(20) unsigned NOT NULL,
  `id_jadwal` bigint(20) unsigned NOT NULL,
  `id_kursi` int(10) unsigned NOT NULL,
  `harga_saat_beli` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_tiket`),
  UNIQUE KEY `uk_tiket_jadwal_kursi` (`id_jadwal`,`id_kursi`),
  KEY `fk_tiket_pemesanan` (`id_pemesanan`),
  KEY `fk_tiket_kursi` (`id_kursi`),
  CONSTRAINT `fk_tiket_jadwal` FOREIGN KEY (`id_jadwal`) REFERENCES `jadwal_tayang` (`id_jadwal`) ON UPDATE CASCADE,
  CONSTRAINT `fk_tiket_kursi` FOREIGN KEY (`id_kursi`) REFERENCES `kursi` (`id_kursi`) ON UPDATE CASCADE,
  CONSTRAINT `fk_tiket_pemesanan` FOREIGN KEY (`id_pemesanan`) REFERENCES `pemesanan` (`id_pemesanan`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
