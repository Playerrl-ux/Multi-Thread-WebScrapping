package com.web;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            long start = System.nanoTime();
            Initializer.init(args);
            System.out.println((System.nanoTime() - start) / 1_000_000_000);
        }catch (IOException ex){
            System.out.println("um erro aconteceu a iniciar o programa");
        }
    }
}