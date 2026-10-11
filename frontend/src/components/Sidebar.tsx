import type React from 'react';
import { MessageSquare, LayoutDashboard, Brain, FileText, Sparkles, Sun, Moon } from 'lucide-react';

export type TabType = 'chat' | 'dashboard' | 'memory' | 'documents';

interface SidebarProps {
  currentTab: TabType;
  onSelectTab: (tab: TabType) => void;
  online: boolean;
  theme: 'light' | 'dark';
  onToggleTheme: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentTab,
  onSelectTab,
  online,
  theme,
  onToggleTheme,
}) => {
  const navItems = [
    { id: 'chat' as TabType, label: 'Assistant', icon: MessageSquare },
    { id: 'dashboard' as TabType, label: 'Overview & Tools', icon: LayoutDashboard },
    { id: 'memory' as TabType, label: 'Memory Bank', icon: Brain },
    { id: 'documents' as TabType, label: 'Knowledge Base', icon: FileText },
  ];

  return (
    <aside style={{
      width: '260px',
      backgroundColor: 'var(--bg-sidebar)',
      borderRight: '1px solid var(--border)',
      display: 'flex',
      flexDirection: 'column',
      padding: '24px 16px',
      justifyContent: 'space-between',
      flexShrink: 0,
      userSelect: 'none',
      transition: 'background-color 0.2s ease, border-color 0.2s ease'
    }}>
      <div>
        {/* Brand */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '32px', paddingLeft: '8px' }}>
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '11px',
            background: 'linear-gradient(135deg, #5457cd 0%, #818cf8 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(84, 87, 205, 0.25)',
            color: '#fff'
          }}>
            <Sparkles size={20} />
          </div>
          <div>
            <h1 style={{
              fontSize: '17px',
              fontWeight: '700',
              letterSpacing: '-0.3px',
              color: 'var(--text-main)',
              lineHeight: 1.2
            }}>
              Origin
            </h1>
            <p style={{
              fontSize: '11px',
              color: 'var(--text-muted)',
              letterSpacing: '0.5px',
              textTransform: 'uppercase',
              fontWeight: 500
            }}>
              Personal AI
            </p>
          </div>
        </div>

        {/* Navigation */}
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  padding: '11px 14px',
                  borderRadius: '10px',
                  border: 'none',
                  backgroundColor: active ? 'var(--bg-card)' : 'transparent',
                  color: active ? 'var(--accent-primary)' : 'var(--text-secondary)',
                  cursor: 'pointer',
                  fontWeight: active ? '600' : '500',
                  fontSize: '14px',
                  textAlign: 'left',
                  transition: 'all 0.15s ease',
                  boxShadow: active ? 'var(--shadow-sm)' : 'none',
                  borderLeft: active ? '3px solid var(--accent-primary)' : '3px solid transparent'
                }}
              >
                <Icon size={18} strokeWidth={active ? 2.3 : 1.8} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </div>

      {/* Footer controls: Theme toggle + System status */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {/* Theme switcher */}
        <button
          onClick={onToggleTheme}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '10px 14px',
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border)',
            borderRadius: '10px',
            color: 'var(--text-secondary)',
            cursor: 'pointer',
            fontSize: '13px',
            fontWeight: 500,
            transition: 'all 0.15s ease',
            boxShadow: 'var(--shadow-sm)'
          }}
          title={`Switch to ${theme === 'light' ? 'Dark' : 'Light'} Mode`}
        >
          <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            {theme === 'light' ? <Sun size={16} color="var(--accent-amber)" /> : <Moon size={16} color="var(--accent-lavender)" />}
            <span>{theme === 'light' ? 'Light Theme' : 'Dark Theme'}</span>
          </span>
          <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Toggle</span>
        </button>

        {/* Engine status indicator */}
        <div style={{
          padding: '12px 14px',
          backgroundColor: 'var(--bg-card)',
          borderRadius: '10px',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <span style={{
              display: 'inline-block',
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: online ? '#10b981' : '#f59e0b',
              boxShadow: online ? '0 0 8px rgba(16, 185, 129, 0.5)' : 'none'
            }} />
            <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-main)' }}>
              {online ? 'Engine Connected' : 'Engine Idle'}
            </span>
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: '1.4' }}>
            zai.glm-5 · Bedrock Mantle
          </p>
        </div>
      </div>
    </aside>
  );
};
