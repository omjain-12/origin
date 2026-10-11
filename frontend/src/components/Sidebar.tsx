import type React from 'react';
import { MessageSquare, LayoutDashboard, Brain, FileText, Sparkles, Sun, Moon, Plus, Trash2, MessageCircle } from 'lucide-react';
import type { ChatSession } from '../types';

export type TabType = 'chat' | 'dashboard' | 'memory' | 'documents';

interface SidebarProps {
  currentTab: TabType;
  onSelectTab: (tab: TabType) => void;
  online: boolean;
  theme: 'light' | 'dark';
  onToggleTheme: () => void;
  sessions?: ChatSession[];
  activeSessionId?: string;
  onSelectSession?: (id: string) => void;
  onNewSession?: () => void;
  onDeleteSession?: (id: string) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentTab,
  onSelectTab,
  online,
  theme,
  onToggleTheme,
  sessions = [],
  activeSessionId,
  onSelectSession,
  onNewSession,
  onDeleteSession,
}) => {
  const navItems = [
    { id: 'chat' as TabType, label: 'Assistant', icon: MessageSquare },
    { id: 'dashboard' as TabType, label: 'Overview & Tools', icon: LayoutDashboard },
    { id: 'memory' as TabType, label: 'Memory Bank', icon: Brain },
    { id: 'documents' as TabType, label: 'Knowledge Base', icon: FileText },
  ];

  return (
    <aside style={{
      width: '280px',
      backgroundColor: 'var(--bg-sidebar)',
      borderRight: '1px solid var(--border)',
      display: 'flex',
      flexDirection: 'column',
      padding: '20px 14px',
      justifyContent: 'space-between',
      flexShrink: 0,
      userSelect: 'none',
      height: '100%',
      boxSizing: 'border-box',
      transition: 'background-color 0.2s ease, border-color 0.2s ease'
    }}>
      <div style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden', minHeight: 0 }}>
        {/* Brand */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px', paddingLeft: '8px' }}>
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '11px',
            background: 'linear-gradient(135deg, #5457cd 0%, #818cf8 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(84, 87, 205, 0.25)',
            color: '#fff',
            flexShrink: 0
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
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '3px', marginBottom: '20px' }}>
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
                  padding: '10px 12px',
                  borderRadius: '10px',
                  border: 'none',
                  backgroundColor: active ? 'var(--bg-card)' : 'transparent',
                  color: active ? 'var(--accent-primary)' : 'var(--text-secondary)',
                  cursor: 'pointer',
                  fontWeight: active ? '600' : '500',
                  fontSize: '13.5px',
                  textAlign: 'left',
                  transition: 'all 0.15s ease',
                  boxShadow: active ? 'var(--shadow-sm)' : 'none',
                  borderLeft: active ? '3px solid var(--accent-primary)' : '3px solid transparent'
                }}
              >
                <Icon size={17} strokeWidth={active ? 2.3 : 1.8} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Conversation History Section */}
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          borderTop: '1px solid var(--border)',
          paddingTop: '14px',
          overflow: 'hidden',
          flex: 1,
          minHeight: 0
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 8px 8px 8px'
          }}>
            <span style={{
              fontSize: '11px',
              fontWeight: 600,
              textTransform: 'uppercase',
              letterSpacing: '0.6px',
              color: 'var(--text-muted)'
            }}>
              Conversations
            </span>
            {onNewSession && (
              <button
                onClick={onNewSession}
                title="New Chat"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  background: 'none',
                  border: '1px solid var(--border)',
                  borderRadius: '6px',
                  padding: '3px 8px',
                  fontSize: '11px',
                  fontWeight: 500,
                  color: 'var(--accent-primary)',
                  cursor: 'pointer',
                  backgroundColor: 'var(--bg-card)'
                }}
              >
                <Plus size={12} />
                <span>New</span>
              </button>
            )}
          </div>

          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '2px',
            overflowY: 'auto',
            paddingRight: '4px'
          }}>
            {sessions.length === 0 ? (
              <p style={{
                fontSize: '12px',
                color: 'var(--text-muted)',
                fontStyle: 'italic',
                padding: '8px'
              }}>
                No saved chats yet.
              </p>
            ) : (
              sessions.map((sess) => {
                const isActive = sess.sessionId === activeSessionId;
                return (
                  <div
                    key={sess.sessionId}
                    onClick={() => {
                      if (onSelectSession) onSelectSession(sess.sessionId);
                      onSelectTab('chat');
                    }}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '8px 10px',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      fontSize: '12.5px',
                      backgroundColor: isActive ? 'var(--bg-card)' : 'transparent',
                      color: isActive ? 'var(--text-main)' : 'var(--text-secondary)',
                      fontWeight: isActive ? 600 : 400,
                      boxShadow: isActive ? 'var(--shadow-sm)' : 'none',
                      borderLeft: isActive ? '2px solid var(--accent-primary)' : '2px solid transparent',
                      transition: 'all 0.12s ease'
                    }}
                  >
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                      flex: 1
                    }}>
                      <MessageCircle size={14} color={isActive ? 'var(--accent-primary)' : 'var(--text-muted)'} style={{ flexShrink: 0 }} />
                      <span style={{
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}>
                        {sess.title || 'Conversation'}
                      </span>
                    </div>

                    {onDeleteSession && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onDeleteSession(sess.sessionId);
                        }}
                        title="Delete Chat"
                        style={{
                          background: 'none',
                          border: 'none',
                          padding: '2px',
                          color: 'var(--text-muted)',
                          cursor: 'pointer',
                          opacity: 0.6,
                          display: 'flex',
                          alignItems: 'center'
                        }}
                        onMouseEnter={(e) => ((e.currentTarget as HTMLElement).style.opacity = '1')}
                        onMouseLeave={(e) => ((e.currentTarget as HTMLElement).style.opacity = '0.6')}
                      >
                        <Trash2 size={13} />
                      </button>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </div>
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
              {online ? 'Origin Online' : 'Origin Idle'}
            </span>
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: '1.4' }}>
            Autonomous Assistant Ready
          </p>
        </div>
      </div>
    </aside>
  );
};
