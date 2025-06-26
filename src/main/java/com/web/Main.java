package com.web;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Initializer.init(args);
        }catch (IOException ex){
            System.out.println("um erro aconteceu a iniciar o programa");
        }
    }
}