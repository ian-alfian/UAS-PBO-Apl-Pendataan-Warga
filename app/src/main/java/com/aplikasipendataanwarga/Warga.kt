package com.aplikasipendataanwarga

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabel_warga") // Tambahkan ini agar Room tahu ini tabel
data class Warga(
    val nama: String,
    val noKK: String,   
    @PrimaryKey val nik: String, // Room wajib punya satu PrimaryKey
    val tempatTanggalLahir: String,
    val status: String,
    val namaAyah: String,
    val namaIbu: String
)