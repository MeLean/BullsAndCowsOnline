import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;


public class SinglePlayer {
	static void singlePlayer() throws InterruptedException {
		Scanner input = new Scanner(System.in);
		input = new Scanner(System.in);
		System.out
				.println("You have started a single player game.\nThe computer has choosen a number!");
		Random gen = new Random();
		int target = 0;
		do {
			target = (gen.nextInt(9000) + 1000);
		} while (BullsAndCowsOnline.hasDupes(target));

		String targetStr = target + "";
		// System.out.println("Because this is a presentation, I give you a Joker: "
		// + targetStr); //hack which printing chosen number for the
		// presentation comment this

		boolean guessed = false;
		int guesses = 0;
		String errMsg = "Wrong number. ";
		do {
			int bulls = 0;
			int cows = 0;
			System.out.print("Enter your guess: ");
			int guess = 0;

			try {
				input = new Scanner(System.in);
				guess = input.nextInt();
			} catch (InputMismatchException e) {
				System.out.print(errMsg);
				continue;
			}

			if (BullsAndCowsOnline.hasDupes(guess) || guess < 1000 || guess > 9999) {
				System.out.print(errMsg);
				continue;
			}

			guesses++;
			String guessStr = guess + "";

			for (int i = 0; i < guessStr.length(); i++) {
				if (guessStr.charAt(i) == targetStr.charAt(i)) {
					bulls++;
				} else if (targetStr.contains(guessStr.charAt(i) + "")) {
					cows++;
				}
			}
			if (bulls == guessStr.length()) {
				guessed = true;
			} else {
				System.out.println(cows + " Cows and " + bulls + " Bulls.");
			}
		} while (!guessed);
		System.out.println("You won after " + guesses + " guesses!");
	}
}
