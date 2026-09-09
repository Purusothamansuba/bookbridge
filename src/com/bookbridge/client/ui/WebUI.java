package com.bookbridge.client.ui;

import com.bookbridge.client.service.LibraryService;
import com.bookbridge.client.service.RequestService;
import com.bookbridge.model.Book;
import com.bookbridge.model.Branch;
import com.bookbridge.model.PurchaseRequest;
import com.bookbridge.model.TransferRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WebUI {

    private static final int PORT = 8081;

    public static void start() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Core Pages
        server.createContext("/", new IndexHandler());
        server.createContext("/user", new UserDashboardHandler());
        server.createContext("/admin", new AdminDashboardHandler());
        server.createContext("/books", new BooksViewHandler());

        // Actions
        server.createContext("/action/borrow", new BorrowActionHandler());
        server.createContext("/action/return", new ReturnActionHandler());
        server.createContext("/action/transfer", new TransferActionHandler());
        server.createContext("/action/purchase", new PurchaseActionHandler());
        server.createContext("/action/addBook", new AddBookActionHandler());
        server.createContext("/action/deleteBook", new DeleteBookActionHandler());
        server.createContext("/action/updateTransfer", new UpdateTransferActionHandler());
        server.createContext("/action/updatePurchase", new UpdatePurchaseActionHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("=================================================");
        System.out.println(" 🌐 BookBridge Premium Web UI Server Online! ");
        System.out.println(" 🔗 Access Web Portal: http://localhost:" + PORT);
        System.out.println("=================================================");
    }

    // --- HTML TEMPLATE & CSS DESIGN SYSTEM ---
    private static String renderPage(String title, String activeNav, String queryParams, String bodyContent) {
        Map<String, String> params = parseQuery(queryParams);
        String successMsg = params.get("msg");
        String errorMsg = params.get("error");

        StringBuilder flashHtml = new StringBuilder();
        if (successMsg != null && !successMsg.isEmpty()) {
            flashHtml.append("<div class='toast toast-success'><span class='toast-icon'>✓</span> ")
                     .append(escapeHtml(decode(successMsg)))
                     .append("<button class='toast-close' onclick='this.parentElement.remove()'>×</button></div>");
        }
        if (errorMsg != null && !errorMsg.isEmpty()) {
            flashHtml.append("<div class='toast toast-error'><span class='toast-icon'>⚠️</span> ")
                     .append(escapeHtml(decode(errorMsg)))
                     .append("<button class='toast-close' onclick='this.parentElement.remove()'>×</button></div>");
        }

        return "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "  <title>" + escapeHtml(title) + " - BookBridge</title>\n" +
                "  <link rel='preconnect' href='https://fonts.googleapis.com'>\n" +
                "  <link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>\n" +
                "  <link href='https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;600&display=swap' rel='stylesheet'>\n" +
                "  <style>\n" +
                "    :root {\n" +
                "      --bg-dark: #090d16;\n" +
                "      --bg-card: rgba(18, 24, 38, 0.75);\n" +
                "      --bg-card-hover: rgba(28, 37, 58, 0.85);\n" +
                "      --border-color: rgba(255, 255, 255, 0.08);\n" +
                "      --border-focus: #6366f1;\n" +
                "      --primary-gradient: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);\n" +
                "      --accent-cyan: #06b6d4;\n" +
                "      --accent-emerald: #10b981;\n" +
                "      --accent-rose: #f43f5e;\n" +
                "      --accent-amber: #f59e0b;\n" +
                "      --text-main: #f8fafc;\n" +
                "      --text-muted: #94a3b8;\n" +
                "      --radius-sm: 8px;\n" +
                "      --radius-md: 14px;\n" +
                "      --radius-lg: 20px;\n" +
                "      --shadow-glow: 0 0 25px rgba(99, 102, 241, 0.25);\n" +
                "    }\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
                "    body {\n" +
                "      font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;\n" +
                "      background-color: var(--bg-dark);\n" +
                "      background-image: \n" +
                "        radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.15) 0px, transparent 50%),\n" +
                "        radial-gradient(at 100% 100%, rgba(139, 92, 246, 0.12) 0px, transparent 50%);\n" +
                "      background-attachment: fixed;\n" +
                "      color: var(--text-main);\n" +
                "      min-height: 100vh;\n" +
                "      display: flex;\n" +
                "      flex-direction: column;\n" +
                "    }\n" +
                "    /* Header / Navbar */\n" +
                "    .navbar {\n" +
                "      background: rgba(9, 13, 22, 0.8);\n" +
                "      backdrop-filter: blur(16px);\n" +
                "      border-bottom: 1px solid var(--border-color);\n" +
                "      padding: 1rem 2rem;\n" +
                "      display: flex;\n" +
                "      justify-content: space-between;\n" +
                "      align-items: center;\n" +
                "      position: sticky;\n" +
                "      top: 0;\n" +
                "      z-index: 100;\n" +
                "    }\n" +
                "    .nav-brand {\n" +
                "      display: flex;\n" +
                "      align-items: center;\n" +
                "      gap: 12px;\n" +
                "      text-decoration: none;\n" +
                "      color: white;\n" +
                "      font-weight: 800;\n" +
                "      font-size: 1.35rem;\n" +
                "      letter-spacing: -0.5px;\n" +
                "    }\n" +
                "    .nav-brand-icon {\n" +
                "      width: 38px;\n" +
                "      height: 38px;\n" +
                "      background: var(--primary-gradient);\n" +
                "      border-radius: var(--radius-sm);\n" +
                "      display: flex;\n" +
                "      align-items: center;\n" +
                "      justify-content: center;\n" +
                "      font-size: 1.2rem;\n" +
                "      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.35);\n" +
                "    }\n" +
                "    .nav-links {\n" +
                "      display: flex;\n" +
                "      gap: 8px;\n" +
                "      align-items: center;\n" +
                "    }\n" +
                "    .nav-link {\n" +
                "      color: var(--text-muted);\n" +
                "      text-decoration: none;\n" +
                "      padding: 8px 16px;\n" +
                "      border-radius: var(--radius-sm);\n" +
                "      font-size: 0.9rem;\n" +
                "      font-weight: 600;\n" +
                "      transition: all 0.2s ease;\n" +
                "    }\n" +
                "    .nav-link:hover, .nav-link.active {\n" +
                "      color: white;\n" +
                "      background: rgba(255, 255, 255, 0.08);\n" +
                "    }\n" +
                "    .container {\n" +
                "      max-width: 1280px;\n" +
                "      width: 100%;\n" +
                "      margin: 0 auto;\n" +
                "      padding: 2rem;\n" +
                "      flex: 1;\n" +
                "    }\n" +
                "    /* Flash Toast Notifications */\n" +
                "    .toast-container { position: fixed; top: 80px; right: 24px; z-index: 999; display: flex; flex-direction: column; gap: 10px; }\n" +
                "    .toast {\n" +
                "      padding: 14px 20px;\n" +
                "      border-radius: var(--radius-md);\n" +
                "      display: flex;\n" +
                "      align-items: center;\n" +
                "      gap: 12px;\n" +
                "      backdrop-filter: blur(12px);\n" +
                "      box-shadow: 0 8px 24px rgba(0,0,0,0.4);\n" +
                "      animation: slideIn 0.3s ease-out;\n" +
                "      font-size: 0.92rem;\n" +
                "      font-weight: 500;\n" +
                "    }\n" +
                "    @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }\n" +
                "    .toast-success { background: rgba(16, 185, 129, 0.2); border: 1px solid var(--accent-emerald); color: #a7f3d0; }\n" +
                "    .toast-error { background: rgba(244, 63, 94, 0.2); border: 1px solid var(--accent-rose); color: #fecdd3; }\n" +
                "    .toast-close { background: none; border: none; color: inherit; font-size: 1.2rem; cursor: pointer; margin-left: 8px; }\n" +
                "    /* Cards & Components */\n" +
                "    .card {\n" +
                "      background: var(--bg-card);\n" +
                "      border: 1px solid var(--border-color);\n" +
                "      border-radius: var(--radius-lg);\n" +
                "      padding: 1.75rem;\n" +
                "      backdrop-filter: blur(12px);\n" +
                "      box-shadow: 0 8px 32px rgba(0,0,0,0.2);\n" +
                "    }\n" +
                "    .stats-grid {\n" +
                "      display: grid;\n" +
                "      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));\n" +
                "      gap: 1.25rem;\n" +
                "      margin-bottom: 2rem;\n" +
                "    }\n" +
                "    .stat-card {\n" +
                "      background: var(--bg-card);\n" +
                "      border: 1px solid var(--border-color);\n" +
                "      border-radius: var(--radius-md);\n" +
                "      padding: 1.25rem 1.5rem;\n" +
                "      display: flex;\n" +
                "      align-items: center;\n" +
                "      gap: 16px;\n" +
                "      transition: transform 0.2s ease, border-color 0.2s ease;\n" +
                "    }\n" +
                "    .stat-card:hover { transform: translateY(-2px); border-color: rgba(99, 102, 241, 0.4); }\n" +
                "    .stat-icon {\n" +
                "      width: 48px; height: 48px; border-radius: var(--radius-sm); display: flex; align-items: center; justify-content: center; font-size: 1.5rem;\n" +
                "    }\n" +
                "    .stat-val { font-size: 1.75rem; font-weight: 800; color: white; line-height: 1.1; }\n" +
                "    .stat-label { font-size: 0.85rem; color: var(--text-muted); font-weight: 500; }\n" +
                "    /* Buttons */\n" +
                "    .btn {\n" +
                "      display: inline-flex;\n" +
                "      align-items: center;\n" +
                "      justify-content: center;\n" +
                "      gap: 8px;\n" +
                "      padding: 10px 18px;\n" +
                "      border-radius: var(--radius-sm);\n" +
                "      font-weight: 600;\n" +
                "      font-size: 0.9rem;\n" +
                "      cursor: pointer;\n" +
                "      border: none;\n" +
                "      text-decoration: none;\n" +
                "      transition: all 0.2s ease;\n" +
                "    }\n" +
                "    .btn-primary { background: var(--primary-gradient); color: white; box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4); }\n" +
                "    .btn-primary:hover { opacity: 0.95; transform: translateY(-1px); box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5); }\n" +
                "    .btn-success { background: var(--accent-emerald); color: #022c22; font-weight: 700; }\n" +
                "    .btn-success:hover { background: #34d399; }\n" +
                "    .btn-warning { background: var(--accent-amber); color: #451a03; font-weight: 700; }\n" +
                "    .btn-warning:hover { background: #fbbf24; }\n" +
                "    .btn-danger { background: rgba(244, 63, 94, 0.15); color: #fda4af; border: 1px solid rgba(244, 63, 94, 0.4); }\n" +
                "    .btn-danger:hover { background: var(--accent-rose); color: white; }\n" +
                "    .btn-secondary { background: rgba(255, 255, 255, 0.08); color: var(--text-main); }\n" +
                "    .btn-secondary:hover { background: rgba(255, 255, 255, 0.15); color: white; }\n" +
                "    .btn-sm { padding: 6px 12px; font-size: 0.8rem; }\n" +
                "    /* Tables */\n" +
                "    .table-container { width: 100%; overflow-x: auto; margin-top: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); }\n" +
                "    table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.92rem; }\n" +
                "    th { background: rgba(255, 255, 255, 0.04); padding: 14px 18px; color: var(--text-muted); font-weight: 600; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.5px; border-bottom: 1px solid var(--border-color); }\n" +
                "    td { padding: 14px 18px; border-bottom: 1px solid var(--border-color); color: #e2e8f0; }\n" +
                "    tr:hover td { background: rgba(255, 255, 255, 0.02); }\n" +
                "    /* Badges */\n" +
                "    .badge {\n" +
                "      display: inline-flex;\n" +
                "      align-items: center;\n" +
                "      padding: 4px 10px;\n" +
                "      border-radius: 999px;\n" +
                "      font-size: 0.75rem;\n" +
                "      font-weight: 700;\n" +
                "      letter-spacing: 0.3px;\n" +
                "    }\n" +
                "    .badge-green { background: rgba(16, 185, 129, 0.15); color: #6ee7b7; border: 1px solid rgba(16, 185, 129, 0.3); }\n" +
                "    .badge-red { background: rgba(244, 63, 94, 0.15); color: #fda4af; border: 1px solid rgba(244, 63, 94, 0.3); }\n" +
                "    .badge-blue { background: rgba(99, 102, 241, 0.15); color: #a5b4fc; border: 1px solid rgba(99, 102, 241, 0.3); }\n" +
                "    .badge-amber { background: rgba(245, 158, 11, 0.15); color: #fde68a; border: 1px solid rgba(245, 158, 11, 0.3); }\n" +
                "    /* Forms & Inputs */\n" +
                "    .form-group { margin-bottom: 1.25rem; }\n" +
                "    label { display: block; font-size: 0.85rem; font-weight: 600; color: var(--text-muted); margin-bottom: 6px; }\n" +
                "    input, select, textarea {\n" +
                "      width: 100%;\n" +
                "      background: rgba(15, 23, 42, 0.8);\n" +
                "      border: 1px solid var(--border-color);\n" +
                "      color: white;\n" +
                "      padding: 10px 14px;\n" +
                "      border-radius: var(--radius-sm);\n" +
                "      font-size: 0.95rem;\n" +
                "      font-family: inherit;\n" +
                "      outline: none;\n" +
                "      transition: border-color 0.2s;\n" +
                "    }\n" +
                "    input:focus, select:focus, textarea:focus {\n" +
                "      border-color: var(--border-focus);\n" +
                "      box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);\n" +
                "    }\n" +
                "    /* Modals */\n" +
                "    .modal-overlay {\n" +
                "      display: none;\n" +
                "      position: fixed;\n" +
                "      top: 0; left: 0; right: 0; bottom: 0;\n" +
                "      background: rgba(0, 0, 0, 0.75);\n" +
                "      backdrop-filter: blur(8px);\n" +
                "      z-index: 500;\n" +
                "      align-items: center;\n" +
                "      justify-content: center;\n" +
                "    }\n" +
                "    .modal-overlay.active { display: flex; }\n" +
                "    .modal {\n" +
                "      background: #111827;\n" +
                "      border: 1px solid var(--border-color);\n" +
                "      border-radius: var(--radius-lg);\n" +
                "      width: 100%;\n" +
                "      max-width: 500px;\n" +
                "      padding: 2rem;\n" +
                "      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6);\n" +
                "      animation: modalPop 0.2s ease-out;\n" +
                "    }\n" +
                "    @keyframes modalPop { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }\n" +
                "    .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }\n" +
                "    .modal-title { font-size: 1.25rem; font-weight: 700; color: white; }\n" +
                "    .modal-close { background: none; border: none; font-size: 1.5rem; color: var(--text-muted); cursor: pointer; }\n" +
                "    .footer { text-align: center; padding: 2rem; color: var(--text-muted); font-size: 0.85rem; border-top: 1px solid var(--border-color); }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <nav class='navbar'>\n" +
                "    <a href='/' class='nav-brand'>\n" +
                "      <div class='nav-brand-icon'>📚</div>\n" +
                "      <span>BookBridge</span>\n" +
                "    </a>\n" +
                "    <div class='nav-links'>\n" +
                "      <a href='/' class='nav-link " + (activeNav.equals("home") ? "active" : "") + "'>Home</a>\n" +
                "      <a href='/user' class='nav-link " + (activeNav.equals("user") ? "active" : "") + "'>User Portal</a>\n" +
                "      <a href='/admin' class='nav-link " + (activeNav.equals("admin") ? "active" : "") + "'>Admin Portal</a>\n" +
                "      <a href='/books' class='nav-link " + (activeNav.equals("books") ? "active" : "") + "'>All Books</a>\n" +
                "    </div>\n" +
                "  </nav>\n" +
                "  <div class='toast-container'>" + flashHtml + "</div>\n" +
                "  <main class='container'>\n" +
                bodyContent +
                "  </main>\n" +
                "  <footer class='footer'>\n" +
                "    <p>BookBridge Distributed Library Management System • High-Concurrency Architecture</p>\n" +
                "  </footer>\n" +
                "  <script>\n" +
                "    function openModal(id) { document.getElementById(id).classList.add('active'); }\n" +
                "    function closeModal(id) { document.getElementById(id).classList.remove('active'); }\n" +
                "    window.onclick = function(event) {\n" +
                "      if (event.target.classList.contains('modal-overlay')) {\n" +
                "        event.target.classList.remove('active');\n" +
                "      }\n" +
                "    }\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private static void sendHtml(HttpExchange t, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        t.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void redirect(HttpExchange t, String location) throws IOException {
        t.getResponseHeaders().set("Location", location);
        t.sendResponseHeaders(302, -1);
    }

    // --- 1. INDEX / LANDING HANDLER ---
    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!t.getRequestURI().getPath().equals("/")) {
                t.sendResponseHeaders(404, -1);
                return;
            }

            Map<String, Object> stats = LibraryService.getSystemStatistics();
            List<Branch> branches = LibraryService.getBranches();

            StringBuilder branchOptions = new StringBuilder();
            for (Branch b : branches) {
                branchOptions.append("<option value='").append(b.getBranchId()).append("'>")
                             .append(escapeHtml(b.getBranchName())).append(" (").append(escapeHtml(b.getLocation())).append(")</option>");
            }

            String html =
                "<div style='text-align: center; max-width: 720px; margin: 2rem auto 3rem;'>\n" +
                "  <h1 style='font-size: 3rem; font-weight: 800; letter-spacing: -1px; margin-bottom: 1rem; background: var(--primary-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent;'>Bridging Libraries, Connecting Readers.</h1>\n" +
                "  <p style='color: var(--text-muted); font-size: 1.15rem;'>Browse interconnected branch catalogs, borrow books instantly, and request seamless inter-branch transfers.</p>\n" +
                "</div>\n" +
                "<div class='stats-grid'>\n" +
                "  <div class='stat-card'>\n" +
                "    <div class='stat-icon' style='background: rgba(99, 102, 241, 0.15); color: #818cf8;'>📖</div>\n" +
                "    <div><div class='stat-val'>" + stats.getOrDefault("totalTitles", 0) + "</div><div class='stat-label'>Book Titles</div></div>\n" +
                "  </div>\n" +
                "  <div class='stat-card'>\n" +
                "    <div class='stat-icon' style='background: rgba(16, 185, 129, 0.15); color: #34d399;'>📦</div>\n" +
                "    <div><div class='stat-val'>" + stats.getOrDefault("totalCopies", 0) + "</div><div class='stat-label'>Available Copies</div></div>\n" +
                "  </div>\n" +
                "  <div class='stat-card'>\n" +
                "    <div class='stat-icon' style='background: rgba(6, 182, 212, 0.15); color: #22d3ee;'>🏛️</div>\n" +
                "    <div><div class='stat-val'>" + stats.getOrDefault("totalBranches", 0) + "</div><div class='stat-label'>Active Branches</div></div>\n" +
                "  </div>\n" +
                "  <div class='stat-card'>\n" +
                "    <div class='stat-icon' style='background: rgba(245, 158, 11, 0.15); color: #fbbf24;'>🔄</div>\n" +
                "    <div><div class='stat-val'>" + stats.getOrDefault("pendingTransfers", 0) + "</div><div class='stat-label'>Pending Transfers</div></div>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<div style='display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 2rem; margin-top: 1.5rem;'>\n" +
                "  <div class='card'>\n" +
                "    <div style='font-size: 2rem; margin-bottom: 1rem;'>👤</div>\n" +
                "    <h2 style='font-size: 1.5rem; margin-bottom: 0.5rem;'>Member Portal</h2>\n" +
                "    <p style='color: var(--text-muted); margin-bottom: 1.5rem; font-size: 0.95rem;'>Borrow and return books, request inter-branch transfers, or suggest new acquisitions.</p>\n" +
                "    <form action='/user' method='GET'>\n" +
                "      <div class='form-group'>\n" +
                "        <label>Your Name</label>\n" +
                "        <input type='text' name='name' placeholder='e.g. Purushothaman' required value='Purushothaman'>\n" +
                "      </div>\n" +
                "      <div class='form-group'>\n" +
                "        <label>Select Home Branch</label>\n" +
                "        <select name='branch'>" + branchOptions + "</select>\n" +
                "      </div>\n" +
                "      <button type='submit' class='btn btn-primary' style='width: 100%; padding: 12px;'>Enter Member Portal →</button>\n" +
                "    </form>\n" +
                "  </div>\n" +
                "  <div class='card'>\n" +
                "    <div style='font-size: 2rem; margin-bottom: 1rem;'>🛡️</div>\n" +
                "    <h2 style='font-size: 1.5rem; margin-bottom: 0.5rem;'>Librarian & Admin Portal</h2>\n" +
                "    <p style='color: var(--text-muted); margin-bottom: 1.5rem; font-size: 0.95rem;'>Manage book catalog inventory, approve inter-branch transfers, and review purchase requests.</p>\n" +
                "    <div style='margin-top: 2.5rem;'>\n" +
                "      <a href='/admin' class='btn btn-secondary' style='width: 100%; padding: 12px;'>Open Admin Dashboard ⚙️</a>\n" +
                "      <a href='/books' class='btn btn-secondary' style='width: 100%; padding: 12px; margin-top: 10px;'>Browse Public Catalog 📖</a>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>";

            sendHtml(t, renderPage("Home", "home", t.getRequestURI().getQuery(), html));
        }
    }

    // --- 2. USER DASHBOARD HANDLER ---
    static class UserDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Map<String, String> query = parseQuery(t.getRequestURI().getQuery());
            String userName = query.getOrDefault("name", "Reader");
            int branchId = 1;
            try {
                if (query.containsKey("branch")) branchId = Integer.parseInt(query.get("branch"));
            } catch (Exception ignored) {}

            String searchQuery = query.getOrDefault("q", "");
            String branchName = LibraryService.getBranchName(branchId);
            List<Book> books = searchQuery.isEmpty() ? LibraryService.fetchAllBooks() : LibraryService.searchBooks(searchQuery, null);
            List<Branch> branches = LibraryService.getBranches();

            StringBuilder tableRows = new StringBuilder();
            for (Book b : books) {
                boolean inCurrentBranch = (b.getBranchId() == branchId);
                boolean hasStock = (b.getAvailableCopies() > 0);

                tableRows.append("<tr>")
                         .append("<td><span style='font-family: JetBrains Mono; font-size: 0.85rem; color: var(--text-muted);'>#").append(b.getBookId()).append("</span></td>")
                         .append("<td><strong style='color: white; font-size: 1rem;'>").append(escapeHtml(b.getTitle())).append("</strong><br><span style='font-size: 0.8rem; color: var(--text-muted);'>").append(escapeHtml(b.getCategory())).append("</span></td>")
                         .append("<td>").append(escapeHtml(b.getAuthor())).append("</td>")
                         .append("<td><span class='badge ").append(b.getBranchId() == branchId ? "badge-blue" : "badge-amber").append("'>").append(escapeHtml(b.getBranchName() != null ? b.getBranchName() : "Branch " + b.getBranchId())).append("</span></td>")
                         .append("<td><span class='badge ").append(hasStock ? "badge-green" : "badge-red").append("'>").append(b.getAvailableCopies()).append(" Copies</span></td>")
                         .append("<td><div style='display: flex; gap: 8px;'>");

                if (inCurrentBranch) {
                    if (hasStock) {
                        tableRows.append("<form action='/action/borrow' method='POST' style='display:inline;'>")
                                 .append("<input type='hidden' name='bookId' value='").append(b.getBookId()).append("'>")
                                 .append("<input type='hidden' name='branch' value='").append(branchId).append("'>")
                                 .append("<input type='hidden' name='name' value='").append(escapeHtml(userName)).append("'>")
                                 .append("<button type='submit' class='btn btn-success btn-sm'>Borrow</button></form>");
                    } else {
                        tableRows.append("<button class='btn btn-secondary btn-sm' disabled>Out of Stock</button>");
                    }
                } else {
                    tableRows.append("<button type='button' onclick=\"openTransferModal('").append(escapeHtml(b.getTitle())).append("', '").append(escapeHtml(b.getBranchName())).append("')\" class='btn btn-warning btn-sm'>Request Transfer</button>");
                }

                tableRows.append("</div></td></tr>");
            }

            if (books.isEmpty()) {
                tableRows.append("<tr><td colspan='6' style='text-align: center; padding: 2rem; color: var(--text-muted);'>No books matched your search. <button onclick=\"openModal('modalPurchase')\" class='btn btn-primary btn-sm' style='margin-left: 10px;'>Request to Purchase</button></td></tr>");
            }

            String html =
                "<div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;'>\n" +
                "  <div>\n" +
                "    <h1 style='font-size: 1.85rem; font-weight: 800;'>👋 Welcome, " + escapeHtml(userName) + "</h1>\n" +
                "    <p style='color: var(--text-muted);'>Current Active Branch: <span class='badge badge-blue' style='font-size: 0.85rem;'>" + escapeHtml(branchName) + "</span></p>\n" +
                "  </div>\n" +
                "  <div style='display: flex; gap: 10px;'>\n" +
                "    <button onclick=\"openModal('modalReturn')\" class='btn btn-secondary'>🔄 Return a Book</button>\n" +
                "    <button onclick=\"openModal('modalPurchase')\" class='btn btn-primary'>✨ Suggest Purchase</button>\n" +
                "    <a href='/' class='btn btn-secondary'>Logout</a>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<div class='card' style='margin-bottom: 2rem;'>\n" +
                "  <form action='/user' method='GET' style='display: flex; gap: 12px; flex-wrap: wrap;'>\n" +
                "    <input type='hidden' name='name' value='" + escapeHtml(userName) + "'>\n" +
                "    <input type='hidden' name='branch' value='" + branchId + "'>\n" +
                "    <input type='text' name='q' placeholder='Search by title, author, category, or Book ID...' value='" + escapeHtml(searchQuery) + "' style='flex: 1; min-width: 260px;'>\n" +
                "    <button type='submit' class='btn btn-primary'>🔍 Search</button>\n" +
                (searchQuery.isEmpty() ? "" : "    <a href='/user?name=" + escapeHtml(userName) + "&branch=" + branchId + "' class='btn btn-secondary'>Clear</a>\n") +
                "  </form>\n" +
                "</div>\n" +
                "<div class='card'>\n" +
                "  <h2 style='font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem;'>Inter-Branch Catalog</h2>\n" +
                "  <div class='table-container'>\n" +
                "    <table>\n" +
                "      <thead>\n" +
                "        <tr><th>ID</th><th>Book Title</th><th>Author</th><th>Branch Location</th><th>Stock</th><th>Action</th></tr>\n" +
                "      </thead>\n" +
                "      <tbody>" + tableRows + "</tbody>\n" +
                "    </table>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<!-- MODAL: TRANSFER REQUEST -->\n" +
                "<div id='modalTransfer' class='modal-overlay'>\n" +
                "  <div class='modal'>\n" +
                "    <div class='modal-header'>\n" +
                "      <h3 class='modal-title'>🔄 Request Inter-Branch Transfer</h3>\n" +
                "      <button class='modal-close' onclick=\"closeModal('modalTransfer')\">×</button>\n" +
                "    </div>\n" +
                "    <form action='/action/transfer' method='POST'>\n" +
                "      <input type='hidden' name='userName' value='" + escapeHtml(userName) + "'>\n" +
                "      <input type='hidden' name='userBranch' value='" + branchId + "'>\n" +
                "      <div class='form-group'><label>Book Name</label><input type='text' id='tfBookName' name='bookName' required readonly></div>\n" +
                "      <div class='form-group'><label>From Branch</label><input type='text' id='tfFromBranch' name='fromBranch' required readonly></div>\n" +
                "      <div class='form-group'><label>Deliver To Branch</label><input type='text' name='toBranch' value='" + escapeHtml(branchName) + "' required readonly></div>\n" +
                "      <button type='submit' class='btn btn-primary' style='width: 100%;'>Submit Transfer Request</button>\n" +
                "    </form>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<!-- MODAL: RETURN BOOK -->\n" +
                "<div id='modalReturn' class='modal-overlay'>\n" +
                "  <div class='modal'>\n" +
                "    <div class='modal-header'>\n" +
                "      <h3 class='modal-title'>🔄 Return Borrowed Book</h3>\n" +
                "      <button class='modal-close' onclick=\"closeModal('modalReturn')\">×</button>\n" +
                "    </div>\n" +
                "    <form action='/action/return' method='POST'>\n" +
                "      <input type='hidden' name='userName' value='" + escapeHtml(userName) + "'>\n" +
                "      <input type='hidden' name='branch' value='" + branchId + "'>\n" +
                "      <div class='form-group'><label>Book ID</label><input type='number' name='bookId' placeholder='e.g. 101' required></div>\n" +
                "      <p style='color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1rem;'>Returning to: <strong>" + escapeHtml(branchName) + "</strong></p>\n" +
                "      <button type='submit' class='btn btn-success' style='width: 100%;'>Confirm Return</button>\n" +
                "    </form>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<!-- MODAL: PURCHASE SUGGESTION -->\n" +
                "<div id='modalPurchase' class='modal-overlay'>\n" +
                "  <div class='modal'>\n" +
                "    <div class='modal-header'>\n" +
                "      <h3 class='modal-title'>✨ Suggest New Book Purchase</h3>\n" +
                "      <button class='modal-close' onclick=\"closeModal('modalPurchase')\">×</button>\n" +
                "    </div>\n" +
                "    <form action='/action/purchase' method='POST'>\n" +
                "      <input type='hidden' name='userName' value='" + escapeHtml(userName) + "'>\n" +
                "      <input type='hidden' name='branch' value='" + branchId + "'>\n" +
                "      <div class='form-group'><label>Book Title</label><input type='text' name='bookName' placeholder='e.g. Systems Performance' required></div>\n" +
                "      <div class='form-group'><label>Author</label><input type='text' name='author' placeholder='e.g. Brendan Gregg' required></div>\n" +
                "      <button type='submit' class='btn btn-primary' style='width: 100%;'>Submit Purchase Request</button>\n" +
                "    </form>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<script>\n" +
                "  function openTransferModal(book, from) {\n" +
                "    document.getElementById('tfBookName').value = book;\n" +
                "    document.getElementById('tfFromBranch').value = from;\n" +
                "    openModal('modalTransfer');\n" +
                "  }\n" +
                "</script>";

            sendHtml(t, renderPage("Member Dashboard", "user", t.getRequestURI().getQuery(), html));
        }
    }

    // --- 3. ADMIN DASHBOARD HANDLER ---
    static class AdminDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            List<Book> books = LibraryService.fetchAllBooks();
            List<Branch> branches = LibraryService.getBranches();
            List<TransferRequest> transferRequests = RequestService.fetchTransferRequests();
            List<PurchaseRequest> purchaseRequests = RequestService.fetchPurchaseRequests();

            // Book rows
            StringBuilder bookRows = new StringBuilder();
            for (Book b : books) {
                bookRows.append("<tr>")
                        .append("<td>#").append(b.getBookId()).append("</td>")
                        .append("<td><strong>").append(escapeHtml(b.getTitle())).append("</strong></td>")
                        .append("<td>").append(escapeHtml(b.getAuthor())).append("</td>")
                        .append("<td>").append(escapeHtml(b.getBranchName() != null ? b.getBranchName() : "Branch " + b.getBranchId())).append("</td>")
                        .append("<td><span class='badge badge-blue'>").append(b.getAvailableCopies()).append(" Copies</span></td>")
                        .append("<td><form action='/action/deleteBook' method='POST' onsubmit=\"return confirm('Delete this book?')\">")
                        .append("<input type='hidden' name='bookId' value='").append(b.getBookId()).append("'>")
                        .append("<button type='submit' class='btn btn-danger btn-sm'>Delete</button></form></td>")
                        .append("</tr>");
            }

            // Transfer request rows
            StringBuilder trRows = new StringBuilder();
            for (TransferRequest tr : transferRequests) {
                trRows.append("<tr>")
                      .append("<td>#").append(tr.getId()).append("</td>")
                      .append("<td><strong>").append(escapeHtml(tr.getBookName())).append("</strong></td>")
                      .append("<td>").append(escapeHtml(tr.getFromBranch())).append(" → ").append(escapeHtml(tr.getToBranch())).append("</td>")
                      .append("<td>").append(escapeHtml(tr.getRequesterName())).append("</td>")
                      .append("<td><span class='badge ").append("PENDING".equals(tr.getStatus()) ? "badge-amber" : "badge-green").append("'>").append(tr.getStatus()).append("</span></td>")
                      .append("<td><div style='display:flex; gap:6px;'>");

                if ("PENDING".equalsIgnoreCase(tr.getStatus())) {
                    trRows.append("<form action='/action/updateTransfer' method='POST'>")
                          .append("<input type='hidden' name='id' value='").append(tr.getId()).append("'>")
                          .append("<input type='hidden' name='status' value='APPROVED'>")
                          .append("<button type='submit' class='btn btn-success btn-sm'>Approve</button></form>")
                          .append("<form action='/action/updateTransfer' method='POST'>")
                          .append("<input type='hidden' name='id' value='").append(tr.getId()).append("'>")
                          .append("<input type='hidden' name='status' value='REJECTED'>")
                          .append("<button type='submit' class='btn btn-danger btn-sm'>Reject</button></form>");
                } else {
                    trRows.append("<span style='color: var(--text-muted); font-size: 0.8rem;'>Resolved</span>");
                }
                trRows.append("</div></td></tr>");
            }
            if (transferRequests.isEmpty()) {
                trRows.append("<tr><td colspan='6' style='text-align:center; color: var(--text-muted);'>No transfer requests.</td></tr>");
            }

            // Purchase request rows
            StringBuilder prRows = new StringBuilder();
            for (PurchaseRequest pr : purchaseRequests) {
                prRows.append("<tr>")
                      .append("<td>#").append(pr.getId()).append("</td>")
                      .append("<td><strong>").append(escapeHtml(pr.getBookName())).append("</strong></td>")
                      .append("<td>").append(escapeHtml(pr.getAuthor())).append("</td>")
                      .append("<td>").append(escapeHtml(pr.getRequesterName())).append("</td>")
                      .append("<td><span class='badge ").append("PENDING".equals(pr.getStatus()) ? "badge-amber" : "badge-green").append("'>").append(pr.getStatus()).append("</span></td>")
                      .append("<td><div style='display:flex; gap:6px;'>");

                if ("PENDING".equalsIgnoreCase(pr.getStatus())) {
                    prRows.append("<form action='/action/updatePurchase' method='POST'>")
                          .append("<input type='hidden' name='id' value='").append(pr.getId()).append("'>")
                          .append("<input type='hidden' name='status' value='APPROVED'>")
                          .append("<button type='submit' class='btn btn-success btn-sm'>Approve</button></form>")
                          .append("<form action='/action/updatePurchase' method='POST'>")
                          .append("<input type='hidden' name='id' value='").append(pr.getId()).append("'>")
                          .append("<input type='hidden' name='status' value='ORDERED'>")
                          .append("<button type='submit' class='btn btn-primary btn-sm'>Order</button></form>");
                } else {
                    prRows.append("<span style='color: var(--text-muted); font-size: 0.8rem;'>Resolved</span>");
                }
                prRows.append("</div></td></tr>");
            }
            if (purchaseRequests.isEmpty()) {
                prRows.append("<tr><td colspan='6' style='text-align:center; color: var(--text-muted);'>No purchase requests.</td></tr>");
            }

            StringBuilder branchOpts = new StringBuilder();
            for (Branch b : branches) {
                branchOpts.append("<option value='").append(b.getBranchId()).append("'>")
                          .append(escapeHtml(b.getBranchName())).append("</option>");
            }

            String html =
                "<div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;'>\n" +
                "  <div>\n" +
                "    <h1 style='font-size: 1.85rem; font-weight: 800;'>🛡️ Librarian & Admin Console</h1>\n" +
                "    <p style='color: var(--text-muted);'>Complete Inventory Control, Branch Transfers & Acquisitions Management</p>\n" +
                "  </div>\n" +
                "  <button onclick=\"openModal('modalAddBook')\" class='btn btn-primary'>+ Add New Book</button>\n" +
                "</div>\n" +
                "<div style='display: grid; grid-template-columns: 1fr; gap: 2rem;'>\n" +
                "  <!-- Catalog Management -->\n" +
                "  <div class='card'>\n" +
                "    <h2 style='font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem;'>📚 Library Catalog Inventory (" + books.size() + " Titles)</h2>\n" +
                "    <div class='table-container'>\n" +
                "      <table><thead><tr><th>ID</th><th>Title</th><th>Author</th><th>Branch</th><th>Copies</th><th>Manage</th></tr></thead>\n" +
                "      <tbody>" + bookRows + "</tbody></table>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <!-- Inter-Branch Transfers -->\n" +
                "  <div class='card'>\n" +
                "    <h2 style='font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem;'>🔄 Inter-Branch Transfer Requests</h2>\n" +
                "    <div class='table-container'>\n" +
                "      <table><thead><tr><th>ID</th><th>Book</th><th>Route</th><th>Requester</th><th>Status</th><th>Action</th></tr></thead>\n" +
                "      <tbody>" + trRows + "</tbody></table>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "  <!-- Purchase Requests -->\n" +
                "  <div class='card'>\n" +
                "    <h2 style='font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem;'>✨ New Book Purchase Requests</h2>\n" +
                "    <div class='table-container'>\n" +
                "      <table><thead><tr><th>ID</th><th>Book Title</th><th>Author</th><th>Requester</th><th>Status</th><th>Action</th></tr></thead>\n" +
                "      <tbody>" + prRows + "</tbody></table>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>\n" +
                "<!-- MODAL: ADD BOOK -->\n" +
                "<div id='modalAddBook' class='modal-overlay'>\n" +
                "  <div class='modal'>\n" +
                "    <div class='modal-header'>\n" +
                "      <h3 class='modal-title'>➕ Add New Book to Catalog</h3>\n" +
                "      <button class='modal-close' onclick=\"closeModal('modalAddBook')\">×</button>\n" +
                "    </div>\n" +
                "    <form action='/action/addBook' method='POST'>\n" +
                "      <div class='form-group'><label>Book ID (Integer)</label><input type='number' name='bookId' placeholder='e.g. 109' required></div>\n" +
                "      <div class='form-group'><label>Book Title</label><input type='text' name='title' placeholder='e.g. Modern Software Engineering' required></div>\n" +
                "      <div class='form-group'><label>Author</label><input type='text' name='author' placeholder='e.g. Dave Farley' required></div>\n" +
                "      <div class='form-group'><label>Category / Genre</label><input type='text' name='category' placeholder='e.g. Software Engineering' value='General'></div>\n" +
                "      <div class='form-group'><label>Available Copies</label><input type='number' name='copies' value='3' min='1' required></div>\n" +
                "      <div class='form-group'><label>Assign to Branch</label><select name='branchId'>" + branchOpts + "</select></div>\n" +
                "      <button type='submit' class='btn btn-primary' style='width: 100%;'>Add Book to Database</button>\n" +
                "    </form>\n" +
                "  </div>\n" +
                "</div>";

            sendHtml(t, renderPage("Admin Console", "admin", t.getRequestURI().getQuery(), html));
        }
    }

    // --- 4. BOOKS PUBLIC VIEW HANDLER ---
    static class BooksViewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            List<Book> books = LibraryService.fetchAllBooks();
            StringBuilder rows = new StringBuilder();
            for (Book b : books) {
                rows.append("<tr>")
                    .append("<td>#").append(b.getBookId()).append("</td>")
                    .append("<td><strong>").append(escapeHtml(b.getTitle())).append("</strong></td>")
                    .append("<td>").append(escapeHtml(b.getAuthor())).append("</td>")
                    .append("<td>").append(escapeHtml(b.getCategory())).append("</td>")
                    .append("<td><span class='badge badge-blue'>").append(escapeHtml(b.getBranchName() != null ? b.getBranchName() : "Branch " + b.getBranchId())).append("</span></td>")
                    .append("<td><span class='badge ").append(b.getAvailableCopies() > 0 ? "badge-green" : "badge-red").append("'>").append(b.getAvailableCopies()).append(" Copies</span></td>")
                    .append("</tr>");
            }

            String html =
                "<div style='margin-bottom: 2rem;'>\n" +
                "  <h1 style='font-size: 1.85rem; font-weight: 800;'>📚 Complete Catalog Directory</h1>\n" +
                "  <p style='color: var(--text-muted);'>View available books across all connected library branches</p>\n" +
                "</div>\n" +
                "<div class='card'>\n" +
                "  <div class='table-container'>\n" +
                "    <table><thead><tr><th>ID</th><th>Title</th><th>Author</th><th>Category</th><th>Branch</th><th>Copies</th></tr></thead>\n" +
                "    <tbody>" + rows + "</tbody></table>\n" +
                "  </div>\n" +
                "</div>";

            sendHtml(t, renderPage("Catalog", "books", t.getRequestURI().getQuery(), html));
        }
    }

    // --- ACTION HANDLERS ---

    static class BorrowActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) { redirect(t, "/user"); return; }
            Map<String, String> body = parseBody(t.getRequestBody());
            int bookId = Integer.parseInt(body.get("bookId"));
            int branch = Integer.parseInt(body.get("branch"));
            String name = body.getOrDefault("name", "User");
            try {
                String res = LibraryService.borrowBook(bookId, branch);
                redirect(t, "/user?name=" + encode(name) + "&branch=" + branch + "&msg=" + encode("Book borrowed successfully! " + res));
            } catch (Exception e) {
                redirect(t, "/user?name=" + encode(name) + "&branch=" + branch + "&error=" + encode(e.getMessage()));
            }
        }
    }

    static class ReturnActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) { redirect(t, "/user"); return; }
            Map<String, String> body = parseBody(t.getRequestBody());
            int bookId = Integer.parseInt(body.get("bookId"));
            int branch = Integer.parseInt(body.get("branch"));
            String name = body.getOrDefault("userName", "User");
            try {
                LibraryService.returnBook(bookId, branch);
                redirect(t, "/user?name=" + encode(name) + "&branch=" + branch + "&msg=" + encode("Book returned successfully to this branch!"));
            } catch (Exception e) {
                redirect(t, "/user?name=" + encode(name) + "&branch=" + branch + "&error=" + encode(e.getMessage()));
            }
        }
    }

    static class TransferActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) { redirect(t, "/user"); return; }
            Map<String, String> body = parseBody(t.getRequestBody());
            String bookName = body.get("bookName");
            String fromBranch = body.get("fromBranch");
            String toBranch = body.get("toBranch");
            String name = body.getOrDefault("userName", "User");
            String userBranch = body.getOrDefault("userBranch", "1");
            try {
                RequestService.addTransferRequest(bookName, fromBranch, toBranch, name);
                redirect(t, "/user?name=" + encode(name) + "&branch=" + userBranch + "&msg=" + encode("Transfer request submitted for '" + bookName + "'!"));
            } catch (Exception e) {
                redirect(t, "/user?name=" + encode(name) + "&branch=" + userBranch + "&error=" + encode(e.getMessage()));
            }
        }
    }

    static class PurchaseActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) { redirect(t, "/user"); return; }
            Map<String, String> body = parseBody(t.getRequestBody());
            String bookName = body.get("bookName");
            String author = body.getOrDefault("author", "Unknown");
            String name = body.getOrDefault("userName", "User");
            String userBranch = body.getOrDefault("branch", "1");
            try {
                RequestService.addPurchaseRequest(bookName, author, name);
                redirect(t, "/user?name=" + encode(name) + "&branch=" + userBranch + "&msg=" + encode("Purchase suggestion submitted for '" + bookName + "'!"));
            } catch (Exception e) {
                redirect(t, "/user?name=" + encode(name) + "&branch=" + userBranch + "&error=" + encode(e.getMessage()));
            }
        }
    }

    static class AddBookActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) { redirect(t, "/admin"); return; }
            Map<String, String> body = parseBody(t.getRequestBody());
            try {
                int bookId = Integer.parseInt(body.get("bookId"));
                String title = body.get("title");
                String author = body.get("author");
                String category = body.getOrDefault("category", "General");
                int copies = Integer.parseInt(body.get("copies"));
                int branchId = Integer.parseInt(body.get("branchId"));
                Book book = new Book(bookId, title, author, copies, branchId, category);
                LibraryService.addBook(book);
                redirect(t, "/admin?msg=" + encode("Book '" + title + "' added successfully!"));
            } catch (Exception e) {
                redirect(t, "/admin?error=" + encode(e.getMessage()));
            }
        }
    }

    static class DeleteBookActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) { redirect(t, "/admin"); return; }
            Map<String, String> body = parseBody(t.getRequestBody());
            try {
                int bookId = Integer.parseInt(body.get("bookId"));
                LibraryService.deleteBook(bookId);
                redirect(t, "/admin?msg=" + encode("Book #" + bookId + " deleted successfully!"));
            } catch (Exception e) {
                redirect(t, "/admin?error=" + encode(e.getMessage()));
            }
        }
    }

    static class UpdateTransferActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) { redirect(t, "/admin"); return; }
            Map<String, String> body = parseBody(t.getRequestBody());
            int id = Integer.parseInt(body.get("id"));
            String status = body.get("status");
            try {
                RequestService.updateTransferStatus(id, status);
                redirect(t, "/admin?msg=" + encode("Transfer Request #" + id + " updated to " + status));
            } catch (Exception e) {
                redirect(t, "/admin?error=" + encode(e.getMessage()));
            }
        }
    }

    static class UpdatePurchaseActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equalsIgnoreCase(t.getRequestMethod())) { redirect(t, "/admin"); return; }
            Map<String, String> body = parseBody(t.getRequestBody());
            int id = Integer.parseInt(body.get("id"));
            String status = body.get("status");
            try {
                RequestService.updatePurchaseStatus(id, status);
                redirect(t, "/admin?msg=" + encode("Purchase Request #" + id + " updated to " + status));
            } catch (Exception e) {
                redirect(t, "/admin?error=" + encode(e.getMessage()));
            }
        }
    }

    // --- HELPER UTILITIES ---

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                map.put(decode(pair.substring(0, idx)), decode(pair.substring(idx + 1)));
            }
        }
        return map;
    }

    private static Map<String, String> parseBody(InputStream is) throws IOException {
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return parseQuery(body);
    }

    private static String decode(String val) {
        if (val == null) return "";
        return URLDecoder.decode(val, StandardCharsets.UTF_8);
    }

    private static String encode(String val) {
        if (val == null) return "";
        return java.net.URLEncoder.encode(val, StandardCharsets.UTF_8);
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
