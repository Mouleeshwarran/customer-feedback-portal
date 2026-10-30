import { useState } from "react";
import {
  MessageSquareText,
  LockKeyhole,
  Mail,
  UserCheck,
  ShieldAlert,
  ShieldCheck,
} from "lucide-react";
import { apiRequest } from "./api";
import "./Login.css";

export default function Login({ onLogin }) {
  const [email, setEmail] = useState("customer@example.com");
  const [password, setPassword] = useState("CustomerDemo123!");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const demoAccounts = [
    {
      role: "Customer",
      email: "customer@example.com",
      pass: "CustomerDemo123!",
      icon: UserCheck,
      badge: "customer",
    },
    {
      role: "Moderator",
      email: "moderator@example.com",
      pass: "ModeratorDemo123!",
      icon: ShieldCheck,
      badge: "moderator",
    },
    {
      role: "Administrator",
      email: "admin@example.com",
      pass: "AdminDemo123!",
      icon: ShieldAlert,
      badge: "admin",
    },
  ];

  function fillDemo(demo) {
    setEmail(demo.email);
    setPassword(demo.pass);
    setError("");
  }

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

      // Try administrator user endpoint first
      try {
        await apiRequest("/api/admin/users", credentials);
        role = "ADMINISTRATOR";
      } catch {
        // Next try moderator/administrator feedback endpoint
        try {
          await apiRequest("/api/admin/feedback", credentials);
          role = "MODERATOR";
        } catch {
          // If moderation is forbidden/denied, check customer access
          await apiRequest("/api/feedback/my", credentials);
          role = "CUSTOMER";
        }
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
          <MessageSquareText size={26} />
        </div>

        <p className="login-eyebrow">FEEDBACKHUB SAAS</p>
        <h1>Welcome back</h1>
        <p className="login-description">
          Sign in to submit feedback, review pending items, or manage the portal.
        </p>

        <div className="demo-accounts-bar">
          <span className="demo-label">Quick Demo Logins:</span>
          <div className="demo-chips">
            {demoAccounts.map((d) => (
              <button
                type="button"
                key={d.role}
                className={`demo-chip ${d.badge}`}
                onClick={() => fillDemo(d)}
                title={`Click to fill ${d.role} credentials`}
              >
                <d.icon size={13} />
                <span>{d.role}</span>
              </button>
            ))}
          </div>
        </div>

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
            {loading ? "Signing in..." : "Sign in to FeedbackHub"}
          </button>
        </form>

        <p className="login-footer">
          Customer Feedback Portal &bull; Academic Prototype
        </p>
      </section>
    </main>
  );
}
