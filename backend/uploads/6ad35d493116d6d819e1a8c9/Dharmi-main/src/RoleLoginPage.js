// RoleLoginPage.js
import React, { useState } from "react";
import "./RoleLoginPage.css";
import { FaUserMd, FaUserNurse, FaUserTie, FaVials } from "react-icons/fa";

import { useNavigate } from "react-router-dom";

function RoleLoginPage() {
  const navigate = useNavigate();

  const handleRoleClick = (role) => {
    navigate(`/login/${role}`);
  };



  return (
    
    <div className="role-page">
      <div className="intro-section">
        <div className="intro-box">
          {/* Use the function here */}
          <img
            src="https://acsonnet.com/wp-content/uploads/2021/05/Hospital-Management-System.jpg"
            alt="Hospital Interface"
            className="intro-image"
          />
          <div className="intro-text">
            <h1>Digital Contact Tracing & MDR Screening System</h1>
            <p> A smart hospital tool designed to track contact history, detect risks of multidrug-resistant (MDR) infections, and support faster infection-control decisions.Please log in to access the dashboard and patient screening data. </p>
          </div>
        </div>
      </div>

      {/* Role Boxes */}
      <div className="role-grid">
        <div className="role-box" onClick={() => handleRoleClick("Doctor")}>
          <div className="role-icon"><FaUserMd /></div>
          <div className="role-label">Doctor</div>
        </div>
        {/* Admin */}
  <div className="role-box" onClick={() => handleRoleClick("Admin")}>
    <div className="role-icon"><FaUserTie /></div>   {/* businessperson icon */}
    <div className="role-label">Admin</div>
  </div>

  {/* Nurse */}
  <div className="role-box" onClick={() => handleRoleClick("Nurse")}>
    <div className="role-icon"><FaUserNurse /></div> {/* nurse icon */}
    <div className="role-label">Nurse</div>
  </div>

        <div className="role-box" onClick={() => handleRoleClick("Staff")}>
          <div className="role-icon"><FaUserTie /></div>
          <div className="role-label">Staff</div>
        </div>
        <div className="role-box" onClick={() => handleRoleClick("Lab Technician")}>
          <div className="role-icon"><FaVials /></div>
          <div className="role-label">Lab Technician</div>
        </div>
      </div>
    
    </div>
  );
}

export default RoleLoginPage;
