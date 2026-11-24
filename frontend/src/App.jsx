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

  const [showSignup, setShowSignup] = useState(false);

  const [signupName, setSignupName] = useState("");
  const [signupEmail, setSignupEmail] = useState("");
  const [signupPassword, setSignupPassword] = useState("");

  const [signupOtpRequired, setSignupOtpRequired] = useState(false);
  const [signupOtp, setSignupOtp] = useState("");

  // Forgot Password
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [resetOtpSent, setResetOtpSent] = useState(false);
  const [resetOtp, setResetOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmNewPassword, setConfirmNewPassword] = useState("");


  const [otp, setOtp] = useState("");
  const [mfaRequired, setMfaRequired] = useState(false);

  const [files, setFiles] = useState([]);

  const [selectedFile, setSelectedFile] = useState(null);
  const [filePassword, setFilePassword] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const [loading, setLoading] = useState(false);

  const [showUpload, setShowUpload] = useState(false);

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [fileToDelete, setFileToDelete] = useState(null);
  const [deletePassword, setDeletePassword] = useState("");
  const [showDeletePassword, setShowDeletePassword] = useState(false);


  // Download password modal
  const [showDownloadModal, setShowDownloadModal] = useState(false);
  const [fileToDownload, setFileToDownload] = useState(null);
  const [downloadPassword, setDownloadPassword] = useState("");
  const [showDownloadPassword, setShowDownloadPassword] = useState(false);

  // File encryption password recovery
  const [showFileRecovery, setShowFileRecovery] = useState(false);
  const [recoveryOtpSent, setRecoveryOtpSent] = useState(false);
  const [recoveryOtp, setRecoveryOtp] = useState("");
  const [newEncryptionPassword, setNewEncryptionPassword] = useState("");
  const [confirmEncryptionPassword, setConfirmEncryptionPassword] = useState("");


const [showRecoveryPassword, setShowRecoveryPassword] = useState(false);
const [showConfirmRecoveryPassword, setShowConfirmRecoveryPassword] = useState(false);


  useEffect(() => {
    if (token) {
      loadFiles();
    }
  }, [token]);

  function clearMessages() {
    setMessage("");
    setError("");
  }



function isStrongPassword(password) {
  return (
    password.length >= 8 &&
    /[A-Z]/.test(password) &&
    /[a-z]/.test(password) &&
    /[0-9]/.test(password) &&
    /[^A-Za-z0-9]/.test(password)
  );
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
        email: loginEmail.trim(),
        password: loginPassword
      })
    });

const data = await response.text();
const result = data.trim();

if (!response.ok) {
  throw new Error(
    "Invalid email or password. Please try again."
  );
}

// MFA should open ONLY for this exact success response
if (result !== "Password verified. MFA OTP sent to your email.") {
  throw new Error(result || "Login failed.");
}

setEmail(loginEmail.trim());
setMfaRequired(true);
setOtp("");

setMessage("MFA OTP sent to your registered email.");

  } catch (err) {
    setMfaRequired(false);
    setError(err.message || "Unable to log in. Please try again.");
  } finally {
    setLoading(false);
  }
}

// =========================
// SIGN UP
// =========================

async function signup(e) {
  e.preventDefault();

  clearMessages();

  if (!signupName.trim()) {
    setError("Name is required.");
    return;
  }

  if (!signupEmail.trim()) {
    setError("Email is required.");
    return;
  }

  if (!isStrongPassword(signupPassword)) {
    setError(
      "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character."
    );
    return;
  }

  setLoading(true);

  try {
    const response = await fetch("/api/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        name: signupName,
        email: signupEmail,
        password: signupPassword
      })
    });

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || "Registration failed");
    }

    setSignupOtpRequired(true);

    setMessage(
      "Registration successful. OTP has been sent to your email."
    );

  } catch (err) {
    setError(err.message);
  } finally {
    setLoading(false);
  }
}

// =========================
// SIGNUP OTP
// =========================

