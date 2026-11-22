// src/PatientManagementPage.js
import React, { useState } from "react";
import "./PatientManagementPage.css"; // reuse styling
import { useNavigate } from "react-router-dom"; 
import Sidebar from "./Sidebar";

function PatientManagementPage() {
  const [searchNameId, setSearchNameId] = useState("");
  const [searchWard, setSearchWard] = useState("");
 const [isSidebarOpen, setIsSidebarOpen] = useState(false);
 
   // rename state to avoid conflict with global open
   const [isMenuOpen, setIsMenuOpen] = useState(false);
 
   const handleSidebar = () => setIsSidebarOpen(!isSidebarOpen);
  const navigate = useNavigate();  
  
  const patients = [
    {
      name: "John Doe",
      id: "P001",
      ward: "A1",
      status: "Stable",
      history: "Visited Mumbai, Delhi",
    },
    {
      name: "Jane Smith",
      id: "P002",
      ward: "B2",
      status: "Critical",
      history: "Returned from Dubai",
    },
    // Add more dummy patients as needed
  ];
  const toggleSidebar = () => setIsSidebarOpen(v => !v);
  

  const filteredPatients = patients.filter((p) =>
    (p.name + p.id).toLowerCase().includes(searchNameId.toLowerCase()) &&
    p.ward.toLowerCase().includes(searchWard.toLowerCase())
  );
  
  

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
          

    <div className="login-form">
      <h2>Patient Management</h2>
       
      {/* Patient Table */}
      <table className="patient-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>ID</th>
            <th>Ward</th>
            <th>Status</th>
            <th>Travelling History</th>
            <th>View</th>
          </tr>
        </thead>
        <tbody>
          {filteredPatients.map((p, index) => (
            <tr key={index}>
              <td>{p.name}</td>
              <td>{p.id}</td>
              <td>{p.ward}</td>
              <td>{p.status}</td>
              <td>{p.history}</td>
              <td>
                <button onClick={() => navigate(`/details/${p.id}`, { state: p })}>
                  View
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    </div>
    </div>
    </div>
  );
}
export default PatientManagementPage;