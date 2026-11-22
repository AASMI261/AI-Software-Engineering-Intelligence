import React, { useState } from "react";
import "./Home.css";
import Sidebar from "./Sidebar"; // ✅ Added 
import Notification from "./Notification";// adjust path if needed

function App() {
  // sidebar open state
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  // rename state to avoid conflict with global open
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const handleSidebar = () => setIsSidebarOpen(!isSidebarOpen);
  // table rows state
  const [rows, setRows] = useState([
    { id: 1, name: "Ramesh Patel", pid: "101", ward: "Ward 3 / Room 12", status: "Infected", mdr: "High", admit: "2025-02-01" },
    { id: 2, name: "Meena Shah", pid: "102", ward: "Ward 2 / Room 8", status: "Suspected", mdr: "Medium", admit: "2025-02-03" }
  ]);

  // Add Row
  const addRow = () => {
    const newId = rows.length ? rows[rows.length - 1].id + 1 : 1;
    const newRow = {
      id: newId,
      name: "New Patient",
      pid: String(100 + newId),
      ward: "",
      status: "Unknown",
      mdr: "Low",
      admit: new Date().toISOString().slice(0, 10)
    };
    setRows(prev => [...prev, newRow]);
  };

  // Toggle 3-dot menu
 

  // Toggle Sidebar
  const toggleSidebar = () => setIsSidebarOpen(v => !v);

  return (
     
    <div
  style={{
    marginLeft: isSidebarOpen ? "280px" : "0",
    transition: "margin-left 0.3s"
  }}
   >

    <div className="app-root" style={{ marginLeft: isSidebarOpen ? "20px" : "0" }}>

      {/* Sidebar Component */}
      <Sidebar isOpen={isSidebarOpen} />

      {/* HEADER */}
      <div className="header-content">

        {/* LEFT 3-LINE BUTTON (☰) */}
        <button className="hamburger-btn" onClick={handleSidebar}>☰</button>
          

        {/* Title center */}
        <h1>MDR</h1>

        {/* FOUR BOXES */}
        <div className="boxes-container">
          <div className="role-box">
          <div lassName="role-label">Total MDR Positive</div>
          </div>
          <div className="role-box">
          <div lassName="role-label">Suspected Cases</div>
          </div>
          <div className="role-box">
          <div lassName="role-label">Active Alerts</div>
          </div>
          <div className="role-box">
          <div lassName="role-label">Contact Tracing Aleret</div>
          </div>
        </div>

        
      </div>

      {/* PATIENT TABLE */}
      <div className="patient-table-section">
        <div className="notification-wrapper">
          <Notification />
        </div>
        <div className="table-header">
          <h2>Active Patients List</h2>
          <button className="add-btn" onClick={addRow}>+ Add Patient</button>
        </div>

        <table className="patient-table">
          <thead>
            <tr>
              <th>Patient Name</th>
              <th>ID</th>
              <th>Ward / Room No</th>
              <th>Pathogen Status</th>
              <th>MDR Risk Level</th>
              <th>Admit Date</th>
              <th>Update</th>
            </tr>
          </thead>
          <tbody>
            {rows.map(r => (
              <tr key={r.id}>
                <td>{r.name}</td>
                <td>{r.pid}</td>
                <td>{r.ward}</td>
                <td>{r.status}</td>
                <td>{r.mdr}</td>
                <td>{r.admit}</td>
                <td><button className="update-btn">Update</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
    </div>
  );
}

export default App;
