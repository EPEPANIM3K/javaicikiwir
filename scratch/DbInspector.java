import java.sql.*;
import java.util.*;

public class DbInspector {
    public static void main(String[] args) throws Exception {
        var list = aplikasdatafilm.PemesananService.cariJadwal("");
        System.out.println("Total jadwal: " + list.size());
        for (var j : list) {
            System.out.println(j.idFilm() + ": " + j.judul() + " | " + j.genre() + " | " + j.mulaiTayang() + " | " + j.hargaTiket() + " | poster: " + j.urlPoster());
        }
    }
}
