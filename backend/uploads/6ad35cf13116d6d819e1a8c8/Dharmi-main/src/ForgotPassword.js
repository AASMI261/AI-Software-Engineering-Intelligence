// ForgotPassword.js
import React from "react";

function ForgotPassword() {
  const handleSubmit = (e) => {
    e.preventDefault();
    alert("Password reset link sent to your email!");
  };

  return (
    <div className="login-form">
      <h2>Forgot Password</h2>
      <p> Enter your email and we’ll send you a reset link.</p>
      <form onSubmit={handleSubmit}>
        <fieldset>
          <legend>Email ID</legend>
          <input type="email" required />
        </fieldset>
        <button type="submit">Send Reset Link</button>
      </form>
    </div>
  );
}

export default ForgotPassword;