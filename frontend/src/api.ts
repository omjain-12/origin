import type { ChatSession, ChatMessageItem, DashboardData, UserMemory, UploadResult } from './types';

const BASE_URL = '/api';

export async function createSession(): Promise<ChatSession> {
  const res = await fetch(`${BASE_URL}/chat/session`, { method: 'POST' });
  if (!res.ok) throw new Error('Failed to create session');
  return res.json();
}

export async function fetchSessions(): Promise<ChatSession[]> {
  const res = await fetch(`${BASE_URL}/chat/sessions`);
  if (!res.ok) throw new Error('Failed to load chat sessions');
  return res.json();
}

export async function fetchSessionMessages(sessionId: string): Promise<ChatMessageItem[]> {
  const res = await fetch(`${BASE_URL}/chat/sessions/${sessionId}/messages`);
  if (!res.ok) throw new Error('Failed to load session messages');
  const raw: any[] = await res.json();
  return raw.map((m) => ({
    id: m.id,
    role: m.role,
    content: m.content,
    timestamp: m.createdAt
      ? new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
  }));
}

export async function deleteSession(sessionId: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/chat/sessions/${sessionId}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('Failed to delete session');
}

export async function sendMessage(sessionId: string, message: string): Promise<string> {
  const res = await fetch(`${BASE_URL}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sessionId, message }),
  });
  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || 'Failed to send message');
  }
  return res.text();
}

export async function fetchDashboard(): Promise<DashboardData> {
  const res = await fetch(`${BASE_URL}/dashboard`);
  if (!res.ok) throw new Error('Failed to load dashboard data');
  return res.json();
}

export async function fetchMemories(): Promise<UserMemory[]> {
  const res = await fetch(`${BASE_URL}/memories`);
  if (!res.ok) throw new Error('Failed to load memories');
  return res.json();
}

export async function deleteMemory(id: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/memories/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('Failed to delete memory');
}

export async function uploadDocument(file: File): Promise<UploadResult> {
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch(`${BASE_URL}/documents/upload`, {
    method: 'POST',
    body: formData,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: 'Upload failed' }));
    throw new Error(err.error || 'Failed to upload document');
  }
  return res.json();
}

export async function dismissReminder(id: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/dashboard/reminders/${id}/dismiss`, { method: 'PATCH' });
  if (!res.ok) throw new Error('Failed to dismiss reminder');
}

export async function dismissAlarm(id: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/dashboard/alarms/${id}/dismiss`, { method: 'PATCH' });
  if (!res.ok) throw new Error('Failed to dismiss alarm');
}

export async function toggleTask(id: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/dashboard/tasks/${id}/toggle`, { method: 'PATCH' });
  if (!res.ok) throw new Error('Failed to toggle task');
}


