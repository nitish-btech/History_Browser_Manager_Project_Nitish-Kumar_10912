import java.io.*;
import java.util.*;

/**
 * Browser History Manager  (PS No. 16)
 * ---------------------------------------------------------------
 * A console application that simulates basic browser history.
 *
 *  - LinkedList<String>        : stores every visited page in order.
 *  - Stack<String> backStack   : pages we can go BACK to.
 *  - Stack<String> forwardStack: pages we can go FORWARD to.
 *  - HashMap<String,Integer>   : visit count of every page.
 *  - File handling             : history.txt is loaded at start and
 *                                saved on exit.
 *
 * Rules:
 *  - A valid URL must contain a dot (for example: example.com).
 *  - Visiting a new page clears the forward stack.
 *
 * Time complexity (n = number of pages in history):
 *  visitPage O(1) | goBack O(1) | goForward O(1)
 *  showHistory O(n) | showVisitCount O(k log k), k = distinct pages
 */
public class Main {

    // ===================== DATA STRUCTURES =====================
    static LinkedList<String> history = new LinkedList<>();
    static Stack<String> backStack = new Stack<>();
    static Stack<String> forwardStack = new Stack<>();

    // LinkedHashMap is a HashMap that remembers insertion order,
    // so pages with the same visit count are shown in a stable order.
    static HashMap<String, Integer> visitCount = new LinkedHashMap<>();

    static String currentPage = null;
    static final String FILE_NAME = "history.txt";
    static final int EXIT = 7;

    // ===================== URL VALIDATION =====================
    /**
     * A URL is valid when it is not empty, has no spaces, and contains
     * a dot with at least one character before and after it
     * (so "google", ".com", "google." and "a..b" are all invalid).
     */
    static boolean isValidURL(String url) {
        if (url == null) {
            return false;
        }

        url = url.trim();

        if (url.isEmpty()) {
            return false;
        }

        for (int i = 0; i < url.length(); i++) {
            if (Character.isWhitespace(url.charAt(i))) {
                return false;
            }
        }

        int firstDot = url.indexOf('.');
        int lastDot = url.lastIndexOf('.');

        return firstDot > 0
                && lastDot < url.length() - 1
                && !url.contains("..");
    }

    // ===================== CORE FEATURES =====================
    // Visit a new page
    static void visitPage(String url) {
        url = (url == null) ? "" : url.trim();

        if (!isValidURL(url)) {
            System.out.println("Invalid URL. Enter a URL like example.com.");
            return;
        }

        // The page we are leaving becomes the "previous" page
        if (currentPage != null) {
            backStack.push(currentPage);
        }

        currentPage = url;

        // A new visit clears the forward navigation history
        forwardStack.clear();

        // Store the page and update its visit count
        history.add(url);
        visitCount.put(url, visitCount.getOrDefault(url, 0) + 1);

        System.out.println("Visited: " + url);
        System.out.println("Current page: " + currentPage);
    }

    // Go to the previous page
    static void goBack() {
        if (backStack.isEmpty()) {
            System.out.println("No previous page.");
            printCurrentIfAny();
            return;
        }

        forwardStack.push(currentPage);
        currentPage = backStack.pop();

        System.out.println("Back to: " + currentPage);
        System.out.println("Current page: " + currentPage);
    }

    // Go to the next page
    static void goForward() {
        if (forwardStack.isEmpty()) {
            System.out.println("No forward page.");
            printCurrentIfAny();
            return;
        }

        backStack.push(currentPage);
        currentPage = forwardStack.pop();

        System.out.println("Forward to: " + currentPage);
        System.out.println("Current page: " + currentPage);
    }

    // Show all visited pages (oldest first)
    static void showHistory() {
        System.out.println("\n========== BROWSER HISTORY ==========");

        if (history.isEmpty()) {
            System.out.println("No browsing history available.");
            return;
        }

        // for-each uses the list iterator, so this loop is O(n)
        // (history.get(i) inside a loop would be O(n^2) on a LinkedList)
        int number = 1;
        for (String page : history) {
            System.out.println(number + ". " + page);
            number++;
        }

        System.out.println("=====================================");
    }

