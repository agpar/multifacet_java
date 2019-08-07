package com.company;

import com.company.data_interface.DataReader;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        Path dataDir = Path.of("/home/aparment/Documents/datasets/yelp");
        DataReader dl = new DataReader(dataDir);
        YelpData yd = new YelpData(dl);
        yd.load(0, 1000);
        System.out.println("Done loading.");
        try {
            System.in.read();
        } catch (Exception e) {

        }
    }
}
