package com.bookbridge.client;

import com.bookbridge.client.ui.Menu;
import com.bookbridge.client.ui.WebUI;

public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" 📚 BookBridge Client Application");
        System.out.println("=================================================");

        boolean cliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "--tui".equalsIgnoreCase(arg) || "-t".equalsIgnoreCase(arg)) {
                cliMode = true;
                break;
            }
        }

        try {
            // Test connection to backend socket server
            NetworkClient.connect();
            System.out.println("✅ [Client] Connected to BookBridge Socket Server (port 8080).");

            if (cliMode) {
                System.out.println("🖥️ [Client] Launching Interactive Terminal TUI...");
                Menu.start();
            } else {
                // Default: Start Premium Web UI Server on port 8081
                WebUI.start();
                System.out.println("\n💡 Tip: You can also launch the Terminal UI with: java -cp ... com.bookbridge.client.Main --cli");
            }
        } catch (Exception e) {
            System.err.println("\n❌ Error starting BookBridge Client: " + e.getMessage());
            System.err.println("👉 Please ensure the Socket Server is running first: bash run-server.sh\n");
        }
    }
}
