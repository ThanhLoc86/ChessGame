import { API_BASE } from '../config';

export async function getProfile(token) {
  const res = await fetch(`${API_BASE}/api/users/me`, {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  if (!res.ok) throw new Error('Failed to load profile');
  return res.json();
}

export async function getMatches(token) {
  const res = await fetch(`${API_BASE}/api/users/me/matches`, {
    headers: { 'Authorization': 'Bearer ' + token }
  });
  if (!res.ok) throw new Error('Failed to load matches');
  return res.json();
}


