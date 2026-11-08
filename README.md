# Contact Management System

**SDN260S – Software Design II | Group 39 | Project: Contact Management System**

A Java desktop application for storing, searching, editing and deleting contacts (name, phone number, email). It has a Swing GUI with login and a command-line (CLI) menu. Data is saved to files, so it is still there the next time you run the program.

---

## 1. Requirements

| Item | Requirement |
|---|---|
| Java | JDK 24 (the NetBeans project is set to source/target 24) |
| IDE | Apache NetBeans (recommended) or VS Code with the Java extensions |
| Build tool | Ant (bundled with NetBeans) |
| Libraries | None needed to run. `lib/junit-platform-console-standalone-6.1.3.jar` is only used for tests |
| Operating system | Windows, macOS or Linux |

---

## 2. Instructions

### Running the program in NetBeans

1. Unzip `39_ContactManagementSystem.zip`.
2. In NetBeans choose **File → Open Project** and select the project folder.
3. Check that the project uses JDK 24 (right-click the project → **Properties** → **Libraries**).
4. Press **Run** (F6). The main class is `ui.LoginPage`, which opens the login window.

To run the CLI menu instead, right-click `src/ui/Climenu.java` and choose **Run File**.

### Running the tests (optional)

### Optional settings

Storage locations can be changed with system properties:

| Property | Default | Purpose |
|---|---|---|
| `contacts.file` | `contacts.dat` | Contact data file |
| `contacts.log` | `contacts.log` | Activity log |
| `users.file` | `users.txt` | User accounts file |

Example: `java -Dcontacts.file=mydata.dat -cp out ui.Climenu`

> Data files are created in the folder the program is started from. Always start it from the same folder, or you will see different data.

---

## 3. User Guide

### 3.1 Using the GUI

**Create an account**

1. On the login screen, open **Sign up**.
2. Enter a username, email and password, then submit.

| Field | Rule |
|---|---|
| Username | 3–30 characters: letters, digits, `_`, `.` or `-`. Not case-sensitive and must be unique |
| Email | Must be a valid email address |
| Password | At least 6 characters |

**Log in**

Enter your username and password on the login screen.

**Dashboard**

- **New Contact:** opens a form for name, phone and email.
- **Search:** type in the search box and press **Search** to filter the list. Clear the box to show all contacts again.
- **Edit:** change a contact's details and save.
- **Delete:** removes the selected contact.
- **Logout:** returns to the login screen.

### 3.2 Contact rules

| Field | Rule |
|---|---|
| Name | Required |
| Phone | 7–15 digits with an optional leading `+`. Spaces, brackets and hyphens are removed automatically (`071 234-5678` is saved as `0712345678`) |
| Email | Must be valid. Saved in lower case |
| Uniqueness | Each phone number and each email can only belong to one contact |

If something is wrong, the program shows a message explaining what to fix.

### 3.3 Using the CLI menu

Run `ui.Climenu` and type the number of an option:

| Option | Action |
|---|---|
| 1 | Add a contact |
| 2 | View all contacts |
| 3 | Edit a contact (press Enter to keep the current value) |
| 4 | Delete a contact (asks for confirmation) |
| 5 | Search by name, phone or email |
| 6 | Threaded search by name |
| 7 | Binary search by exact name |
| 8 | Sort by name, phone, or merge sort (by name) |
| 9 | Exit |

### 3.4 Search and sort demo

A console demo of the search and sort algorithms is also included:

```bash
java -cp out search.Contactsearchsortdemo
```

### 3.5 Files created by the program

| File | Contents |
|---|---|
| `contacts.dat` | Saved contacts |
| `users.txt` | User accounts (passwords are stored as salted hashes, never as plain text) |
| `contacts.log` | Activity log with timestamps (adds, edits, deletes, logins, errors) |

### 3.6 Troubleshooting

| Problem | Fix |
|---|---|
| "Could not read contacts" message | The data file is damaged. It is left untouched. Restore a copy or move it aside to start fresh |
| "Could not save" message | The folder may be read-only or locked. Move the project to a folder you can write to |
| Contacts missing after restart | The program was started from a different folder. Run it from the project root |
| Login fails | Use your **username** (not email). Usernames are not case-sensitive, passwords are |
| Compile error about Java version | Install JDK 24 and make sure your IDE or `javac` is using it |
