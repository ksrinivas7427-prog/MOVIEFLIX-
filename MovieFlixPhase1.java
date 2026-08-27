import java.io.*;
import java.util.*;

/**
 * MovieFlix Phase 1
 * Searching using Z-Function / Z-Array (Z-Mapping)
 *
 * Input:
 *   movies.txt
 *
 * File format:
 *   ID|Title|Genre|Rating|Year|Actors|Director|Description
 *
 * Phase 1:
 *   1. Load movie information from text file.
 *   2. Search movie titles using the Z-Algorithm.
 *   3. Display the Z-array (Z-mapping).
 *   4. Display match positions, comparisons and complexity.
 */
public class MovieFlixPhase1 {

    static final String DATA_FILE = "movies.txt";

    static class Movie {
        int id;
        String title;
        String genre;
        double rating;
        int year;
        String actors;
        String director;
        String description;

        Movie(int id, String title, String genre, double rating, int year,
              String actors, String director, String description) {
            this.id = id;
            this.title = title;
            this.genre = genre;
            this.rating = rating;
            this.year = year;
            this.actors = actors;
            this.director = director;
            this.description = description;
        }

        void display() {
            System.out.println("\nID        : " + id);
            System.out.println("Title     : " + title);
            System.out.println("Genre     : " + genre);
            System.out.println("Rating    : " + rating);
            System.out.println("Year      : " + year);
            System.out.println("Actors    : " + actors);
            System.out.println("Director  : " + director);
            System.out.println("Description: " + description);
        }
    }

    static class ZResult {
        int[] z;
        List<Integer> positions;
        long comparisons;
        long timeNs;
        String combined;

        ZResult(int[] z, List<Integer> positions, long comparisons,
                long timeNs, String combined) {
            this.z = z;
            this.positions = positions;
            this.comparisons = comparisons;
            this.timeNs = timeNs;
            this.combined = combined;
        }
    }

    /*
     * Z-Function / Z-Array (Z-Mapping)
     *
     * z[i] = length of the longest substring starting at i
     * that matches the prefix of s.
     *
     * The [L, R] window is the current Z-box.
     */
    static int[] zFunction(String s, long[] comparisonCounter) {
        int n = s.length();
        int[] z = new int[n];

        int left = 0;
        int right = 0;

        for (int i = 1; i < n; i++) {

            if (i <= right) {
                z[i] = Math.min(right - i + 1, z[i - left]);
            }

            while (i + z[i] < n) {
                comparisonCounter[0]++;

                if (s.charAt(z[i]) != s.charAt(i + z[i])) {
                    break;
                }

                z[i]++;
            }

            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }
        }

