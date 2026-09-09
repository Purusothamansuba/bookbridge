#!/usr/bin/env bash
set -e

COOKIE_JAR_MEMBER="/tmp/bb_cookie_member.txt"
COOKIE_JAR_ADMIN="/tmp/bb_cookie_admin.txt"
rm -f "$COOKIE_JAR_MEMBER" "$COOKIE_JAR_ADMIN"

echo "================================================="
echo " 🧪 RUNNING COMPLETE BOOKBRIDGE FEATURE AUDIT"
echo "================================================="

# Test 1: Public Pages
echo "▶ [1/10] Testing Public Endpoints..."
STATUS_HOME=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/)
STATUS_BOOKS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/books)
STATUS_LOGIN=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/login)

if [ "$STATUS_HOME" -eq 200 ] && [ "$STATUS_BOOKS" -eq 200 ] && [ "$STATUS_LOGIN" -eq 200 ]; then
    echo "  ✅ Public pages (Home, All Books, Login) return HTTP 200"
else
    echo "  ❌ Failed public page test: Home=$STATUS_HOME, Books=$STATUS_BOOKS, Login=$STATUS_LOGIN"
    exit 1
fi

# Test 2: Unauthenticated Access Guard
echo "▶ [2/10] Testing Unauthenticated Guards on Protected Routes..."
REDIRECT_USER=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/user)
REDIRECT_ADMIN=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/admin)

if [ "$REDIRECT_USER" -eq 302 ] && [ "$REDIRECT_ADMIN" -eq 302 ]; then
    echo "  ✅ Unauthenticated requests to /user and /admin are redirected (HTTP 302)"
else
    echo "  ❌ Failed unauthenticated guard test: user=$REDIRECT_USER, admin=$REDIRECT_ADMIN"
    exit 1
fi

