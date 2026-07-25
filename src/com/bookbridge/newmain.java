package com.bookbridge;

import com.bookbridge.newMenu;

public class newmain {

    public static void main(String[] args) {
        try {
            Input.enableRawMode();

            TerminalUI.init("BOOKBRIDGE", "↑↓ Navigate  Enter Select  q Quit");

            newMenu.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Input.disableRawMode();

            TerminalUI.showCursor();

            TerminalUI.clear();
        }
    }
}
