# JnCK-bot Modification to Entelect Challenge 2020 - Overdrive
> Tugas Besar Mata Kuliah IF2211 Strategi ITB.
> Built by Kelompok 2 - JnCK

## Table of Contents
* [General Info](#general-information)
* [Our Approach](#our-approach)
* [Technologies Used](#technologies-used)
* [Setup](#setup)
* [Usage](#usage)
* [Project Status](#project-status)
* [Room for Improvement](#room-for-improvement)
* [Acknowledgements](#acknowledgements)
* [Contact](#contact)
<!-- * [License](#license) -->


## General Information
[Overdrive](https://github.com/EntelectChallenge/2020-Overdrive) adalah sebuah game yang mempertandingan 2 bot mobil dalam sebuah ajang balapan. Setiap pemain akan memiliki sebuah bot mobil dan masing-masing bot akan saling bertanding untuk mencapai garis finish dan memenangkan pertandingan. Agar dapat memenangkan pertandingan, setiap pemain harus mengimplementasikan strategi tertentu untuk dapat mengalahkan lawannya. 

Pada tugas besar ini, setiap kelompok diminta untuk membuat sebuah bot yang akan diadu dengan bot kelompok lain pada permainan Overdrive di kompetisi Tubes 1. Bot tersebut mengimplementasikan algoritma greedy yang berperan sebagai strategi bot agar dapat menyelesaikan fungsi objektif dari permainan Overdrive, yaitu memenangkan permainan dengan cara mencapai garis finish lebih awal atau mencapai garis finish bersamaan tetapi dengan kecepatan lebih besar atau memiliki skor terbesar jika kedua komponen tersebut masih bernilai imbang. 

## Our Approach
Algoritma yang kelompok kami terapkan adalah algoritma _greedy_ (sesuasi dengan spesifikasi tubes). _Greedy_ yang digunakan adalah hasil penggabungan dari tiga aspek dasar permainan yaitu _cautious_ yang mengurus pergerakan bot dalam memilih dan menghindari _obstacle_, _opportunist_ mengurus segala hal dalam penggunaan _power up_ untuk kepentingan bot kami sendiri, dan _destructive_ berfungsi dalam penggunaan _power up_ penyerangan untuk dapat membuat _bot_ musuh semakin tertinggal ataupun menghentikan gerakan mereka. Hasil dari penggabungan ketiga aspek tersebut menjadikan JnCK-bot sebagai _bot_ yang _all-rounder_, dengan kemampuan seimbang antara penggunaan _power up_ menyerang maupun bertahan dan juga mampu menganalisis _lane_ terbaik.

_Greedy_ yang dilakukan kurang lebih untuk mencari _lane_ terbaik digunakan _greedy_ terhadap _weight_ tiap _lane_, dengan _weight_ ditentukan berdasarkan banyaknya _power up_ dan juga _terrain_. Untuk penggunaan _power up_ aspek _opportunist_ juga dengan pendekatan yang mirip yaitu mencari _lane_ dengan _weight_ paling optimum untuk melakukan _power up_. Sedangkan, untuk _power up_ penyerangan dilakukan pengecekan terhadap kondisi _bot_ saat ini lalu jika kondisi-kondisi terpenuhi maka akan dipilih _power up_ terbaik untuk digunakan berdasarkan _weight_ (prioritas). 

## Technologies Used
- Java (Maven)
- NodeJS

## Setup
Untuk menjalankan permainan dibutuhkan:
- Java Development Kit (Minimal 8)
- IntelliJ IDEA
- NodeJS
- Apache Maven
- Starter Pack Overdrive, bisa diunduh [disini](https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4)

Untuk menggunakan bot sebenarnya tidak perlu di-_build_ lagi karena sudah ada versi _build_ paling baru pada _commit_ terakhir,jika ingin melakukan build:
- Buka folder _repository_ pada IntelliJ IDEA
- Akan muncul pop-up untuk melakukan _load_ terhadap Maven Projet atau jika tidak muncul bisa klik kanan pada `pom.xml` lalu `Add to maven project`
- Load Maven Project tersebut
- Di kanan atas nanti terlihat ada tulisan "Maven", klik tulisan tersebut
- Lalu klik "JnCK-bot", "Lifecycle", dan `install`
- Hasil dari _build_ tersebut akan muncul di folder bin

## Usage
Pada _repository_ tersedia _source code_ beserta _file jar_ dari bot yang kelompok kami buat. Untuk menggunakannya:
- Download starter pack dari Overdrive untuk bisa menjalankan permainan
- Extract starter pack tersebut
- Lakukan `clone` atau unduh _repository_ ini
- Tempatkan folder _repository_ pada _root_ folder yang sama tempat meng-_extract_ starter pack Overdrive. 
- Edit konfigurasi _bot_ pada game-runner-config.json. 
- Pilih 'player-a' ataupun 'player-b'
- Edit path pada salah satu player yang dipilih menjadi menjadi "./" + folder _repository_
- Jalankan _command_ ``start run.bat`` atau klik dua kali pada file ``run.bat``di file explorer.


## Project Status
Project ini sudah  _selesai_ 

## Room for Improvement
Room for improvement:
- Strategi algoritma yang digunakan pada _bot_ ini merupakan strategi _greedy_ sebagai pemenuhan kriteria tugas besar. Pada dasarnya algoritma _greedy_ belum tentu menghasilkan hasil yang terbaik,sehingga untuk _improvement_ bisa dilakukan percobaan terhadap jenis-jenis algoritma lainnya.
- Jenis _greedy_ yang kelompok kami gunakan juga mungkin belum menjadi yang paling optimum, sehingga bisa dilakukan eksplorasi terhadap alternatif-alternatif _greedy_ yang lainnya.
- Bot kami merupakan bot _all-rounder_ yang tidak menggunakan strategi khusus pada skenario tertentu, sehingga mungkin bisa dilakukan berbagai macam strategi _cheesy_ yang memungkinkan.

## Acknowledgements
- Projek ini dikerjakan untuk memenuhi tugas besar mata kuliah IF2211 Strategi Algoritma
- Terima kasih kepada seluruh dosen pengajar mata kuliah IF2211 dan asisten

## Contact
Created by:
- [@azkazkazka](https://github.com/azkazkazka)
- [@apwic](https://github.com/apwic)
- [@EdgarAllanPoo](https://github.com/EdgarAllanPoo)
<!-- Optional -->
<!-- ## License -->
<!-- This project is open source and available under the [... License](). -->

<!-- You don't have to include all sections - just the one's relevant to your project -->
