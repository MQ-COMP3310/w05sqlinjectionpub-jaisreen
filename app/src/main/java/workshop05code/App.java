package workshop05code;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");

        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("hello_logger created and connected.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            return;
        }

        if (wordleDatabaseConnection.createWordleTables()) {
            System.out.println("hello_logger structures in place.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            return;
        }

        // Add valid words from file
        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.matches("^[a-z]{4}$")) {
                    wordleDatabaseConnection.addValidWord(i, line);
                    logger.info("Valid word added: " + line);
                    i++;
                } else {
                    logger.severe("Invalid word in data.txt: '" + line + "'");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading words from file.", e);
            System.out.println("Could not load words. Please try again later.");
            return;
        }

        // Accept user guesses
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                if (guess.matches("^[a-z]{4}$")) {
                    System.out.println("You've guessed '" + guess + "'.");

                    if (wordleDatabaseConnection.isValidWord(guess)) {
                        System.out.println("Success! It is in the list.\n");
                    } else {
                        System.out.println("Sorry. This word is NOT in the list.\n");
                    }
                } else {
                    System.out.println("The word '" + guess + "' is not a valid word.\n");
                    logger.fine("User entered invalid guess: '" + guess + "'");
                }                

                System.out.print("Enter a 4 letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }

        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Error with user input.", e);
            System.out.println("Something went wrong with input. Please try again.");
        }
    }
}