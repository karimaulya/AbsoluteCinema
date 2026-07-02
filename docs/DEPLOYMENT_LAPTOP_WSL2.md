# Self-Hosting di Laptop (WSL2 + Cloudflare Tunnel)

Panduan menjadikan laptop kamu sendiri sebagai "VPS" untuk hosting AbsoluteCinema, dengan akses **internet publik** via **Cloudflare Tunnel** (tanpa port forwarding di router, tanpa IP publik terungkap).

> **Arsitektur singkat:**
> ```
> Internet → Cloudflare Edge (HTTPS) ──tunnel outbound──→ WSL2 (cloudflared) → Spring Boot :8080 → MySQL :3306
> ```
> Yang spesial: cloudflared membuat koneksi **keluar** (outbound). Jadi router rumah **tidak perlu** di-port-forward dan IP publik rumah **tetap tersembunyi**.

---

## Prasyarat

| Komponen | Catatan |
|---|---|
| WSL2 (Ubuntu/Debian) | Sudah aktif, systemd sudah on (`/etc/wsl.conf` berisi `[boot]\nsystemd=true`) |
| Java 17+ | Sudah ada di `/usr/bin/java` |
| Maven | Saat ini hanya di `/tmp/maven-install/` (ephemeral!). Akan kita install permanen di bawah. |
| Akun Cloudflare (gratis) | Daftar di https://dash.cloudflare.com/sign-up — untuk Named Tunnel persistent |
| Opsional: domain sendiri | Untuk URL stabil seperti `cinema.namadomain.com`. Tanpa domain, pakai `random.trycloudflare.com` (Quick Tunnel). |

---

## Step 1 — Install Maven permanen + tools

`/tmp` di WSL2 dibersihkan saat restart, jadi Maven dari `/tmp/maven-install/` akan hilang. Install via apt:

```bash
sudo apt update
sudo apt install -y maven curl
mvn -version          # pastikan keluar versi
java -version
```

> **Catatan distro:**
> - **Ubuntu/Debian**: `sudo apt install -y mysql-server`
> - **Kali Linux**: pakai `mariadb-server` (Kali tidak menyediakan paket `mysql-server`; MariaDB adalah drop-in replacement yang kompatibel penuh dengan MySQL untuk aplikasi ini). JDBC driver `mysql-connector-j` dan Hibernate `MySQLDialect` di `application.yml` tetap works dengan MariaDB.

## Step 2 — Setup Database

Pilih sesuai distro:

**Kali Linux (MariaDB):**
```bash
sudo apt install -y mariadb-server
sudo systemctl enable --now mariadb
sudo systemctl status mariadb
```

**Ubuntu/Debian (MySQL):**
```bash
sudo apt install -y mysql-server
sudo systemctl enable --now mysql
sudo systemctl status mysql
```

Lalu buat database & user (perintah sama untuk MariaDB & MySQL):

```bash
sudo mysql -u root <<'SQL'
CREATE DATABASE absolutecinema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cinema_user'@'localhost' IDENTIFIED BY 'GantiPasswordKuatIni123!';
GRANT ALL PRIVILEGES ON absolutecinema.* TO 'cinema_user'@'localhost';
FLUSH PRIVILEGES;
SQL
```

Verifikasi:

```bash
mysql -u cinema_user -p absolutecinema    # masukkan password di atas
```

## Step 3 — Clone & Build

```bash
cd /mnt/d/Projects/Absolute\ Cinema/AbsoluteCinema     # path repo kamu saat ini
# atau kalau mau fresh clone:
# cd ~ && git clone https://github.com/karimaulya/AbsoluteCinema.git && cd AbsoluteCinema

mvn -q clean package -DskipTests
ls target/*.jar     # pastikan ada AbsoluteCinema-0.0.1-SNAPSHOT.jar
```

## Step 4 — Environment Variables

```bash
sudo tee /opt/absolutecinema.env > /dev/null <<'EOF'
DB_URL=jdbc:mysql://localhost:3306/absolutecinema?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USER=cinema_user
DB_PASS=GantiPasswordKuatIni123!
PORT=8080
MAIL_USERNAME=
MAIL_PASSWORD=
IMAGEKIT_PRIVATE_KEY=
IMAGEKIT_PUBLIC_KEY=
IMAGEKIT_URL_ENDPOINT=
TMDB_API_KEY=
EOF
sudo chmod 600 /opt/absolutecinema.env
```

> Isi `MAIL_*` / `IMAGEKIT_*` / `TMDB_API_KEY` kalau mau fitur kirim OTP, upload poster, dan import TMDB jalan.

## Step 5 — systemd Service untuk Aplikasi

