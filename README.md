# 🔐 Kalivira – Secure File Locker

Kalivira is a secure file storage application that helps users keep their personal files safe.

Users can upload their files, protect them with a password, and access them whenever they need. Files are encrypted before being stored, so the original content is not directly stored in the cloud.

Kalivira uses modern security technologies like AES-256 encryption, AWS S3, and AWS KMS to provide secure file storage and recovery.

## ✨ Features

- **Secure File Upload:** Upload your personal files to your private storage.
- **File Encryption:** Files are encrypted using AES-256-GCM before storage.
- **Password Protection:** Set a separate encryption password to protect your files.
- **Secure File Download:** Download and access files using the correct encryption password.
- **Secure File Deletion:** Verify your password before deleting a protected file.
- **Email Verification:** Verify your account using an email OTP.
- **Two-Factor Authentication (MFA):** Add an extra layer of security during login.
- **JWT Authentication:** Protect user accounts and secure access to application features.
- **Password Recovery:** Reset your file encryption password through email OTP verification.
- **AWS Cloud Storage:** Store encrypted files using Amazon S3.
- **AWS Key Management Service (KMS):** Securely manage encryption keys and support file password recovery.

## 🛠️ Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Maven

### Frontend
- React.js
- JavaScript
- HTML
- CSS
- Vite

### Database
- PostgreSQL

### Cloud & Security
- Amazon Web Services (AWS)
- Amazon S3 – Stores encrypted files.
- AWS KMS – Helps securely manage encryption keys.
- AES-256-GCM – Encrypts file contents.
- PBKDF2 – Helps derive encryption keys from passwords.
- BCrypt – Securely hashes account passwords.
- JWT – Handles secure user authentication.
- Email OTP – Verifies users and supports password recovery.

## 📁 Project Structure

```text
Kalivira-project/
│
├── backend/       # Java Spring Boot application
├── frontend/      # React user interface
├── database/      # Database-related files
├── docs/          # Project documentation
└── storage/       # Local file storage
```

## 🔒 How Kalivira Works

1. **Create an Account:** Register using your name, email, and password.
2. **Verify Your Email:** Enter the OTP sent to your registered email.
3. **Login Securely:** Sign in using your account credentials and complete MFA verification.
4. **Upload a File:** Choose a file and set an encryption password.
5. **Encrypt and Store:** Kalivira encrypts the file before storing it in AWS S3.
6. **Download Your File:** Enter the correct encryption password to decrypt and download your file.
7. **Recover Access:** If you forget your file encryption password, verify your email using an OTP and set a new one.

## 👩‍💻 Developer

**Anshika**  
B.Tech – Computer Science Engineering  
Specialization: Cyber Security

---

**Kalivira – Your Files, Your Privacy, Your Control.**
