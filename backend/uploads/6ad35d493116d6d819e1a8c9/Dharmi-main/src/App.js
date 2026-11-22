// App.js
import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import RoleLoginPage from "./RoleLoginPage";
import LoginPage from "./LoginPage"; // new login page component
import ForgotPassword from "./ForgotPassword";
import SignUpPage from "./SignUpPage";
import PatientManagementPage from "./PatientManagementPage";
import PatientDetailsPage from "./PatientDetailsPage";
import ReportPage from "./ReportPage";
import Sidebar from "./Sidebar";
import Home from "./Home";
import Notification from "./Notification";   // ✅ your notification component
import timeAgo from "./timeAgo";
 

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<RoleLoginPage />} />
        <Route path="/login/:role" element={<LoginPage />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/sign-up" element={<SignUpPage />} />
        <Route path="/patient-management" element={<PatientManagementPage />} />
        <Route path="/details/:id" element={<PatientDetailsPage />} />
        <Route path="/ReportPage" element={<ReportPage.js />} />
        <Route path="/ReportPage" element={<ReportPage />} /> 
        <Route path="/notifications" element={<Notification />} />
        <Route
          path="/timeago"
          element={
            <div style={{ padding: "20px" }}>
              <h2>TimeAgo Demo</h2>
              <p>{timeAgo(new Date(Date.now() - 5 * 60 * 1000))}</p>
            </div>
          }
        />

        <Route
          path="/patient-management"
          element={
            <div style={{ display: "flex" }}>
              <Sidebar />
              <div style={{ marginLeft: "220px", width: "100%" }}>
                <PatientManagementPage />
              </div>
            </div>
          }
        />
        <Route
          path="/details/:id"
          element={
            <div style={{ display: "flex" }}>
              <Sidebar />
              <div style={{ marginLeft: "220px", width: "100%" }}>
                <PatientDetailsPage />
              </div>
            </div>
          }
        />

        <Route
  path="/report"
  element={
    <div style={{ display: "flex" }}>
      <Sidebar />
      <div style={{ marginLeft: "220px", width: "100%" }}>
        <ReportPage />
      </div>
    </div>
  }
/>

        <Route
  path="/Home"
  element={
    <div style={{ display: "flex" }}>
      <Sidebar />
      <div style={{ marginLeft: "0px", width: "100%" }}>
        <Home />
      </div>
    </div>
  }
/>

      </Routes> 
    </Router>
  );
}

export default App;