# Panduan Deploy AbsoluteCinema ke VPS

Panduan deployment untuk Spring Boot 3.3.0 + Thymeleaf + MySQL 8 di VPS Ubuntu/Debian.

- **GitHub Repo:** https://github.com/karimaulya/AbsoluteCinema
- **Stack:** Java 17+, Maven, MySQL 8, (opsional) Nginx

---

## Prasyarat

| Komponen | Versi | Catatan |
|---|---|---|
| OS | Ubuntu 22.04 / Debian 12 (atau serupa) | |
| Java | JDK 17 atau 21 | `apt install openjdk-17-jdk` |
| Maven | 3.8+ | `apt install maven` |
| MySQL | 8.0+ | `apt install mysql-server` |
| Git | any | `apt install git` |
| (Opsional) Nginx | any | Reverse proxy + HTTPS |
| RAM | min. 1 GB, rekomendasi 2 GB | MySQL + JVM |

---

## Step 1 — Install Dependensi

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven git mysql-server nginx ufw
java -version    # pastikan keluar versi
mvn -version     # pastikan keluar versi
```

## Step 2 — Clone Repo

```bash
cd /opt
sudo git clone https://github.com/karimaulya/AbsoluteCinema.git
sudo chown -R $USER:$USER /opt/AbsoluteCinema
cd /opt/AbsoluteCinema
```

## Step 3 — Setup Database MySQL

```bash
sudo mysql -u root <<'SQL'
CREATE DATABASE absolutecinema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cinema_user'@'localhost' IDENTIFIED BY 'GantiPasswordKuatIni123!';
GRANT ALL PRIVILEGES ON absolutecinema.* TO 'cinema_user'@'localhost';
FLUSH PRIVILEGES;
SQL
```

Verifikasi koneksi:

```bash
mysql -u cinema_user -p absolutecinema   # masukkan password di atas
```

## Step 4 — Seed Data (penting, jalankan SEKALI saja setelah aplikasi pertama kali jalan)

File `database_seed.sql` di repo berisi user awal + 3 tayangan contoh. Tabel dibuat otomatis oleh Hibernate (`ddl-auto: update`) saat app pertama start. Jadi:

1. Start app dulu (Step 6/7), biarkan Hibernate buat tabel.
2. Stop app.
3. Import seed:
   ```bash
   mysql -u cinema_user -p absolutecinema < /opt/AbsoluteCinema/database_seed.sql
   ```
4. Start app lagi.

## Step 5 — Build JAR

```bash
cd /opt/AbsoluteCinema
mvn -q clean package -DskipTests
```

Hasil: `target/AbsoluteCinema-0.0.1-SNAPSHOT.jar` (cek nama pastinya dengan `ls target/*.jar`).

## Step 6 — Siapkan Environment Variables

Buat file `/opt/AbsoluteCinema/.env` (jangan di-commit!):

```bash
cat > /opt/AbsoluteCinema/.env <<'EOF'
# Database
DB_URL=jdbc:mysql://localhost:3306/absolutecinema?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USER=cinema_user
DB_PASS=GantiPasswordKuatIni123!

# Server
PORT=8080

# Email (Brevo / Gmail App Password)
MAIL_USERNAME=alamat@email.com
MAIL_PASSWORD=app_password_dari_brevo_atau_gmail

# ImageKit (untuk upload poster tayangan)
IMAGEKIT_PRIVATE_KEY=private_xxxxx
IMAGEKIT_PUBLIC_KEY=public_xxxxx
IMAGEKIT_URL_ENDPOINT=https://ik.imagekit.io/xxxxx

# TMDB (untuk fitur import dari TMDB di halaman admin)
TMDB_API_KEY=xxxxx
EOF
sudo chmod 600 /opt/AbsoluteCinema/.env
```

> `MAIL_*`, `IMAGEKIT_*`, `TMDB_API_KEY` bisa dikosongkan dulu — app tetap jalan, fitur terkait baru error saat dipakai.

## Step 7 — Jalan sebagai Service (systemd) — REKOMENDASI

```bash
sudo tee /etc/systemd/system/absolutecinema.service > /dev/null <<'EOF'
[Unit]
Description=AbsoluteCinema Spring Boot App
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/AbsoluteCinema
EnvironmentFile=/opt/AbsoluteCinema/.env
ExecStart=/usr/bin/java -jar /opt/AbsoluteCinema/target/AbsoluteCinema-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=5
StandardOutput=append:/var/log/absolutecinema.log
StandardError=append:/var/log/absolutecinema.log

[Install]
WantedBy=multi-user.target
EOF

sudo chown www-data:www-data /opt/AbsoluteCinema/.env
sudo touch /var/log/absolutecinema.log && sudo chown www-data:www-data /var/log/absolutecinema.log
sudo systemctl daemon-reload
sudo systemctl enable --now absolutecinema
sudo systemctl status absolutecinema
```

Cek log:

```bash
sudo tail -f /var/log/absolutecinema.log
```

## Step 8 — (Opsional tapi Direkomendasikan) Nginx Reverse Proxy + HTTPS

Buat `/etc/nginx/sites-available/absolutecinema`:

```nginx
server {
    listen 80;
    server_name domain-teman.com ATAU_IP_VPS;

    client_max_body_size 10m;   # penting untuk upload poster

    location / {
        proxy_pass         http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;
    }
}
```

Aktifkan:

```bash
sudo ln -s /etc/nginx/sites-available/absolutecinema /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
sudo ufw allow 'Nginx Full'
sudo ufw allow OpenSSH
sudo ufw enable
```

HTTPS gratis (kalau ada domain):

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d domain-teman.com
```

## Step 9 — Cek Hasil Deploy

- Tanpa Nginx: `http://IP_VPS:8080/` (pastikan `sudo ufw allow 8080` kalau firewall aktif)
- Dengan Nginx: `http://domain-teman.com/` atau `http://IP_VPS/`

Login admin default (dari `database_seed.sql`):

- Username: `admin`
- Password: `admin123`

## Step 10 — Update Aplikasi (kalau ada commit baru)

```bash
cd /opt/AbsoluteCinema
git pull origin main
mvn -q clean package -DskipTests
sudo systemctl restart absolutecinema
```

---

## Troubleshooting

| Gejala | Cek |
|---|---|
| Service gagal start | `sudo journalctl -u absolutecinema -n 100` |
| 500 / DB error | `DB_URL` / `DB_USER` / `DB_PASS` di `.env` salah |
| Halaman login → error kirim OTP | `MAIL_USERNAME`/`MAIL_PASSWORD` salah/kosong |
| Upload poster gagal di admin | `IMAGEKIT_*` kosong atau salah |
| Search TMDB kosong di admin | `TMDB_API_KEY` kosong |
| Build error: Java version | Pastikan `java -version` ≥ 17 |
| Port 8080 dipakai proses lain | `sudo lsof -i :8080` lalu hentikan |

## Ringkasan Path di VPS

```
/opt/AbsoluteCinema/                          # source code + .env
/opt/AbsoluteCinema/.env                       # environment variables (chmod 600!)
/opt/AbsoluteCinema/target/*.jar               # hasil build
/etc/systemd/system/absolutecinema.service     # systemd unit
/var/log/absolutecinema.log                    # log aplikasi
/etc/nginx/sites-available/absolutecinema      # konfigurasi nginx (opsional)
```

## Catatan Keamanan

1. **`.env` jangan di-commit/dishare publik** — berisi kredensial.
2. **Ganti password default `admin123`** setelah login pertama (atau hapus user seed setelah bikin admin baru).
3. **`ddl-auto: update`** akan modifikasi schema otomatis. Aman untuk dev, tapi kalau produksi serius, ganti ke `validate` + pakai migration Flyway/Liquibase.
4. **Backup DB harian**: `mysqldump -u cinema_user -p absolutecinema | gzip > backup-$(date +%F).sql.gz`
