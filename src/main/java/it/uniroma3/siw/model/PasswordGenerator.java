package it.uniroma3.siw.model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = "provaAdmin"; 
        String hashedPassword = passwordEncoder.encode(password);
        System.out.println("Password hash per l'admin: " + hashedPassword);
    }
}