```bash
sudo tee /etc/systemd/system/absolutecinema.service > /dev/null <<'EOF'
[Unit]
Description=AbsoluteCinema Spring Boot App
After=network.target mysql.service mariadb.service

[Service]
Type=simple
User=YOUR_WSL_USERNAME
WorkingDirectory=/mnt/d/Projects/Absolute Cinema/AbsoluteCinema
EnvironmentFile=/opt/absolutecinema.env
ExecStart=/usr/bin/java -jar /mnt/d/Projects/Absolute Cinema/AbsoluteCinema/target/AbsoluteCinema-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=5
StandardOutput=append:/var/log/absolutecinema.log
StandardError=append:/var/log/absolutecinema.log

[Install]
WantedBy=multi-user.target
EOF

# Ganti YOUR_WSL_USERNAME dengan username kamu:
sudo sed -i "s/YOUR_WSL_USERNAME/$USER/" /etc/systemd/system/absolutecinema.service

sudo touch /var/log/absolutecinema.log && sudo chown $USER:$USER /var/log/absolutecinema.log
sudo systemctl daemon-reload
sudo systemctl enable --now absolutecinema
sudo systemctl status absolutecinema
```

Tes lokal dulu (harus muncul halaman login):

```bash
curl -I http://localhost:8080/      # 200 OK = jalan
```

Jalankan seed data setelah app pertama kali start (lihat Step 4 di `DEPLOYMENT_VPS.md`).

## Step 6 — Install cloudflared

```bash
# Untuk Debian/Ubuntu (x86_64):
curl -L --output cloudflared.deb https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
sudo dpkg -i cloudflared.deb
rm cloudflared.deb
cloudflared --version
```

## Step 7 — Pilih Salah Satu Mode Tunnel

### 🚀 Mode A — Quick Tunnel (instan, URL berubah tiap restart)

Paling cepat. Tanpa akun Cloudflare, tanpa konfigurasi:

```bash
cloudflared tunnel --url http://localhost:8080
```

Outputnya kira-kira:
```
Your quick Tunnel has been created! Visit it at:
  https://random-words-xyz.trycloudflare.com
```

Buka URL itu di browser mana saja (HP juga) → website kamu online. **Catatan:** URL berubah setiap kali `cloudflared` di-restart. Cocok untuk demo / testing.

### 🌐 Mode B — Named Tunnel (URL persistent, butuh akun + domain)

Untuk URL stabil seperti `cinema.namadomain.com`.

```bash
# 1. Login (buka browser, authorize)
cloudflared tunnel login

# 2. Buat tunnel bernama "absolutecinema"
cloudflared tunnel create absolutecinema
# → output: Created tunnel absolutecinema with id <UUID>
# → file credential otomatis di ~/.cloudflared/<UUID>.json

# 3. Buat record DNS (ganti cinema.namadomain.com dengan subdomainmu)
cloudflared tunnel route dns absolutecinema cinema.namadomain.com

# 4. Jalankan tunnel
cloudflared tunnel run --url http://localhost:8080 absolutecinema
```

Domain harus sudah terdaftar di akun Cloudflare kamu (add site di dashboard, free plan OK). Kalau belum punya domain, opsi murah: `.my.id` (~Rp 20rb/tahun) atau `.com` (~$10/tahun).

## Step 8 — Auto-start Tunnel via systemd (Mode B — persistent)

Hanya untuk Mode B. Buat konfigurasi & service:

```bash
mkdir -p ~/.cloudflared

# Salin credential & cert dari hasil login Step 7
# (sudah otomatis ada di ~/.cloudflared/ setelah `cloudflared tunnel login` & `create`)

# Config file
cat > ~/.cloudflared/config.yml <<'EOF'
tunnel: absolutecinema
credentials-file: /home/YOUR_WSL_USERNAME/.cloudflared/<UUID>.json
ingress:
  - hostname: cinema.namadomain.com
    service: http://localhost:8080
  - service: http_status:404
EOF
sed -i "s/YOUR_WSL_USERNAME/$USER/" ~/.cloudflared/config.yml
# Ganti <UUID> dengan ID tunnel dari `cloudflared tunnel list`

# systemd unit
sudo tee /etc/systemd/system/cloudflared.service > /dev/null <<'EOF'
[Unit]
Description=Cloudflare Tunnel (absolutecinema)
After=network-online.target absolutecinema.service
Wants=network-online.target

[Service]
Type=simple
User=YOUR_WSL_USERNAME
ExecStart=/usr/bin/cloudflared --config /home/YOUR_WSL_USERNAME/.cloudflared/config.yml tunnel run
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
sudo sed -i "s/YOUR_WSL_USERNAME/$USER/" /etc/systemd/system/cloudflared.service

sudo systemctl daemon-reload
sudo systemctl enable --now cloudflared
sudo systemctl status cloudflared
```

Untuk **Mode A (Quick Tunnel)**, bisa juga dibuatkan systemd unit sederhana — ganti `ExecStart` jadi `/usr/bin/cloudflared tunnel --url http://localhost:8080 --no-autoupdate`. URL akan berubah tiap restart, tapi tunnel akan tetup auto-jalan.

## Step 9 — Auto-start WSL2 saat Windows Booting (opsional)

WSL2 tidak otomatis berjalan saat Windows baru start — harus dibuka terminal WSL dulu. Untuk auto-start (sehingga systemd & service langsung on):

1. Buka **Task Scheduler** di Windows → Create Basic Task
2. Trigger: **At log on**
3. Action: Start a program
   - Program: `wsl.exe`
   - Arguments: `-d Ubuntu --exec /bin/bash -c "sleep infinity"`