async function verifySignupOtp(e) {
  e.preventDefault();

  clearMessages();

  if (!signupOtp) {
    setError("Please enter the OTP.");
    return;
  }

  setLoading(true);

  try {
    const response = await fetch(
      `/api/auth/verify-otp?email=${encodeURIComponent(
        signupEmail
      )}&otp=${encodeURIComponent(signupOtp)}`,
      {
        method: "POST"
      }
    );

    const data = await response.text();

    if (!response.ok) {
      throw new Error(
        data || "OTP verification failed"
      );
    }

    setMessage(
      "Email verified successfully. You can now login."
    );

    setSignupName("");
    setSignupEmail("");
    setSignupPassword("");
    setSignupOtp("");
    setSignupOtpRequired(false);
    setShowSignup(false);

    setLoginEmail(signupEmail);

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
     // A valid JWT must contain three Base64URL sections
     if (jwt.split(".").length !== 3) {
       throw new Error(
         data || "Invalid MFA OTP. Please try again."
       );
     }

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
// FORGOT PASSWORD
// =========================

async function forgotPassword(e) {
  e.preventDefault();
  clearMessages();

  if (!loginEmail.trim()) {
    setError("Please enter your registered email.");
    return;
  }

  setLoading(true);

  try {
    const response = await fetch(
      `/api/auth/forgot-password?email=${encodeURIComponent(loginEmail.trim())}`,
      {
        method: "POST"
      }
    );

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || "Unable to process request.");
    }

    // The backend intentionally gives a generic response
    // whether the email exists or not.
    setResetOtpSent(true);
    setMessage(data);

  } catch (err) {
    setError(err.message);
  } finally {
    setLoading(false);
  }
}


// =========================
// RESET PASSWORD
// =========================

