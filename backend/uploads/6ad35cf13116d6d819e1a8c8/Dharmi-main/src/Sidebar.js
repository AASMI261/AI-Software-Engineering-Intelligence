import React from "react";
import { Link, useLocation } from "react-router-dom";
import "./Sidebar.css";

function Sidebar({ isOpen }) {
  const location = useLocation(); // get current URL

  return (
    <div className={`sidebar ${isOpen ? "open" : "closed"}`}>
      <h2 className="sidebar-title">Dashboard</h2>
      <ul className="menu">
        <li className={location.pathname === "/home" ? "active" : ""}>
          <Link to="/home">Home</Link>
        </li>
        <li className={location.pathname === "/patient-management" ? "active" : ""}>
          <Link to="/patient-management">Patient Management</Link>
        </li>
        <li className={location.pathname === "/report" ? "active" : ""}>
          <Link to="/report">Reports</Link>
        </li>
        <li>
          <Link to="/login/admin">Logout</Link>
        </li>
      </ul>
    </div>
  );
}

export default Sidebar;
