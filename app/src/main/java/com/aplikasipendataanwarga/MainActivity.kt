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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// --- GLOBAL STATE ---
var gNama by mutableStateOf("Ahmad Brody")
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
        composable("home") {
            HomeScreen(dataFilter, searchQuery, { searchQuery = it }, { noKK -> navController.navigate("detail/$noKK") }, { navController.navigate("create") }, { navController.navigate("account") })
        }
        composable("detail/{noKK}") { backStackEntry ->
            val noKK = backStackEntry.arguments?.getString("noKK") ?: ""
            DetailScreen(dataFilter.filter { it.noKK == noKK }, { navController.popBackStack() }, { nik -> navController.navigate("edit/$nik") }, { scope.launch { dao.hapus(it) } })
        }
        composable("create") { CreateScreen(onSave = { scope.launch { dao.simpan(it) } }, onBack = { navController.popBackStack() }) }
        composable("edit/{nik}") { backStackEntry ->
            val nik = backStackEntry.arguments?.getString("nik") ?: ""
            val warga = dataFilter.find { it.nik == nik }
            warga?.let { EditScreen(it, { navController.popBackStack() }, { scope.launch { dao.update(it) } }) }
        }
        composable("account") { AccountScreen({ navController.navigate("home") }, { navController.navigate("create") }, { navController.navigate("profil") }, { navController.navigate("settings") }, { navController.navigate("login") }) }
        composable("profil") { ProfilDetailScreen({ navController.popBackStack() }, { navController.navigate("edit_profil") }) }
        composable("edit_profil") { EditProfilScreen { navController.popBackStack() } }
        composable("settings") { SettingsScreen { navController.popBackStack() } }
    }
}

// --- AUTH SCREENS ---
@Composable
fun LoginScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    val context = LocalContext.current
    var uTyped by remember { mutableStateOf("") }; var pTyped by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            drawPath(path = Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width, size.height * 0.8f); quadraticTo(size.width / 2, size.height, 0f, size.height * 0.8f); close() }, color = PrimaryBlue)
        }
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Card(shape = RoundedCornerShape(20.dp), border = BorderStroke(2.dp, PrimaryBlue), colors = CardDefaults.cardColors(Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LOGIN", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(30.dp))
                    OutlinedTextField(value = uTyped, onValueChange = { uTyped = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp))
                    Spacer(modifier = Modifier.height(15.dp))
                    OutlinedTextField(value = pTyped, onValueChange = { pTyped = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp))
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(onClick = {
                        if (uTyped == registeredUser && pTyped == registeredPass && uTyped.isNotEmpty()) onLogin()
                        else Toast.makeText(context, "Username/Password Salah!", Toast.LENGTH_SHORT).show()
                    }, colors = ButtonDefaults.buttonColors(PrimaryBlue), modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp)) { Text("Login") }
                    TextButton(onClick = onRegister) { Text("Belum memiliki akun? Buat Akun Baru", color = PrimaryBlue, fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(onReg: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var n by remember { mutableStateOf("") }; var ni by remember { mutableStateOf("") }
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var k by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) { drawPath(path = Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width, size.height * 0.7f); quadraticTo(size.width / 2, size.height, 0f, size.height * 0.7f); close() }, color = PrimaryBlue) }
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(80.dp))
            Card(shape = RoundedCornerShape(20.dp), border = BorderStroke(2.dp, PrimaryBlue), colors = CardDefaults.cardColors(Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REGISTER", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = ni, onValueChange = { ni = it }, label = { Text("NIK") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = u, onValueChange = { u = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = k, onValueChange = { k = it }, label = { Text("Konfirmasi Password") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp))
                    Button(onClick = {
                        if (n.isEmpty() || u.isEmpty() || p != k) Toast.makeText(context, "Cek data kembali!", Toast.LENGTH_SHORT).show()
                        else { registeredUser = u; registeredPass = p; onReg() }
                    }, colors = ButtonDefaults.buttonColors(PrimaryBlue), modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 20.dp), shape = RoundedCornerShape(15.dp)) { Text("Register") }
                }
            }
        }
    }
}