async function resetPassword(e) {
  e.preventDefault();
  clearMessages();

  if (!resetOtp.trim()) {
    setError("Please enter the OTP.");
    return;
  }

  if (!isStrongPassword(newPassword)) {
    setError(
      "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character."
    );
    return;
  }

  if (newPassword !== confirmNewPassword) {
    setError("Passwords do not match.");
    return;
  }

  setLoading(true);

  try {
    const response = await fetch(
      `/api/auth/reset-password?email=${encodeURIComponent(loginEmail.trim())}&otp=${encodeURIComponent(resetOtp.trim())}&newPassword=${encodeURIComponent(newPassword)}`,
      {
        method: "POST"
      }
    );

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || "Password reset failed.");
    }

    if (data.trim() !== "Password reset successfully.") {
      throw new Error(data || "Password reset failed.");
    }

    setMessage("Password reset successfully. Please login with your new password.");

    setLoginPassword("");
    setResetOtp("");
    setNewPassword("");
    setConfirmNewPassword("");

    setResetOtpSent(false);
    setShowForgotPassword(false);

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

    const responseText = await response.text();
    console.log("Files API status:", response.status);
    console.log("Files API response:", responseText);

    if (!response.ok) {
      throw new Error(
        responseText || `Failed to load files (${response.status})`
      );
    }

    const data = responseText ? JSON.parse(responseText) : [];
    setFiles(data);

  } catch (err) {
    console.error("Load files error:", err);
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

   if (!isStrongPassword(filePassword)) {
     setError(
       "File password must be at least 8 characters and contain uppercase, lowercase, number, and special character."
     );
     return;
   }

    if (!isStrongPassword(filePassword)) {
      setError(
        "File password must be at least 8 characters and contain uppercase, lowercase, number, and special character."
      );
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
// DOWNLOAD MODAL
// =========================

function downloadFile(file) {
  clearMessages();
  setFileToDownload(file);
  setDownloadPassword("");
  setShowDownloadPassword(false);
  setShowDownloadModal(true);
}

// =========================
// CONFIRM DOWNLOAD
// =========================

async function confirmDownloadFile(e) {
  e.preventDefault();
  clearMessages();

  if (!downloadPassword) {
    setError("Please enter your file encryption password.");
    return;
  }

  setLoading(true);

  try {
    const url =
      `/api/files/download?fileId=${fileToDownload.id}` +
      `&password=${encodeURIComponent(downloadPassword)}`;

    const response = await fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (!response.ok) {
      const data = await response.text();
      throw new Error(data || "Download failed.");
    }

    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = downloadUrl;
    a.download = fileToDownload.originalName;

    document.body.appendChild(a);
    a.click();
    a.remove();

    window.URL.revokeObjectURL(downloadUrl);

    setMessage(`${fileToDownload.originalName} downloaded successfully.`);

    setShowDownloadModal(false);
    setFileToDownload(null);
    setDownloadPassword("");

  } catch (err) {
    setError(err.message);
  } finally {
    setLoading(false);
  }
}

  // =========================
  // DELETE BY FILE ID
  // =========================
function deleteFile(file) {
  clearMessages();

  setFileToDelete(file);
  setDeletePassword("");
  setShowDeletePassword(false);
  setShowDeleteModal(true);
}

async function confirmDeleteFile(e) {
  e.preventDefault();
  clearMessages();

  if (!deletePassword) {
    setError("Please enter your file encryption password.");
    return;
  }

  setLoading(true);

  try {
    const response = await fetch(
      `/api/files/delete?fileId=${fileToDelete.id}&password=${encodeURIComponent(deletePassword)}`,
      {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || "Delete failed.");
    }

    setMessage(data);

    setShowDeleteModal(false);
    setFileToDelete(null);
    setDeletePassword("");

    await loadFiles();

  } catch (err) {
    setError(err.message);
  } finally {
    setLoading(false);
  }
}
// =========================
// OPEN FILE PASSWORD RECOVERY
// =========================

function openFileRecovery() {
  clearMessages();

  setShowDownloadModal(false);
  setShowDeleteModal(false);

  setRecoveryOtpSent(false);
  setRecoveryOtp("");
  setNewEncryptionPassword("");
  setConfirmEncryptionPassword("");

  setShowFileRecovery(true);
}

// =========================
// REQUEST FILE RECOVERY OTP
// =========================

async function requestFileRecoveryOtp(e) {
  e.preventDefault();
  clearMessages();

  setLoading(true);

  try {
    const response = await fetch(
      "/api/files/recovery/request-otp",
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || "Unable to send recovery OTP.");
    }

    setRecoveryOtpSent(true);
    setMessage(data || "Recovery OTP sent to your registered email.");

  } catch (err) {
    setError(err.message);
  } finally {
    setLoading(false);
  }
}

// =========================
// RESET FILE ENCRYPTION PASSWORD
// =========================

async function resetFileEncryptionPassword(e) {
  e.preventDefault();
  clearMessages();

  if (!/^[0-9]{6}$/.test(recoveryOtp.trim())) {
    setError("Please enter a valid 6-digit OTP.");
    return;
  }

  if (!isStrongPassword(newEncryptionPassword)) {
    setError(
      "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character."
    );
    return;
  }

  if (newEncryptionPassword !== confirmEncryptionPassword) {
    setError("Passwords do not match.");
    return;
  }

  setLoading(true);

  try {
    const response = await fetch(
      "/api/files/recovery/reset-encryption-password",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify({
          otp: recoveryOtp.trim(),
          newPassword: newEncryptionPassword
        })
      }
    );

    const data = await response.text();

    if (!response.ok) {
      throw new Error(data || "Encryption password recovery failed.");
    }

    setShowFileRecovery(false);
    setRecoveryOtpSent(false);
    setRecoveryOtp("");
    setNewEncryptionPassword("");
    setConfirmEncryptionPassword("");

    setShowDownloadModal(false);
    setShowDeleteModal(false);
    setFileToDownload(null);
    setFileToDelete(null);
    setDownloadPassword("");
    setDeletePassword("");

    setMessage(
      "File encryption password updated successfully. Use your new password to access your recoverable files."
    );

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
        showForgotPassword={showForgotPassword}
        setShowForgotPassword={setShowForgotPassword}

        resetOtpSent={resetOtpSent}
        setResetOtpSent={setResetOtpSent}

        resetOtp={resetOtp}
        setResetOtp={setResetOtp}

        newPassword={newPassword}
        setNewPassword={setNewPassword}

        confirmNewPassword={confirmNewPassword}
        setConfirmNewPassword={setConfirmNewPassword}

        forgotPassword={forgotPassword}
        resetPassword={resetPassword}
        loading={loading}
        message={message}
        error={error}

        showSignup={showSignup}
        setShowSignup={setShowSignup}

        signupName={signupName}
        setSignupName={setSignupName}
        signupEmail={signupEmail}
        setSignupEmail={setSignupEmail}
        signupPassword={signupPassword}
        setSignupPassword={setSignupPassword}

        signup={signup}
        signupOtpRequired={signupOtpRequired}
        signupOtp={signupOtp}
        setSignupOtp={setSignupOtp}
        verifySignupOtp={verifySignupOtp}
        setSignupOtpRequired={setSignupOtpRequired}
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


      {/* DOWNLOAD PASSWORD MODAL */}
      {showDownloadModal && fileToDownload && (
        <div className="modal-overlay">
          <div className="modal">

            <div className="modal-header">
              <div>
                <p className="small-label">SECURE DOWNLOAD</p>
                <h2>Download File</h2>
              </div>

              <button
                type="button"
                className="close-button"
                onClick={() => {
                  setShowDownloadModal(false);
                  setFileToDownload(null);
                  setDownloadPassword("");
                }}
              >
                ×
              </button>
            </div>

            <p>
              Enter the encryption password for:
              <strong> {fileToDownload.originalName}</strong>
            </p>

            <form onSubmit={confirmDownloadFile}>
              <label>Encryption Password</label>

              <div className="delete-password-wrapper">
                <input
                  type={showDownloadPassword ? "text" : "password"}
                  placeholder="Enter file encryption password"
                  value={downloadPassword}
                  onChange={(e) => setDownloadPassword(e.target.value)}
                  required
                />

                <button
                  type="button"
                  className="delete-password-toggle"
                  onClick={() => setShowDownloadPassword(!showDownloadPassword)}
                  aria-label={showDownloadPassword ? "Hide password" : "Show password"}
                >
                  {showDownloadPassword ? "🙈" : "👁"}
                </button>
              </div>

              <div style={{ textAlign: "right", marginTop: "10px" }}>
                <button
                  type="button"
                  className="text-button"
                  onClick={openFileRecovery}
                >
                  Forgot Encryption Password?
                </button>
              </div>



              <div className="header-actions">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setShowDownloadModal(false);
                    setFileToDownload(null);
                    setDownloadPassword("");
                  }}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="primary-button"
                  disabled={loading}
                >
                  {loading ? "Downloading..." : "Download"}
                </button>
              </div>
            </form>

          </div>
        </div>
      )}


        {/* DELETE MODAL */}
        {showDeleteModal && fileToDelete && (
          <div className="modal-overlay">
            <div className="modal">

              <div className="modal-header">
                <div>
                  <p className="small-label">SECURE DELETE</p>
                  <h2>Delete File</h2>
                </div>

                <button
                  type="button"
                  className="close-button"
                  onClick={() => {
                    setShowDeleteModal(false);
                    setFileToDelete(null);
                    setDeletePassword("");
                  }}
                >
                  ×
                </button>
              </div>

              <p>
                Are you sure you want to delete:
                <strong> {fileToDelete.originalName}</strong>
              </p>

              <form onSubmit={confirmDeleteFile}>

                <label>Encryption Password</label>

               <div className="delete-password-wrapper">
                 <input
                   type={showDeletePassword ? "text" : "password"}
                   placeholder="Enter file encryption password"
                   value={deletePassword}
                   onChange={(e) => setDeletePassword(e.target.value)}
                   required
                 />

                 <button
                   type="button"
                   className="delete-password-toggle"
                   onClick={() => setShowDeletePassword(!showDeletePassword)}
                   aria-label={showDeletePassword ? "Hide password" : "Show password"}
                   title={showDeletePassword ? "Hide password" : "Show password"}
                 >
                   {showDeletePassword ? "🙈" : "👁"}
                 </button>
               </div>
<div style={{ textAlign: "right", marginTop: "10px" }}>
  <button
    type="button"
    className="text-button"
    onClick={openFileRecovery}
  >
    Forgot Encryption Password?
  </button>
</div>
                <div className="header-actions">

                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => {
                      setShowDeleteModal(false);
                      setFileToDelete(null);
                      setDeletePassword("");
                    }}
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    className="primary-button"
                    disabled={loading}
                  >
                    {loading ? "Deleting..." : "Confirm Delete"}
                  </button>

                </div>

              </form>
            </div>
          </div>
        )}


