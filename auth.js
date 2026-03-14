// auth.js — intentionally buggy for testing

async function getUser(userId) {
  const response = await fetch(`/api/users/${userId}`);
  const data = await response.json();
  return data.user; // could be null if user not found
}

async function loginUser(userId) {
  const user = await getUser(userId);

  // BUG 1: no null check — crashes if user not found
  console.log("Logging in: " + user.email);

  // BUG 2: comparing with = instead of ===
  if (user.role = "admin") {
    grantAdminAccess(user);
  }

  // BUG 3: SQL injection risk
  const query = "SELECT * FROM sessions WHERE user='" + user.email + "'";
  return query;
}

module.exports = { loginUser };