4. Run with highest privileges ✓, dan **Run whether user is logged on or not** kalau mau jalan terus.

Alternatif: tambahkan shortcut `wsl.exe` ke folder `shell:startup` (`Win+R` → ketik itu).

---

## Cek & Operasional

```bash
# Status semua service
sudo systemctl status absolutecinema mysql cloudflared

# Log aplikasi (real-time)
sudo tail -f /var/log/absolutecinema.log

# Log cloudflared
sudo journalctl -u cloudflared -f

# Restart aplikasi (setelah git pull + rebuild)
cd /mnt/d/Projects/Absolute\ Cinema/AbsoluteCinema
git pull origin main
mvn -q clean package -DskipTests
sudo systemctl restart absolutecinema

# Cek URL tunnel aktif
curl -I https://cinema.namadomain.com      # Mode B
# atau lihat output systemd cloudflared untuk URL trycloudflare.com (Mode A)
```

---

## Penting: Batasan Self-Hosting di Laptop

| Issue | Dampak | Solusi |
|---|---|---|
| **Laptop harus tetap nyala** | Kalau sleep/matot, website offline | Prevent sleep: `Windows Settings → Power → Sleep = Never` saat plugged in |
| **IP rumah tersembunyi, tapi ISP bisa tahu** | Traffic tetap lewat ISP | Tidak ada yang bisa dilakukan, tapi kontennya terenkripsi antara Cloudflare ↔ tunnel |
| **TOS ISP** | Beberapa ISP melarang hosting | Pakai Cloudflare Tunnel fine, traffic terlihat seperti HTTPS biasa |
| **Bandwidth rumah** | Upload speed rumah biasanya rendah | Cloudflare cache asset statis (CSS/JS/gambar) otomatis — performa OK |
| **WSL2 RAM besar** | Default bisa makan 50%+ RAM | Bikin `C:\Users\Kamu\.wslconfig` dengan isi `[wsl2]\nmemory=4GB` lalu `wsl --shutdown` |
| **`.env` jangan di-commit** | Kredensial bocor | Sudah di luar repo (`/opt/absolutecinema.env`) — aman |
| **URL Quick Tunnel berubah** | Mode A: tiap restart cloudflared | Pakai Mode B (Named Tunnel) kalau mau URL stabil |

---

## Troubleshooting WSL2-Specific

| Gejala | Cek / Fix |
|---|---|
| `systemctl status absolutecinema` → failed | `sudo journalctl -u absolutecinema -n 50` — biasanya `.env` salah atau DB belum running |
| App jalan tapi tunnel error 502 | Tunnel bisa connect tapi app di `localhost:8080` mati. Pastikan `curl http://localhost:8080/` 200 dulu |
| `cloudflared tunnel login` tidak buka browser | Copy URL manual dari output ke browser Windows |
| MySQL/MariaDB service mati setelah reboot WSL | Kali: `sudo systemctl enable mariadb`. Ubuntu: `sudo systemctl enable mysql` (sudah dilakukan di Step 2) |
| Port 8080 dipakai proses lain | `sudo lsof -i :8080` lalu hentikan, atau ganti `PORT` di `.env` |
| Maven error "Java version" | `java -version` harus ≥ 17. Kalau perlu: `sudo apt install openjdk-17-jdk` |
| `cloudflared: command not found` setelah reboot | Install via `.deb` (Step 6), bukan dari cache sementara |
| URL tunnel bilang "connection refused" tapi app jalan | Tunggu 30-60 detik setelah restart app — tunnel perlu reconnect |

---

## Keamanan Tambahan yang Direkomendasikan

1. **Ubah password admin default `admin123`** setelah login pertama.
2. **Backup DB otomatis** via cron:
   ```bash
   echo "0 3 * * * /usr/bin/mysqldump -u cinema_user -pGantiPasswordKuatIni123! absolutecinema | gzip > /home/$USER/backups/cinema-$(date +\%F).sql.gz" | sudo tee /etc/cron.d/cinema-backup
   sudo mkdir -p /home/$USER/backups
   ```
3. **Cloudflare Access** (gratis): tambahkan auth gate di depan tunnel supaya hanya email tertentu yang bisa akses — berguna kalau app masih dev. Setup di dashboard Cloudflare → Zero Trust → Access.
4. **Rate limiting** di Cloudflare dashboard (gratis) untuk mitigasi abuse.

---

## Perbandingan: VPS vs Laptop Self-Host

| Aspek | VPS (Railway / teman) | Laptop + Cloudflare Tunnel |
|---|---|---|
| Biaya | $5-20/bulan | Gratis (listrik laptop saja) |
| Uptime | 99.9%+ | Hanya saat laptop nyala |
| Performa | Stabil | Tergantung laptop & koneksi rumah |
| Setup | Lebih simple | Lebih banyak konfigurasi |
| Cocok untuk | Produksi serius | Belajar, demo, dev pribadi |

Untuk tugas kuliah / demo pribadi, **laptop + Cloudflare Tunnel** lebih dari cukup. Untuk produksi pengguna nyana, pakai VPS.
