import { useEffect, useState } from 'react';
import type React from 'react';
import { Brain, Trash2, RefreshCw, Filter } from 'lucide-react';
import type { UserMemory } from '../types';
import { fetchMemories, deleteMemory } from '../api';

export const MemoryView: React.FC = () => {
  const [memories, setMemories] = useState<UserMemory[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');

  const loadMemories = async () => {
    setLoading(true);
    try {
      const data = await fetchMemories();
      setMemories(data);
    } catch (err) {
      console.error('Failed to load memories:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMemories();
  }, []);

  const handleDelete = async (id: string) => {
    try {
      await deleteMemory(id);
      setMemories((prev) => prev.filter((m) => m.id !== id));
    } catch (err) {
      console.error('Failed to delete memory:', err);
    }
  };

  const categories = ['all', ...Array.from(new Set(memories.map((m) => m.category || 'other')))];

  const filteredMemories = selectedCategory === 'all'
    ? memories
    : memories.filter((m) => (m.category || 'other').toLowerCase() === selectedCategory.toLowerCase());

  const getCategoryBadge = (category: string) => {
    switch (category.toLowerCase()) {
      case 'preference':
        return { color: 'var(--accent-periwinkle)', bg: 'var(--accent-periwinkle-bg)' };
      case 'habit':
        return { color: 'var(--accent-sage)', bg: 'var(--accent-sage-bg)' };
      case 'schedule':
        return { color: 'var(--accent-lavender)', bg: 'var(--accent-lavender-bg)' };
      default:
        return { color: 'var(--accent-amber)', bg: 'var(--accent-amber-bg)' };
    }
  };

  return (
    <div style={{
      padding: '32px',
      overflowY: 'auto',
      height: '100%',
      backgroundColor: 'var(--bg-app)',
      display: 'flex',
      flexDirection: 'column',
      gap: '24px'
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-main)', letterSpacing: '-0.3px' }}>
            Durable Memory Bank
          </h1>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Facts and preferences autonomously extracted by the learning layer to personalize future answers.
          </p>
        </div>
        <button
          onClick={loadMemories}
          disabled={loading}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '9px 16px',
            backgroundColor: 'var(--bg-card)',
            border: '1px solid var(--border)',
            borderRadius: '10px',
            color: 'var(--text-main)',
            fontSize: '13px',
            fontWeight: 500,
            cursor: 'pointer',
            boxShadow: 'var(--shadow-sm)'
          }}
        >
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Category Filter Pills */}
      {categories.length > 1 && (
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', alignItems: 'center' }}>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Filter size={12} /> Category:
          </span>
          {categories.map((cat) => {
            const active = selectedCategory === cat;
            return (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                style={{
                  padding: '5px 12px',
                  borderRadius: '20px',
                  border: active ? '1px solid var(--accent-primary)' : '1px solid var(--border)',
                  backgroundColor: active ? 'var(--accent-light)' : 'var(--bg-card)',
                  color: active ? 'var(--accent-primary)' : 'var(--text-secondary)',
                  fontSize: '12px',
                  fontWeight: active ? 600 : 500,
                  cursor: 'pointer',
                  textTransform: 'capitalize'
                }}
              >
                {cat}
              </button>
            );
          })}
        </div>
      )}

      {/* Memories Grid */}
      {filteredMemories.length > 0 ? (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
          gap: '16px'
        }}>
          {filteredMemories.map((mem) => {
            const badge = getCategoryBadge(mem.category || 'other');
            return (
              <div
                key={mem.id}
                style={{
                  padding: '18px 20px',
                  borderRadius: '14px',
                  backgroundColor: 'var(--bg-card)',
                  border: '1px solid var(--border)',
                  boxShadow: 'var(--shadow-sm)',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  gap: '12px',
                  transition: 'transform 0.15s ease'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <span style={{
                    fontSize: '11px',
                    padding: '3px 8px',
                    borderRadius: '6px',
                    backgroundColor: badge.bg,
                    color: badge.color,
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    letterSpacing: '0.4px'
                  }}>
                    {mem.category || 'General'}
                  </span>
                  <button
                    onClick={() => handleDelete(mem.id)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--text-muted)',
                      cursor: 'pointer',
                      padding: '4px',
                      borderRadius: '6px',
                      transition: 'color 0.15s ease'
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.color = 'var(--accent-red)')}
                    onMouseLeave={(e) => (e.currentTarget.style.color = 'var(--text-muted)')}
                    title="Delete memory item"
                  >
                    <Trash2 size={15} />
                  </button>
                </div>

                <p style={{
                  fontSize: '14px',
                  color: 'var(--text-main)',
                  lineHeight: '1.5',
                  fontWeight: 500
                }}>
                  "{mem.factText}"
                </p>

                <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                  Learned on {new Date(mem.createdAt).toLocaleDateString()}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div style={{
          padding: '48px 20px',
          textAlign: 'center',
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '12px'
        }}>
          <Brain size={32} color="var(--text-muted)" />
          <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-main)' }}>
            No memories stored yet
          </h3>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', maxWidth: '420px' }}>
            Chat with Origin about your preferences, working habits, or daily routine. The learning layer will automatically extract and index them here.
          </p>
        </div>
      )}
    </div>
  );
};