    // Show how many times each page was visited (most visited first)
    static void showVisitCount() {
        System.out.println("\n========== PAGE VISIT COUNT ==========");

        if (visitCount.isEmpty()) {
            System.out.println("No pages visited yet.");
            return;
        }

        List<Map.Entry<String, Integer>> entries =
                new ArrayList<>(visitCount.entrySet());

        // Stable sort: equal counts keep their original order
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a,
                               Map.Entry<String, Integer> b) {
                return b.getValue() - a.getValue();
            }
        });

        for (Map.Entry<String, Integer> entry : entries) {
            int count = entry.getValue();
            System.out.println(entry.getKey() + " : " + count
                    + (count == 1 ? " visit" : " visits"));
        }

        System.out.println("======================================");
    }

    // Show the page that is open right now
    static void showCurrentPage() {
        if (currentPage == null) {
            System.out.println("No page is currently open.");
        } else {
            System.out.println("Current page: " + currentPage);
        }
    }

    private static void printCurrentIfAny() {
        if (currentPage != null) {
            System.out.println("Current page: " + currentPage);
        }
    }

    // ===================== FILE HANDLING =====================
    // Save everything to history.txt (called when the user exits)
    static void saveHistory() {
        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(new FileWriter(FILE_NAME)))) {

            writer.println("VISIT_COUNTS");
            for (Map.Entry<String, Integer> entry : visitCount.entrySet()) {
                writer.println(entry.getKey() + "|" + entry.getValue());
            }

            writer.println("HISTORY");
            for (String page : history) {
                writer.println(page);
            }

            // Stacks are written bottom -> top, and loaded back in the
            // same order, so their contents are restored exactly.
            writer.println("BACK");
            for (String page : backStack) {
                writer.println(page);
            }

            writer.println("CURRENT");
            if (currentPage != null) {
                writer.println(currentPage);
            }

            writer.println("FORWARD");
            for (String page : forwardStack) {
                writer.println(page);
            }

            writer.println("END");

            if (writer.checkError()) {
                System.out.println("Error saving history: could not write to "
                        + FILE_NAME);
                return;
            }

            System.out.println("History saved successfully.");

        } catch (IOException e) {
            System.out.println("Error saving history: " + e.getMessage());
        }
    }

    // Load saved data from history.txt (called when the program starts)
    static void loadHistory() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("No previous history found. Starting fresh.");
            return;
        }

        String section = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.equals("VISIT_COUNTS")
                        || line.equals("HISTORY")
                        || line.equals("BACK")
                        || line.equals("CURRENT")
                        || line.equals("FORWARD")
                        || line.equals("END")) {
                    section = line;
                    continue;
                }

                if (line.isEmpty()) {
                    continue;
                }

                switch (section) {
                    case "VISIT_COUNTS":
                        // Split at the LAST '|' so a '|' inside a URL
                        // can never break the line.
                        int bar = line.lastIndexOf('|');

                        if (bar > 0 && bar < line.length() - 1) {
                            try {
                                visitCount.put(line.substring(0, bar),
                                        Integer.parseInt(line.substring(bar + 1)));
                            } catch (NumberFormatException e) {
                                System.out.println("Skipped an invalid saved visit count.");
                            }
                        }
                        break;

                    case "HISTORY":
                        history.add(line);
                        break;

                    case "BACK":
                        backStack.push(line);
                        break;

                    case "CURRENT":
                        currentPage = line;
                        break;

                    case "FORWARD":
                        forwardStack.push(line);
                        break;

                    default:
                        // Ignore lines outside a recognised section
                        break;
                }
            }

            System.out.println("Previous history loaded successfully.");

        } catch (IOException e) {
            // Do not keep half-loaded data if the file could not be read
            history.clear();
            backStack.clear();
            forwardStack.clear();
            visitCount.clear();
            currentPage = null;
            System.out.println("Error loading history: " + e.getMessage());
            System.out.println("Starting fresh.");
        }
    }

    // ===================== MENU AND MAIN =====================
    static void displayMenu() {
        System.out.println("\n====================================");
        System.out.println("       BROWSER HISTORY MANAGER");
        System.out.println("====================================");
        System.out.println("1. Visit a new page");
        System.out.println("2. Go Back");
        System.out.println("3. Go Forward");
        System.out.println("4. Show Browsing History");
        System.out.println("5. Show Page Visit Count");
        System.out.println("6. Show Current Page");
        System.out.println("7. Exit");
        System.out.println("====================================");
        System.out.print("Enter your choice: ");
    }

    // Save data and say goodbye
    static void exitProgram() {
        saveHistory();
        System.out.println("Thank you for using Browser History Manager!");
        System.out.println("Program exited successfully.");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Restore saved data before showing the menu
        loadHistory();

        int choice = 0;

        do {
            displayMenu();

            // If the input stream ends (Ctrl+D / Ctrl+Z), save and quit safely
            if (!sc.hasNextLine()) {
                System.out.println();
                choice = EXIT;
                exitProgram();
                continue;
            }

            String input = sc.nextLine().trim();

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number from 1 to 7.");
                choice = 0;
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter URL: ");

                    if (!sc.hasNextLine()) {
                        System.out.println();
                        choice = EXIT;
                        exitProgram();
                        break;
                    }

                    visitPage(sc.nextLine());
                    break;

                case 2:
                    goBack();
                    break;

                case 3:
                    goForward();
                    break;

                case 4:
                    showHistory();
                    break;

                case 5:
                    showVisitCount();
                    break;

                case 6:
                    showCurrentPage();
                    break;

                case EXIT:
                    exitProgram();
                    break;

                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 7.");
                    break;
            }

        } while (choice != EXIT);

        sc.close();
    }
}