{/* FILE ENCRYPTION PASSWORD RECOVERY MODAL */}
{showFileRecovery && (
  <div className="modal-overlay">
    <div className="modal recovery-modal">

      <button
        type="button"
        className="recovery-close"
        onClick={() => {
          setShowFileRecovery(false);
          setRecoveryOtpSent(false);
          setRecoveryOtp("");
          setNewEncryptionPassword("");
          setConfirmEncryptionPassword("");
          setShowRecoveryPassword(false);
          setShowConfirmRecoveryPassword(false);
          clearMessages();
        }}
      >
        ×
      </button>

      <div className="recovery-icon">
        {recoveryOtpSent ? "🔐" : "🛡️"}
      </div>

      <p className="recovery-eyebrow">
        KALIVIRA SECURITY
      </p>

      <h2>
        {recoveryOtpSent
          ? "Reset Encryption Password"
          : "Recover Encryption Password"}
      </h2>

      <p className="recovery-description">
        {recoveryOtpSent
          ? "Verify your email with the OTP and create a new password to access your protected files."
          : "Forgot your file encryption password? Verify your registered email to securely recover access."}
      </p>

      <div className="recovery-step">
        <span className={recoveryOtpSent ? "step completed" : "step active"}>
          1
        </span>
        <div className="step-line"></div>
        <span className={recoveryOtpSent ? "step active" : "step"}>
          2
        </span>
      </div>

      <div className="recovery-step-labels">
        <span>Email Verification</span>
        <span>New Password</span>
      </div>

      {error && (
        <div className="recovery-error">
          ✕ {error}
        </div>
      )}

      {message && (
        <div className="recovery-success">
          ✓ {message}
        </div>
      )}

      {!recoveryOtpSent ? (
        <form
          className="recovery-form"
          onSubmit={requestFileRecoveryOtp}
        >
          <div className="recovery-info">
            <span className="recovery-info-icon">✉</span>
            <div>
              <strong>Email verification required</strong>
              <p>
                A 6-digit OTP will be sent to your registered email address.
                The OTP is valid for 5 minutes.
              </p>
            </div>
          </div>

          <button
            type="submit"
            className="primary-button recovery-submit"
            disabled={loading}
          >
            {loading ? "Sending OTP..." : "Send Recovery OTP →"}
          </button>
        </form>
      ) : (
        <form
          className="recovery-form"
          onSubmit={resetFileEncryptionPassword}
        >
          <label>6-Digit Recovery OTP</label>

          <input
            className="recovery-input recovery-otp-input"
            type="text"
            inputMode="numeric"
            placeholder="Enter your 6-digit OTP"
            value={recoveryOtp}
            onChange={(e) =>
              setRecoveryOtp(e.target.value.replace(/\D/g, "").slice(0, 6))
            }
            maxLength={6}
            autoComplete="one-time-code"
            required
          />

          <div className="recovery-field">
            <label>New Encryption Password</label>

            <div className="recovery-password-wrapper">
              <input
                className="recovery-input"
                type={showRecoveryPassword ? "text" : "password"}
                placeholder="Enter new password"
                value={newEncryptionPassword}
                onChange={(e) => setNewEncryptionPassword(e.target.value)}
                autoComplete="new-password"
                required
              />

              <button
                type="button"
                onClick={() => setShowRecoveryPassword(!showRecoveryPassword)}
                aria-label={showRecoveryPassword ? "Hide password" : "Show password"}
              >
                {showRecoveryPassword ? "🙈" : "👁"}
              </button>
            </div>
          </div>

          <div className="recovery-field">
            <label>Confirm New Password</label>

            <div className="recovery-password-wrapper">
              <input
                className="recovery-input"
                type={showConfirmRecoveryPassword ? "text" : "password"}
                placeholder="Confirm new password"
                value={confirmEncryptionPassword}
                onChange={(e) => setConfirmEncryptionPassword(e.target.value)}
                autoComplete="new-password"
                required
              />

              <button
                type="button"
                onClick={() =>
                  setShowConfirmRecoveryPassword(!showConfirmRecoveryPassword)
                }
                aria-label={
                  showConfirmRecoveryPassword ? "Hide password" : "Show password"
                }
              >
                {showConfirmRecoveryPassword ? "🙈" : "👁"}
              </button>
            </div>
          </div>

          <div className="recovery-password-hint">
            <strong>Password requirements</strong>
            <p>At least 8 characters, including:</p>
            <ul>
              <li>One uppercase letter</li>
              <li>One lowercase letter</li>
              <li>One number</li>
              <li>One special character</li>
            </ul>
          </div>

          <button
            type="submit"
            className="primary-button recovery-submit"
            disabled={loading}
          >
            {loading ? "Resetting Password..." : "Reset Encryption Password"}
          </button>

          <button
            type="button"
            className="recovery-back-button"
            onClick={() => {
              setRecoveryOtpSent(false);
              setRecoveryOtp("");
              clearMessages();
            }}
          >
            ← Back to email verification
          </button>
        </form>
      )}

      <div className="recovery-footer">
        <span>🔒</span>
        Your file encryption recovery is protected by email verification.
      </div>

      <button
        type="button"
        className="recovery-cancel"
        onClick={() => {
          setShowFileRecovery(false);
          setRecoveryOtpSent(false);
          setRecoveryOtp("");
          setNewEncryptionPassword("");
          setConfirmEncryptionPassword("");
          clearMessages();
        }}
      >
        Cancel
      </button>

    </div>
  </div>
)}


