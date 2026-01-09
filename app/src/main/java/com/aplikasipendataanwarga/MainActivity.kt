@file:OptIn(ExperimentalMaterial3Api::class)

package com.aplikasipendataanwarga

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

// --- CONFIG WARNA ---
val PrimaryBlue = Color(0xFF00ADEE)
val LightBlue = Color(0xFFE1F5FE)
val WarningRed = Color(0xFFE53935)

// --- GLOBAL STATE PROFIL ---
// FIX: Menambahkan variabel yang menyebabkan error "Unresolved reference" di gambar Bos
var gNama by mutableStateOf("Ahmad Brody")
var gJenisKelamin by mutableStateOf("Laki-laki") // SEBELUMNYA KURANG INI
var gAlamatUser by mutableStateOf("Semarang, Jawa Tengah") // SEBELUMNYA KURANG INI
var gNoKK by mutableStateOf("33030392373408")
var gNik by mutableStateOf("3301010101010001")
var gTtl by mutableStateOf("Semarang, 10-01-1995")
var gPendidikan by mutableStateOf("S1 Informatika")
var gPekerjaan by mutableStateOf("Karyawan Swasta")
var gStatusUser by mutableStateOf("Belum Kawin")

var registeredUser by mutableStateOf("")
var registeredPass by mutableStateOf("")

class AnggotaFormState {
    var nik by mutableStateOf(""); var nama by mutableStateOf(""); var ttl by mutableStateOf("")
    var status by mutableStateOf(""); var ayah by mutableStateOf(""); var ibu by mutableStateOf("")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppNavigation() }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.wargaDao()
    val scope = rememberCoroutineScope()
    val daftarWargaDB by dao.ambilSemua().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val dataFilter = remember(searchQuery, daftarWargaDB) {
        if (searchQuery.isEmpty()) daftarWargaDB
        else daftarWargaDB.filter { it.nama.contains(searchQuery, true) || it.nik.contains(searchQuery) }
    }
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen({ navController.navigate("home") }, { navController.navigate("register") }) }
        composable("register") { RegisterScreen({ navController.navigate("login") }, { navController.popBackStack() }) }
        composable(route = "home") { HomeScreen(dataWarga = dataFilter, searchQuery = searchQuery, onSearch = { searchQuery = it }, onDetail = { noKK -> navController.navigate("detail/$noKK") }, onCreate = { navController.navigate("create") }, onAccount = { navController.navigate("account") }) }
        composable("detail/{noKK}") { backStackEntry ->
            val noKK = backStackEntry.arguments?.getString("noKK") ?: ""
            DetailScreen(dataFilter.filter { it.noKK == noKK }, { navController.popBackStack() }, { nik -> navController.navigate("edit/$nik") }, { scope.launch { dao.hapus(it) } })
        }
        composable("create") { CreateScreen(onSaveToDB = { scope.launch { dao.simpan(it) } }, onBack = { navController.popBackStack() }) }
        composable("edit/{nik}") { backStackEntry ->
            val nik = backStackEntry.arguments?.getString("nik") ?: ""
            val warga = dataFilter.find { it.nik == nik }
            warga?.let { EditScreen(it, { navController.popBackStack() }, { up -> scope.launch { dao.update(up) } }) }
        }
        composable("account") { AccountScreen({ navController.navigate("home") }, { navController.navigate("create") }, { navController.navigate("profil") }, { navController.navigate("settings") }, { navController.navigate("login") }) }
        composable("profil") { ProfilDetailScreen(onBack = { navController.popBackStack() }, onEditProfil = { navController.navigate("edit_profil") }) }
        composable("edit_profil") { EditProfilScreen { navController.popBackStack() } }
        composable("settings") { SettingsScreen { navController.popBackStack() } }
    }
}

