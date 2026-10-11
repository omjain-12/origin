import { useState, useRef, useEffect } from 'react';
import type React from 'react';
import { Send, Sparkles, User, Bot, RotateCcw, ArrowRight, Clock } from 'lucide-react';
import type { ChatMessageItem } from '../types';
import { sendMessage, createSession } from '../api';

interface ChatViewProps {
  sessionId: string;
  onRefreshSession: (newId: string) => void;
  onDataModified?: () => void;
}

export const ChatView: React.FC<ChatViewProps> = ({ sessionId, onRefreshSession, onDataModified }) => {
  const [messages, setMessages] = useState<ChatMessageItem[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const quickPrompts = [
    'Plan my day with upcoming tasks',
    'Schedule a team sync on Thursday at 3 PM',
    'Set a reminder to drink water in 30 minutes',
    'What personal preferences do you remember?',
  ];

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  const handleSend = async (textToSend?: string) => {
    const text = (textToSend || input).trim();
    if (!text || loading) return;

    const userMessage: ChatMessageItem = {
      id: crypto.randomUUID(),
      role: 'user',
      content: text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMessage]);
    if (!textToSend) setInput('');
    setLoading(true);

    try {
      const response = await sendMessage(sessionId, text);
      const assistantMessage: ChatMessageItem = {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: response,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, assistantMessage]);
      if (onDataModified) onDataModified();
    } catch (err: any) {
      const errorMessage: ChatMessageItem = {
        id: crypto.randomUUID(),
        role: 'assistant',
        content: `⚠️ Error: ${err.message || 'Unable to connect to assistant.'}`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  const handleNewSession = async () => {
    try {
      const newSession = await createSession();
      onRefreshSession(newSession.sessionId);
      setMessages([]);
    } catch (e) {
      console.error(e);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      backgroundColor: 'var(--bg-app)',
      position: 'relative'
    }}>
      {/* Top Header */}
      <header style={{
        padding: '16px 28px',
        borderBottom: '1px solid var(--border)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        backgroundColor: 'var(--bg-card)',
        boxShadow: 'var(--shadow-sm)'
      }}>
        <div>
          <h2 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-main)' }}>
            Conversation
          </h2>
          <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
            Session ID: {sessionId.substring(0, 8)}...
          </p>
        </div>
        <button
          onClick={handleNewSession}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '7px 12px',
            backgroundColor: 'var(--bg-subtle)',
            border: '1px solid var(--border)',
            borderRadius: '8px',
            color: 'var(--text-secondary)',
            fontSize: '12px',
            fontWeight: 500,
            cursor: 'pointer',
            transition: 'all 0.15s ease'
          }}
          title="Start fresh conversation"
        >
          <RotateCcw size={14} />
          <span>New Chat</span>
        </button>
      </header>

      {/* Message History & Hero State */}
      <div style={{
        flex: 1,
        overflowY: 'auto',
        padding: '24px 28px',
        display: 'flex',
        flexDirection: 'column',
        gap: '20px',
        maxWidth: '900px',
        width: '100%',
        margin: '0 auto'
      }}>
        {messages.length === 0 ? (
          <div style={{
            margin: 'auto 0',
            textAlign: 'center',
            padding: '40px 20px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '16px'
          }}>
            <div style={{
              width: '56px',
              height: '56px',
              borderRadius: '16px',
              background: 'linear-gradient(135deg, #5457cd 0%, #818cf8 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              boxShadow: '0 8px 24px rgba(84, 87, 205, 0.25)',
              marginBottom: '8px'
            }}>
              <Sparkles size={28} />
            </div>
            
            <h1 style={{
              fontSize: '28px',
              fontWeight: 700,
              letterSpacing: '-0.5px',
              color: 'var(--text-main)',
              lineHeight: 1.2
            }}>
              Your day, thoughtfully organized.
            </h1>
            <p style={{
              fontSize: '15px',
              color: 'var(--text-secondary)',
              maxWidth: '520px',
              lineHeight: 1.5
            }}>
              I can help coordinate your schedule, track priority tasks, configure recurring jobs, and recall personal facts across conversations.
            </p>

            {/* Quick Action Suggestion Chips */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
              gap: '10px',
              width: '100%',
              maxWidth: '620px',
              marginTop: '16px'
            }}>
              {quickPrompts.map((prompt, idx) => (
                <button
                  key={idx}
                  onClick={() => handleSend(prompt)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '12px 14px',
                    backgroundColor: 'var(--bg-card)',
                    border: '1px solid var(--border)',
                    borderRadius: '10px',
                    color: 'var(--text-main)',
                    fontSize: '13px',
                    textAlign: 'left',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease',
                    boxShadow: 'var(--shadow-sm)'
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = 'var(--accent-primary)';
                    e.currentTarget.style.transform = 'translateY(-1px)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = 'var(--border)';
                    e.currentTarget.style.transform = 'translateY(0)';
                  }}
                >
                  <span style={{ color: 'var(--text-secondary)' }}>{prompt}</span>
                  <ArrowRight size={14} color="var(--text-muted)" />
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg) => (
            <div
              key={msg.id}
              style={{
                display: 'flex',
                gap: '12px',
                alignItems: 'flex-start',
                alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
                maxWidth: '82%'
              }}
            >
              {msg.role === 'assistant' && (
                <div style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '10px',
                  background: 'linear-gradient(135deg, #5457cd 0%, #818cf8 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#fff',
                  flexShrink: 0,
                  marginTop: '2px'
                }}>
                  <Bot size={18} />
                </div>
              )}

              <div style={{
                padding: '14px 18px',
                borderRadius: '14px',
                backgroundColor: msg.role === 'user' ? 'var(--accent-primary)' : 'var(--bg-card)',
                color: msg.role === 'user' ? '#ffffff' : 'var(--text-main)',
                border: msg.role === 'user' ? 'none' : '1px solid var(--border)',
                boxShadow: 'var(--shadow-sm)',
                lineHeight: 1.6,
                fontSize: '14px',
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-word'
              }}>
                {msg.content}
                <div style={{
                  fontSize: '11px',
                  marginTop: '6px',
                  textAlign: 'right',
                  opacity: 0.7,
                  color: msg.role === 'user' ? '#e0e7ff' : 'var(--text-muted)'
                }}>
                  {msg.timestamp}
                </div>
              </div>

              {msg.role === 'user' && (
                <div style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '10px',
                  backgroundColor: 'var(--bg-subtle)',
                  border: '1px solid var(--border)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--text-secondary)',
                  flexShrink: 0,
                  marginTop: '2px'
                }}>
                  <User size={18} />
                </div>
              )}
            </div>
          ))
        )}

        {loading && (
          <div style={{
            display: 'flex',
            gap: '12px',
            alignItems: 'center',
            alignSelf: 'flex-start'
          }}>
            <div style={{
              width: '32px',
              height: '32px',
              borderRadius: '10px',
              background: 'linear-gradient(135deg, #5457cd 0%, #818cf8 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              flexShrink: 0
            }}>
              <Bot size={18} />
            </div>
            <div style={{
              padding: '12px 18px',
              borderRadius: '14px',
              backgroundColor: 'var(--bg-card)',
              border: '1px solid var(--border)',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              color: 'var(--text-secondary)',
              fontSize: '13px'
            }}>
              <Clock size={16} className="animate-spin" />
              <span>Thinking & inspecting schedule...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div style={{
        padding: '16px 28px 24px',
        backgroundColor: 'var(--bg-card)',
        borderTop: '1px solid var(--border)',
        boxShadow: 'var(--shadow-sm)'
      }}>
        <div style={{
          maxWidth: '900px',
          margin: '0 auto',
          display: 'flex',
          gap: '10px',
          alignItems: 'flex-end',
          position: 'relative'
        }}>
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask Origin to schedule an event, set a task, or recall information..."
            rows={2}
            style={{
              flex: 1,
              padding: '12px 16px',
              backgroundColor: 'var(--bg-app)',
              border: '1px solid var(--border)',
              borderRadius: '12px',
              color: 'var(--text-main)',
              fontSize: '14px',
              resize: 'none',
              outline: 'none',
              transition: 'border-color 0.15s ease'
            }}
            onFocus={(e) => (e.target.style.borderColor = 'var(--accent-primary)')}
            onBlur={(e) => (e.target.style.borderColor = 'var(--border)')}
          />
          <button
            onClick={() => handleSend()}
            disabled={!input.trim() || loading}
            style={{
              height: '46px',
              padding: '0 18px',
              borderRadius: '12px',
              border: 'none',
              backgroundColor: input.trim() && !loading ? 'var(--accent-primary)' : 'var(--bg-subtle)',
              color: input.trim() && !loading ? '#ffffff' : 'var(--text-muted)',
              cursor: input.trim() && !loading ? 'pointer' : 'not-allowed',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              fontWeight: 600,
              fontSize: '14px',
              transition: 'all 0.15s ease',
              boxShadow: input.trim() && !loading ? '0 4px 12px rgba(84, 87, 205, 0.25)' : 'none'
            }}
          >
            <Send size={16} />
            <span>Send</span>
          </button>
        </div>
        <div style={{
          textAlign: 'center',
          marginTop: '8px',
          fontSize: '11px',
          color: 'var(--text-muted)'
        }}>
          Press <kbd style={{ padding: '2px 4px', background: 'var(--bg-subtle)', borderRadius: '4px' }}>Enter</kbd> to send, <kbd style={{ padding: '2px 4px', background: 'var(--bg-subtle)', borderRadius: '4px' }}>Shift + Enter</kbd> for new line
        </div>
      </div>
    </div>
  );
};
