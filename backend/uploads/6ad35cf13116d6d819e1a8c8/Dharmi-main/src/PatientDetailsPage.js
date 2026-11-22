// src/PatientDetailsPage.js
import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import "./PatientManagementPage.css"; // reuse styling

function PatientDetailsPage() {
  const { state } = useLocation(); // received patient data
  const navigate = useNavigate();

  if (!state) {
    return <h2>No patient data found</h2>;
  }

  const p = state;

  return (
    <div className="details-container">
      <h2>Patient Detail Info</h2>

      <div className="details-grid">
        <p><strong>Name:</strong> {p.name}</p>
        <p><strong>Contact No:</strong> {p.alternateContact}</p>

        <p><strong>Age:</strong> {p.age}</p>
        <p><strong>Gender:</strong> {p.gender}</p>

        <p><strong>Ward:</strong> {p.ward}</p>
        <p><strong>Doctor:</strong> {p.doctor}</p>

        <p><strong>Current Medicines:</strong> {p.medicines}</p>
        <p><strong>Precautions:</strong> {p.precautions}</p>

        <p><strong>Symptoms:</strong> {p.symptoms}</p>
        <p><strong>Immunity:</strong> {p.immunity}</p>

        <p><strong>Isolation Info:</strong> {p.isolation}</p>

        <p><strong>Admission Date:</strong> {p.admissionDate}</p>

        <p><strong>Guardian Name:</strong> {p.guardianName}</p>
        <p><strong>Relation:</strong> {p.relation}</p>

        <p><strong>Alternate Contact No:</strong> {p.alternateContact}</p>

        <p><strong>Past History:</strong> {p.pastHistory}</p>
        <p><strong>Antibiotic History:</strong> {p.antibioticHistory}</p>

        <p><strong>Last Checkup:</strong> {p.lastCheckup}</p>
        <p><strong>Risk Score:</strong> {p.riskScore}</p>

        <p><strong>Reports:</strong> 
          <a href={p.reports} target="_blank" rel="noreferrer"> View PDF</a>
        </p>
      </div>

      <div className="button-row">
        <button onClick={() => navigate(-1)}>Back</button>
        <button>Edit</button>
        <button>Update</button>
      </div>
    </div>
  );
}

export default PatientDetailsPage;