// --- SCREEN: EDIT PROFIL (Lengkap Sesuai image_f22846.png) ---
@Composable
fun EditProfilScreen(onBack: () -> Unit) {
    var tNama by remember { mutableStateOf(gNama) }
    var tJK by remember { mutableStateOf(gJenisKelamin) }
    var tAlamat by remember { mutableStateOf(gAlamatUser) }
    var tKK by remember { mutableStateOf(gNoKK) }
    var tNik by remember { mutableStateOf(gNik) }
    var tTtl by remember { mutableStateOf(gTtl) }
    var tPendidikan by remember { mutableStateOf(gPendidikan) }
    var tPekerjaan by remember { mutableStateOf(gPekerjaan) }
    var tStatus by remember { mutableStateOf(gStatusUser) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profil", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(PrimaryBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, PrimaryBlue, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(LightBlue.copy(0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EditTextField("Nama Lengkap", tNama) { tNama = it }
                    EditTextField("Jenis Kelamin", tJK) { tJK = it }
                    EditTextField("Alamat", tAlamat) { tAlamat = it }
                    EditTextField("No KK", tKK) { tKK = it }
                    EditTextField("NIK", tNik) { tNik = it }
                    EditTextField("Tempat , Tanggal Lahir", tTtl) { tTtl = it }
                    EditTextField("Pendidikan", tPendidikan) { tPendidikan = it }
                    EditTextField("Pekerjaan", tPekerjaan) { tPekerjaan = it }
                    EditTextField("Status Pernikahan", tStatus) { tStatus = it }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    gNama = tNama; gJenisKelamin = tJK; gAlamatUser = tAlamat; gNoKK = tKK
                    gNik = tNik; gTtl = tTtl; gPendidikan = tPendidikan
                    gPekerjaan = tPekerjaan; gStatusUser = tStatus
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(PrimaryBlue),
                shape = RoundedCornerShape(25.dp)
            ) { Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
    }
}

// --- SCREEN LAINNYA (Login, Register, Home, Create, Detail, Edit, Account, Settings, Reusables) ---
@Composable
fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    val context = LocalContext.current
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().background(LightBlue).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("LOGIN", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
        Spacer(modifier = Modifier.height(32.dp))
        EditTextField("Username", user) { user = it }
        EditTextField("Password", pass, true) { pass = it }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { if (user == registeredUser && pass == registeredPass && user.isNotEmpty()) onLogin() else Toast.makeText(context, "Username/Password Salah!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(PrimaryBlue), shape = RoundedCornerShape(10.dp)) { Text("Login") }
        TextButton(onClick = onRegister) { Text("Belum memiliki akun? Buat Akun Baru", color = PrimaryBlue) }
    }
}

@Composable
fun RegisterScreen(onReg: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var nama by remember { mutableStateOf("") }
    var nik by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var conf by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlue)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("REGISTER", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
        Spacer(modifier = Modifier.height(24.dp))

        EditTextField("Nama Lengkap", nama) { nama = it }
        EditTextField("NIK", nik) { nik = it }
        EditTextField("Alamat E-Mail", email) { email = it }
        EditTextField("Username", user) { user = it }
        EditTextField("Password", pass, true) { pass = it }
        EditTextField("Konfirmasi Password", conf, true) { conf = it }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nama.isEmpty() || user.isEmpty() || pass != conf) {
                    Toast.makeText(context, "Cek kembali data Anda!", Toast.LENGTH_SHORT).show()
                } else {
                    // --- LOGIKA SIMPAN DATA LOGIN ---
                    registeredUser = user
                    registeredPass = pass

                    // --- KUNCI: UPDATE PROFIL DENGAN NAMA PENDAFTAR ---
                    gNama = nama

                    Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                    onReg() // Pindah ke Login
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(PrimaryBlue),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Register", color = Color.White, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onBack) {
            Text("Sudah memiliki akun? Login", color = PrimaryBlue)
        }
    }
}

@Composable
fun HomeScreen(dataWarga: List<Warga>, searchQuery: String, onSearch: (String) -> Unit, onDetail: (String) -> Unit, onCreate: () -> Unit, onAccount: () -> Unit) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(PrimaryBlue).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Surface(shape = CircleShape, modifier = Modifier.size(40.dp), color = Color.White) { Icon(Icons.Default.Person, null, tint = PrimaryBlue, modifier = Modifier.padding(8.dp)) }; Spacer(modifier = Modifier.width(8.dp)); Text(gNama, color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = searchQuery, onValueChange = onSearch, placeholder = { Text("Search", color = PrimaryBlue) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(50.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent), leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) })
            }
        },
        bottomBar = { BottomAppDesign({}, onAccount, "home") },
        floatingActionButton = { FabDesign(onCreate) },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) { items(dataWarga.filter { it.status.contains("Kepala Keluarga", true) }) { warga -> Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onDetail(warga.noKK) }, border = BorderStroke(1.dp, PrimaryBlue), colors = CardDefaults.cardColors(Color.White)) { Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text(warga.nama, color = PrimaryBlue, fontWeight = FontWeight.Bold); Text("No KK ${warga.noKK}", fontSize = 12.sp, color = PrimaryBlue) }; Icon(Icons.Default.Visibility, null, tint = PrimaryBlue) } } } }
    }
}

