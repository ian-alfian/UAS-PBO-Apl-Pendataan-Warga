package com.aplikasipendataanwarga

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WargaDao {
    // 1. Ambil semua data warga (Ganti ke Warga agar sinkron dengan MainActivity)
    @Query("SELECT * FROM tabel_warga")
    fun ambilSemua(): Flow<List<Warga>>

    // 2. Tambah warga baru
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpan(warga: Warga)

    // 3. Hapus satu warga
    @Delete
    suspend fun hapus(warga: Warga)

    // 4. Update data warga
    @Update
    suspend fun update(warga: Warga)
}