// --- HOME SCREEN ---
@Composable
fun HomeScreen(dataWarga: List<Warga>, searchQuery: String, onSearch: (String) -> Unit, onDetail: (String) -> Unit, onCreate: () -> Unit, onAccount: () -> Unit) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(PrimaryBlue).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = PrimaryBlue) }
                    Spacer(modifier = Modifier.width(12.dp)); Text(gNama, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(15.dp))
                OutlinedTextField(value = searchQuery, onValueChange = onSearch, placeholder = { Text("Search", color = PrimaryBlue) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(50.dp), leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryBlue) }, colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent))
            }
        },
        bottomBar = { BottomAppDesign(onHomeClick = {}, onAccountClick = onAccount, activeTab = "home") },
        floatingActionButton = { FabDesign(onCreate) },
        floatingActionButtonPosition = FabPosition.Center
    ) { p ->
        LazyColumn(modifier = Modifier.padding(p).padding(16.dp)) {
            items(dataWarga.filter { it.status.contains("Kepala Keluarga", true) }) { warga ->
                Column(modifier = Modifier.fillMaxWidth().clickable { onDetail(warga.noKK) }.padding(vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(warga.nama, color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("No KK ${warga.noKK}", color = PrimaryBlue.copy(0.6f), fontSize = 12.sp)
                        }
                        Icon(Icons.Default.Visibility, null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                    }
                    HorizontalDivider(color = PrimaryBlue.copy(0.2f), thickness = 1.dp)
                }
            }
        }
    }
}

// --- CREATE SCREEN (Langkah Bertahap) ---
@Composable
fun CreateScreen(onSave: (Warga) -> Unit, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var noKK by remember { mutableStateOf("") }; var namaKK_Id by remember { mutableStateOf("") }; var alamat by remember { mutableStateOf("") }
    var ni_KK by remember { mutableStateOf("") }; var na_KK by remember { mutableStateOf("") }; var tt_KK by remember { mutableStateOf("") }
    var st_KK by remember { mutableStateOf("") }; var ay_KK by remember { mutableStateOf("") }; var ib_KK by remember { mutableStateOf("") }
    val listAnggota = remember { mutableStateListOf<AnggotaFormState>() }

    Scaffold(containerColor = Color.White) { p ->
        Column(modifier = Modifier.padding(p).fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            val title = when(step) { 1 -> "Data Identitas KK"; 2 -> "Data Kepala Keluarga"; else -> "Data Anggota Keluarga" }
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            when (step) {
                1 -> {
                    CustomField("No KK", noKK) { noKK = it }
                    CustomField("Nama Kepala Keluarga", namaKK_Id) { namaKK_Id = it }
                    CustomField("Alamat Lengkap", alamat, isLarge = true) { alamat = it }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { step = 2 }, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(PrimaryBlue)) { Text("Berikutnya") }
                }
                2 -> {
                    CustomField("NIK", ni_KK) { ni_KK = it }; CustomField("Nama", na_KK) { na_KK = it }
                    CustomField("TTL", tt_KK) { tt_KK = it }; CustomField("Status", st_KK) { st_KK = it }
                    CustomField("Ayah", ay_KK) { ay_KK = it }; CustomField("Ibu", ib_KK) { ib_KK = it }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { step = 1 }) { Text("Sebelumnya") }
                        Button(onClick = { step = 3 }, colors = ButtonDefaults.buttonColors(PrimaryBlue)) { Text("Berikutnya") }
                    }
                }
                3 -> {
                    if (listAnggota.isEmpty()) listAnggota.add(AnggotaFormState())
                    listAnggota.forEachIndexed { idx, ang ->
                        Text("Anggota ${idx+1}", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        CustomField("NIK", ang.nik) { ang.nik = it }
                        CustomField("Nama", ang.nama) { ang.nama = it }
                        CustomField("TTL", ang.ttl) { ang.ttl = it }
                    }
                    Button(onClick = { listAnggota.add(AnggotaFormState()) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Text("Add New member") }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        OutlinedButton(onClick = { step = 2 }) { Text("Sebelumnya") }
                        Button(onClick = {
                            onSave(Warga(na_KK, noKK, ni_KK, tt_KK, "Kepala Keluarga", ay_KK, ib_KK))
                            listAnggota.forEach { if(it.nama.isNotEmpty()) onSave(Warga(it.nama, noKK, it.nik, it.ttl, it.status, it.ayah, it.ibu)) }
                            onBack()
                        }, colors = ButtonDefaults.buttonColors(PrimaryBlue)) { Text("Simpan") }
                    }
                }
            }
        }
    }
}