@Composable
fun CreateScreen(onSaveToDB: (Warga) -> Unit, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var noKK by remember { mutableStateOf("") }; var na_KK by remember { mutableStateOf("") }; var ni_KK by remember { mutableStateOf("") }; var tt_KK by remember { mutableStateOf("") }; var st_KK by remember { mutableStateOf("") }; var ay_KK by remember { mutableStateOf("") }; var ib_KK by remember { mutableStateOf("") }
    val anggotaForms = remember { mutableStateListOf<AnggotaFormState>() }
    Scaffold(containerColor = LightBlue) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val title = when(step) { 1 -> "Data Identitas KK"; 2 -> "Data Kepala Keluarga"; else -> "Data Anggota Keluarga" }
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue); Spacer(modifier = Modifier.height(32.dp))
            when (step) {
                1 -> { EditTextField("No KK", noKK) { noKK = it } }
                2 -> { EditTextField("NIK", ni_KK) { ni_KK = it }; EditTextField("Nama", na_KK) { na_KK = it }; EditTextField("TTL", tt_KK) { tt_KK = it }; EditTextField("Status", st_KK) { st_KK = it }; EditTextField("Nama Ayah", ay_KK) { ay_KK = it }; EditTextField("Nama Ibu", ib_KK) { ib_KK = it } }
                3 -> { if (anggotaForms.isEmpty()) anggotaForms.add(AnggotaFormState()); anggotaForms.forEachIndexed { index, form -> Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text("Anggota ${index + 1}", fontWeight = FontWeight.Bold, color = PrimaryBlue); EditTextField("NIK", form.nik) { form.nik = it }; EditTextField("Nama", form.nama) { form.nama = it }; EditTextField("Tgl Lahir", form.ttl) { form.ttl = it }; EditTextField("Status", form.status) { form.status = it }; EditTextField("Nama Ayah", form.ayah) { form.ayah = it }; EditTextField("Nama Ibu", form.ibu) { form.ibu = it } }; HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = PrimaryBlue.copy(alpha = 0.2f)) }; Button(onClick = { anggotaForms.add(AnggotaFormState()) }) { Icon(Icons.Default.Add, null); Text("Add New", modifier = Modifier.padding(start = 8.dp)) } }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Row(modifier = Modifier.fillMaxWidth()) { if (step > 1) OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f).padding(end = 8.dp)) { Text("Back") }; Button(onClick = { if (step < 3) step++ else { onSaveToDB(Warga(na_KK, noKK, ni_KK, tt_KK, st_KK, ay_KK, ib_KK)); anggotaForms.forEach { f -> if(f.nama.isNotEmpty()) onSaveToDB(Warga(f.nama, noKK, f.nik, f.ttl, f.status, f.ayah, f.ibu)) }; onBack() } }, modifier = Modifier.weight(1f).padding(start = 8.dp), colors = ButtonDefaults.buttonColors(PrimaryBlue)) { Text(if(step == 3) "Simpan" else "Next") } }
        }
    }
}

