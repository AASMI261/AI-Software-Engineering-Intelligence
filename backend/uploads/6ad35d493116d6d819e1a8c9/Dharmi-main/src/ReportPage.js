import React, { useState } from "react";
import { ResponsiveContainer } from "recharts";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  BarChart,
  Bar,
  Legend,
} from "recharts";
import Sidebar from "./Sidebar";
import "./ReportPage.css";

const ReportPage = () => {
  // Dummy data → Replace with DB data later
  const stats = [
    { title: "Active MDR Cases", value: 120 },
    { title: "Total Exposed Cases", value: 79 },
    { title: "High-Risk Contacts", value: 45 },
    { title: "Recovery Rate", value: "78%" },
  ];

  const mdrCasesData = [
    { date: "01 Jan", cases: 30 },
    { date: "05 Jan", cases: 45 },
    { date: "10 Jan", cases: 60 },
    { date: "15 Jan", cases: 42 },
    { date: "20 Jan", cases: 70 },
  ];

  const trackingActivity = [
    { date: "Mon", count: 20 },
    { date: "Tue", count: 28 },
    { date: "Wed", count: 25 },
    { date: "Thu", count: 30 },
    { date: "Fri", count: 24 },
  ];

  const treatmentTable = [
    { name: "A", patients: 120, recovered: 95, rate: "79%" },
    { name: "B", patients: 80, recovered: 62, rate: "77%" },
  ];

  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const toggleSidebar = () => setIsSidebarOpen(v => !v);
  const handleSidebar = () => setIsSidebarOpen((prev) => !prev);

  return (
    <div
        className="report-container"
        style={{
          marginLeft: isSidebarOpen ? 280 : 0, // shift content when sidebar opens
          transition: "margin-left 0.3s",
        }}
      >
    <div className="app-root" style={{ marginLeft: isSidebarOpen ? "20px" : "0" }}> 
      {/* Sidebar Component */}
      <Sidebar isOpen={isSidebarOpen} />

      <div className="header-content">

        {/* LEFT 3-LINE BUTTON (☰) */}
        <button className="hamburger-btn" onClick={handleSidebar}>☰</button>
      <h1> Report And Analysis</h1>
        {/* HEADER */}
        <div className="top-bar">
          <select className="filter-select">
            <option>Daily</option>
            <option>Weekly</option>
            <option>Monthly</option>
          </select>

          <button className="report-btn">Download Report (PDF)</button>

          <input type="date" className="date-picker" />
        </div>

        {/* STAT BOXES */}
        <div className="stats-box-container">
          {stats.map((item, index) => (
            <div className="stat-box" key={index}>
              <h4>{item.title}</h4>
              <p>{item.value}</p>
            </div>
          ))}
        </div>

        {/* GRAPHS SECTION */}
        
        {/* TREATMENT TABLE */}
        <h3 className="table-title">Treatment Efficiency Table</h3>
        <table className="treatment-table">
          <thead>
            <tr>
              <th>Treatment</th>
              <th>Total Patients</th>
              <th>Recovered Patients</th>
              <th>Success Rate</th>
            </tr>
          </thead>
          <tbody>
            {treatmentTable.map((row, index) => (
              <tr key={index}>
                <td>{row.name}</td>
                <td>{row.patients}</td>
                <td>{row.recovered}</td>
                <td>{row.rate}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
    </div>
  );
};

export default ReportPage;