// --- DETAIL SCREEN & POP-UP DELETE ---
@Composable
fun DetailScreen(anggota: List<Warga>, onBack: () -> Unit, onEdit: (String) -> Unit, onDelete: (Warga) -> Unit) {
    var showDel by remember { mutableStateOf(false) }; var target by remember { mutableStateOf<Warga?>(null) }
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Data", fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, fontSize = 26.sp) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryBlue) } }) }, containerColor = LightBlue.copy(0.2f)) { p ->
        LazyColumn(modifier = Modifier.padding(p).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(anggota) { w ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, PrimaryBlue), colors = CardDefaults.cardColors(Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Surface(modifier = Modifier.clickable { onEdit(w.nik) }, shape = RoundedCornerShape(20.dp), color = PrimaryBlue) { Text("Update", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(modifier = Modifier.clickable { target = w; showDel = true }, shape = RoundedCornerShape(20.dp), color = WarningRed) { Text("Delete", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold) }
                        }
                        InfoBaris("No KK", w.noKK); InfoBaris("Nama", w.nama); InfoBaris("NIK", w.nik); InfoBaris("TTL", w.tempatTanggalLahir); InfoBaris("Ayah", w.namaAyah); InfoBaris("Ibu", w.namaIbu)
                    }
                }
            }
        }
        if (showDel && target != null) {
            AlertDialog(onDismissRequest = { showDel = false }, containerColor = Color.White, shape = RoundedCornerShape(16.dp),
                title = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Delete, null, tint = PrimaryBlue, modifier = Modifier.size(50.dp)); Text("CONFIRM DELETING DATA", fontWeight = FontWeight.Bold, fontSize = 18.sp) } },
                text = { Text("Are you sure you want to delete your data? All your data will be permanently deleted and this action cannot be undone.", textAlign = TextAlign.Center) },
                confirmButton = { Button(onClick = { onDelete(target!!); showDel = false; onBack() }, colors = ButtonDefaults.buttonColors(PrimaryBlue)) { Text("Yes") } },
                dismissButton = { OutlinedButton(onClick = { showDel = false }) { Text("No") } }
            )
        }
    }
}

