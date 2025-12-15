/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pauasms;

/**
 *
 * @author Nkon1
 */
import java.util.Random;

public class CredentialsGenerator {

    private static final Random rand = new Random();

    //Generates 3 random numbers
    private static String generateRandomDigits(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(rand.nextInt(10)); // random digit from 0–9
        }
        return sb.toString();
    }

    //Generates username: name + 3 random numbers
    public static String generateUsername(String name) {
        return name.trim().replaceAll("\\s+", "").toLowerCase() + generateRandomDigits(3);
    }

    //Generates password: Pau + 3 random numbers
    public static String generatePassword() {
        return "Pau" + generateRandomDigits(3);
    }

    //Combines and returns both
    public static String[] generateCredentials(String name) {
        String username = generateUsername(name);
        String password = generatePassword();
        return new String[] { username, password };
    }

    //Used this for a test case
    public static void main(String[] args) {
        String[] creds = generateCredentials("John Doe");
        System.out.println("Username: " + creds[0]);
        System.out.println("Password: " + creds[1]);
    }
}