@Composable
fun DetailScreen(anggota: List<Warga>, onBack: () -> Unit, onNavigateToEdit: (String) -> Unit, onDelete: (Warga) -> Unit) {
    var wargaTerpilih by remember { mutableStateOf<Warga?>(null) }; var showDeleteDialog by remember { mutableStateOf(false) }
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Data", fontWeight = FontWeight.Bold, color = PrimaryBlue) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryBlue) } }) }, containerColor = LightBlue) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(anggota) { p -> Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, PrimaryBlue), colors = CardDefaults.cardColors(Color.White)) { Column(modifier = Modifier.padding(16.dp)) { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Button(onClick = { onNavigateToEdit(p.nik) }, modifier = Modifier.height(32.dp), shape = RoundedCornerShape(20.dp)) { Text("Update", fontSize = 11.sp) }; Spacer(modifier = Modifier.width(8.dp)); Button(onClick = { wargaTerpilih = p; showDeleteDialog = true }, modifier = Modifier.height(32.dp), colors = ButtonDefaults.buttonColors(Color.Red), shape = RoundedCornerShape(20.dp)) { Text("Delete", fontSize = 11.sp) } }; InfoBaris("No KK", p.noKK); InfoBaris("Nama", p.nama); InfoBaris("NIK", p.nik); InfoBaris("TTL", p.tempatTanggalLahir); InfoBaris("Status", p.status); InfoBaris("Nama Ayah", p.namaAyah); InfoBaris("Nama Ibu", p.namaIbu) } } } }
        if (showDeleteDialog && wargaTerpilih != null) { AlertDialog(onDismissRequest = { showDeleteDialog = false }, title = { Text("CONFIRM DELETING DATA", fontWeight = FontWeight.Bold) }, text = { Text("Are you sure you want to delete your data? This action cannot be undone.") }, confirmButton = { Button(onClick = { onDelete(wargaTerpilih!!); showDeleteDialog = false; onBack() }) { Text("Yes") } }, dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("No") } }) }
    }
}

@Composable
fun EditScreen(warga: Warga, onBack: () -> Unit, onUpdate: (Warga) -> Unit) {
    var n by remember { mutableStateOf(warga.nama) }; var ni by remember { mutableStateOf(warga.nik) }; var tt by remember { mutableStateOf(warga.tempatTanggalLahir) }; var st by remember { mutableStateOf(warga.status) }; var ay by remember { mutableStateOf(warga.namaAyah) }; var ib by remember { mutableStateOf(warga.namaIbu) }
    Scaffold(containerColor = LightBlue) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp).verticalScroll(rememberScrollState())) {
            Text("Edit Data", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            EditTextField("NIK", ni) { ni = it }; EditTextField("Nama", n) { n = it }; EditTextField("TTL", tt) { tt = it }; EditTextField("Status", st) { st = it }; EditTextField("Nama Ayah", ay) { ay = it }; EditTextField("Nama Ibu", ib) { ib = it }
            Button(onClick = { onUpdate(warga.copy(nama = n, nik = ni, tempatTanggalLahir = tt, status = st, namaAyah = ay, namaIbu = ib)); onBack() }, modifier = Modifier.align(Alignment.End)) { Text("Simpan") }
        }
    }
}

@Composable
fun AccountScreen(onNavigateToHome: () -> Unit, onNavigateToCreate: () -> Unit, onNavigateToProfil: () -> Unit, onNavigateToSettings: () -> Unit, onLogout: () -> Unit) {
    var showLeaveDialog by remember { mutableStateOf(false) }
    Scaffold(topBar = { Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(PrimaryBlue).padding(24.dp), contentAlignment = Alignment.BottomStart) { Text("Account", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) } }, bottomBar = { BottomAppDesign(onNavigateToHome, {}, "account") }, floatingActionButton = { FabDesign(onNavigateToCreate) }, floatingActionButtonPosition = FabPosition.Center) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White).padding(24.dp).verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(LightBlue.copy(0.5f))) { Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) { Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(60.dp)) { Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.padding(12.dp)) }; Spacer(modifier = Modifier.width(16.dp)); Text(gNama, fontWeight = FontWeight.Bold) } }
            Spacer(modifier = Modifier.height(32.dp)); Text("Account", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(LightBlue.copy(0.5f))) { Column { MenuItem(Icons.Outlined.AccountCircle, "Profil") { onNavigateToProfil() }; MenuItem(Icons.Outlined.Settings, "Settings") { onNavigateToSettings() } } }
            Spacer(modifier = Modifier.height(24.dp)); Text("Lainnya", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(LightBlue.copy(0.5f))) { Column { MenuItem(Icons.Outlined.HelpOutline, "Help") {}; MenuItem(Icons.AutoMirrored.Filled.ExitToApp, "Leave", true) { showLeaveDialog = true } } }
        }
        if (showLeaveDialog) { AlertDialog(onDismissRequest = { showLeaveDialog = false }, title = { Text("Keluar") }, text = { Text("Yakin ingin keluar?") }, confirmButton = { Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(WarningRed)) { Text("Ya") } }, dismissButton = { OutlinedButton(onClick = { showLeaveDialog = false }) { Text("Tidak") } }) }
    }
}