// --- EDIT SCREEN ---
@Composable
fun EditScreen(warga: Warga, onBack: () -> Unit, onUpdate: (Warga) -> Unit) {
    var ni by remember { mutableStateOf(warga.nik) }; var na by remember { mutableStateOf(warga.nama) }
    var tt by remember { mutableStateOf(warga.tempatTanggalLahir) }; var st by remember { mutableStateOf(warga.status) }
    var ay by remember { mutableStateOf(warga.namaAyah) }; var ib by remember { mutableStateOf(warga.namaIbu) }
    Scaffold(containerColor = Color.White) { p ->
        Column(modifier = Modifier.padding(p).fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text("Edit Data", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            CustomField("NIK", ni) { ni = it }; CustomField("Nama", na) { na = it }
            CustomField("TTL", tt) { tt = it }; CustomField("Status", st) { st = it }
            CustomField("Ayah", ay) { ay = it }; CustomField("Ibu", ib) { ib = it }
            Spacer(modifier = Modifier.weight(1f)); Button(onClick = { onUpdate(warga.copy(nama = na, nik = ni, tempatTanggalLahir = tt, status = st, namaAyah = ay, namaIbu = ib)); onBack() }, colors = ButtonDefaults.buttonColors(PrimaryBlue), modifier = Modifier.align(Alignment.End)) { Text("Simpan") }
        }
    }
}

// --- REUSABLES ---
@Composable
fun CustomField(l: String, v: String, isLarge: Boolean = false, onV: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(l, fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = v, onValueChange = onV, modifier = Modifier.fillMaxWidth().then(if(isLarge) Modifier.height(120.dp) else Modifier), shape = RoundedCornerShape(15.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = PrimaryBlue, unfocusedBorderColor = PrimaryBlue))
    }
}

@Composable
fun BottomAppDesign(onHomeClick: () -> Unit, onAccountClick: () -> Unit, activeTab: String) {
    BottomAppBar(containerColor = PrimaryBlue, modifier = Modifier.height(70.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
            IconButton(onClick = onHomeClick) { Icon(Icons.Default.Home, null, tint = if(activeTab=="home") Color.White else Color.White.copy(0.6f)) }
            IconButton(onClick = onAccountClick) { Icon(Icons.Default.AccountCircle, null, tint = if(activeTab=="account") Color.White else Color.White.copy(0.6f)) }
        }
    }
}

@Composable
fun FabDesign(onClick: () -> Unit) { FloatingActionButton(onClick = onClick, containerColor = PrimaryBlue, contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(65.dp).offset(y = 42.dp)) { Icon(Icons.Default.Add, null) } }

@Composable
fun InfoBaris(l: String, v: String) { Column(modifier = Modifier.padding(vertical = 4.dp)) { Text(l, fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold); Text(v, fontSize = 14.sp, color = Color.Black) } }

@Composable
fun AccountScreen(onH: () -> Unit, onC: () -> Unit, onP: () -> Unit, onS: () -> Unit, onL: () -> Unit) {
    Scaffold(bottomBar = { BottomAppDesign(onH, onL, "account") }, floatingActionButton = { FabDesign(onC) }) { p ->
        Column(modifier = Modifier.padding(p).padding(20.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(LightBlue.copy(0.3f))) { Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.LightGray) }; Spacer(modifier = Modifier.width(15.dp)); Text(gNama, fontWeight = FontWeight.Bold) } }
            MenuItem(Icons.Outlined.Person, "Profil") { onP() }
            MenuItem(Icons.AutoMirrored.Filled.ExitToApp, "Leave", true) { onL() }
        }
    }
}

@Composable
fun MenuItem(i: ImageVector, t: String, w: Boolean = false, onClick: () -> Unit) { Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(15.dp)) { Icon(i, null, tint = if(w) WarningRed else PrimaryBlue); Spacer(modifier = Modifier.width(15.dp)); Text(t, color = if(w) WarningRed else Color.Black) } }
@Composable
fun ProfilDetailScreen(onB: () -> Unit, onE: () -> Unit) { Scaffold { p -> Column(modifier = Modifier.padding(p).padding(20.dp)) { Text("Profil", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue); Button(onClick = onB) { Text("Back") } } } }
@Composable
fun EditProfilScreen(onB: () -> Unit) { Scaffold { p -> Column(modifier = Modifier.padding(p).padding(20.dp)) { Text("Edit Profil", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue); Button(onClick = onB) { Text("Save") } } } }
@Composable
fun SettingsScreen(onB: () -> Unit) { Scaffold { p -> Column(modifier = Modifier.padding(p).padding(20.dp)) { Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue); Button(onClick = onB) { Text("Back") } } } }