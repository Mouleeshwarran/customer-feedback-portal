import { useEffect, useState, useRef } from "react";
import {
  MessageSquareText,
  LayoutDashboard,
  History,
  ShieldCheck,
  Search,
  Star,
  Send,
  ArrowUpRight,
  CheckCircle,
  XCircle,
  EyeOff,
  RefreshCw,
  Flag,
  Users,
  Tags,
  FileText,
  Paperclip,
  Download,
  Plus,
  Shield,
  UserCheck,
  AlertTriangle,
} from "lucide-react";
import "./App.css";
import Login from "./Login";
import { apiRequest } from "./api";

export default function App() {
  const [user, setUser] = useState(null);
  const [activeTab, setActiveTab] = useState("Overview");

  // Global Categories list fetched from backend
  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);

  // Submit Feedback state
  const [selectedCategory, setSelectedCategory] = useState("");
  const [rating, setRating] = useState(5);
  const [feedbackText, setFeedbackText] = useState("");
  const [isPrivate, setIsPrivate] = useState(false);
  const [attachmentFile, setAttachmentFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [submitNotice, setSubmitNotice] = useState("");
  const [submitError, setSubmitError] = useState("");
  const fileInputRef = useRef(null);

  // Dashboard stats
  const [dashboardStats, setDashboardStats] = useState({
    totalFeedback: 0,
    averageRating: 0,
    pendingFeedback: 0,
  });
  const [loadingStats, setLoadingStats] = useState(false);
  const [statsReload, setStatsReload] = useState(0);

  // Admin Dashboard stats
  const [adminStats, setAdminStats] = useState({
    totalUsers: 0,
    totalFeedback: 0,
    averageRating: 0,
    pendingModeration: 0,
    unresolvedAbuseReports: 0,
    totalCategories: 0,
  });
  const [loadingAdminStats, setLoadingAdminStats] = useState(false);

  // Public Feedback browse
  const [publicFeedback, setPublicFeedback] = useState([]);
  const [loadingPublic, setLoadingPublic] = useState(false);
  const [publicError, setPublicError] = useState("");
  const [search, setSearch] = useState("");
  const [filterCategory, setFilterCategory] = useState("All");
  const [filterRating, setFilterRating] = useState("All");
  const [publicPage, setPublicPage] = useState(0);
  const [publicTotalPages, setPublicTotalPages] = useState(1);
  const [publicReload, setPublicReload] = useState(0);

  // Customer: My Submissions
  const [mySubmissions, setMySubmissions] = useState([]);
  const [loadingSubmissions, setLoadingSubmissions] = useState(false);
  const [submissionsError, setSubmissionsError] = useState("");
  const [myPage, setMyPage] = useState(0);
  const [myTotalPages, setMyTotalPages] = useState(1);

  // Moderator: Pending queue
  const [pendingFeedback, setPendingFeedback] = useState([]);
  const [loadingPending, setLoadingPending] = useState(false);
  const [moderationError, setModerationError] = useState("");
  const [moderationNotice, setModerationNotice] = useState("");
  const [moderationReasons, setModerationReasons] = useState({});
  const [moderatingId, setModeratingId] = useState(null);
  const [moderationReload, setModerationReload] = useState(0);

  // Moderator: Abuse Reports
  const [abuseReports, setAbuseReports] = useState([]);
  const [unresolvedAbuseCount, setUnresolvedAbuseCount] = useState(0);
  const [reportsLoading, setReportsLoading] = useState(false);
  const [reportsError, setReportsError] = useState("");
  const [reportsNotice, setReportsNotice] = useState("");
  const [resolvingReportId, setResolvingReportId] = useState(null);
  const [reportsReload, setReportsReload] = useState(0);

  // Abuse Flag Modal (for customers reporting feedback)
  const [reportingFeedbackId, setReportingFeedbackId] = useState(null);
  const [reportReason, setReportReason] = useState("");
  const [reportSubmitting, setReportSubmitting] = useState(false);
  const [reportError, setReportError] = useState("");
  const [reportNotice, setReportNotice] = useState("");

  // Admin: User Management
  const [usersList, setUsersList] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [usersError, setUsersError] = useState("");
  const [usersNotice, setUsersNotice] = useState("");
  const [usersReload, setUsersReload] = useState(0);

  // Admin: Category Management
  const [adminCategories, setAdminCategories] = useState([]);
  const [loadingAdminCats, setLoadingAdminCats] = useState(false);
  const [newCatName, setNewCatName] = useState("");
  const [newCatDesc, setNewCatDesc] = useState("");
  const [newCatActive, setNewCatActive] = useState(true);
  const [catNotice, setCatNotice] = useState("");
  const [catError, setCatError] = useState("");
  const [creatingCat, setCreatingCat] = useState(false);
  const [showCreateCatModal, setShowCreateCatModal] = useState(false);
  const [categoriesReload, setCategoriesReload] = useState(0);

  // Admin: Audit Logs
  const [auditLogs, setAuditLogs] = useState([]);
  const [loadingAudit, setLoadingAudit] = useState(false);
  const [auditError, setAuditError] = useState("");

  const isModerator = user?.role === "MODERATOR" || user?.role === "ADMINISTRATOR";
  const isAdmin = user?.role === "ADMINISTRATOR";

  // Navigation Items
  const navItems = [
    { name: "Overview", icon: LayoutDashboard },
    { name: "Browse Feedback", icon: MessageSquareText },
    { name: "My Submissions", icon: History, role: "CUSTOMER" },
    ...(isModerator
      ? [
          { name: "Moderation", icon: ShieldCheck, tag: "Mod", tagClass: "mod" },
          { name: "Abuse Reports", icon: Flag, tag: "Mod", tagClass: "mod" },
        ]
      : []),
    ...(isAdmin
      ? [
          { name: "Users", icon: Users, tag: "Admin", tagClass: "admin" },
          { name: "Categories", icon: Tags, tag: "Admin", tagClass: "admin" },
          { name: "Audit Trail", icon: FileText, tag: "Admin", tagClass: "admin" },
        ]
      : []),
  ];

  // 1. Fetch active categories for dropdowns
  useEffect(() => {
    async function loadCategories() {
      setLoadingCategories(true);
      try {
        const list = await apiRequest("/api/categories");
        if (Array.isArray(list) && list.length > 0) {
          setCategories(list);
          setSelectedCategory(list[0].id.toString());
        }
      } catch (err) {
        console.error("Failed to load categories:", err);
      } finally {
        setLoadingCategories(false);
      }
    }
    loadCategories();
  }, [categoriesReload]);

  // 2. Fetch Dashboard Statistics
  useEffect(() => {
    if (!user) return;
    async function loadStats() {
      setLoadingStats(true);
      try {
        const resp = await apiRequest("/api/feedback/stats", {
          username: user.email,
          password: user.password,
        });
        setDashboardStats({
          totalFeedback: Number(resp?.totalFeedback ?? 0),
          averageRating: Number(resp?.averageRating ?? 0),
          pendingFeedback: Number(resp?.pendingFeedback ?? 0),
        });
      } catch (err) {
        console.error("Dashboard stats error:", err);
      } finally {
        setLoadingStats(false);
      }
    }
    loadStats();
  }, [user, statsReload]);

  // 3. Fetch Admin Stats (if Admin)
  useEffect(() => {
    if (!user || !isAdmin) return;
    async function loadAdminStats() {
      setLoadingAdminStats(true);
      try {
        const resp = await apiRequest("/api/admin/stats", {
          username: user.email,
          password: user.password,
        });
        setAdminStats({
          totalUsers: Number(resp?.totalUsers ?? 0),
          totalFeedback: Number(resp?.totalFeedback ?? 0),
          averageRating: Number(resp?.averageRating ?? 0),
          pendingModeration: Number(resp?.pendingModeration ?? 0),
          unresolvedAbuseReports: Number(resp?.unresolvedAbuseReports ?? 0),
          totalCategories: Number(resp?.totalCategories ?? 0),
        });
      } catch (err) {
        console.error("Admin stats error:", err);
      } finally {
        setLoadingAdminStats(false);
      }
    }
    loadAdminStats();
  }, [user, isAdmin, statsReload]);

  // 4. Fetch Unresolved Abuse Count (for Moderator badge)
  useEffect(() => {
    if (!user || !isModerator) return;
    async function loadAbuseCount() {
      try {
        const resp = await apiRequest("/api/admin/reports?page=0&size=1", {
          username: user.email,
          password: user.password,
        });
        setUnresolvedAbuseCount(Number(resp?.totalElements ?? 0));
      } catch (err) {
        console.error("Abuse count error:", err);
      }
    }
    loadAbuseCount();
  }, [user, isModerator, reportsReload]);

  // 5. Fetch Public Feedback
  useEffect(() => {
    if (activeTab !== "Browse Feedback" && activeTab !== "Overview") return;
    async function loadPublic() {
      setLoadingPublic(true);
      setPublicError("");
      try {
        const params = new URLSearchParams({
          page: publicPage.toString(),
          size: "15",
        });
        if (search.trim()) params.set("search", search.trim());
        if (filterCategory !== "All") params.set("categoryId", filterCategory);
        if (filterRating !== "All") params.set("rating", filterRating);

        const resp = await apiRequest(`/api/feedback/public?${params.toString()}`);
        setPublicFeedback(resp?.content ?? []);
        setPublicTotalPages(resp?.totalPages ?? 1);
      } catch (err) {
        setPublicError(err.message || "Unable to load public feedback.");
      } finally {
        setLoadingPublic(false);
      }
    }
    loadPublic();
  }, [activeTab, search, filterCategory, filterRating, publicPage, publicReload]);

  // 6. Fetch My Submissions (Customer)
  useEffect(() => {
    if (activeTab !== "My Submissions" || !user) return;
    async function loadMy() {
      setLoadingSubmissions(true);
      setSubmissionsError("");
      try {
        const resp = await apiRequest(`/api/feedback/my?page=${myPage}&size=10`, {
          username: user.email,
          password: user.password,
        });
        setMySubmissions(resp?.content ?? []);
        setMyTotalPages(resp?.totalPages ?? 1);
      } catch (err) {
        setSubmissionsError(err.message || "Unable to load your submissions.");
      } finally {
        setLoadingSubmissions(false);
      }
    }
    loadMy();
  }, [activeTab, user, myPage, statsReload]);

  // 7. Fetch Pending Feedback (Moderator)
  useEffect(() => {
    if (activeTab !== "Moderation" || !user || !isModerator) return;
    async function loadPending() {
      setLoadingPending(true);
      setModerationError("");
      try {
        const resp = await apiRequest("/api/admin/feedback?page=0&size=50", {
          username: user.email,
          password: user.password,
        });
        setPendingFeedback(resp?.content ?? []);
      } catch (err) {
        setModerationError(err.message || "Unable to load pending moderation queue.");
      } finally {
        setLoadingPending(false);
      }
    }
    loadPending();
  }, [activeTab, user, isModerator, moderationReload]);

  // 8. Fetch Abuse Reports (Moderator)
  useEffect(() => {
    if (activeTab !== "Abuse Reports" || !user || !isModerator) return;
    async function loadReports() {
      setReportsLoading(true);
      setReportsError("");
      try {
        const resp = await apiRequest("/api/admin/reports?page=0&size=50", {
          username: user.email,
          password: user.password,
        });
        setAbuseReports(resp?.content ?? []);
      } catch (err) {
        setReportsError(err.message || "Unable to load abuse reports.");
      } finally {
        setReportsLoading(false);
      }
    }
    loadReports();
  }, [activeTab, user, isModerator, reportsReload]);

  // 9. Fetch Users List (Admin)
  useEffect(() => {
    if (activeTab !== "Users" || !user || !isAdmin) return;
    async function loadUsers() {
      setLoadingUsers(true);
      setUsersError("");
      try {
        const resp = await apiRequest("/api/admin/users?page=0&size=50", {
          username: user.email,
          password: user.password,
        });
        setUsersList(resp?.content ?? []);
      } catch (err) {
        setUsersError(err.message || "Failed to load users list.");
      } finally {
        setLoadingUsers(false);
      }
    }
    loadUsers();
  }, [activeTab, user, isAdmin, usersReload]);

  // 10. Fetch Categories List (Admin)
  useEffect(() => {
    if (activeTab !== "Categories" || !user || !isAdmin) return;
    async function loadAdminCats() {
      setLoadingAdminCats(true);
      try {
        const resp = await apiRequest("/api/admin/categories?page=0&size=50", {
          username: user.email,
          password: user.password,
        });
        setAdminCategories(resp?.content ?? []);
      } catch (err) {
        console.error("Failed to load admin categories:", err);
      } finally {
        setLoadingAdminCats(false);
      }
    }
    loadAdminCats();
  }, [activeTab, user, isAdmin, categoriesReload]);

  // 11. Fetch Audit Logs (Admin)
  useEffect(() => {
    if (activeTab !== "Audit Trail" || !user || !isAdmin) return;
    async function loadAudit() {
      setLoadingAudit(true);
      setAuditError("");
      try {
        const resp = await apiRequest("/api/admin/audit?page=0&size=50", {
          username: user.email,
          password: user.password,
        });
        setAuditLogs(resp?.content ?? []);
      } catch (err) {
        setAuditError(err.message || "Failed to load audit logs.");
      } finally {
        setLoadingAudit(false);
      }
    }
    loadAudit();
  }, [activeTab, user, isAdmin]);

  // Submit Feedback Handler with Attachment Upload
  async function submitFeedback(e) {
    e.preventDefault();
    if (!user || submitting) return;
    const trimmed = feedbackText.trim();
    if (!trimmed) {
      setSubmitError("Please enter your feedback comments.");
      return;
    }
    if (!selectedCategory) {
      setSubmitError("Please select a feedback category.");
      return;
    }

    setSubmitting(true);
    setSubmitNotice("");
    setSubmitError("");

    try {
      const result = await apiRequest("/api/feedback", {
        method: "POST",
        username: user.email,
        password: user.password,
        body: JSON.stringify({
          categoryId: Number(selectedCategory),
          rating: Number(rating),
          text: trimmed,
          privateFeedback: isPrivate,
        }),
      });

      // If an attachment file is selected, upload it
      if (attachmentFile && result?.id) {
        const formData = new FormData();
        formData.append("file", attachmentFile);
        await apiRequest(`/api/feedback/${result.id}/attachments`, {
          method: "POST",
          username: user.email,
          password: user.password,
          body: formData,
        });
      }

      setSubmitNotice("Feedback submitted successfully! It is now pending moderation.");
      setFeedbackText("");
      setRating(5);
      setIsPrivate(false);
      setAttachmentFile(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
      setStatsReload((prev) => prev + 1);
    } catch (err) {
      setSubmitError(err.message || "Failed to submit feedback.");
    } finally {
      setSubmitting(false);
    }
  }

  // Moderator Decision Handler
  async function moderateFeedback(feedbackId, decision) {
    if (!user || !isModerator || moderatingId !== null) return;
    const reason = (moderationReasons[feedbackId] ?? "").trim();
    if (!reason) {
      setModerationNotice("Please provide a reason before recording your moderation decision.");
      return;
    }

    setModeratingId(feedbackId);
    setModerationError("");
    setModerationNotice("");

    try {
      await apiRequest(`/api/admin/feedback/${feedbackId}/moderate`, {
        method: "PATCH",
        username: user.email,
        password: user.password,
        body: JSON.stringify({
          decision,
          reason,
        }),
      });

      setModerationNotice(`Feedback #${feedbackId} was ${decision.toLowerCase()}.`);
      setModerationReasons((prev) => {
        const next = { ...prev };
        delete next[feedbackId];
        return next;
      });
      setModerationReload((prev) => prev + 1);
      setStatsReload((prev) => prev + 1);
    } catch (err) {
      setModerationError(err.message || "Failed to moderate feedback.");
    } finally {
      setModeratingId(null);
    }
  }

  // Resolve Abuse Report Handler
  async function resolveReport(reportId) {
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
      setReportsNotice(`Abuse report #${reportId} has been marked as resolved.`);
      setReportsReload((prev) => prev + 1);
      setStatsReload((prev) => prev + 1);
    } catch (err) {
      setReportsError(err.message || "Failed to resolve abuse report.");
    } finally {
      setResolvingReportId(null);
    }
  }

  // Customer Abuse Flag Submit Handler
  async function submitAbuseFlag(e) {
    e.preventDefault();
    if (!user || reportingFeedbackId === null || reportSubmitting) return;
    const reason = reportReason.trim();
    if (!reason) {
      setReportError("Please enter a reason for reporting this feedback.");
      return;
    }

    setReportSubmitting(true);
    setReportError("");
    setReportNotice("");

    try {
      await apiRequest(`/api/feedback/${reportingFeedbackId}/flags`, {
        method: "POST",
        username: user.email,
        password: user.password,
        body: JSON.stringify({ reason }),
      });
      setReportNotice("Report received. Our moderation team will review it.");
      setReportingFeedbackId(null);
      setReportReason("");
    } catch (err) {
      setReportError(err.message || "Failed to submit abuse report.");
    } finally {
      setReportSubmitting(false);
    }
  }

  // Admin User Activation Toggle
  async function toggleUserActive(userId, currentActive) {
    try {
      const endpoint = currentActive
        ? `/api/admin/users/${userId}/deactivate`
        : `/api/admin/users/${userId}/activate`;
      await apiRequest(endpoint, {
        method: "PATCH",
        username: user.email,
        password: user.password,
      });
      setUsersNotice(`User #${userId} status updated.`);
      setUsersReload((prev) => prev + 1);
    } catch (err) {
      setUsersError(err.message || "Failed to update user status.");
    }
  }

  // Admin Create Category Handler
  async function createCategory(e) {
    e.preventDefault();
    if (!newCatName.trim()) return;
    setCreatingCat(true);
    setCatError("");
    setCatNotice("");
    try {
      await apiRequest("/api/admin/categories", {
        method: "POST",
        username: user.email,
        password: user.password,
        body: JSON.stringify({
          name: newCatName.trim(),
          description: newCatDesc.trim(),
          active: newCatActive,
        }),
      });
      setCatNotice(`Category "${newCatName.trim()}" created successfully.`);
      setNewCatName("");
      setNewCatDesc("");
      setShowCreateCatModal(false);
      setCategoriesReload((prev) => prev + 1);
    } catch (err) {
      setCatError(err.message || "Failed to create category.");
    } finally {
      setCreatingCat(false);
    }
  }

  if (!user) {
    return <Login onLogin={(credentials) => setUser(credentials)} />;
  }

  return (
    <div className="app-shell">
      {/* ─── Sidebar ────────────────────────────────────────────────────────── */}
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">
            <MessageSquareText size={22} />
          </div>
          <div>
            <h2>FeedbackHub</h2>
            <span>Customer Portal</span>
          </div>
        </div>

        <div className="nav-label">WORKSPACE</div>

        <nav>
          {navItems.map(({ name, icon: Icon, tag, tagClass }) => (
            <button
              key={name}
              type="button"
              className={`nav-item ${activeTab === name ? "active" : ""}`}
              onClick={() => {
                setActiveTab(name);
                setSubmitNotice("");
                setSubmitError("");
                setModerationNotice("");
              }}
            >
              <Icon size={18} />
              <span>{name}</span>
              {tag && <span className={`nav-tag ${tagClass}`}>{tag}</span>}
            </button>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="user-profile">
            <div className="avatar">
              {user.email.substring(0, 2).toUpperCase()}
            </div>
            <div className="user-info">
              <strong>{user.email}</strong>
              <span className={`role-badge ${user.role.toLowerCase()}`}>
                {user.role}
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

      {/* ─── Main Content ────────────────────────────────────────────────────── */}
      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">
            Workspace <span>/</span> <strong>{activeTab}</strong>
          </div>
          <div className="topbar-right">
            <span className="status-dot" />
            <span>Connected &bull; {user.role} Session</span>
            <div className="avatar small">
              {user.email.substring(0, 2).toUpperCase()}
            </div>
          </div>
        </header>

        <section className="page-content">
          {/* Welcome Row */}
          <div className="welcome-row">
            <div>
              <div className="eyebrow">
                {isAdmin
                  ? "SYSTEM ADMINISTRATION"
                  : isModerator
                  ? "MODERATION DESK"
                  : "CUSTOMER EXPERIENCE"}
              </div>
              <h1>
                {activeTab === "Overview"
                  ? "Your voice shapes our platform."
                  : activeTab}
              </h1>
              <p className="subtitle">
                {isAdmin
                  ? "Monitor security metrics, manage users, categories, and system audit events."
                  : isModerator
                  ? "Review feedback, inspect reported content, and uphold portal standards."
                  : "Submit verified feedback, track your review status, and explore community insights."}
              </p>
            </div>

            {user.role === "CUSTOMER" && (
              <button
                type="button"
                className="primary-button"
                onClick={() => {
                  setActiveTab("Submit Feedback");
                  setSubmitNotice("");
                  setSubmitError("");
                }}
              >
                <Send size={16} /> Submit Feedback
              </button>
            )}
          </div>

          {/* ─── Role-Aware Dashboard KPI Cards ────────────────────────────── */}
          {activeTab === "Overview" && (
            <>
              {isAdmin ? (
                <div className="stats-grid">
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>Total Users</span>
                      <div className="stat-icon purple"><Users size={18} /></div>
                    </div>
                    <h2>{loadingAdminStats ? "…" : adminStats.totalUsers}</h2>
                    <span className="stat-note">Registered local accounts</span>
                  </div>
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>Portal Feedback</span>
                      <div className="stat-icon blue"><MessageSquareText size={18} /></div>
                    </div>
                    <h2>{loadingAdminStats ? "…" : adminStats.totalFeedback}</h2>
                    <span className="stat-note">Total submitted records</span>
                  </div>
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>Average Rating</span>
                      <div className="stat-icon yellow"><Star size={18} /></div>
                    </div>
                    <h2>
                      {loadingAdminStats ? "…" : adminStats.averageRating.toFixed(1)}{" "}
                      <small>/ 5.0</small>
                    </h2>
                    <span className="stat-note">Global satisfaction score</span>
                  </div>
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>Pending Review</span>
                      <div className="stat-icon mint"><ShieldCheck size={18} /></div>
                    </div>
                    <h2>{loadingAdminStats ? "…" : adminStats.pendingModeration}</h2>
                    <span className="stat-note">Awaiting moderation</span>
                  </div>
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>Open Abuse Reports</span>
                      <div className="stat-icon blush"><Flag size={18} /></div>
                    </div>
                    <h2>{loadingAdminStats ? "…" : adminStats.unresolvedAbuseReports}</h2>
                    <span className="stat-note">Reports pending resolution</span>
                  </div>
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>Feedback Categories</span>
                      <div className="stat-icon purple"><Tags size={18} /></div>
                    </div>
                    <h2>{loadingAdminStats ? "…" : adminStats.totalCategories}</h2>
                    <span className="stat-note">Configured categories</span>
                  </div>
                </div>
              ) : (
                <div className="stats-grid">
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>{isModerator ? "Total Submissions" : "My Submissions"}</span>
                      <div className="stat-icon purple"><MessageSquareText size={18} /></div>
                    </div>
                    <h2>{loadingStats ? "…" : dashboardStats.totalFeedback}</h2>
                    <span className="stat-note">
                      {isModerator ? "Portal-wide count" : "Submitted by you"}
                    </span>
                  </div>
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>Average Rating</span>
                      <div className="stat-icon yellow"><Star size={18} /></div>
                    </div>
                    <h2>
                      {loadingStats ? "…" : dashboardStats.averageRating.toFixed(1)}{" "}
                      <small>/ 5</small>
                    </h2>
                    <span className="stat-note">
                      {isModerator ? "Global average rating" : "Your average rating"}
                    </span>
                  </div>
                  <div className="stat-card">
                    <div className="stat-top">
                      <span>Awaiting Review</span>
                      <div className="stat-icon mint"><History size={18} /></div>
                    </div>
                    <h2>{loadingStats ? "…" : dashboardStats.pendingFeedback}</h2>
                    <span className="stat-note">Pending moderation decisions</span>
                  </div>
                  {isModerator && (
                    <div className="stat-card">
                      <div className="stat-top">
                        <span>Unresolved Reports</span>
                        <div className="stat-icon blush"><Flag size={18} /></div>
                      </div>
                      <h2>{unresolvedAbuseCount}</h2>
                      <span className="stat-note">Reports awaiting resolution</span>
                    </div>
                  )}
                </div>
              )}
            </>
          )}

          {/* ─── TAB 1: SUBMIT FEEDBACK ───────────────────────────────────── */}
          {activeTab === "Submit Feedback" && (
            <section className="panel submit-panel">
              <div className="panel-heading">
                <h2>Share your experience</h2>
                <p>Your honest review helps us build a better product.</p>
              </div>

              {submitNotice && <p className="notice">{submitNotice}</p>}
              {submitError && <p className="login-error">{submitError}</p>}

              <form onSubmit={submitFeedback}>
                <label htmlFor="feedback-cat">Feedback category</label>
                <select
                  id="feedback-cat"
                  value={selectedCategory}
                  onChange={(e) => setSelectedCategory(e.target.value)}
                  disabled={loadingCategories}
                >
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name} {c.description ? `— ${c.description}` : ""}
                    </option>
                  ))}
                </select>

                <label>Rating</label>
                <div className="rating-picker">
                  {[1, 2, 3, 4, 5].map((val) => (
                    <button
                      type="button"
                      key={val}
                      className={`star ${val <= rating ? "selected" : ""}`}
                      onClick={() => setRating(val)}
                      aria-label={`${val} stars`}
                    >
                      <Star size={24} fill="currentColor" />
                    </button>
                  ))}
                  <span>{rating} of 5 Stars</span>
                </div>

                <label htmlFor="feedback-text">Feedback Comments</label>
                <textarea
                  id="feedback-text"
                  rows={5}
                  maxLength={3000}
                  placeholder="Describe your experience in detail..."
                  value={feedbackText}
                  onChange={(e) => setFeedbackText(e.target.value)}
                  required
                />
                <div className="character-count">
                  {feedbackText.length}/3000 characters
                </div>

                <label>Evidence Attachment (Optional)</label>
                <div
                  className="file-upload-box"
                  onClick={() => fileInputRef.current?.click()}
                >
                  <Paperclip size={20} style={{ color: "#7a70d6" }} />
                  <p style={{ margin: "6px 0 2px", fontSize: "12.5px", fontWeight: 600 }}>
                    {attachmentFile ? attachmentFile.name : "Click to attach screenshot or document"}
                  </p>
                  <span style={{ fontSize: "11px", color: "#8a94ac" }}>
                    Allowed: JPG, PNG, GIF, PDF, TXT, DOCX, XLSX (Max 10 MB)
                  </span>
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept=".jpg,.jpeg,.png,.gif,.pdf,.txt,.docx,.xlsx"
                    onChange={(e) => {
                      if (e.target.files?.[0]) {
                        const file = e.target.files[0];
                        if (file.size > 10 * 1024 * 1024) {
                          setSubmitError("File size exceeds 10 MB limit.");
                          return;
                        }
                        setAttachmentFile(file);
                        setSubmitError("");
                      }
                    }}
                  />
                </div>

                {attachmentFile && (
                  <div className="file-preview">
                    <span>
                      {attachmentFile.name} ({(attachmentFile.size / 1024).toFixed(1)} KB)
                    </span>
                    <button
                      type="button"
                      className="text-button"
                      onClick={() => {
                        setAttachmentFile(null);
                        if (fileInputRef.current) fileInputRef.current.value = "";
                      }}
                    >
                      Remove
                    </button>
                  </div>
                )}

                <label className="privacy-option">
                  <input
                    type="checkbox"
                    checked={isPrivate}
                    onChange={(e) => setIsPrivate(e.target.checked)}
                  />
                  Keep this submission private (Visible only to portal moderators)
                </label>

                <button
                  type="submit"
                  className="primary-button"
                  disabled={submitting}
                >
                  <Send size={16} />
                  {submitting ? "Submitting..." : "Submit Feedback"}
                </button>
              </form>
            </section>
          )}

          {/* ─── TAB 2: MY SUBMISSIONS (Customer) ─────────────────────────── */}
          {activeTab === "My Submissions" && (
            <section className="panel">
              <div className="section-heading">
                <div>
                  <h2>My Submissions</h2>
                  <p>Track your submitted feedback and review progress.</p>
                </div>
              </div>

              {loadingSubmissions ? (
                <div className="empty-state">
                  <History size={32} />
                  <p>Loading your submissions...</p>
                </div>
              ) : submissionsError ? (
                <div className="empty-state">
                  <p>{submissionsError}</p>
                </div>
              ) : mySubmissions.length === 0 ? (
                <div className="empty-state">
                  <History size={32} />
                  <h3>No submissions yet</h3>
                  <p>Share your first review by clicking "Submit Feedback".</p>
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
                          <span className="category-pill">{item.category}</span>
                          <span className={`status-badge ${item.status.toLowerCase()}`}>
                            {item.status}
                          </span>
                        </div>

                        <div className="stars">
                          {Array.from({ length: item.rating }).map((_, i) => (
                            <Star key={i} size={15} fill="currentColor" />
                          ))}
                        </div>

                        <p>{item.text}</p>

                        <div className="feedback-card-footer">
                          <span className="feedback-date">
                            Submitted: {new Date(item.createdAt).toLocaleDateString()} &bull;{" "}
                            {item.privateFeedback ? "Private" : "Public"}
                          </span>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          )}

          {/* ─── TAB 3: BROWSE PUBLIC FEEDBACK ────────────────────────────── */}
          {(activeTab === "Browse Feedback" || activeTab === "Overview") && (
            <section className="panel" style={{ marginTop: "24px" }}>
              <div className="section-heading">
                <div>
                  <h2>
                    {activeTab === "Browse Feedback"
                      ? "Approved Community Feedback"
                      : "Recent Approved Feedback"}
                  </h2>
                  <p>Read transparent feedback approved by our moderators.</p>
                </div>
                {activeTab === "Overview" && (
                  <button
                    type="button"
                    className="text-button"
                    onClick={() => setActiveTab("Browse Feedback")}
                  >
                    View all <ArrowUpRight size={15} />
                  </button>
                )}
              </div>

              {reportNotice && <p className="notice">{reportNotice}</p>}

              <div className="search-bar">
                <Search size={18} />
                <input
                  placeholder="Search feedback keywords..."
                  value={search}
                  onChange={(e) => {
                    setSearch(e.target.value);
                    setPublicPage(0);
                  }}
                />
              </div>

              <div className="public-filter-row">
                <label>
                  Category:
                  <select
                    value={filterCategory}
                    onChange={(e) => {
                      setFilterCategory(e.target.value);
                      setPublicPage(0);
                    }}
                  >
                    <option value="All">All Categories</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id.toString()}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  Rating:
                  <select
                    value={filterRating}
                    onChange={(e) => {
                      setFilterRating(e.target.value);
                      setPublicPage(0);
                    }}
                  >
                    <option value="All">All Ratings</option>
                    <option value="5">5 Stars</option>
                    <option value="4">4 Stars</option>
                    <option value="3">3 Stars</option>
                    <option value="2">2 Stars</option>
                    <option value="1">1 Star</option>
                  </select>
                </label>

                <button
                  type="button"
                  className="text-button"
                  onClick={() => {
                    setSearch("");
                    setFilterCategory("All");
                    setFilterRating("All");
                    setPublicPage(0);
                  }}
                >
                  Clear Filters
                </button>
              </div>

              {loadingPublic ? (
                <div className="empty-state">
                  <p>Loading feedback records...</p>
                </div>
              ) : publicError ? (
                <div className="empty-state">
                  <p>{publicError}</p>
                </div>
              ) : publicFeedback.length === 0 ? (
                <div className="empty-state">
                  <Search size={28} />
                  <p>No approved public feedback found matching your filter.</p>
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
                          <span className="category-pill">{item.category}</span>
                          <span className="feedback-date">
                            {new Date(item.createdAt).toLocaleDateString()}
                          </span>
                        </div>

                        <div className="stars">
                          {Array.from({ length: item.rating }).map((_, i) => (
                            <Star key={i} size={15} fill="currentColor" />
                          ))}
                        </div>

                        <p>{item.text}</p>

                        <div className="feedback-card-footer">
                          <span className="stat-note">Verified Submission</span>
                          {user && !isModerator && (
                            <button
                              type="button"
                              className="text-button"
                              style={{ color: "#d9485e" }}
                              onClick={() => {
                                setReportingFeedbackId(item.id);
                                setReportReason("");
                                setReportError("");
                              }}
                            >
                              <Flag size={14} /> Report Abuse
                            </button>
                          )}
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              )}

              {publicTotalPages > 1 && (
                <div className="pagination-row">
                  <button
                    type="button"
                    className="page-btn"
                    disabled={publicPage === 0}
                    onClick={() => setPublicPage((p) => Math.max(0, p - 1))}
                  >
                    Previous
                  </button>
                  <span style={{ fontSize: "12px", color: "#6e7792" }}>
                    Page {publicPage + 1} of {publicTotalPages}
                  </span>
                  <button
                    type="button"
                    className="page-btn"
                    disabled={publicPage + 1 >= publicTotalPages}
                    onClick={() => setPublicPage((p) => p + 1)}
                  >
                    Next
                  </button>
                </div>
              )}
            </section>
          )}

          {/* ─── TAB 4: MODERATION QUEUE (Moderator & Admin) ───────────────── */}
          {activeTab === "Moderation" && isModerator && (
            <section className="panel">
              <div className="section-heading">
                <div>
                  <h2>Moderation Queue</h2>
                  <p>Review submitted customer feedback and record decisions.</p>
                </div>
                <button
                  type="button"
                  className="text-button"
                  onClick={() => setModerationReload((prev) => prev + 1)}
                  disabled={loadingPending}
                >
                  <RefreshCw size={15} /> Refresh
                </button>
              </div>

              {moderationNotice && <p className="notice">{moderationNotice}</p>}
              {moderationError && <p className="login-error">{moderationError}</p>}

              {loadingPending ? (
                <div className="empty-state">
                  <p>Loading pending reviews...</p>
                </div>
              ) : pendingFeedback.length === 0 ? (
                <div className="empty-state">
                  <ShieldCheck size={32} />
                  <h3>Queue is empty</h3>
                  <p>All submitted feedback items have been reviewed.</p>
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
                          <span className="category-pill">{item.category}</span>
                          <span className="status-badge pending">PENDING REVIEW</span>
                        </div>

                        <div className="stars">
                          {Array.from({ length: item.rating }).map((_, i) => (
                            <Star key={i} size={15} fill="currentColor" />
                          ))}
                        </div>

                        <p>{item.text}</p>

                        <div style={{ marginTop: "12px" }}>
                          <label style={{ fontSize: "11.5px", fontWeight: 700, color: "#48526d" }}>
                            Moderation Reason (Required by Portal Guidelines):
                          </label>
                          <textarea
                            rows={2}
                            maxLength={500}
                            placeholder="State reason for approval, rejection, or hiding..."
                            value={moderationReasons[item.id] ?? ""}
                            onChange={(e) =>
                              setModerationReasons((prev) => ({
                                ...prev,
                                [item.id]: e.target.value,
                              }))
                            }
                            style={{
                              width: "100%",
                              padding: "8px 12px",
                              marginTop: "4px",
                              borderRadius: "8px",
                              border: "1px solid #e2e6f0",
                              fontSize: "12px",
                            }}
                          />
                        </div>

                        <div className="moderation-actions">
                          <button
                            type="button"
                            className="primary-button btn-approve"
                            disabled={moderatingId !== null}
                            onClick={() => moderateFeedback(item.id, "APPROVED")}
                          >
                            <CheckCircle size={15} /> Approve
                          </button>
                          <button
                            type="button"
                            className="primary-button btn-reject"
                            disabled={moderatingId !== null}
                            onClick={() => moderateFeedback(item.id, "REJECTED")}
                          >
                            <XCircle size={15} /> Reject
                          </button>
                          <button
                            type="button"
                            className="primary-button btn-hide"
                            disabled={moderatingId !== null}
                            onClick={() => moderateFeedback(item.id, "HIDDEN")}
                          >
                            <EyeOff size={15} /> Hide
                          </button>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          )}

          {/* ─── TAB 5: ABUSE REPORTS (Moderator & Admin) ──────────────────── */}
          {activeTab === "Abuse Reports" && isModerator && (
            <section className="panel">
              <div className="section-heading">
                <div>
                  <h2>Unresolved Abuse Reports</h2>
                  <p>Investigate feedback flagged by community members.</p>
                </div>
                <button
                  type="button"
                  className="text-button"
                  onClick={() => setReportsReload((prev) => prev + 1)}
                  disabled={reportsLoading}
                >
                  <RefreshCw size={15} /> Refresh
                </button>
              </div>

              {reportsNotice && <p className="notice">{reportsNotice}</p>}
              {reportsError && <p className="login-error">{reportsError}</p>}

              {reportsLoading ? (
                <div className="empty-state">
                  <p>Loading reports...</p>
                </div>
              ) : abuseReports.length === 0 ? (
                <div className="empty-state">
                  <CheckCircle size={32} />
                  <h3>No open reports</h3>
                  <p>All abuse reports have been resolved.</p>
                </div>
              ) : (
                <div className="feedback-list">
                  {abuseReports.map((report) => (
                    <article className="feedback-card" key={report.id}>
                      <div className="feedback-avatar" style={{ background: "#fdf0f2", color: "#ad3346" }}>
                        <Flag size={18} />
                      </div>
                      <div className="feedback-body">
                        <div className="feedback-meta">
                          <span className="category-pill">Report #{report.id}</span>
                          <span className="feedback-date">
                            {new Date(report.createdAt).toLocaleString()}
                          </span>
                        </div>
                        <h4 style={{ margin: "4px 0", color: "#1e2438", fontSize: "14px" }}>
                          Reported Feedback #{report.feedbackId} ({report.category})
                        </h4>
                        <p style={{ margin: "4px 0", fontSize: "12.5px" }}>
                          <strong>Reporter:</strong> {report.reporterEmail}
                        </p>
                        <p style={{ margin: "4px 0", fontSize: "12.5px", color: "#b33246" }}>
                          <strong>Report Reason:</strong> {report.reason}
                        </p>
                        <div
                          style={{
                            background: "#f8f9fd",
                            padding: "10px 14px",
                            borderRadius: "8px",
                            margin: "8px 0",
                            fontSize: "12.5px",
                            border: "1px solid #eaebf2",
                          }}
                        >
                          <strong>Content:</strong> {report.feedbackText}
                        </div>
                        <button
                          type="button"
                          className="primary-button btn-approve"
                          disabled={resolvingReportId !== null}
                          onClick={() => resolveReport(report.id)}
                        >
                          <CheckCircle size={15} /> Mark as Resolved
                        </button>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </section>
          )}

          {/* ─── TAB 6: USER MANAGEMENT (Admin Only) ──────────────────────── */}
          {activeTab === "Users" && isAdmin && (
            <section className="panel">
              <div className="section-heading">
                <div>
                  <h2>User Account Administration</h2>
                  <p>Manage customer, moderator, and administrator accounts.</p>
                </div>
                <button
                  type="button"
                  className="text-button"
                  onClick={() => setUsersReload((prev) => prev + 1)}
                  disabled={loadingUsers}
                >
                  <RefreshCw size={15} /> Refresh
                </button>
              </div>

              {usersNotice && <p className="notice">{usersNotice}</p>}
              {usersError && <p className="login-error">{usersError}</p>}

              {loadingUsers ? (
                <div className="empty-state">
                  <p>Loading user accounts...</p>
                </div>
              ) : (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Status</th>
                      <th>Created</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {usersList.map((u) => (
                      <tr key={u.id}>
                        <td>#{u.id}</td>
                        <td><strong>{u.email}</strong></td>
                        <td>
                          <span className={`role-badge ${u.role.toLowerCase()}`}>
                            {u.role}
                          </span>
                        </td>
                        <td>
                          <span className={`status-badge ${u.active ? "approved" : "rejected"}`}>
                            {u.active ? "ACTIVE" : "DISABLED"}
                          </span>
                        </td>
                        <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                        <td>
                          <button
                            type="button"
                            className={`action-btn-sm ${u.active ? "deactivate" : "activate"}`}
                            onClick={() => toggleUserActive(u.id, u.active)}
                          >
                            {u.active ? "Deactivate" : "Activate"}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          )}

          {/* ─── TAB 7: CATEGORY MANAGEMENT (Admin Only) ──────────────────── */}
          {activeTab === "Categories" && isAdmin && (
            <section className="panel">
              <div className="section-heading">
                <div>
                  <h2>Feedback Categories</h2>
                  <p>Configure and maintain feedback classification taxonomies.</p>
                </div>
                <button
                  type="button"
                  className="primary-button"
                  onClick={() => setShowCreateCatModal(true)}
                >
                  <Plus size={16} /> New Category
                </button>
              </div>

              {catNotice && <p className="notice">{catNotice}</p>}

              {loadingAdminCats ? (
                <div className="empty-state">
                  <p>Loading categories...</p>
                </div>
              ) : (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Category Name</th>
                      <th>Description</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {adminCategories.map((c) => (
                      <tr key={c.id}>
                        <td>#{c.id}</td>
                        <td><strong>{c.name}</strong></td>
                        <td>{c.description || "—"}</td>
                        <td>
                          <span className={`status-badge ${c.active ? "approved" : "rejected"}`}>
                            {c.active ? "ACTIVE" : "INACTIVE"}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          )}

          {/* ─── TAB 8: AUDIT TRAIL (Admin Only) ──────────────────────────── */}
          {activeTab === "Audit Trail" && isAdmin && (
            <section className="panel">
              <div className="section-heading">
                <div>
                  <h2>Security Audit Trail</h2>
                  <p>Immutable log of administrative, moderation, and auth events.</p>
                </div>
              </div>

              {auditError && <p className="login-error">{auditError}</p>}

              {loadingAudit ? (
                <div className="empty-state">
                  <p>Loading audit stream...</p>
                </div>
              ) : auditLogs.length === 0 ? (
                <div className="empty-state">
                  <FileText size={32} />
                  <p>No audit events recorded yet.</p>
                </div>
              ) : (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Actor</th>
                      <th>Action</th>
                      <th>Target</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {auditLogs.map((log) => (
                      <tr key={log.id}>
                        <td style={{ whiteSpace: "nowrap", fontSize: "11.5px" }}>
                          {new Date(log.createdAt).toLocaleString()}
                        </td>
                        <td><code>{log.actorEmail}</code></td>
                        <td>
                          <span className="category-pill">{log.action}</span>
                        </td>
                        <td>
                          {log.entityType} #{log.entityId}
                        </td>
                        <td>{log.details}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          )}
        </section>
      </main>

      {/* ─── MODAL: REPORT ABUSE (Customer) ─────────────────────────────────── */}
      {reportingFeedbackId !== null && (
        <div className="modal-backdrop">
          <section className="panel" role="dialog" aria-modal="true">
            <div className="section-heading">
              <h2>Report Feedback #{reportingFeedbackId}</h2>
              <button
                type="button"
                className="text-button"
                onClick={() => setReportingFeedbackId(null)}
              >
                Cancel
              </button>
            </div>
            {reportError && <p className="login-error">{reportError}</p>}
            <form onSubmit={submitAbuseFlag}>
              <label htmlFor="report-reason">Reason for Reporting</label>
              <textarea
                id="report-reason"
                rows={4}
                maxLength={500}
                required
                value={reportReason}
                onChange={(e) => setReportReason(e.target.value)}
                placeholder="Explain why this content violates guidelines (spam, offensive, etc.)..."
                style={{
                  width: "100%",
                  padding: "10px",
                  borderRadius: "8px",
                  border: "1px solid #dfe3ee",
                  fontSize: "12.5px",
                }}
              />
              <div className="character-count">
                {reportReason.length}/500 characters
              </div>
              <button
                type="submit"
                className="primary-button btn-reject"
                disabled={reportSubmitting}
                style={{ marginTop: "12px" }}
              >
                <Flag size={15} />
                {reportSubmitting ? "Submitting..." : "Submit Abuse Report"}
              </button>
            </form>
          </section>
        </div>
      )}

      {/* ─── MODAL: CREATE CATEGORY (Admin) ─────────────────────────────────── */}
      {showCreateCatModal && (
        <div className="modal-backdrop">
          <section className="panel" role="dialog" aria-modal="true">
            <div className="section-heading">
              <h2>Create New Category</h2>
              <button
                type="button"
                className="text-button"
                onClick={() => setShowCreateCatModal(false)}
              >
                Cancel
              </button>
            </div>
            {catError && <p className="login-error">{catError}</p>}
            <form onSubmit={createCategory} style={{ display: "grid", gap: "12px" }}>
              <div>
                <label style={{ fontSize: "12px", fontWeight: 700 }}>Category Name</label>
                <input
                  type="text"
                  maxLength={60}
                  required
                  value={newCatName}
                  onChange={(e) => setNewCatName(e.target.value)}
                  placeholder="e.g., Billing & Invoicing"
                  style={{
                    width: "100%",
                    padding: "10px",
                    borderRadius: "8px",
                    border: "1px solid #dfe3ee",
                    fontSize: "12.5px",
                    marginTop: "4px",
                  }}
                />
              </div>
              <div>
                <label style={{ fontSize: "12px", fontWeight: 700 }}>Description</label>
                <textarea
                  rows={3}
                  maxLength={300}
                  value={newCatDesc}
                  onChange={(e) => setNewCatDesc(e.target.value)}
                  placeholder="Brief description of feedback to group under this category..."
                  style={{
                    width: "100%",
                    padding: "10px",
                    borderRadius: "8px",
                    border: "1px solid #dfe3ee",
                    fontSize: "12.5px",
                    marginTop: "4px",
                  }}
                />
              </div>
              <label className="privacy-option">
                <input
                  type="checkbox"
                  checked={newCatActive}
                  onChange={(e) => setNewCatActive(e.target.checked)}
                />
                Active immediately
              </label>
              <button
                type="submit"
                className="primary-button"
                disabled={creatingCat}
              >
                <Plus size={16} />
                {creatingCat ? "Saving..." : "Save Category"}
              </button>
            </form>
          </section>
        </div>
      )}
    </div>
  );
}
