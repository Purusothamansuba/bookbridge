package com.bookbridge.client.ui;

import java.io.IOException;

public class Input {

    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int ENTER = 5;
    public static final int ESC = 6;
    public static final int Q = 7;
    public static final int OTHER = 0;

    public static void enableRawMode() {
        try {
            Runtime.getRuntime()
                .exec(new String[] {
                    "/bin/sh",
                    "-c",
                    "stty -icanon min 1 -echo < /dev/tty",
                })
                .waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableRawMode() {
        try {
            Runtime.getRuntime()
                .exec(new String[] { "/bin/sh", "-c", "stty sane < /dev/tty" })
                .waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int readKey() throws IOException {
        int ch = System.in.read();

        if (ch == 10 || ch == 13) return ENTER;

        if (ch == 'q' || ch == 'Q') return Q;

        if (ch == 27) {
            if (System.in.read() == '[') {
                switch (System.in.read()) {
                    case 'A':
                        return UP;
                    case 'B':
                        return DOWN;
                    case 'C':
                        return RIGHT;
                    case 'D':
                        return LEFT;
                }
            }

            return ESC;
        }

        return OTHER;
    }
}
