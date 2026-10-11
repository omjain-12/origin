import { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import type { TabType } from './components/Sidebar';
import { ChatView } from './components/ChatView';
import { DashboardView } from './components/DashboardView';
import { MemoryView } from './components/MemoryView';
import { DocumentsView } from './components/DocumentsView';
import { createSession, fetchDashboard } from './api';

export function App() {
  const [currentTab, setCurrentTab] = useState<TabType>('chat');
  const [sessionId, setSessionId] = useState<string>('');
  const [online, setOnline] = useState<boolean>(false);
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

  // Initialize or restore session
  useEffect(() => {
    const init = async () => {
      try {
        const sess = await createSession();
        setSessionId(sess.sessionId);
        setOnline(true);
      } catch (err) {
        // Fallback session ID when backend is not yet booted
        setSessionId(crypto.randomUUID());
        setOnline(false);
      }
    };
    init();

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
  }, []);

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
            onRefreshSession={setSessionId}
          />
        )}
        {currentTab === 'dashboard' && <DashboardView />}
        {currentTab === 'memory' && <MemoryView />}
        {currentTab === 'documents' && <DocumentsView />}
      </main>
    </div>
  );
}

export default App;
