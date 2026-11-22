// src/SignUpPage.js
import React from "react";
import { useNavigate } from "react-router-dom";
import "./RoleLoginPage.css"; // reuse styling


function SignUpPage() {
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    alert("Sign up submitted!");
  };

  return (
    <div className="login-form">
      <h2>Sign Up Form</h2>
      <form onSubmit={handleSubmit}>
        <fieldset>
          <legend>Name</legend>
          <input type="text" required />
        </fieldset>

        <fieldset>
          <legend>Mobile Number</legend>
          <input type="tel" required />
        </fieldset>

        <fieldset>
          <legend>Email</legend>
          <input type="email" required />
        </fieldset>

        <fieldset>
          <legend>Set Password</legend>
          <input type="password" required />
        </fieldset>

        <button type="submit">Submit</button>
      </form>

      {/* Back to Login */}
      <p className="forgot-password" >
        <button type="button" onClick={() => navigate("/")}>
          Back to Login
        </button>
      </p>
    </div>
  );
}

export default SignUpPage;