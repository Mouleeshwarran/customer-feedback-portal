const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function apiRequest(
  path,
  { username, password, ...options } = {}
) {
  const isFormData = options.body instanceof FormData;

  const headers = {
    ...(!isFormData && { "Content-Type": "application/json" }),
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
    let errorMessage = `Request failed: ${response.status}`;
    try {
      const errorJson = await response.json();
      errorMessage = errorJson.message || errorJson.error || errorMessage;
    } catch {
      try {
        const text = await response.text();
        if (text) errorMessage = text;
      } catch {
        // use default message
      }
    }
    throw new Error(errorMessage);
  }

  // Handle responses that contain no body
  if (response.status === 204 || response.status === 205) {
    return null;
  }

  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    return response.json();
  }

  return response.text();
}
