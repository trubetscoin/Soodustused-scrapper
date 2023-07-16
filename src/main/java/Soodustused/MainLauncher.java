package Soodustused;

import Soodustused.scrapper.Scrapper;
import org.springframework.boot.SpringApplication;

public class MainLauncher {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-debug")) {
            Scrapper.main(args);
        } else {
            SpringApplication.run(SoodustusedApplication.class, args);
        }
    }
}