@Composable
fun ProfilDetailScreen(onBack: () -> Unit, onEditProfil: () -> Unit) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Profil", color = Color.White, fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }, actions = { IconButton(onClick = onEditProfil) { Icon(Icons.Default.Edit, null, tint = Color.White) } }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(PrimaryBlue)) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp)) {
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), colors = CardDefaults.cardColors(LightBlue.copy(0.5f))) { Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(80.dp)) { Icon(Icons.Default.Person, null, tint = Color.LightGray, modifier = Modifier.padding(16.dp)) }; Spacer(modifier = Modifier.height(16.dp)); Text(gNama, fontWeight = FontWeight.Bold, fontSize = 20.sp) } }
            InfoProfilCard(listOf("Nama Lengkap" to gNama, "Jenis Kelamin" to gJenisKelamin, "Alamat" to gAlamatUser, "No KK" to gNoKK, "NIK" to gNik, "Tempat, Tanggal Lahir" to gTtl, "Pendidikan" to gPendidikan, "Pekerjaan" to gPekerjaan, "Status Pernikahan" to gStatusUser))
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // State untuk mengontrol apakah sedang di halaman utama setting atau halaman "PENTING"
    var isPentingPage by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        // Jika sedang di halaman PENTING, balik ke menu setting utama.
                        // Jika di menu utama, balik ke halaman sebelumnya (Account).
                        if (isPentingPage) isPentingPage = false else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(PrimaryBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            if (!isPentingPage) {
                // TAMPILAN AWAL SETTINGS
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { isPentingPage = true },
                    colors = CardDefaults.cardColors(LightBlue.copy(0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Hapus Akun", color = WarningRed, fontWeight = FontWeight.Bold)
                        Text("Menghapus akun akan menghapus data secara permanen.", fontSize = 12.sp)
                    }
                }
            } else {
                // TAMPILAN HALAMAN "PENTING" (Setelah Klik Hapus Akun)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(LightBlue.copy(0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFFFC107))
                            Text(" PENTING", color = Color(0xFFFFC107), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tindakan ini bersifat final. Setelah akun dihapus:", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Data tidak dapat diakses lagi", fontSize = 12.sp)
                        Text("• Seluruh informasi pribadi akan terhapus", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Tombol Delete Merah Besar di Bawah
                Button(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(Color.Red),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Dialog Konfirmasi Terakhir
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Account?") },
                text = { Text("This action cannot be undone. Are you sure?") },
                confirmButton = {
                    TextButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                        Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}

// --- REUSABLES ---
@Composable
fun MenuItem(i: ImageVector, t: String, w: Boolean = false, onClick: () -> Unit) { Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(i, null, tint = if(w) WarningRed else Color.Black); Spacer(modifier = Modifier.width(16.dp)); Text(t, color = if(w) WarningRed else Color.Black) } }
@Composable
fun InfoProfilCard(items: List<Pair<String, String>>) { Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(LightBlue.copy(0.3f))) { Column(modifier = Modifier.padding(16.dp)) { items.forEach { (l, v) -> Text(l, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue); Text(v, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp)) } } } }
@Composable
fun BottomAppDesign(onHomeClick: () -> Unit, onAccountClick: () -> Unit, activeTab: String) {
    BottomAppBar(containerColor = PrimaryBlue, modifier = Modifier.height(70.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            IconButton(onClick = onHomeClick) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Home, null, tint = if(activeTab=="home") Color.White else Color.White.copy(0.6f)); Text("Home", color = Color.White, fontSize = 10.sp) } }
            Spacer(modifier = Modifier.width(60.dp))
            IconButton(onClick = onAccountClick) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AccountCircle, null, tint = if(activeTab=="account") Color.White else Color.White.copy(0.6f)); Text("Account", color = Color.White, fontSize = 10.sp) } }
        }
    }
}
@Composable
fun FabDesign(onClick: () -> Unit) { FloatingActionButton(onClick = onClick, containerColor = PrimaryBlue, contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(70.dp).offset(y = 50.dp), elevation = FloatingActionButtonDefaults.elevation(0.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(40.dp)) } }
@Composable
fun EditTextField(l: String, v: String, isPassword: Boolean = false, onV: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(l, fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = v, onValueChange = onV, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
        )
    }
}
@Composable
fun InfoBaris(l: String, v: String) { Column(modifier = Modifier.padding(vertical = 4.dp)) { Text(l, fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold); Text(v, fontSize = 14.sp, color = Color.Black) } }