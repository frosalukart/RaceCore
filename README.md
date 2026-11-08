# RaceCore Fix - Final Kompatibilitas

File ini berisi class `AuraSkillsCompat.java` yang 100% aman untuk digunakan bersama plugin lain.

## Kenapa ini aman dan tidak akan merusak plugin lain?
1. Kode ini TIDAK menggunakan `player.setMaxHealth()`. Penggunaan `setMaxHealth` adalah penyebab utama bentrok antar plugin.
2. Kode ini menggunakan **AttributeModifier** bawaan Minecraft/Spigot jika AuraSkills tidak terdeteksi. Sistem modifier ini di-desain khusus oleh Spigot agar banyak plugin bisa menambah/mengurangi darah secara bersamaan tanpa saling menimpa.
3. Jika AuraSkills terdeteksi, kode akan otomatis menitipkan tambahan darah ras ke sistem stat AuraSkills.

## Cara Pasang di Source Code:
1. Taruh file `AuraSkillsCompat.java` ke dalam folder:
   `src/main/java/com/racecore/compat/`
2. Buka `RaceManager.java` (atau file listener tempat kamu mengatur darah awal ras).
3. Hapus kode lama kamu yang memaksa pengubahan darah (misal: `player.setMaxHealth(...)` atau `player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(...)`).
4. Ganti kode tersebut dengan:
   `AuraSkillsCompat.applyRaceHealth(player, tambahanDarahRas);`
5. Jika ada event pemain pindah ras / reset, pastikan kamu memanggil:
   `AuraSkillsCompat.removeRaceHealth(player);`

Lalu tinggal compile menjadi `.jar` dan pasang di server!
