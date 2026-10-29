import { useEffect, useState } from "react";
import {
  MessageSquareText,
  LayoutDashboard,
  History,
  ShieldCheck,
  Search,
  Star,
  Send,
  ThumbsUp,
  ArrowUpRight,
  CheckCircle,
  XCircle,
  EyeOff,
  RefreshCw,
  Flag,
} from "lucide-react";
import "./App.css";
import Login from "./Login";
import { apiRequest } from "./api";

const categoryIds = {
  Product: 1,
  Service: 2,
  Support: 3,
  Other: 4,
};

function App() {
  const [activeTab, setActiveTab] = useState("Overview");
  const [category, setCategory] = useState("Product");
  const [rating, setRating] = useState(5);
  const [feedbackText, setFeedbackText] = useState("");
  const [isPrivate, setIsPrivate] = useState(false);
  const [search, setSearch] = useState("");
  const [notice, setNotice] = useState("");
  const [user, setUser] = useState(null);
  const [reportingFeedbackId, setReportingFeedbackId] = useState(null);
const [reportReason, setReportReason] = useState("");
const [reportSubmitting, setReportSubmitting] = useState(false);
const [reportNotice, setReportNotice] = useState("");
const [reportError, setReportError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [dashboardStats, setDashboardStats] = useState({
    totalFeedback: 0,
    averageRating: 0,
    pendingFeedback: 0,
  });
  const [loadingStats, setLoadingStats] = useState(false);
  const [statsError, setStatsError] = useState("");
  const [statsReload, setStatsReload] = useState(0);
  const [unresolvedAbuseCount, setUnresolvedAbuseCount] = useState(0);
const [loadingAbuseCount, setLoadingAbuseCount] = useState(false);
const [abuseCountError, setAbuseCountError] = useState("");
  const [mySubmissions, setMySubmissions] = useState([]);
  const [loadingSubmissions, setLoadingSubmissions] = useState(false);
  const [submissionsError, setSubmissionsError] = useState("");

  const [publicFeedback, setPublicFeedback] = useState([]);
  const [loadingPublic, setLoadingPublic] = useState(false);
  const [publicError, setPublicError] = useState("");
  const [publicCategory, setPublicCategory] = useState("All");
  const [publicRating, setPublicRating] = useState("All");
  const [publicReload, setPublicReload] = useState(0);

  const [pendingFeedback, setPendingFeedback] = useState([]);
  const [loadingPending, setLoadingPending] = useState(false);
  const [moderationError, setModerationError] = useState("");
  const [moderationReasons, setModerationReasons] = useState({});
  const [moderatingId, setModeratingId] = useState(null);
  const [moderationNotice, setModerationNotice] = useState("");
  const [moderationReload, setModerationReload] = useState(0);

  const [abuseReports, setAbuseReports] = useState([]);
  const [reportsLoading, setReportsLoading] = useState(false);
  const [reportsError, setReportsError] = useState("");
  const [reportsNotice, setReportsNotice] = useState("");
  const [reportsReload, setReportsReload] = useState(0);
  const [resolvingReportId, setResolvingReportId] = useState(null);

  const isModerator =
    user?.role === "MODERATOR" ||
    user?.role === "ADMINISTRATOR";

  const navItems = [
    { name: "Overview", icon: LayoutDashboard },
    { name: "Browse Feedback", icon: MessageSquareText },
    { name: "My Submissions", icon: History },
    ...(isModerator
      ? [
          { name: "Moderation", icon: ShieldCheck },
          { name: "Abuse Reports", icon: Flag },
        ]
      : []),
  ];

  // Submit new feedback.
  async function submitFeedback(event) {
    event.preventDefault();

    if (!user || submitting) return;

    const trimmedText = feedbackText.trim();

    if (!trimmedText) {
      setNotice("Please enter your feedback before submitting.");
      return;
    }

    setSubmitting(true);
    setNotice("");

    try {
      await apiRequest("/api/feedback", {
        method: "POST",
        username: user.email,
        password: user.password,
        body: JSON.stringify({
          categoryId: categoryIds[category],
          rating,
          text: trimmedText,
          privateFeedback: isPrivate,
        }),
      });

      setNotice(
        "Feedback submitted successfully! It is awaiting moderation."
      );

      setFeedbackText("");
      setRating(5);
      setCategory("Product");
      setIsPrivate(false);
      setStatsReload((previous) => previous + 1);
    } catch (error) {
      setNotice(
        error.message || "Unable to submit feedback. Please try again."
      );
    } finally {
      setSubmitting(false);
    }
  }
  async function submitAbuseReport(event) {
  event.preventDefault();

  if (!user || reportingFeedbackId === null || reportSubmitting) {
    return;
  }

  const reason = reportReason.trim();

  if (!reason) {
    setReportError("Please enter a reason for reporting this feedback.");
    return;
  }

  if (reason.length > 500) {
    setReportError("The reason cannot exceed 500 characters.");
    return;
  }

  setReportSubmitting(true);
  setReportError("");
  setReportNotice("");

  try {
    await apiRequest(
      `/api/feedback/${reportingFeedbackId}/report`,
      {
        method: "POST",
        username: user.email,
        password: user.password,
        body: JSON.stringify({ reason }),
      }
    );

    setReportNotice("Report submitted successfully.");
    setReportingFeedbackId(null);
    setReportReason("");
  } catch (error) {
    setReportError(
      error.message || "Unable to submit the report. Please try again."
    );
  } finally {
    setReportSubmitting(false);
  }
}

  // Fetch dashboard statistics from the backend.
  useEffect(() => {
    if (!user) return;

    let cancelled = false;

    async function loadDashboardStats() {
      setLoadingStats(true);
      setStatsError("");

      try {
        const response = await apiRequest("/api/feedback/stats", {
          username: user.email,
          password: user.password,
        });

        if (!cancelled) {
          setDashboardStats({
            totalFeedback: Number(response?.totalFeedback ?? 0),
            averageRating: Number(response?.averageRating ?? 0),
            pendingFeedback: Number(response?.pendingFeedback ?? 0),
          });
        }
      } catch (error) {
        if (!cancelled) {
          setStatsError(error.message || "Unable to load dashboard statistics.");
        }
      } finally {
        if (!cancelled) setLoadingStats(false);
      }
    }

    loadDashboardStats();

    return () => {
      cancelled = true;
    };
  }, [user, statsReload]);
  
  // Fetch the unresolved abuse-report count for moderators.
  useEffect(() => {
    if (!user || !isModerator) {
      setUnresolvedAbuseCount(0);
      return;
    }

    let cancelled = false;

    async function loadUnresolvedAbuseCount() {
      setLoadingAbuseCount(true);
      setAbuseCountError("");

      try {
        const response = await apiRequest(
          "/api/admin/reports?page=0&size=1",
          {
            username: user.email,
            password: user.password,
          }
        );

        if (!cancelled) {
          setUnresolvedAbuseCount(
            Number(response?.totalElements ?? 0)
          );
        }
      } catch (error) {
        if (!cancelled) {
          setAbuseCountError(
            error.message || "Unable to load abuse-report count."
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingAbuseCount(false);
        }
      }
    }

    loadUnresolvedAbuseCount();

    return () => {
      cancelled = true;
    };
  }, [user, isModerator, reportsReload, statsReload]);


  // Fetch the logged-in customer's own feedback.
  useEffect(() => {
    if (activeTab !== "My Submissions" || !user) return;

    let cancelled = false;

    async function loadMySubmissions() {
      setLoadingSubmissions(true);
      setSubmissionsError("");

      try {
        const response = await apiRequest("/api/feedback/my", {
          username: user.email,
          password: user.password,
        });

        if (!cancelled) {
          const items = Array.isArray(response)
            ? response
            : response?.content ?? response?.data ?? [];

          setMySubmissions(items);
        }
      } catch (error) {
        if (!cancelled) {
          setSubmissionsError(
            error.message || "Unable to load your submissions."
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingSubmissions(false);
        }
      }
    }

    loadMySubmissions();

    return () => {
      cancelled = true;
    };
  }, [activeTab, user]);

  // Fetch approved, non-private public feedback with server-side filters.
  useEffect(() => {
    if (
      activeTab !== "Browse Feedback" &&
      activeTab !== "Overview"
    ) {
      return;
    }

    let cancelled = false;

    async function loadPublicFeedback() {
      setLoadingPublic(true);
      setPublicError("");

      try {
        const params = new URLSearchParams({
          page: "0",
          size: "20",
        });

        if (search.trim()) {
          params.set("search", search.trim());
        }

        if (publicCategory !== "All") {
          params.set("categoryId", String(categoryIds[publicCategory]));
        }

        if (publicRating !== "All") {
          params.set("rating", String(publicRating));
        }

        const response = await apiRequest(
          `/api/feedback/public?${params.toString()}`
        );

        if (!cancelled) {
          const items = Array.isArray(response)
            ? response
            : response?.content ?? response?.data ?? [];

          setPublicFeedback(items);
        }
      } catch (error) {
        if (!cancelled) {
          setPublicError(
            error.message || "Unable to load public feedback."
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingPublic(false);
        }
      }
    }

    loadPublicFeedback();

    return () => {
      cancelled = true;
    };
  }, [activeTab, search, publicCategory, publicRating, publicReload]);

  // Fetch pending feedback for authorized moderators.
  useEffect(() => {
    if (activeTab !== "Moderation" || !user || !isModerator) {
      return;
    }

    let cancelled = false;

    async function loadPendingFeedback() {
      setLoadingPending(true);
      setModerationError("");
      setModerationNotice("");

      try {
        const response = await apiRequest(
          "/api/admin/feedback?page=0&size=50",
          {
            username: user.email,
            password: user.password,
          }
        );

        if (!cancelled) {
          const items = Array.isArray(response)
            ? response
            : response?.content ?? response?.data ?? [];

          setPendingFeedback(items);
        }
      } catch (error) {
        if (!cancelled) {
          setModerationError(
            error.message || "Unable to load pending feedback."
          );
        }
      } finally {
        if (!cancelled) {
          setLoadingPending(false);
        }
      }
    }

    loadPendingFeedback();

    return () => {
      cancelled = true;
    };
  }, [activeTab, user, isModerator, moderationReload]);

  // Fetch unresolved abuse reports for authorized moderators.
  useEffect(() => {
    if (activeTab !== "Abuse Reports" || !user || !isModerator) {
      return;
    }

    let cancelled = false;

    async function loadAbuseReports() {
      setReportsLoading(true);
      setReportsError("");
      setReportsNotice("");

      try {
        const response = await apiRequest(
          "/api/admin/reports?page=0&size=50",
          {
            username: user.email,
            password: user.password,
          }
        );

        if (!cancelled) {
          const items = Array.isArray(response)
            ? response
            : response?.content ?? response?.data ?? [];
          setAbuseReports(items);
        }
      } catch (error) {
        if (!cancelled) {
          setReportsError(error.message || "Unable to load abuse reports.");
        }
      } finally {
        if (!cancelled) setReportsLoading(false);
      }
    }

    loadAbuseReports();

    return () => {
      cancelled = true;
    };
  }, [activeTab, user, isModerator, reportsReload]);

  // Mark an abuse report as resolved. This does not change the feedback status.
  

async function resolveAbuseReport(reportId) {
  if (!user || !isModerator || resolvingReportId !== null) return;

  setResolvingReportId(reportId);
  setReportsError("");
  setReportsNotice("");

  try {
    await apiRequest(`/api/admin/reports/${reportId}/resolve`, {
      method: "PATCH",
      username: user.email,
      password: user.password,
    });

    // Add these three lines here.
    setUnresolvedAbuseCount((previous) => Math.max(0, previous - 1));
    setReportsReload((previous) => previous + 1);
    setStatsReload((previous) => previous + 1);

    // Keep your existing code below this point.
    setAbuseReports((previous) =>
      previous.filter((report) => report.id !== reportId)
    );

    setReportsNotice(`Abuse report #${reportId} resolved.`);
  } catch (error) {
    setReportsError(error.message || "Unable to resolve the abuse report.");
  } finally {
    setResolvingReportId(null);
  }
}


  // Approve, reject or hide feedback.
  async function moderateFeedback(feedbackId, decision) {
    if (!user || !isModerator || moderatingId !== null) return;

    const reason = (moderationReasons[feedbackId] ?? "").trim();

    if (!reason) {
      setModerationNotice(
        "Enter a reason before making a moderation decision."
      );
      return;
    }

    if (reason.length > 500) {
      setModerationNotice(
        "The reason must not exceed 500 characters."
      );
      return;
    }

    setModeratingId(feedbackId);
    setModerationNotice("");
    setModerationError("");

    try {
      await apiRequest(
        `/api/admin/feedback/${feedbackId}/moderate`,
        {
          method: "PATCH",
          username: user.email,
          password: user.password,
          body: JSON.stringify({
            decision,
            reason,
          }),
        }
      );

      setModerationNotice(
        `Feedback #${feedbackId} was ${decision.toLowerCase()}.`
      );

      setModerationReasons((previous) => {
        const updated = { ...previous };
        delete updated[feedbackId];
        return updated;
      });

      // Reload pending feedback so the processed item disappears.
      setModerationReload((previous) => previous + 1);
      setStatsReload((previous) => previous + 1);
    } catch (error) {
      setModerationError(
        error.message || "Unable to process this feedback."
      );
    } finally {
      setModeratingId(null);
    }
  }

  if (!user) {
    return <Login onLogin={(credentials) => setUser(credentials)} />;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">
            <MessageSquareText size={23} />
          </div>
          <div>
            <h2>FeedbackHub</h2>
            <span>Customer portal</span>
          </div>
        </div>

        <div className="nav-label">WORKSPACE</div>

        <nav>
          {navItems.map(({ name, icon: Icon }) => (
            <button
              key={name}
              type="button"
              className={`nav-item ${
                activeTab === name ? "active" : ""
              }`}
              onClick={() => {
                setActiveTab(name);
                setNotice("");
                setModerationNotice("");
              }}
            >
              <Icon size={19} />
              <span>{name}</span>

              {(name === "Moderation" || name === "Abuse Reports") && (
                <span className="nav-tag">Admin</span>
              )}
            </button>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="help-card">
            <div className="help-icon">
              <ThumbsUp size={19} />
            </div>
            <h3>Your voice matters.</h3>
            <p>Help us improve by sharing your experience.</p>

            <button
              type="button"
              onClick={() => {
                setActiveTab("Submit Feedback");
                setNotice("");
              }}
            >
              Share feedback <ArrowUpRight size={15} />
            </button>
          </div>

          <div className="user-profile">
            <div className="avatar">CU</div>
            <div className="user-info">
              <strong>{user.email}</strong>
              <span>
                {isModerator ? "Moderator account" : "Customer account"}
              </span>
            </div>

            <button
              type="button"
              className="logout-button"
              onClick={() => {
                setUser(null);
                setActiveTab("Overview");
              }}
            >
              Logout
            </button>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">
            Workspace <span>/</span> <strong>{activeTab}</strong>
          </div>

          <div className="topbar-right">
            <span className="status-dot" />
            <span>Portal preview</span>
            <div className="avatar small">CU</div>
          </div>
        </header>

        <section className="page-content">
          <div className="welcome-row">
            <div>
              <div className="eyebrow">CUSTOMER EXPERIENCE</div>
              <h1>
                {activeTab === "Overview"
                  ? "Your feedback, our progress."
                  : activeTab}
              </h1>
              <p className="subtitle">
                Share your thoughts and help us build a better experience.
              </p>
            </div>

            <button
              type="button"
              className="primary-button"
              onClick={() => {
                setActiveTab("Submit Feedback");
                setNotice("");
              }}
            >
              <Send size={17} /> Submit feedback
            </button>
          </div>

          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-top">
                <span>Total feedback</span>
                <div className="stat-icon purple">
                  <MessageSquareText size={19} />
                </div>
              </div>
              <h2>{loadingStats ? "…" : dashboardStats.totalFeedback}</h2>
              <span className="stat-note">{statsError ? "Statistics unavailable" : "Feedback received"}</span>
            </div>

            <div className="stat-card">
              <div className="stat-top">
                <span>Average rating</span>
                <div className="stat-icon yellow">
                  <Star size={19} />
                </div>
              </div>
              <h2>{loadingStats ? "…" : dashboardStats.averageRating.toFixed(1)} <small>/ 5</small></h2>
              <span className="stat-note">{statsError ? "Statistics unavailable" : "Average customer rating"}</span>
            </div>

            <div className="stat-card">
              <div className="stat-top">
                <span>Awaiting review</span>
                <div className="stat-icon blue">
                  <History size={19} />
                </div>
              </div>
              <h2>{loadingStats ? "…" : dashboardStats.pendingFeedback}</h2>
              <span className="stat-note">{statsError ? "Statistics unavailable" : "Pending moderation"}</span>
            </div>
            
{isModerator && (
  <div className="stat-card">
    <div className="stat-top">
      <span>Unresolved abuse reports</span>
      <div className="stat-icon blue">
        <Flag size={19} />
      </div>
    </div>

    <h2>
      {loadingAbuseCount ? "…" : unresolvedAbuseCount}
    </h2>

    <span className="stat-note">
      {abuseCountError
        ? "Count unavailable"
        : "Reports awaiting resolution"}
    </span>
  </div>
)}

          </div>

          {/* Submit Feedback */}
          {activeTab === "Submit Feedback" ? (
            <section className="panel submit-panel">
              <div className="panel-heading">
                <div>
                  <h2>Share your experience</h2>
                  <p>
                    Your feedback helps us improve our products and services.
                  </p>
                </div>
              </div>

              <form onSubmit={submitFeedback}>
                <label htmlFor="category">Feedback category</label>
                <select
                  id="category"
                  value={category}
                  onChange={(event) => setCategory(event.target.value)}
                >
                  <option>Product</option>
                  <option>Service</option>
                  <option>Support</option>
                  <option>Other</option>
                </select>

                <label>Your rating</label>
                <div className="rating-picker">
                  {[1, 2, 3, 4, 5].map((value) => (
                    <button
                      type="button"
                      key={value}
                      className={value <= rating ? "star selected" : "star"}
                      onClick={() => setRating(value)}
                      aria-label={`${value} stars`}
                    >
                      <Star size={27} fill="currentColor" />
                    </button>
                  ))}
                  <span>{rating} out of 5</span>
                </div>

                <label htmlFor="feedback">Your comments</label>
                <textarea
                  id="feedback"
                  rows="5"
                  maxLength="3000"
                  placeholder="Tell us about your experience..."
                  value={feedbackText}
                  onChange={(event) => setFeedbackText(event.target.value)}
                  required
                />

                <div className="character-count">
                  {feedbackText.length}/3000 characters
                </div>

                <label className="privacy-option">
                  <input
                    type="checkbox"
                    checked={isPrivate}
                    onChange={(event) => setIsPrivate(event.target.checked)}
                  />
                  Keep this feedback private
                </label>

                <button
                  className="primary-button"
                  type="submit"
                  disabled={submitting}
                >
                  <Send size={17} />
                  {submitting ? "Submitting..." : "Submit feedback"}
                </button>

                {notice && (
                  <p className="notice" role="status">{notice}</p>
                )}
              </form>
            </section>
          ) : activeTab === "My Submissions" ? (
            <section className="panel">
              <h2>My submissions</h2>
              <p className="subtitle">
                Track your feedback and its review status.
              </p>

              {loadingSubmissions ? (
                <div className="empty-state">
                  <History size={30} />
                  <p>Loading your submissions...</p>
                </div>
              ) : submissionsError ? (
                <div className="empty-state">
                  <p>{submissionsError}</p>
                  <button
                    type="button"
                    className="primary-button"
                    onClick={() => {
                      setActiveTab("Overview");
                      setTimeout(
                        () => setActiveTab("My Submissions"),
                        0
                      );
                    }}
                  >
                    Try again
                  </button>
                </div>
              ) : mySubmissions.length === 0 ? (
                <div className="empty-state">
                  <History size={30} />
                  <p>You haven't submitted any feedback yet.</p>
                </div>
              ) : (
                <div className="feedback-list">
                  {mySubmissions.map((item) => (
                    <article className="feedback-card" key={item.id}>
                      <div className="feedback-avatar">
                        {(item.category ?? "F").charAt(0)}
                      </div>

                      <div className="feedback-body">
                        <div className="feedback-meta">
                          <span className="category-pill">
                            {item.category ?? "Feedback"}
                          </span>
                          <span className="feedback-date">
                            {item.createdAt
                              ? new Date(item.createdAt).toLocaleDateString()
                              : "Date unavailable"}
                          </span>
                        </div>

                        <div className="stars">
                          {Array.from(
                            {
                              length: Math.min(
                                5,
                                Math.max(0, Number(item.rating) || 0)
                              ),
                            },
                            (_, index) => (
                              <Star
                                key={index}
                                size={15}
                                fill="currentColor"
                              />
                            )
                          )}
                        </div>

                        <p>{item.text}</p>
                        <span className="feedback-author">
                          Status: {item.status ?? "Unknown"}
                        </span>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          ) : activeTab === "Moderation" ? (
            <section className="panel">
              <div className="section-heading">
                <div>
                  <h2>Moderation workspace</h2>
                  <p className="subtitle">
                    Review pending feedback and record your decision.
                  </p>
                </div>

                <button
                  type="button"
                  className="text-button"
                  disabled={loadingPending || moderatingId !== null}
                  onClick={() =>
                    setModerationReload((previous) => previous + 1)
                  }
                >
                  <RefreshCw size={16} /> Refresh
                </button>
              </div>

              {moderationNotice && (
                <p className="notice" role="status">
                  {moderationNotice}
                </p>
              )}

              {moderationError && (
                <p className="login-error" role="alert">
                  {moderationError}
                </p>
              )}

              {loadingPending ? (
                <div className="empty-state">
                  <History size={30} />
                  <p>Loading pending feedback...</p>
                </div>
              ) : moderationError && pendingFeedback.length === 0 ? (
                <div className="empty-state">
                  <p>Could not load pending feedback.</p>
                  <button
                    type="button"
                    className="primary-button"
                    onClick={() =>
                      setModerationReload((previous) => previous + 1)
                    }
                  >
                    Try again
                  </button>
                </div>
              ) : pendingFeedback.length === 0 ? (
                <div className="empty-state">
                  <ShieldCheck size={30} />
                  <h3>No pending feedback</h3>
                  <p>All submitted feedback has been reviewed.</p>
                </div>
              ) : (
                <div className="feedback-list">
                  {pendingFeedback.map((item) => (
                    <article className="feedback-card" key={item.id}>
                      <div className="feedback-avatar">
                        {(item.category ?? "F").charAt(0)}
                      </div>

                      <div className="feedback-body">
                        <div className="feedback-meta">
                          <span className="category-pill">
                            {item.category ?? "Feedback"}
                          </span>
                          <span className="feedback-date">
                            {item.createdAt
                              ? new Date(item.createdAt).toLocaleDateString()
                              : "Date unavailable"}
                          </span>
                        </div>

                        <div className="stars">
                          {Array.from(
                            {
                              length: Math.min(
                                5,
                                Math.max(0, Number(item.rating) || 0)
                              ),
                            },
                            (_, index) => (
                              <Star
                                key={index}
                                size={15}
                                fill="currentColor"
                              />
                            )
                          )}
                        </div>

                        <p>{item.text}</p>

                        <label htmlFor={`reason-${item.id}`}>
                          Moderation reason
                        </label>
                        <textarea
                          id={`reason-${item.id}`}
                          rows="2"
                          maxLength="500"
                          placeholder="Enter the reason for your decision..."
                          value={moderationReasons[item.id] ?? ""}
                          onChange={(event) =>
                            setModerationReasons((previous) => ({
                              ...previous,
                              [item.id]: event.target.value,
                            }))
                          }
                          disabled={moderatingId !== null}
                        />

                        <div className="character-count">
                          {(moderationReasons[item.id] ?? "").length}/500
                          characters
                        </div>

                        <div className="moderation-actions">
                          <button
                            type="button"
                            className="primary-button"
                            disabled={moderatingId !== null}
                            onClick={() =>
                              moderateFeedback(item.id, "APPROVED")
                            }
                          >
                            <CheckCircle size={16} />
                            {moderatingId === item.id
                              ? "Processing..."
                              : "Approve"}
                          </button>

                          <button
                            type="button"
                            className="primary-button"
                            disabled={moderatingId !== null}
                            onClick={() =>
                              moderateFeedback(item.id, "REJECTED")
                            }
                          >
                            <XCircle size={16} /> Reject
                          </button>

                          <button
                            type="button"
                            className="primary-button"
                            disabled={moderatingId !== null}
                            onClick={() =>
                              moderateFeedback(item.id, "HIDDEN")
                            }
                          >
                            <EyeOff size={16} /> Hide
                          </button>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          ) : activeTab === "Abuse Reports" && isModerator ? (
            <section className="panel">
              <div className="section-heading">
                <div>
                  <h2>Abuse reports</h2>
                  <p className="subtitle">
                    Review reports submitted about customer feedback.
                  </p>
                </div>

                <button
                  type="button"
                  className="text-button"
                  disabled={reportsLoading || resolvingReportId !== null}
                  onClick={() =>
                    setReportsReload((previous) => previous + 1)
                  }
                >
                  <RefreshCw size={16} /> Refresh
                </button>
              </div>

              {reportsNotice && (
                <p className="notice" role="status">{reportsNotice}</p>
              )}

              {reportsError && (
                <p className="login-error" role="alert">{reportsError}</p>
              )}

              {reportsLoading ? (
                <div className="empty-state">
                  <Flag size={30} />
                  <p>Loading abuse reports...</p>
                </div>
              ) : reportsError && abuseReports.length === 0 ? (
                <div className="empty-state">
                  <p>Could not load abuse reports.</p>
                  <button
                    type="button"
                    className="primary-button"
                    onClick={() =>
                      setReportsReload((previous) => previous + 1)
                    }
                  >
                    Try again
                  </button>
                </div>
              ) : abuseReports.length === 0 ? (
                <div className="empty-state">
                  <Flag size={30} />
                  <h3>No unresolved abuse reports</h3>
                  <p>New reports will appear here for moderator review.</p>
                </div>
              ) : (
                <div className="feedback-list">
                  {abuseReports.map((report) => (
                    <article className="feedback-card" key={report.id}>
                      <div className="feedback-avatar">
                        <Flag size={18} />
                      </div>

                      <div className="feedback-body">
                        <div className="feedback-meta">
                          <span className="category-pill">
                            Report #{report.id}
                          </span>
                          <span className="feedback-date">
                            {report.createdAt
                              ? new Date(report.createdAt).toLocaleString()
                              : "Date unavailable"}
                          </span>
                        </div>

                        <h3>
                          Feedback #{report.feedbackId} — {report.category ?? "Feedback"}
                        </h3>
                        <p>
                          <strong>Feedback status:</strong>{" "}
                          {report.feedbackStatus ?? "Unknown"}
                        </p>
                        <p>
                          <strong>Reported by:</strong>{" "}
                          {report.reporterEmail ?? "Unknown reporter"}
                        </p>
                        <p>
                          <strong>Report reason:</strong>{" "}
                          {report.reason}
                        </p>
                        <p>
                          <strong>Reported feedback:</strong>{" "}
                          {report.feedbackText}
                        </p>

                        <div className="moderation-actions">
                          <button
                            type="button"
                            className="primary-button"
                            disabled={resolvingReportId !== null}
                            onClick={() => resolveAbuseReport(report.id)}
                          >
                            <CheckCircle size={16} />
                            {resolvingReportId === report.id
                              ? "Resolving..."
                              : "Mark as Resolved"}
                          </button>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          ) : (
            <>
              <div className="section-heading">
                <div>
                  <h2>
                    {activeTab === "Browse Feedback"
                      ? "Customer feedback"
                      : "Recent feedback"}
                  </h2>
                  <p>Discover what customers are saying.</p>
                </div>

                <button
                  type="button"
                  className="text-button"
                  onClick={() => setActiveTab("Browse Feedback")}
                >
                  View all <ArrowUpRight size={16} />
                </button>
              </div>

              <div className="search-bar">
                <Search size={19} />
                <input
                  placeholder="Search feedback text..."
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </div>

              <div className="public-filter-row">
                <label>
                  Category
                  <select
                    value={publicCategory}
                    onChange={(event) => setPublicCategory(event.target.value)}
                  >
                    <option value="All">All categories</option>
                    <option value="Product">Product</option>
                    <option value="Service">Service</option>
                    <option value="Support">Support</option>
                    <option value="Other">Other</option>
                  </select>
                </label>

                <label>
                  Rating
                  <select
                    value={publicRating}
                    onChange={(event) => setPublicRating(event.target.value)}
                  >
                    <option value="All">All ratings</option>
                    <option value="5">5 stars</option>
                    <option value="4">4 stars</option>
                    <option value="3">3 stars</option>
                    <option value="2">2 stars</option>
                    <option value="1">1 star</option>
                  </select>
                </label>

                <button
                  type="button"
                  className="text-button"
                  onClick={() => {
                    setSearch("");
                    setPublicCategory("All");
                    setPublicRating("All");
                  }}
                >
                  Clear filters
                </button>
              </div>

              {loadingPublic ? (
                <div className="empty-state">
                  <p>Loading public feedback...</p>
                </div>
              ) : publicError ? (
                <div className="empty-state">
                  <p>{publicError}</p>
                  <button
                    type="button"
                    className="primary-button"
                    onClick={() =>
                      setPublicReload((previous) => previous + 1)
                    }
                  >
                    Try again
                  </button>
                </div>
              ) : (
                <div className="feedback-list">
                  {publicFeedback.map((item) => (
                    <article className="feedback-card" key={item.id}>
                      <div className="feedback-avatar">
                        {(item.category ?? "F").charAt(0)}
                      </div>

                      <div className="feedback-body">
                        <div className="feedback-meta">
                          <span className="category-pill">
                            {item.category ?? "Feedback"}
                          </span>
                          <span className="feedback-date">
                            {item.createdAt
                              ? new Date(item.createdAt).toLocaleDateString()
                              : "Date unavailable"}
                          </span>
                        </div>

                        <div className="stars">
                          {Array.from(
                            {
                              length: Math.min(
                                5,
                                Math.max(0, Number(item.rating) || 0)
                              ),
                            },
                            (_, index) => (
                              <Star
                                key={index}
                                size={15}
                                fill="currentColor"
                              />
                            )
                          )}
                        </div>

                       <p>{item.text}</p>

{user && !isModerator && (
  <button
    type="button"
    className="text-button"
    onClick={() => {
      setReportingFeedbackId(item.id);
      setReportReason("");
      setReportError("");
      setReportNotice("");
    }}
  >
    <Flag size={15} />
    Report
  </button>
)}
                      </div>
                    </article>
                  ))}

                  {publicFeedback.length === 0 && (
                    <div className="empty-state">
                      <Search size={26} />
                      <p>No approved public feedback found.</p>
                    </div>
                  )}
                </div>
              )}
              {reportNotice && (
  <p className="success-message" role="status">
    {reportNotice}
  </p>
)}

{reportingFeedbackId !== null && (
  <div className="modal-backdrop">
    <section
      className="panel"
      role="dialog"
      aria-modal="true"
      aria-labelledby="report-dialog-title"
    >
      <div className="section-heading">
        <h2 id="report-dialog-title">
          Report Feedback #{reportingFeedbackId}
        </h2>

        <button
          type="button"
          className="text-button"
          onClick={() => {
            setReportingFeedbackId(null);
            setReportReason("");
            setReportError("");
          }}
        >
          Cancel
        </button>
      </div>

      <form onSubmit={submitAbuseReport}>
        <label htmlFor="report-reason">
          Reason for reporting
        </label>

        <textarea
          id="report-reason"
          rows={4}
          maxLength={500}
          required
          value={reportReason}
          onChange={(event) => setReportReason(event.target.value)}
          placeholder="Explain why this feedback should be reviewed..."
        />

        <p>{reportReason.length}/500 characters</p>

        {reportError && (
          <p role="alert" className="login-error">
            {reportError}
          </p>
        )}

        <button
          type="submit"
          className="primary-button"
          disabled={reportSubmitting}
        >
          {reportSubmitting ? "Submitting..." : "Submit Report"}
        </button>
      </form>
    </section>
  </div>
)}
            </>
          )}
        </section>
      </main>
    </div>
  );
}

export default App;
