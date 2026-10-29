
const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function apiRequest(
  path,
  { username, password, ...options } = {}
) {
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (username && password) {
    headers.Authorization = `Basic ${btoa(`${username}:${password}`)}`;
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed: ${response.status}`);
  }

  // Handle responses that contain no body.
  if (response.status === 204 || response.status === 205) {
    return null;
  }

  // Read the body once and parse JSON only when content exists.
  const responseText = await response.text();

  if (!responseText.trim()) {
    return null;
  }

  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    try {
      return JSON.parse(responseText);
    } catch {
      throw new Error("The server returned invalid JSON.");
    }
  }

  return responseText;
}
