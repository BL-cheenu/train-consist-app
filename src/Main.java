
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("     TRAIN CONSIST MANAGEMENT APP");
        System.out.println("========================================");

        ConsistBuilder builder = new ConsistBuilder(scanner);
        TrainConsist consist = builder.build();

        ConsistPrinter printer = new ConsistPrinter();
        printer.print(consist);

        scanner.close();
    }
}
