/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package configs;

/**
 *
 * @author doans
 */
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncodeData64 {

    public static String enCode(String s) {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        String s2 = Base64.getEncoder().encodeToString(data);
        return s2;
    }

    public static String deCode(String s) {
        byte[] decodedBytes = Base64.getDecoder().decode(s);
        String s2 = new String(decodedBytes, StandardCharsets.UTF_8);
        return s2;
    }

    public static void main(String[] args) {
        // Example usage of enCode method
        String originalString = "0338345459.Doan";
        String encodedString = enCode(originalString);
        System.out.println("Encoded string: " + encodedString);

        // Example usage of deCode method
        String decodedString = deCode(encodedString);
        System.out.println("Decoded string: " + decodedString);
    }
}
