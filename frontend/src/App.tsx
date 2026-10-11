import { useState, useEffect, useCallback } from 'react';
import { Sidebar } from './components/Sidebar';
import type { TabType } from './components/Sidebar';
import { ChatView } from './components/ChatView';
import { DashboardView } from './components/DashboardView';
import { MemoryView } from './components/MemoryView';
import { DocumentsView } from './components/DocumentsView';
import type { ChatSession } from './types';
import { createSession, fetchDashboard, fetchSessions, deleteSession } from './api';

export function App() {
  const [currentTab, setCurrentTab] = useState<TabType>('chat');
  const [sessionId, setSessionId] = useState<string>(() => {
    return localStorage.getItem('origin_active_session_id') || '';
  });
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [online, setOnline] = useState<boolean>(false);
  const [dashboardKey, setDashboardKey] = useState<number>(0);
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    const saved = localStorage.getItem('origin_theme');
    if (saved === 'light' || saved === 'dark') return saved;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });

  // Apply theme to document element
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('origin_theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));
  };

  // Persist active session ID
  const selectSession = useCallback((id: string) => {
    setSessionId(id);
    localStorage.setItem('origin_active_session_id', id);
  }, []);

  // Load all sessions from backend
  const loadSessions = useCallback(async () => {
    try {
      const list = await fetchSessions();
      setSessions(list);
      setOnline(true);

      // If active sessionId is empty or not in the list, pick the first or create one
      const savedId = localStorage.getItem('origin_active_session_id');
      if (list.length > 0) {
        const found = list.find((s) => s.sessionId === savedId);
        if (found) {
          setSessionId(found.sessionId);
        } else if (!savedId || !list.some((s) => s.sessionId === sessionId)) {
          selectSession(list[0].sessionId);
        }
      } else {
        const newSess = await createSession();
        setSessions([newSess]);
        selectSession(newSess.sessionId);
      }
    } catch {
      setOnline(false);
      if (!sessionId) {
        const fallbackId = crypto.randomUUID();
        selectSession(fallbackId);
      }
    }
  }, [selectSession, sessionId]);

  const handleNewSession = async () => {
    try {
      const newSess = await createSession();
      setSessions((prev) => [newSess, ...prev]);
      selectSession(newSess.sessionId);
      setCurrentTab('chat');
    } catch (e) {
      console.error('Failed to create new session:', e);
    }
  };

  const handleDeleteSession = async (idToDelete: string) => {
    try {
      await deleteSession(idToDelete);
      const remaining = sessions.filter((s) => s.sessionId !== idToDelete);
      setSessions(remaining);
      if (sessionId === idToDelete) {
        if (remaining.length > 0) {
          selectSession(remaining[0].sessionId);
        } else {
          handleNewSession();
        }
      }
    } catch (e) {
      console.error('Failed to delete session:', e);
    }
  };

  // Initial load
  useEffect(() => {
    loadSessions();

    // Healthcheck ping
    const interval = setInterval(async () => {
      try {
        await fetchDashboard();
        setOnline(true);
      } catch {
        setOnline(false);
      }
    }, 15000);

    return () => clearInterval(interval);
  }, [loadSessions]);

  // Trigger dashboard update
  const handleDataModified = () => {
    setDashboardKey((prev) => prev + 1);
  };

  return (
    <div style={{
      display: 'flex',
      width: '100vw',
      height: '100vh',
      overflow: 'hidden',
      backgroundColor: 'var(--bg-app)'
    }}>
      <Sidebar
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        online={online}
        theme={theme}
        onToggleTheme={toggleTheme}
        sessions={sessions}
        activeSessionId={sessionId}
        onSelectSession={selectSession}
        onNewSession={handleNewSession}
        onDeleteSession={handleDeleteSession}
      />
      <main style={{
        flex: 1,
        height: '100%',
        overflow: 'hidden',
        position: 'relative'
      }}>
        {currentTab === 'chat' && (
          <ChatView
            sessionId={sessionId}
            onRefreshSession={selectSession}
            onDataModified={handleDataModified}
            onSessionTitleUpdated={loadSessions}
          />
        )}
        {currentTab === 'dashboard' && <DashboardView key={dashboardKey} />}
        {currentTab === 'memory' && <MemoryView />}
        {currentTab === 'documents' && <DocumentsView />}
      </main>
    </div>
  );
}

export default App;
