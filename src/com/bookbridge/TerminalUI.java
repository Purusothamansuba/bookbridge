package com.bookbridge;

public class TerminalUI {

    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";

    public static final String WHITE = "\033[37m";
    public static final String CYAN = "\033[36m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";

    public static final String BG_GREEN = "\033[42m";
    public static final String BG_BLUE = "\033[44m";

    private static final int WIDTH = 62;
    private static boolean initialized = false;

    public static void clear() {
        System.out.print("\033[2J");
        System.out.print("\033[H");
        System.out.flush();
    }

    public static void hideCursor() {
        System.out.print("\033[?25l");
    }

    public static void showCursor() {
        System.out.print("\033[?25h");
    }

    public static void move(int row, int col) {
        System.out.printf("\033[%d;%dH", row, col);
    }

    public static void init(String title, String footer) {
        if (initialized) return;

        clear();
        hideCursor();

        drawBox();
        title(title);
        separator(4);
        footer(footer);

        initialized = true;
    }

    public static void drawBox() {
        move(1, 1);

        System.out.print("╔");
        repeat("═", WIDTH - 2);
        System.out.println("╗");

        for (int i = 0; i < 20; i++) {
            System.out.print("║");
            repeat(" ", WIDTH - 2);
            System.out.println("║");
        }

        System.out.print("╚");
        repeat("═", WIDTH - 2);
        System.out.println("╝");
    }

    public static void title(String text) {
        move(2, 3);

        System.out.print(CYAN + BOLD);

        int left = (WIDTH - text.length()) / 2;

        repeat(" ", left);

        System.out.print(text);

        System.out.print(RESET);
    }

    public static void separator(int row) {
        move(row, 1);

        System.out.print("╠");

        repeat("═", WIDTH - 2);

        System.out.print("╣");
    }

    public static void footer(String text) {
        move(22, 3);

        System.out.print(YELLOW);

        System.out.print(text);

        System.out.print(RESET);
    }

    /* Draw the entire menu once */
    public static void drawMenu(String[] items, int selected) {
        for (int i = 0; i < items.length; i++) {
            drawItem(items[i], i, i == selected);
        }

        System.out.flush();
    }

    /* Draw only one menu row */
    public static void drawItem(String item, int index, boolean selected) {
        int row = 6 + index;

        move(row, 5);

        System.out.print("\033[K");

        if (selected) {
            System.out.print(BG_GREEN);
            System.out.print(WHITE);
            System.out.printf("► %-45s", item);
            System.out.print(RESET);
        } else {
            System.out.printf("  %-45s", item);
        }
    }

    /* Update only two rows */
    public static void updateSelection(
        String[] items,
        int oldSelection,
        int newSelection
    ) {
        drawItem(items[oldSelection], oldSelection, false);

        drawItem(items[newSelection], newSelection, true);

        System.out.flush();
    }

    public static void reset() {
        initialized = false;
    }

    private static void repeat(String s, int n) {
        for (int i = 0; i < n; i++) System.out.print(s);
    }
}