        return z;
    }

    /*
     * Choose a delimiter that does not occur in either
     * the pattern or text.
     */
    static char findDelimiter(String pattern, String text) {
        char[] candidates = {
            '#', '$', '%', '@', '^', '&', '*', '~', '|', '!',
            '\u0001', '\u0002', '\u0003'
        };

        for (char c : candidates) {
            if (pattern.indexOf(c) == -1 && text.indexOf(c) == -1) {
                return c;
            }
        }

        // Last-resort delimiter. Extremely unlikely for normal input.
        return '\u0000';
    }

    /*
     * Search pattern in text using:
     *
     * pattern + delimiter + text
     *
     * If z[i] == pattern.length(), the pattern occurs in text.
     */
    static ZResult zSearch(String text, String pattern) {

        long start = System.nanoTime();

        List<Integer> positions = new ArrayList<>();

        if (text == null || pattern == null) {
            return new ZResult(
                new int[0], positions, 0,
                System.nanoTime() - start, ""
            );
        }

        if (pattern.isEmpty()) {
            // Defined behavior for this project:
            // empty pattern is considered a match at position 0.
            positions.add(0);

            return new ZResult(
                new int[0], positions, 0,
                System.nanoTime() - start, text
            );
        }

        if (text.isEmpty() || pattern.length() > text.length()) {
            return new ZResult(
                new int[0], positions, 0,
                System.nanoTime() - start, ""
            );
        }

        char delimiter = findDelimiter(pattern, text);

        String combined = pattern + delimiter + text;

        long[] comparisonCounter = {0};

        int[] z = zFunction(combined, comparisonCounter);

        int patternLength = pattern.length();

        for (int i = patternLength + 1; i < combined.length(); i++) {

            if (z[i] == patternLength) {

                int textPosition =
                    i - patternLength - 1;

                positions.add(textPosition);
            }
        }

        long end = System.nanoTime();

        return new ZResult(
            z,
            positions,
            comparisonCounter[0],
            end - start,
            combined
        );
    }

    static List<Movie> loadMovies(String filename) {

        List<Movie> movies = new ArrayList<>();

        File file = new File(filename);

        if (!file.exists()) {
            System.out.println("\nERROR: " + filename + " was not found.");
            System.out.println("Keep movies.txt in the same folder as the Java program.");
            return movies;
        }

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                if (parts.length < 8) {
                    System.out.println(
                        "Skipping invalid line: " + line
                    );
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String title = parts[1].trim();
                    String genre = parts[2].trim();
                    double rating = Double.parseDouble(parts[3].trim());
                    int year = Integer.parseInt(parts[4].trim());
                    String actors = parts[5].trim();
                    String director = parts[6].trim();
                    String description = parts[7].trim();

                    movies.add(new Movie(
                        id, title, genre, rating, year,
                        actors, director, description
                    ));

                } catch (NumberFormatException e) {
                    System.out.println(
                        "Skipping invalid numeric data: " + line
                    );
                }
            }

        } catch (IOException e) {
            System.out.println(
                "ERROR reading " + filename + ": " + e.getMessage()
            );
        }

        return movies;
    }

    static void displayAllMovies(List<Movie> movies) {

        System.out.println("\n========== MOVIE DATA ==========");

        if (movies.isEmpty()) {
            System.out.println("No movie data available.");
            return;
        }

        for (Movie movie : movies) {
            System.out.println(
                movie.id + " | " +
                movie.title + " | " +
                movie.genre + " | " +
                movie.rating + " | " +
                movie.year
            );
        }

        System.out.println("================================");
    }

    static void displayZMapping(String combined, int[] z) {

        System.out.println("\n========== Z-MAPPING / Z-ARRAY ==========");

        System.out.printf(
            "%-8s %-12s %-10s%n",
            "Index", "Character", "Z[i]"
        );

        System.out.println("------------------------------------------");

        for (int i = 0; i < combined.length(); i++) {

            char c = combined.charAt(i);

            String displayChar;

            if (c == '\u0000') {
                displayChar = "\\0";
            } else if (c == '\u0001') {
                displayChar = "\\1";
            } else if (c == '\u0002') {
                displayChar = "\\2";
            } else if (c == '\u0003') {
                displayChar = "\\3";
            } else {
                displayChar = String.valueOf(c);
            }

            System.out.printf(
                "%-8d %-12s %-10d%n",
                i, displayChar, z[i]
            );
        }

        System.out.println("==========================================");
    }

    static void demonstrateZFunction() {

        Scanner scanner = new Scanner(System.in);

        System.out.print("\nEnter text: ");
        String text = scanner.nextLine();

        System.out.print("Enter pattern: ");
        String pattern = scanner.nextLine();

        System.out.print(
            "Case-insensitive search? (y/n): "
        );

        String option = scanner.nextLine().trim();

        boolean ignoreCase =
            option.equalsIgnoreCase("y");

        String searchText = ignoreCase
            ? text.toLowerCase(Locale.ROOT)
            : text;

        String searchPattern = ignoreCase
            ? pattern.toLowerCase(Locale.ROOT)
            : pattern;

        ZResult result =
            zSearch(searchText, searchPattern);

        System.out.println("\n========== Z-FUNCTION RESULT ==========");

        System.out.println("Original Text : " + text);
        System.out.println("Pattern       : " + pattern);
        System.out.println("Combined      : " + result.combined);

        if (result.positions.isEmpty()) {
            System.out.println("Match Found   : NO");
        } else {
            System.out.println("Match Found   : YES");
            System.out.println(
                "Match Position(s): " + result.positions
            );
        }

        System.out.println(
            "Character Comparisons: " +
            result.comparisons
        );

        System.out.println(
            "Execution Time: " +
            result.timeNs + " ns"
        );

        System.out.println(
            "Time Complexity: O(n + m)"
        );

        System.out.println(
            "Space Complexity: O(n + m)"
        );

        if (result.z.length > 0) {
            displayZMapping(
                result.combined,
                result.z
            );
        }

        System.out.println(
            "\nZ-Function meaning:"
        );

        System.out.println(
            "Z[i] = length of the longest substring starting at i "
            + "that matches the prefix of the combined string."
        );
    }

    static void searchMovies(List<Movie> movies) {

        Scanner scanner = new Scanner(System.in);

        System.out.print(
            "\nEnter movie title/pattern to search: "
        );

        String pattern = scanner.nextLine();

        if (pattern.trim().isEmpty()) {
            System.out.println(
                "Search pattern cannot be empty."
            );
            return;
        }

        System.out.print(
            "Case-insensitive search? (y/n): "
        );

        boolean ignoreCase =
            scanner.nextLine()
                   .trim()
                   .equalsIgnoreCase("y");

        String normalizedPattern =
            ignoreCase
                ? pattern.toLowerCase(Locale.ROOT)
                : pattern;

        boolean found = false;

        System.out.println(
            "\n========== PHASE 1 MOVIE SEARCH =========="
        );

        System.out.println(
            "Algorithm: Z-Function / Z-Array"
        );

        System.out.println(
            "Pattern: " + pattern
        );

        System.out.println(
            "------------------------------------------"
        );

        for (Movie movie : movies) {

            String title =
                ignoreCase
                    ? movie.title.toLowerCase(Locale.ROOT)
                    : movie.title;

            ZResult result =
                zSearch(title, normalizedPattern);

            if (!result.positions.isEmpty()) {

                found = true;

                System.out.println(
                    "\nMATCH FOUND"
                );

                System.out.println(
                    "Movie ID       : " + movie.id
                );

                System.out.println(
                    "Movie Title    : " + movie.title
                );

                System.out.println(
                    "Genre          : " + movie.genre
                );

                System.out.println(
                    "Rating         : " + movie.rating
                );

                System.out.println(
                    "Year           : " + movie.year
                );

                System.out.println(
                    "Match Position : " +
                    result.positions
                );

                System.out.println(
                    "Comparisons    : " +
                    result.comparisons
                );
            }
        }

        if (!found) {

            System.out.println(
                "\nNo movie title matched: " +
                pattern
            );
        }

        System.out.println(
            "\nTime Complexity : O(n + m) per movie"
        );

        System.out.println(
            "Space Complexity: O(n + m) per movie"
        );

        System.out.println(
            "=========================================="
        );
    }

    static void showMenu() {

        System.out.println(
            "\n\n=========================================="
        );

        System.out.println(
            "       MOVIEFLIX - DSA PHASE 1"
        );

        System.out.println(
            "   Z-FUNCTION / Z-MAPPING SEARCH"
        );

        System.out.println(
            "=========================================="
        );

        System.out.println(
            "1. Display Movies from Text File"
        );

        System.out.println(
            "2. Search Movies using Z-Algorithm"
        );

        System.out.println(
            "3. Demonstrate Z-Function / Z-Mapping"
        );

        System.out.println(
            "4. Reload Movie Data"
        );

        System.out.println(
            "5. Exit"
        );

        System.out.println(
            "=========================================="
        );
    }

    public static void main(String[] args) {

        List<Movie> movies =
            loadMovies(DATA_FILE);

        if (movies.isEmpty()) {

            System.out.println(
                "\nNo movies loaded."
            );

            System.out.println(
                "Make sure movies.txt is in the same folder."
            );
        } else {

            System.out.println(
                "\nSuccessfully loaded " +
                movies.size() +
                " movies from " +
                DATA_FILE
            );
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {

            showMenu();

            System.out.print("Enter choice: ");

            String input = scanner.nextLine();

            int choice;

            try {
                choice = Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println(
                    "Please enter a valid number."
                );
                continue;
            }

            switch (choice) {

                case 1:
                    displayAllMovies(movies);
                    break;

                case 2:
                    searchMovies(movies);
                    break;

                case 3:
                    demonstrateZFunction();
                    break;

                case 4:
                    movies = loadMovies(DATA_FILE);

                    System.out.println(
                        "Reloaded " +
                        movies.size() +
                        " movies."
                    );
                    break;

                case 5:
                    System.out.println(
                        "\nMovieFlix Phase 1 completed."
                    );

                    System.out.println(
                        "Z-Function / Z-Mapping search terminated."
                    );

                    scanner.close();
                    return;

                default:
                    System.out.println(
                        "Invalid choice."
                    );
            }
        }
    }
}
