package nl.MenTych.lvl1;

public class Util {

    public enum Color {

        RESET("\033[0m"),

        // Regular Colors
        BLACK("\033[0;30m"),    // BLACK
        RED("\033[0;31m"),      // RED
        GREEN("\033[0;32m"),    // GREEN
        YELLOW("\033[0;33m"),   // YELLOW
        BLUE("\033[0;34m"),     // BLUE
        MAGENTA("\033[0;35m"),  // MAGENTA
        CYAN("\033[0;36m"),     // CYAN
        WHITE("\033[0;37m");    // WHITE

        private final String code;

        Color(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    public static void printLnWithColor(Color colorCode, String message) {
        System.out.print(colorCode);
        System.out.println(message);
        System.out.print(Color.RESET);
    }

    public static void printWithColor(Color colorCode, String message) {
        System.out.print(colorCode);
        System.out.print(message);
        System.out.print(Color.RESET);
    }
}