</div>
);
}

//=========================================================
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
  error,

  showSignup,
  setShowSignup,

  signupName,
  setSignupName,
  signupEmail,
  setSignupEmail,
  signupPassword,
  setSignupPassword,

  signup,
  signupOtpRequired,
  signupOtp,
  setSignupOtp,
  verifySignupOtp,
  setSignupOtpRequired,


  showForgotPassword,
  setShowForgotPassword,
  resetOtpSent,
  setResetOtpSent,
  resetOtp,
  setResetOtp,
  newPassword,
  setNewPassword,
  confirmNewPassword,
  setConfirmNewPassword,
  forgotPassword,
  resetPassword
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


        {/* LOGIN FORM */}
        {!showSignup && !showForgotPassword && !mfaRequired && (
          <>
            <form onSubmit={login}>
              <label>Email</label>

              <input
                type="email"
                placeholder="Enter your email"
                value={loginEmail}
                onChange={(e) => setLoginEmail(e.target.value)}
                required
              />

              <label>Password</label>

              <input
                type="password"
                placeholder="Enter your password"
                value={loginPassword}
                onChange={(e) => setLoginPassword(e.target.value)}
                required
              />

              <div style={{ textAlign: "right", marginBottom: "15px" }}>
                <button
                  type="button"
                  className="text-button"
                  onClick={() => {
                    setShowForgotPassword(true);
                    setResetOtpSent(false);
                  }}
                >
                  Forgot Password?
                </button>
              </div>

              <button
                type="submit"
                className="primary-button"
                disabled={loading}
              >
                {loading ? "Signing in..." : "Sign In"}
              </button>
            </form>

            <p className="signup-switch">
              New to Kalivira?{" "}
              <button
                type="button"
                className="text-button"
                onClick={() => {
                  setShowSignup(true);
                  setSignupOtpRequired(false);
                }}
              >
                Create Account
              </button>
            </p>
          </>
        )}

        {/* MFA FORM */}
        {!showSignup && !showForgotPassword && mfaRequired && (
          <form onSubmit={verifyMfa}>
            <div className="otp-info">
              MFA OTP sent to
              <strong>{loginEmail}</strong>
            </div>

            <label>MFA OTP</label>

            <input
              type="text"
              placeholder="Enter 6-digit OTP"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              maxLength={6}
              pattern="[0-9]{6}"
              required
            />

            <button
              type="submit"
              className="primary-button"
              disabled={loading}
            >
              {loading ? "Verifying..." : "Verify & Continue"}
            </button>
          </form>
        )}

        {/* FORGOT PASSWORD - REQUEST OTP */}
        {!showSignup && showForgotPassword && !resetOtpSent && (
          <form onSubmit={forgotPassword}>
            <h2>Forgot Password?</h2>

            <p className="otp-info">
              Enter your registered email address to request a password reset OTP.
            </p>

            <label>Email</label>

            <input
              type="email"
              placeholder="Enter your registered email"
              value={loginEmail}
              onChange={(e) => setLoginEmail(e.target.value)}
              required
            />

            <button
              type="submit"
              className="primary-button"
              disabled={loading}
            >
              {loading ? "Sending..." : "Send Reset OTP"}
            </button>

            <button
              type="button"
              className="secondary-button"
              onClick={() => {
                setShowForgotPassword(false);
                setResetOtpSent(false);
              }}
            >
              Back to Login
            </button>
          </form>
        )}



        {/* FORGOT PASSWORD - RESET FORM */}
        {!showSignup && showForgotPassword && resetOtpSent && (
          <form onSubmit={resetPassword}>
            <h2>Reset Password</h2>

            <p className="otp-info">
              If an account exists, a password reset OTP has been sent to your email.
            </p>

            <label>Reset OTP</label>

            <input
              type="text"
              placeholder="Enter 6-digit OTP"
              value={resetOtp}
              onChange={(e) => setResetOtp(e.target.value)}
              maxLength={6}
              pattern="[0-9]{6}"
              required
            />

            <label>New Password</label>

            <input
              type="password"
              placeholder="Enter new password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />

            <label>Confirm New Password</label>

            <input
              type="password"
              placeholder="Confirm new password"
              value={confirmNewPassword}
              onChange={(e) => setConfirmNewPassword(e.target.value)}
              required
            />

            <button
              type="submit"
              className="primary-button"
              disabled={loading}
            >
              {loading ? "Resetting..." : "Reset Password"}
            </button>

            <button
              type="button"
              className="secondary-button"
              onClick={() => {
                setResetOtpSent(false);
                setResetOtp("");
                setNewPassword("");
                setConfirmNewPassword("");
              }}
            >
              Back
            </button>
          </form>
        )}


      {/* SIGNUP FORM */}
        {showSignup && !signupOtpRequired && (
          <form onSubmit={signup}>

            <label>Name</label>

            <input
              type="text"
              placeholder="Enter your name"
              value={signupName}
              onChange={(e) =>
                setSignupName(e.target.value)
              }
              required
            />

            <label>Email</label>

            <input
              type="email"
              placeholder="Enter your email"
              value={signupEmail}
              onChange={(e) =>
                setSignupEmail(e.target.value)
              }
              required
            />

            <label>Password</label>

            <input
              type="password"
              placeholder="Create a strong password"
              value={signupPassword}
              onChange={(e) =>
                setSignupPassword(e.target.value)
              }
              required
            />

            <p className="input-help">
              Password must be at least 8 characters
              and include uppercase, lowercase, a number,
              and a special character. This password
              encrypts your file. Remember it carefully;
              you will need the same password to decrypt
              and download it.
            </p>

            <button
              type="submit"
              className="primary-button"
              disabled={loading}
            >
              {loading
                ? "Creating Account..."
                : "Create Account"}
            </button>

            <button
              type="button"
              className="secondary-button"
              onClick={() => {
                setShowSignup(false);
                setSignupOtpRequired(false);
              }}
            >
              Back to Login
            </button>

          </form>
        )}

        {/* SIGNUP EMAIL OTP FORM */}
        {showSignup && signupOtpRequired && (
          <form onSubmit={verifySignupOtp}>

            <div className="otp-info">
              Registration OTP sent to
              <strong>{signupEmail}</strong>
            </div>

            <label>Email Verification OTP</label>

            <input
              type="text"
              placeholder="Enter 6-digit OTP"
              value={signupOtp}
              onChange={(e) =>
                setSignupOtp(e.target.value)
              }
              maxLength={6}
              required
            />

            <button
              type="submit"
              className="primary-button"
              disabled={loading}
            >
              {loading ? "Verifying..." : "Verify Email"}
            </button>

            <button
              type="button"
              className="secondary-button"
              onClick={() => {
                setSignupOtpRequired(false);
                setSignupOtp("");
              }}
            >
              Back
            </button>

          </form>
        )}

        {/* SUCCESS AND ERROR MESSAGES */}
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