import { useState } from "react";
import {
  MessageSquareText,
  LockKeyhole,
  Mail,
} from "lucide-react";
import { apiRequest } from "./api";
import "./Login.css";

export default function Login({ onLogin }) {
  const [email, setEmail] = useState("customer@example.com");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);

    const credentials = {
      username: email.trim(),
      password,
    };

    try {
      let role;

      // Check moderator/administrator access first.
      try {
        await apiRequest("/api/admin/feedback", credentials);
        role = "MODERATOR";
      } catch {
        // If moderation access is denied, check customer access.
        await apiRequest("/api/feedback/my", credentials);
        role = "CUSTOMER";
      }

      onLogin({
        email: email.trim(),
        password,
        role,
      });
    } catch {
      setError(
        "Login failed. Check your email and password, and ensure the backend is running."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-card">
        <div className="login-logo">
          <MessageSquareText size={28} />
        </div>

        <p className="login-eyebrow">CUSTOMER EXPERIENCE</p>
        <h1>Welcome back</h1>
        <p className="login-description">
          Sign in to share feedback and track your submissions.
        </p>

        <form onSubmit={handleSubmit}>
          <label htmlFor="email">Email address</label>

          <div className="login-input">
            <Mail size={18} />
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              autoComplete="username"
              required
            />
          </div>

          <label htmlFor="password">Password</label>

          <div className="login-input">
            <LockKeyhole size={18} />
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Enter your password"
              autoComplete="current-password"
              required
            />
          </div>

          {error && (
            <p className="login-error" role="alert">
              {error}
            </p>
          )}

          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <p className="login-footer">
          Customer Feedback Portal
        </p>
      </section>
    </main>
  );
}
