import { useEffect, useState } from "react";
import "./App.css";

function App() {
  const [token, setToken] = useState(
    localStorage.getItem("kalivira_token")
  );

  const [email, setEmail] = useState(
    localStorage.getItem("kalivira_email") || ""
  );

  const [page, setPage] = useState("dashboard");

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");

  const [otp, setOtp] = useState("");
  const [mfaRequired, setMfaRequired] = useState(false);

  const [files, setFiles] = useState([]);

  const [selectedFile, setSelectedFile] = useState(null);
  const [filePassword, setFilePassword] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const [loading, setLoading] = useState(false);

  const [showUpload, setShowUpload] = useState(false);

  useEffect(() => {
    if (token) {
      loadFiles();
    }
  }, [token]);

  function clearMessages() {
    setMessage("");
    setError("");
  }

  // =========================
  // LOGIN
  // =========================

  async function login(e) {
    e.preventDefault();

    clearMessages();
    setLoading(true);

    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          email: loginEmail,
          password: loginPassword
        })
      });

      const data = await response.text();

      if (!response.ok) {
        throw new Error(data || "Login failed");
      }

      setEmail(loginEmail);
      setMfaRequired(true);

      setMessage(
        "Password verified. MFA OTP has been sent to your email."
      );
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

 // =========================
 // MFA
 // =========================

 async function verifyMfa(e) {
   e.preventDefault();

   clearMessages();
   setLoading(true);

   try {
     const response = await fetch(
       `/api/auth/verify-mfa?email=${encodeURIComponent(loginEmail)}&otp=${encodeURIComponent(otp)}`,
       {
         method: "POST"
       }
     );

     const data = await response.text();

     if (!response.ok) {
       throw new Error(data || "MFA verification failed");
     }

     const jwt = data.trim();

     localStorage.setItem("kalivira_token", jwt);
     localStorage.setItem("kalivira_email", loginEmail);

     setToken(jwt);
     setEmail(loginEmail);
     setMfaRequired(false);
     setOtp("");

     setMessage("Login successful.");
   } catch (err) {
     setError(err.message);
   } finally {
     setLoading(false);
   }
 }

  // =========================
  // LOGOUT
  // =========================

  function logout() {
    localStorage.removeItem("kalivira_token");
    localStorage.removeItem("kalivira_email");

    setToken(null);
    setEmail("");
    setFiles([]);
    setPage("dashboard");
    setMfaRequired(false);

    clearMessages();
  }

  // =========================
  // LOAD FILES
  // =========================

  async function loadFiles() {
    try {
      const response = await fetch("/api/files/my-files", {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (response.status === 401) {
        logout();
        return;
      }

      const data = await response.json();

      if (!response.ok) {
        throw new Error("Failed to load files");
      }

      setFiles(data);
    } catch (err) {
      setError(err.message);
    }
  }

  // =========================
  // UPLOAD
  // =========================

  async function uploadFile(e) {
    e.preventDefault();

    clearMessages();

    if (!selectedFile) {
      setError("Please select a file.");
      return;
    }

    if (!filePassword) {
      setError("Please enter a file password.");
      return;
    }

    setLoading(true);

    try {
      const formData = new FormData();

      formData.append("file", selectedFile);
      formData.append("password", filePassword);

      const response = await fetch("/api/files/upload", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`
        },
        body: formData
      });

      const data = await response.text();

      if (!response.ok) {
        throw new Error(data || "Upload failed");
      }

      setMessage(data);

      setSelectedFile(null);
      setFilePassword("");
      setShowUpload(false);

      const input = document.getElementById("fileInput");

      if (input) {
        input.value = "";
      }

      await loadFiles();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // =========================
  // DOWNLOAD BY FILE ID
  // =========================

  async function downloadFile(file) {
    clearMessages();

    const password = window.prompt(
      `Enter password for ${file.originalName}`
    );

    if (password === null) {
      return;
    }

    if (!password) {
      setError("Password is required.");
      return;
    }

    setLoading(true);

    try {
      const url =
        `/api/files/download?fileId=${file.id}` +
        `&password=${encodeURIComponent(password)}`;

      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      if (!response.ok) {
        const data = await response.text();
        throw new Error(data || "Download failed");
      }

      const blob = await response.blob();

      const downloadUrl = window.URL.createObjectURL(blob);

      const a = document.createElement("a");

      a.href = downloadUrl;
      a.download = file.originalName;

      document.body.appendChild(a);

      a.click();

      a.remove();

      window.URL.revokeObjectURL(downloadUrl);

      setMessage(
        `${file.originalName} downloaded successfully.`
      );
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // =========================
  // DELETE BY FILE ID
  // =========================

  async function deleteFile(file) {
    clearMessages();

    const confirmed = window.confirm(
      `Are you sure you want to delete "${file.originalName}"?`
    );

    if (!confirmed) {
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
        `/api/files/delete?fileId=${file.id}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      const data = await response.text();

      if (!response.ok) {
        throw new Error(data || "Delete failed");
      }

      setMessage(data);

      await loadFiles();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // =========================
  // LOGIN SCREEN
  // =========================

  if (!token) {
    return (
      <LoginScreen
        loginEmail={loginEmail}
        setLoginEmail={setLoginEmail}
        loginPassword={loginPassword}
        setLoginPassword={setLoginPassword}
        login={login}
        mfaRequired={mfaRequired}
        otp={otp}
        setOtp={setOtp}
        verifyMfa={verifyMfa}
        loading={loading}
        message={message}
        error={error}
      />
    );
  }

  // =========================
  // DASHBOARD
  // =========================

  return (
    <div className="app">

      <aside className="sidebar">

        <div className="logo">

          <div className="logo-icon">
            K
          </div>

          <div>
            <h2>KALIVIRA</h2>
            <span>Secure File Locker</span>
          </div>

        </div>

        <nav>

          <button
            className={
              page === "dashboard"
                ? "nav-active"
                : ""
            }
            onClick={() => setPage("dashboard")}
          >
            <span>▣</span>
            Dashboard
          </button>

          <button
            className={
              page === "files"
                ? "nav-active"
                : ""
            }
            onClick={() => {
              setPage("files");
              loadFiles();
            }}
          >
            <span>▤</span>
            My Files
          </button>

        </nav>

        <button
          className="logout-button"
          onClick={logout}
        >
          ⇥ Logout
        </button>

      </aside>

      <main className="main">

        <header className="topbar">

          <div>

            <p className="small-label">
              SECURE STORAGE
            </p>

            <h1>
              {page === "dashboard"
                ? "Dashboard"
                : "My Files"}
            </h1>

          </div>

          <div className="user-info">

            <div className="avatar">
              {email.charAt(0).toUpperCase()}
            </div>

            <div>
              <strong>{email}</strong>
              <span>Authenticated user</span>
            </div>

          </div>

        </header>

        {message && (
          <div className="success-message">
            ✓ {message}
          </div>
        )}

        {error && (
          <div className="error-message">
            ✕ {error}
          </div>
        )}

        {page === "dashboard" && (
          <Dashboard
            files={files}
            setPage={setPage}
            setShowUpload={setShowUpload}
            downloadFile={downloadFile}
            deleteFile={deleteFile}
          />
        )}

        {page === "files" && (
          <FilesPage
            files={files}
            setShowUpload={setShowUpload}
            downloadFile={downloadFile}
            deleteFile={deleteFile}
            loadFiles={loadFiles}
          />
        )}

        {showUpload && (
          <UploadModal
            selectedFile={selectedFile}
            setSelectedFile={setSelectedFile}
            filePassword={filePassword}
            setFilePassword={setFilePassword}
            uploadFile={uploadFile}
            setShowUpload={setShowUpload}
            loading={loading}
          />
        )}

      </main>

    </div>
  );
}


// ======================================================
// LOGIN SCREEN COMPONENT
// ======================================================

function LoginScreen({
  loginEmail,
  setLoginEmail,
  loginPassword,
  setLoginPassword,
  login,
  mfaRequired,
  otp,
  setOtp,
  verifyMfa,
  loading,
  message,
  error
}) {

  return (
    <div className="login-page">

      <div className="login-card">

        <div className="login-logo">

          <div className="logo-icon large">
            K
          </div>

        </div>

        <h1>KALIVIRA</h1>

        <p className="login-subtitle">
          Secure File Storage System
        </p>

        {!mfaRequired ? (

          <form onSubmit={login}>

            <label>
              Email
            </label>

            <input
              type="email"
              placeholder="Enter your email"
              value={loginEmail}
              onChange={(e) =>
                setLoginEmail(e.target.value)
              }
              required
            />

            <label>
              Password
            </label>

            <input
              type="password"
              placeholder="Enter your password"
              value={loginPassword}
              onChange={(e) =>
                setLoginPassword(e.target.value)
              }
              required
            />

            <button
              className="primary-button"
              disabled={loading}
            >
              {loading
                ? "Signing in..."
                : "Sign In"}
            </button>

          </form>

        ) : (

          <form onSubmit={verifyMfa}>

            <div className="otp-info">

              MFA OTP sent to

              <strong>
                {loginEmail}
              </strong>

            </div>

            <label>
              MFA OTP
            </label>

            <input
              type="text"
              placeholder="Enter 6-digit OTP"
              value={otp}
              onChange={(e) =>
                setOtp(e.target.value)
              }
              maxLength="6"
              required
            />

            <button
              className="primary-button"
              disabled={loading}
            >
              {loading
                ? "Verifying..."
                : "Verify & Continue"}
            </button>

          </form>

        )}

        {message && (
          <div className="success-message">
            ✓ {message}
          </div>
        )}

        {error && (
          <div className="error-message">
            ✕ {error}
          </div>
        )}

        <div className="security-note">

          🔐 AES encrypted storage
          <br />

          🛡 JWT + MFA protected

        </div>

      </div>

    </div>
  );
}


// ======================================================
// DASHBOARD
// ======================================================

function Dashboard({
  files,
  setPage,
  setShowUpload,
  downloadFile,
  deleteFile
}) {

  return (
    <>

      <section className="hero-card">

        <div>

          <p className="small-label">
            YOUR DIGITAL VAULT
          </p>

          <h2>
            Protect your files.
            <br />
            Keep control.
          </h2>

          <p>
            Upload files and store them securely
            with encrypted cloud storage.
          </p>

          <button
            className="primary-button hero-button"
            onClick={() => setShowUpload(true)}
          >
            + Upload Secure File
          </button>

        </div>

        <div className="security-graphic">

          <div className="shield">
            ◆
          </div>

          <span>
            AES-256
          </span>

          <small>
            ENCRYPTED
          </small>

        </div>

      </section>

      <section className="stats">

        <div className="stat-card">

          <span>
            Total Files
          </span>

          <strong>
            {files.length}
          </strong>

        </div>

        <div className="stat-card">

          <span>
            Storage Status
          </span>

          <strong className="green">
            SECURE
          </strong>

        </div>

        <div className="stat-card">

          <span>
            Authentication
          </span>

          <strong className="green">
            MFA
          </strong>

        </div>

      </section>

      <section className="section">

        <div className="section-header">

          <div>

            <p className="small-label">
              RECENT FILES
            </p>

            <h2>
              Your Files
            </h2>

          </div>

          <button
            className="secondary-button"
            onClick={() => setPage("files")}
          >
            View All →
          </button>

        </div>

        <FileTable
          files={files.slice(0, 5)}
          downloadFile={downloadFile}
          deleteFile={deleteFile}
        />

      </section>

    </>
  );
}


// ======================================================
// MY FILES
// ======================================================

function FilesPage({
  files,
  setShowUpload,
  downloadFile,
  deleteFile,
  loadFiles
}) {

  return (
    <section className="section">

      <div className="section-header">

        <div>

          <p className="small-label">
            SECURE STORAGE
          </p>

          <h2>
            All Your Files
          </h2>

        </div>

        <div className="header-actions">

          <button
            className="secondary-button"
            onClick={loadFiles}
          >
            ↻ Refresh
          </button>

          <button
            className="primary-button"
            onClick={() => setShowUpload(true)}
          >
            + Upload
          </button>

        </div>

      </div>

      <FileTable
        files={files}
        downloadFile={downloadFile}
        deleteFile={deleteFile}
      />

    </section>
  );
}


// ======================================================
// FILE TABLE
// ======================================================

function FileTable({
  files,
  downloadFile,
  deleteFile
}) {

  if (files.length === 0) {

    return (
      <div className="empty-state">

        <div className="empty-icon">
          ▱
        </div>

        <h3>
          No files yet
        </h3>

        <p>
          Upload your first secure file to get started.
        </p>

      </div>
    );
  }

  return (
    <div className="file-table">

      <div className="table-header">

        <span>FILE</span>
        <span>SIZE</span>
        <span>UPLOADED</span>
        <span>STATUS</span>
        <span>ACTIONS</span>

      </div>

      {files.map((file) => (

        <div
          className="file-row"
          key={file.id}
        >

          <div className="file-name">

            <div className="file-icon">
              {getFileIcon(file.originalName)}
            </div>

            <div>

              <strong>
                {file.originalName}
              </strong>

              <small>
                ID: {file.id}
              </small>

            </div>

          </div>

          <span>
            {formatBytes(file.fileSize)}
          </span>

          <span>
            {formatDate(file.uploadTime)}
          </span>

          <span className="status">

            <i></i>

            Encrypted

          </span>

          <div className="actions">

            <button
              className="download-btn"
              onClick={() => downloadFile(file)}
              title="Download"
            >
              ↓
            </button>

            <button
              className="delete-btn"
              onClick={() => deleteFile(file)}
              title="Delete"
            >
              🗑
            </button>

          </div>

        </div>

      ))}

    </div>
  );
}


// ======================================================
// UPLOAD MODAL
// ======================================================

function UploadModal({
  selectedFile,
  setSelectedFile,
  filePassword,
  setFilePassword,
  uploadFile,
  setShowUpload,
  loading
}) {

  return (
    <div className="modal-overlay">

      <div className="modal">

        <div className="modal-header">

          <div>

            <p className="small-label">
              SECURE UPLOAD
            </p>

            <h2>
              Upload File
            </h2>

          </div>

          <button
            className="close-button"
            onClick={() => setShowUpload(false)}
          >
            ×
          </button>

        </div>

        <form onSubmit={uploadFile}>

          <label>
            Select File
          </label>

          <div className="file-input-box">

            <input
              id="fileInput"
              type="file"
              onChange={(e) =>
                setSelectedFile(
                  e.target.files[0]
                )
              }
              required
            />

            <span>
              {selectedFile
                ? selectedFile.name
                : "Choose a file"}
            </span>

          </div>

          <label>
            Encryption Password
          </label>

          <input
            type="password"
            placeholder="Enter file password"
            value={filePassword}
            onChange={(e) =>
              setFilePassword(e.target.value)
            }
            required
          />

          <p className="input-help">
            This password is used to encrypt your
            file. Remember it for downloading.
          </p>

          <div className="modal-actions">

            <button
              type="button"
              className="secondary-button"
              onClick={() => setShowUpload(false)}
            >
              Cancel
            </button>

            <button
              type="submit"
              className="primary-button"
              disabled={loading}
            >
              {loading
                ? "Encrypting..."
                : "Encrypt & Upload"}
            </button>

          </div>

        </form>

      </div>

    </div>
  );
}


// ======================================================
// HELPER FUNCTIONS
// ======================================================

function formatBytes(bytes) {

  if (!bytes) {
    return "0 B";
  }

  const units = [
    "B",
    "KB",
    "MB",
    "GB"
  ];

  const index = Math.floor(
    Math.log(bytes) / Math.log(1024)
  );

  return (
    (bytes / Math.pow(1024, index)).toFixed(1)
    + " "
    + units[index]
  );
}


function formatDate(date) {

  if (!date) {
    return "-";
  }

  return new Date(date).toLocaleString();
}


function getFileIcon(filename) {

  const extension =
    filename
      .split(".")
      .pop()
      .toLowerCase();

  if (extension === "pdf") {
    return "PDF";
  }

  if (
    ["jpg", "jpeg", "png"].includes(extension)
  ) {
    return "IMG";
  }

  if (
    ["doc", "docx"].includes(extension)
  ) {
    return "DOC";
  }

  if (
    ["xls", "xlsx"].includes(extension)
  ) {
    return "XLS";
  }

  if (
    ["zip", "rar"].includes(extension)
  ) {
    return "ZIP";
  }

  return "FILE";
}


export default App;