# Test 3: Invalid Credentials Login
echo "▶ [3/10] Testing Invalid Credentials Login..."
RES_INVALID=$(curl -s -i -X POST -d "username=invalid_user&password=wrong_password" http://localhost:8081/action/login)
if echo "$RES_INVALID" | grep -q "Invalid+username+or+password"; then
    echo "  ✅ Invalid credentials correctly rejected"
else
    echo "  ❌ Invalid credentials test failed"
    exit 1
fi

# Test 4: Member Authentication & Session
echo "▶ [4/10] Testing Member Login (purushothaman / user123)..."
curl -s -c "$COOKIE_JAR_MEMBER" -X POST -d "username=purushothaman&password=user123" http://localhost:8081/action/login > /dev/null
USER_PAGE=$(curl -s -b "$COOKIE_JAR_MEMBER" http://localhost:8081/user)

if echo "$USER_PAGE" | grep -q "Welcome, Purushothaman" && echo "$USER_PAGE" | grep -q "Guindy Library"; then
    echo "  ✅ Member authenticated with cookie session and personalized dashboard"
else
    echo "  ❌ Member login failed"
    exit 1
fi

# Test 5: Member Catalog Search & Filter
echo "▶ [5/10] Testing Live Catalog Search..."
SEARCH_RES=$(curl -s -b "$COOKIE_JAR_MEMBER" "http://localhost:8081/user?q=Clean")
if echo "$SEARCH_RES" | grep -q "Clean Code"; then
    echo "  ✅ Search correctly filtered catalog for query 'Clean'"
else
    echo "  ❌ Catalog search failed"
    exit 1
fi

# Test 6: Member Borrow and Return Flow
echo "▶ [6/10] Testing Member Borrow & Return Lifecycle..."
BORROW_RES=$(curl -s -b "$COOKIE_JAR_MEMBER" -i -X POST -d "bookId=101&branch=1" http://localhost:8081/action/borrow)
if echo "$BORROW_RES" | grep -q "Book+borrowed+successfully"; then
    echo "  ✅ Book #101 borrowed successfully"
else
    echo "  ❌ Borrow failed"
    exit 1
fi

RETURN_RES=$(curl -s -b "$COOKIE_JAR_MEMBER" -i -X POST -d "bookId=101" http://localhost:8081/action/return)
if echo "$RETURN_RES" | grep -q "Book+returned+successfully"; then
    echo "  ✅ Book #101 returned successfully"
else
    echo "  ❌ Return failed"
    exit 1
fi

# Test 7: Inter-Branch Transfer & Purchase Suggestions
echo "▶ [7/10] Testing Inter-Branch Transfer & Purchase Suggestions..."
TRANSFER_RES=$(curl -s -b "$COOKIE_JAR_MEMBER" -i -X POST -d "bookName=Data+Structures+%26+Algorithms&fromBranch=Adyar+Library&toBranch=Guindy+Library" http://localhost:8081/action/transfer)
if echo "$TRANSFER_RES" | grep -q "Transfer+request+submitted"; then
    echo "  ✅ Inter-branch transfer request submitted"
else
    echo "  ❌ Transfer request failed"
    exit 1
fi

PURCHASE_RES=$(curl -s -b "$COOKIE_JAR_MEMBER" -i -X POST -d "bookName=Domain-Driven+Design&author=Eric+Evans" http://localhost:8081/action/purchase)
if echo "$PURCHASE_RES" | grep -q "Purchase+suggestion+submitted"; then
    echo "  ✅ Book purchase suggestion submitted"
else
    echo "  ❌ Purchase suggestion failed"
    exit 1
fi

# Test 8: Admin Authentication & Console
echo "▶ [8/10] Testing Admin Login (admin / admin123)..."
curl -s -c "$COOKIE_JAR_ADMIN" -X POST -d "username=admin&password=admin123" http://localhost:8081/action/login > /dev/null
ADMIN_PAGE=$(curl -s -b "$COOKIE_JAR_ADMIN" http://localhost:8081/admin)

if echo "$ADMIN_PAGE" | grep -q "Librarian & Admin Console"; then
    echo "  ✅ Admin authenticated and accessed console"
else
    echo "  ❌ Admin console access failed"
    exit 1
fi

# Test 9: Admin User Management (Create & List)
echo "▶ [9/10] Testing User Creation in Admin Panel..."
NEW_USER_RES=$(curl -s -b "$COOKIE_JAR_ADMIN" -i -X POST -d "username=testuser_audit&password=auditpass&fullName=Audit+Tester&role=MEMBER&branchId=2" http://localhost:8081/action/createUser)
if echo "$NEW_USER_RES" | grep -q "User+account+%40testuser_audit+created+successfully"; then
    echo "  ✅ Admin created user @testuser_audit"
else
    echo "  ❌ Admin user creation failed"
    exit 1
fi

# Verify new user can sign in
COOKIE_JAR_NEWUSER="/tmp/bb_cookie_newuser.txt"
curl -s -c "$COOKIE_JAR_NEWUSER" -X POST -d "username=testuser_audit&password=auditpass" http://localhost:8081/action/login > /dev/null
NEWUSER_PAGE=$(curl -s -b "$COOKIE_JAR_NEWUSER" http://localhost:8081/user)
if echo "$NEWUSER_PAGE" | grep -q "Welcome, Audit Tester" && echo "$NEWUSER_PAGE" | grep -q "Adyar Library"; then
    echo "  ✅ Newly created user @testuser_audit logged in successfully with assigned Adyar branch"
else
    echo "  ❌ Newly created user login failed"
    exit 1
fi

# Test 10: Admin Book Management (Add & Delete)
echo "▶ [10/10] Testing Admin Add Book & Delete Book..."
ADD_BOOK_RES=$(curl -s -b "$COOKIE_JAR_ADMIN" -i -X POST -d "bookId=999&title=Audit+Testing+Handbook&author=QA+Team&category=Testing&copies=10&branchId=1" http://localhost:8081/action/addBook)
if echo "$ADD_BOOK_RES" | grep -q "added+successfully"; then
    echo "  ✅ Admin added book #999"
else
    echo "  ❌ Add book failed"
    exit 1
fi

DEL_BOOK_RES=$(curl -s -b "$COOKIE_JAR_ADMIN" -i -X POST -d "bookId=999" http://localhost:8081/action/deleteBook)
if echo "$DEL_BOOK_RES" | grep -q "deleted+successfully"; then
    echo "  ✅ Admin deleted book #999"
else
    echo "  ❌ Delete book failed"
    exit 1
fi

echo "================================================="
echo " 🎉 ALL 10 TEST SUITES PASSED FLAWLESSLY!"
echo "================================================="
rm -f "$COOKIE_JAR_MEMBER" "$COOKIE_JAR_ADMIN" "$COOKIE_JAR_NEWUSER"
