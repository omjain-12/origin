import { useState, useEffect, useCallback, useMemo } from 'react';
import type React from 'react';
import {
  Bell,
  Clock,
  CheckSquare,
  Square,
  Check,
  CalendarDays,
  ChevronDown,
  ChevronUp,
  PanelRightClose,
  PanelRightOpen,
  RefreshCw,
  Zap
} from 'lucide-react';
import type { DashboardData } from '../types';
import { fetchDashboard, dismissReminder, dismissAlarm, toggleTask } from '../api';

interface RightSidebarProps {
  isOpen: boolean;
  onToggle: () => void;
  refreshKey?: number;
}

type FilterType = 'all' | 'today' | 'urgent';

export const RightSidebar: React.FC<RightSidebarProps> = ({
  isOpen,
  onToggle,
  refreshKey = 0,
}) => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState<FilterType>('all');

  // Accordion open/close state for each dropdown
  const [sectionsOpen, setSectionsOpen] = useState({
    spotlight: true,
    reminders: true,
    alarms: true,
    tasks: true,
    events: false,
  });

  const toggleSection = (section: keyof typeof sectionsOpen) => {
    setSectionsOpen((prev) => ({ ...prev, [section]: !prev[section] }));
  };

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetchDashboard();
      setData(res);
    } catch (err) {
      console.warn('Could not load right sidebar schedule data', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 15000);
    return () => clearInterval(interval);
  }, [loadData, refreshKey]);

  // Relative time helper
  const getRelativeTime = (isoString?: string) => {
    if (!isoString) return '—';
    try {
      const target = new Date(isoString).getTime();
      const now = Date.now();
      const diffMs = target - now;
      const diffMin = Math.round(diffMs / 60000);

      if (diffMin < -60) {
        return 'Overdue';
      }
      if (diffMin < 0) {
        return 'Just now';
      }
      if (diffMin === 0) {
        return 'Now';
      }
      if (diffMin < 60) {
        return `in ${diffMin}m`;
      }
      const diffHrs = Math.floor(diffMin / 60);
      const remainingMin = diffMin % 60;
      if (diffHrs < 24) {
        return remainingMin > 0 ? `in ${diffHrs}h ${remainingMin}m` : `in ${diffHrs}h`;
      }
      const diffDays = Math.floor(diffHrs / 24);
      return `in ${diffDays}d`;
    } catch {
      return isoString;
    }
  };

  const formatShortTime = (isoString?: string) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
      return isoString;
    }
  };

  const formatShortDate = (isoString?: string) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
    } catch {
      return isoString;
    }
  };

  const isToday = (isoString?: string) => {
    if (!isoString) return false;
    try {
      const d = new Date(isoString);
      if (isNaN(d.getTime())) return false;
      const today = new Date();
      return (
        d.getFullYear() === today.getFullYear() &&
        d.getMonth() === today.getMonth() &&
        d.getDate() === today.getDate()
      );
    } catch {
      return false;
    }
  };

  // Find the single closest upcoming item respecting active filter
  const nextSpotlightItem = useMemo(() => {
    if (!data) return null;
    const now = Date.now();
    const candidates: Array<{
      type: 'alarm' | 'reminder' | 'event' | 'task';
      id: string;
      title: string;
      time: number;
      timeStr: string;
      badgeColor: string;
      badgeLabel: string;
    }> = [];

    // Alarms
    data.activeAlarms?.forEach((a) => {
      if (filter === 'today' && !isToday(a.triggerAt)) return;
      if (filter === 'urgent' && a.dismissed) return;
      const t = new Date(a.triggerAt).getTime();
      candidates.push({
        type: 'alarm',
        id: a.id,
        title: `Alarm: ${a.label}`,
        time: t,
        timeStr: a.triggerAt,
        badgeColor: 'var(--accent-red)',
        badgeLabel: 'Alarm',
      });
    });

    // Reminders
    data.activeReminders?.forEach((r) => {
      if (filter === 'today' && !isToday(r.remindAt)) return;
      if (filter === 'urgent' && r.fired) return;
      const t = new Date(r.remindAt).getTime();
      candidates.push({
        type: 'reminder',
        id: r.id,
        title: r.message,
        time: t,
        timeStr: r.remindAt,
        badgeColor: 'var(--accent-amber)',
        badgeLabel: 'Reminder',
      });
    });

    // Events
    data.upcomingEvents?.forEach((e) => {
      if (filter === 'today' && !isToday(e.startTime)) return;
      if (filter === 'urgent') return;
      const t = new Date(e.startTime).getTime();
      if (t >= now - 300000) { // from 5m ago onwards
        candidates.push({
          type: 'event',
          id: e.id,
          title: e.title,
          time: t,
          timeStr: e.startTime,
          badgeColor: 'var(--accent-primary)',
          badgeLabel: 'Calendar',
        });
      }
    });

    // Tasks (with dueDate)
    data.activeTasks?.forEach((task) => {
      if (!task.dueDate) return;
      if (task.status === 'COMPLETED') return;
      if (filter === 'today' && !isToday(task.dueDate)) return;
      if (filter === 'urgent' && task.priority !== 'URGENT' && task.priority !== 'HIGH') return;
      const t = new Date(task.dueDate).getTime();
      candidates.push({
        type: 'task',
        id: task.id,
        title: `Task: ${task.title}`,
        time: t,
        timeStr: task.dueDate,
        badgeColor: 'var(--accent-sage)',
        badgeLabel: 'Task',
      });
    });

    // Sort by ascending time
    candidates.sort((a, b) => a.time - b.time);
    return candidates.length > 0 ? candidates[0] : null;
  }, [data, filter]);

  // Handlers for optional actions
  const handleDismissReminder = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      setData((prev) =>
        prev
          ? {
              ...prev,
              activeReminders: prev.activeReminders.filter((r) => r.id !== id),
            }
          : prev
      );
      await dismissReminder(id);
    } catch (err) {
      console.warn('Could not dismiss reminder', err);
    }
  };

  const handleDismissAlarm = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      setData((prev) =>
        prev
          ? {
              ...prev,
              activeAlarms: prev.activeAlarms.filter((a) => a.id !== id),
            }
          : prev
      );
      await dismissAlarm(id);
    } catch (err) {
      console.warn('Could not dismiss alarm', err);
    }
  };

  const handleToggleTask = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      setData((prev) =>
        prev
          ? {
              ...prev,
              activeTasks: prev.activeTasks.map((t) =>
                t.id === id
                  ? {
                      ...t,
                      status: t.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED',
                    }
                  : t
              ),
            }
          : prev
      );
      await toggleTask(id);
    } catch (err) {
      console.warn('Could not toggle task', err);
    }
  };

  const getPriorityStyle = (priority: string) => {
    switch (priority) {
      case 'URGENT':
        return { color: 'var(--accent-red)', bg: 'var(--accent-red-bg)' };
      case 'HIGH':
        return { color: 'var(--accent-amber)', bg: 'var(--accent-amber-bg)' };
      case 'MEDIUM':
        return { color: 'var(--accent-periwinkle)', bg: 'var(--accent-periwinkle-bg)' };
      default:
        return { color: 'var(--accent-sage)', bg: 'var(--accent-sage-bg)' };
    }
  };

  // Filtered lists
  const filteredReminders = useMemo(() => {
    if (!data?.activeReminders) return [];
    if (filter === 'today') return data.activeReminders.filter((r) => isToday(r.remindAt));
    if (filter === 'urgent') return data.activeReminders.filter((r) => !r.fired);
    return data.activeReminders;
  }, [data?.activeReminders, filter]);

  const filteredAlarms = useMemo(() => {
    if (!data?.activeAlarms) return [];
    if (filter === 'today') return data.activeAlarms.filter((a) => isToday(a.triggerAt));
    return data.activeAlarms;
  }, [data?.activeAlarms, filter]);

  const filteredTasks = useMemo(() => {
    if (!data?.activeTasks) return [];
    if (filter === 'urgent')
      return data.activeTasks.filter((t) => t.priority === 'URGENT' || t.priority === 'HIGH');
    if (filter === 'today')
      return data.activeTasks.filter((t) => isToday(t.dueDate));
    return data.activeTasks;
  }, [data?.activeTasks, filter]);

  const filteredEvents = useMemo(() => {
    if (!data?.upcomingEvents) return [];
    if (filter === 'today') return data.upcomingEvents.filter((e) => isToday(e.startTime));
    return data.upcomingEvents;
  }, [data?.upcomingEvents, filter]);

  const currentTotalCount = useMemo(() => {
    if (filter === 'all') {
      return (
        (data?.activeAlarms?.length || 0) +
        (data?.activeReminders?.length || 0) +
        (data?.activeTasks?.length || 0)
      );
    }
    return (
      filteredReminders.length +
      filteredAlarms.length +
      filteredTasks.length +
      filteredEvents.length
    );
  }, [filter, data, filteredReminders.length, filteredAlarms.length, filteredTasks.length, filteredEvents.length]);

  // COLLAPSED RAIL VIEW (50px wide)
  if (!isOpen) {
    return (
      <aside
        style={{
          width: '50px',
          height: '100%',
          backgroundColor: 'var(--bg-sidebar)',
          borderLeft: '1px solid var(--border)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          padding: '16px 0',
          gap: '16px',
          flexShrink: 0,
          userSelect: 'none',
          transition: 'width 0.2s ease, background-color 0.2s ease',
          zIndex: 10,
        }}
      >
        <button
          onClick={onToggle}
          title="Expand Schedule & Alerts"
          style={{
            width: '34px',
            height: '34px',
            borderRadius: '8px',
            border: '1px solid var(--border)',
            backgroundColor: 'var(--bg-card)',
            color: 'var(--text-main)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            transition: 'all 0.15s ease',
            boxShadow: 'var(--shadow-sm)',
          }}
        >
          <PanelRightOpen size={16} />
        </button>

        <div style={{ width: '24px', height: '1px', backgroundColor: 'var(--border)' }} />

        {/* Vertical Icon Rail with Badges */}
        {nextSpotlightItem && (
          <button
            onClick={() => {
              onToggle();
              setSectionsOpen((prev) => ({ ...prev, spotlight: true }));
            }}
            title={`Upcoming Next: ${nextSpotlightItem.title}`}
            style={{
              width: '34px',
              height: '34px',
              borderRadius: '8px',
              border: 'none',
              backgroundColor: 'var(--accent-light)',
              color: 'var(--accent-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              position: 'relative',
            }}
          >
            <Zap size={16} />
            <span
              style={{
                position: 'absolute',
                top: '-2px',
                right: '-2px',
                width: '8px',
                height: '8px',
                borderRadius: '50%',
                backgroundColor: 'var(--accent-red)',
                boxShadow: '0 0 4px var(--accent-red)',
              }}
            />
          </button>
        )}

        {/* Reminders Icon */}
        <button
          onClick={() => {
            onToggle();
            setSectionsOpen((prev) => ({ ...prev, reminders: true }));
          }}
          title={`Reminders (${data?.activeReminders?.length || 0})`}
          style={{
            width: '34px',
            height: '34px',
            borderRadius: '8px',
            border: 'none',
            backgroundColor: 'transparent',
            color: 'var(--text-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            position: 'relative',
          }}
        >
          <Bell size={16} />
          {(data?.activeReminders?.length || 0) > 0 && (
            <span
              style={{
                position: 'absolute',
                top: '2px',
                right: '2px',
                fontSize: '9px',
                fontWeight: 700,
                backgroundColor: 'var(--accent-amber)',
                color: '#fff',
                padding: '1px 4px',
                borderRadius: '10px',
                lineHeight: 1,
              }}
            >
              {data!.activeReminders.length}
            </span>
          )}
        </button>

        {/* Alarms Icon */}
        <button
          onClick={() => {
            onToggle();
            setSectionsOpen((prev) => ({ ...prev, alarms: true }));
          }}
          title={`Alarms (${data?.activeAlarms?.length || 0})`}
          style={{
            width: '34px',
            height: '34px',
            borderRadius: '8px',
            border: 'none',
            backgroundColor: 'transparent',
            color: 'var(--text-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            position: 'relative',
          }}
        >
          <Clock size={16} />
          {(data?.activeAlarms?.length || 0) > 0 && (
            <span
              style={{
                position: 'absolute',
                top: '2px',
                right: '2px',
                fontSize: '9px',
                fontWeight: 700,
                backgroundColor: 'var(--accent-red)',
                color: '#fff',
                padding: '1px 4px',
                borderRadius: '10px',
                lineHeight: 1,
              }}
            >
              {data!.activeAlarms.length}
            </span>
          )}
        </button>

        {/* Tasks Icon */}
        <button
          onClick={() => {
            onToggle();
            setSectionsOpen((prev) => ({ ...prev, tasks: true }));
          }}
          title={`Tasks (${data?.activeTasks?.length || 0})`}
          style={{
            width: '34px',
            height: '34px',
            borderRadius: '8px',
            border: 'none',
            backgroundColor: 'transparent',
            color: 'var(--text-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            position: 'relative',
          }}
        >
          <CheckSquare size={16} />
          {(data?.activeTasks?.length || 0) > 0 && (
            <span
              style={{
                position: 'absolute',
                top: '2px',
                right: '2px',
                fontSize: '9px',
                fontWeight: 700,
                backgroundColor: 'var(--accent-sage)',
                color: '#fff',
                padding: '1px 4px',
                borderRadius: '10px',
                lineHeight: 1,
              }}
            >
              {data!.activeTasks.length}
            </span>
          )}
        </button>

        {/* Calendar Icon */}
        <button
          onClick={() => {
            onToggle();
            setSectionsOpen((prev) => ({ ...prev, events: true }));
          }}
          title={`Calendar Events (${data?.upcomingEvents?.length || 0})`}
          style={{
            width: '34px',
            height: '34px',
            borderRadius: '8px',
            border: 'none',
            backgroundColor: 'transparent',
            color: 'var(--text-secondary)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            position: 'relative',
          }}
        >
          <CalendarDays size={16} />
          {(data?.upcomingEvents?.length || 0) > 0 && (
            <span
              style={{
                position: 'absolute',
                top: '2px',
                right: '2px',
                fontSize: '9px',
                fontWeight: 700,
                backgroundColor: 'var(--accent-primary)',
                color: '#fff',
                padding: '1px 4px',
                borderRadius: '10px',
                lineHeight: 1,
              }}
            >
              {data!.upcomingEvents.length}
            </span>
          )}
        </button>
      </aside>
    );
  }

  // EXPANDED VIEW (~320px wide)
  return (
    <aside
      style={{
        width: '320px',
        height: '100%',
        backgroundColor: 'var(--bg-sidebar)',
        borderLeft: '1px solid var(--border)',
        display: 'flex',
        flexDirection: 'column',
        flexShrink: 0,
        boxSizing: 'border-box',
        overflow: 'hidden',
        userSelect: 'none',
        transition: 'width 0.2s ease, background-color 0.2s ease',
        zIndex: 10,
      }}
    >
      {/* Top Header */}
      <div
        style={{
          padding: '16px 18px',
          borderBottom: '1px solid var(--border)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          backgroundColor: 'var(--bg-card)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <h3
            style={{
              fontSize: '14px',
              fontWeight: 700,
              color: 'var(--text-main)',
              letterSpacing: '-0.2px',
            }}
          >
            Schedule & Alerts
          </h3>
          {currentTotalCount > 0 && (
            <span
              style={{
                fontSize: '11px',
                fontWeight: 700,
                backgroundColor: 'var(--accent-light)',
                color: 'var(--accent-primary)',
                padding: '2px 7px',
                borderRadius: '10px',
              }}
            >
              {currentTotalCount}
            </span>
          )}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <button
            onClick={loadData}
            disabled={loading}
            title="Refresh schedule"
            style={{
              width: '28px',
              height: '28px',
              borderRadius: '6px',
              border: 'none',
              backgroundColor: 'transparent',
              color: 'var(--text-muted)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              transition: 'all 0.15s ease',
            }}
          >
            <RefreshCw size={13} className={loading ? 'animate-spin' : ''} />
          </button>
          <button
            onClick={onToggle}
            title="Collapse sidebar"
            style={{
              width: '28px',
              height: '28px',
              borderRadius: '6px',
              border: '1px solid var(--border)',
              backgroundColor: 'var(--bg-app)',
              color: 'var(--text-secondary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              transition: 'all 0.15s ease',
            }}
          >
            <PanelRightClose size={14} />
          </button>
        </div>
      </div>

      {/* Filter Tabs Row */}
      <div
        style={{
          display: 'flex',
          gap: '6px',
          padding: '10px 18px',
          borderBottom: '1px solid var(--border)',
          backgroundColor: 'var(--bg-app)',
        }}
      >
        {(['all', 'today', 'urgent'] as FilterType[]).map((f) => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            style={{
              flex: 1,
              padding: '5px 0',
              fontSize: '11px',
              fontWeight: filter === f ? 600 : 500,
              borderRadius: '6px',
              border: '1px solid',
              borderColor: filter === f ? 'var(--accent-primary)' : 'var(--border)',
              backgroundColor: filter === f ? 'var(--accent-light)' : 'var(--bg-card)',
              color: filter === f ? 'var(--accent-primary)' : 'var(--text-secondary)',
              cursor: 'pointer',
              textTransform: 'capitalize',
              transition: 'all 0.15s ease',
            }}
          >
            {f}
          </button>
        ))}
      </div>

      {/* Scrollable Content Container */}
      <div
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '14px 16px',
          display: 'flex',
          flexDirection: 'column',
          gap: '12px',
        }}
      >
        {/* Empty State for Today if no items */}
        {filter === 'today' && currentTotalCount === 0 && (
          <div
            style={{
              padding: '18px 14px',
              backgroundColor: 'var(--bg-card)',
              borderRadius: '12px',
              border: '1px solid var(--border)',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '6px',
            }}
          >
            <span style={{ fontSize: '20px' }}>☀️</span>
            <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-main)' }}>
              All Clear for Today
            </span>
            <span style={{ fontSize: '11px', color: 'var(--text-muted)', lineHeight: 1.4 }}>
              No reminders, alarms, or tasks scheduled for today. Switch to &quot;All&quot; to see upcoming items.
            </span>
          </div>
        )}

        {/* SPOTLIGHT HERO CARD */}
        {nextSpotlightItem && (
          <div
            style={{
              padding: '12px 14px',
              backgroundColor: 'var(--bg-card)',
              borderRadius: '12px',
              border: '1px solid var(--accent-primary)',
              boxShadow: 'var(--shadow-sm)',
              position: 'relative',
              overflow: 'hidden',
            }}
          >
            <div
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '4px',
                height: '100%',
                backgroundColor: nextSpotlightItem.badgeColor,
              }}
            />
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '6px',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Zap size={13} color={nextSpotlightItem.badgeColor} />
                <span
                  style={{
                    fontSize: '10px',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    color: nextSpotlightItem.badgeColor,
                    letterSpacing: '0.4px',
                  }}
                >
                  Happening Next
                </span>
              </div>
              <span
                style={{
                  fontSize: '10px',
                  fontWeight: 600,
                  backgroundColor: 'var(--bg-subtle)',
                  padding: '2px 6px',
                  borderRadius: '4px',
                  color: 'var(--text-main)',
                }}
              >
                {getRelativeTime(nextSpotlightItem.timeStr)}
              </span>
            </div>

            <div
              style={{
                fontSize: '13px',
                fontWeight: 600,
                color: 'var(--text-main)',
                lineHeight: 1.3,
                wordBreak: 'break-word',
              }}
            >
              {nextSpotlightItem.title}
            </div>

            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginTop: '8px',
                paddingTop: '6px',
                borderTop: '1px solid var(--border-subtle)',
              }}
            >
              <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                {formatShortTime(nextSpotlightItem.timeStr)} · {formatShortDate(nextSpotlightItem.timeStr)}
              </span>
              {nextSpotlightItem.type === 'reminder' && (
                <button
                  onClick={(e) => handleDismissReminder(nextSpotlightItem.id, e)}
                  style={{
                    fontSize: '11px',
                    padding: '2px 8px',
                    borderRadius: '5px',
                    border: '1px solid var(--border)',
                    backgroundColor: 'var(--bg-subtle)',
                    color: 'var(--text-main)',
                    cursor: 'pointer',
                    fontWeight: 500,
                  }}
                >
                  Dismiss
                </button>
              )}
              {nextSpotlightItem.type === 'alarm' && (
                <button
                  onClick={(e) => handleDismissAlarm(nextSpotlightItem.id, e)}
                  style={{
                    fontSize: '11px',
                    padding: '2px 8px',
                    borderRadius: '5px',
                    border: '1px solid var(--border)',
                    backgroundColor: 'var(--bg-subtle)',
                    color: 'var(--text-main)',
                    cursor: 'pointer',
                    fontWeight: 500,
                  }}
                >
                  Dismiss
                </button>
              )}
            </div>
          </div>
        )}

        {/* SECTION 1: REMINDERS & NOTIFICATIONS */}
        <div
          style={{
            backgroundColor: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border)',
            overflow: 'hidden',
          }}
        >
          <button
            onClick={() => toggleSection('reminders')}
            style={{
              width: '100%',
              padding: '10px 14px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer',
              textAlign: 'left',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Bell size={14} color="var(--accent-amber)" />
              <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-main)' }}>
                Reminders
              </span>
              <span
                style={{
                  fontSize: '10px',
                  fontWeight: 600,
                  backgroundColor: 'var(--accent-amber-bg)',
                  color: 'var(--accent-amber)',
                  padding: '1px 6px',
                  borderRadius: '8px',
                }}
              >
                {filteredReminders.length}
              </span>
            </div>
            {sectionsOpen.reminders ? (
              <ChevronUp size={14} color="var(--text-muted)" />
            ) : (
              <ChevronDown size={14} color="var(--text-muted)" />
            )}
          </button>

          {sectionsOpen.reminders && (
            <div
              style={{
                padding: '0 12px 10px',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
              }}
            >
              {filteredReminders.length > 0 ? (
                filteredReminders.map((rem) => (
                  <div
                    key={rem.id}
                    style={{
                      padding: '8px 10px',
                      borderRadius: '8px',
                      backgroundColor: 'var(--bg-app)',
                      border: '1px solid var(--border)',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '4px',
                    }}
                  >
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'flex-start',
                        gap: '8px',
                      }}
                    >
                      <span
                        style={{
                          fontSize: '12px',
                          fontWeight: 500,
                          color: 'var(--text-main)',
                          lineHeight: 1.3,
                        }}
                      >
                        {rem.message}
                      </span>
                      <button
                        onClick={(e) => handleDismissReminder(rem.id, e)}
                        title="Dismiss reminder"
                        style={{
                          border: 'none',
                          backgroundColor: 'transparent',
                          color: 'var(--text-muted)',
                          cursor: 'pointer',
                          padding: '2px',
                          borderRadius: '4px',
                          flexShrink: 0,
                        }}
                      >
                        <Check size={13} />
                      </button>
                    </div>
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        fontSize: '10px',
                        color: 'var(--text-muted)',
                      }}
                    >
                      <span>{formatShortTime(rem.remindAt)} · {formatShortDate(rem.remindAt)}</span>
                      <span
                        style={{
                          color: 'var(--accent-amber)',
                          fontWeight: 600,
                        }}
                      >
                        {getRelativeTime(rem.remindAt)}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <p
                  style={{
                    fontSize: '11px',
                    color: 'var(--text-muted)',
                    fontStyle: 'italic',
                    padding: '6px 0',
                    margin: 0,
                  }}
                >
                  No pending reminders.
                </p>
              )}
            </div>
          )}
        </div>

        {/* SECTION 2: ALARMS */}
        <div
          style={{
            backgroundColor: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border)',
            overflow: 'hidden',
          }}
        >
          <button
            onClick={() => toggleSection('alarms')}
            style={{
              width: '100%',
              padding: '10px 14px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer',
              textAlign: 'left',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Clock size={14} color="var(--accent-red)" />
              <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-main)' }}>
                Alarms
              </span>
              <span
                style={{
                  fontSize: '10px',
                  fontWeight: 600,
                  backgroundColor: 'var(--accent-red-bg)',
                  color: 'var(--accent-red)',
                  padding: '1px 6px',
                  borderRadius: '8px',
                }}
              >
                {filteredAlarms.length}
              </span>
            </div>
            {sectionsOpen.alarms ? (
              <ChevronUp size={14} color="var(--text-muted)" />
            ) : (
              <ChevronDown size={14} color="var(--text-muted)" />
            )}
          </button>

          {sectionsOpen.alarms && (
            <div
              style={{
                padding: '0 12px 10px',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
              }}
            >
              {filteredAlarms.length > 0 ? (
                filteredAlarms.map((alarm) => (
                  <div
                    key={alarm.id}
                    style={{
                      padding: '8px 10px',
                      borderRadius: '8px',
                      backgroundColor: 'var(--bg-app)',
                      border: '1px solid var(--border)',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '4px',
                    }}
                  >
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'flex-start',
                        gap: '8px',
                      }}
                    >
                      <span
                        style={{
                          fontSize: '12px',
                          fontWeight: 500,
                          color: 'var(--text-main)',
                          lineHeight: 1.3,
                        }}
                      >
                        {alarm.label}
                      </span>
                      <button
                        onClick={(e) => handleDismissAlarm(alarm.id, e)}
                        title="Dismiss alarm"
                        style={{
                          border: 'none',
                          backgroundColor: 'transparent',
                          color: 'var(--text-muted)',
                          cursor: 'pointer',
                          padding: '2px',
                          borderRadius: '4px',
                          flexShrink: 0,
                        }}
                      >
                        <Check size={13} />
                      </button>
                    </div>
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        fontSize: '10px',
                        color: 'var(--text-muted)',
                      }}
                    >
                      <span>{formatShortTime(alarm.triggerAt)} · {formatShortDate(alarm.triggerAt)}</span>
                      <span
                        style={{
                          color: 'var(--accent-red)',
                          fontWeight: 600,
                        }}
                      >
                        {getRelativeTime(alarm.triggerAt)}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <p
                  style={{
                    fontSize: '11px',
                    color: 'var(--text-muted)',
                    fontStyle: 'italic',
                    padding: '6px 0',
                    margin: 0,
                  }}
                >
                  No active alarms.
                </p>
              )}
            </div>
          )}
        </div>

        {/* SECTION 3: TASKS & TODOS */}
        <div
          style={{
            backgroundColor: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border)',
            overflow: 'hidden',
          }}
        >
          <button
            onClick={() => toggleSection('tasks')}
            style={{
              width: '100%',
              padding: '10px 14px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer',
              textAlign: 'left',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CheckSquare size={14} color="var(--accent-sage)" />
              <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-main)' }}>
                Tasks
              </span>
              <span
                style={{
                  fontSize: '10px',
                  fontWeight: 600,
                  backgroundColor: 'var(--accent-sage-bg)',
                  color: 'var(--accent-sage)',
                  padding: '1px 6px',
                  borderRadius: '8px',
                }}
              >
                {filteredTasks.length}
              </span>
            </div>
            {sectionsOpen.tasks ? (
              <ChevronUp size={14} color="var(--text-muted)" />
            ) : (
              <ChevronDown size={14} color="var(--text-muted)" />
            )}
          </button>

          {sectionsOpen.tasks && (
            <div
              style={{
                padding: '0 12px 10px',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
              }}
            >
              {filteredTasks.length > 0 ? (
                filteredTasks.map((task) => {
                  const pStyle = getPriorityStyle(task.priority);
                  const isDone = task.status === 'COMPLETED';
                  return (
                    <div
                      key={task.id}
                      style={{
                        padding: '8px 10px',
                        borderRadius: '8px',
                        backgroundColor: 'var(--bg-app)',
                        border: '1px solid var(--border)',
                        display: 'flex',
                        alignItems: 'flex-start',
                        gap: '8px',
                      }}
                    >
                      <button
                        onClick={(e) => handleToggleTask(task.id, e)}
                        title={isDone ? 'Mark as pending' : 'Mark as completed'}
                        style={{
                          border: 'none',
                          backgroundColor: 'transparent',
                          color: isDone ? 'var(--accent-sage)' : 'var(--text-muted)',
                          cursor: 'pointer',
                          padding: '0',
                          marginTop: '2px',
                          display: 'flex',
                          alignItems: 'center',
                        }}
                      >
                        {isDone ? <CheckSquare size={14} /> : <Square size={14} />}
                      </button>

                      <div style={{ flex: 1, minWidth: 0 }}>
                        <div
                          style={{
                            fontSize: '12px',
                            fontWeight: 500,
                            color: isDone ? 'var(--text-muted)' : 'var(--text-main)',
                            textDecoration: isDone ? 'line-through' : 'none',
                            lineHeight: 1.3,
                            wordBreak: 'break-word',
                          }}
                        >
                          {task.title}
                        </div>
                        <div
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px',
                            marginTop: '4px',
                          }}
                        >
                          <span
                            style={{
                              fontSize: '9px',
                              fontWeight: 700,
                              backgroundColor: pStyle.bg,
                              color: pStyle.color,
                              padding: '1px 5px',
                              borderRadius: '4px',
                            }}
                          >
                            {task.priority}
                          </span>
                          {task.dueDate && (
                            <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                              Due {formatShortDate(task.dueDate)}
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })
              ) : (
                <p
                  style={{
                    fontSize: '11px',
                    color: 'var(--text-muted)',
                    fontStyle: 'italic',
                    padding: '6px 0',
                    margin: 0,
                  }}
                >
                  No active tasks.
                </p>
              )}
            </div>
          )}
        </div>

        {/* SECTION 4: CALENDAR EVENTS */}
        <div
          style={{
            backgroundColor: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border)',
            overflow: 'hidden',
          }}
        >
          <button
            onClick={() => toggleSection('events')}
            style={{
              width: '100%',
              padding: '10px 14px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer',
              textAlign: 'left',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CalendarDays size={14} color="var(--accent-primary)" />
              <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-main)' }}>
                Calendar
              </span>
              <span
                style={{
                  fontSize: '10px',
                  fontWeight: 600,
                  backgroundColor: 'var(--accent-light)',
                  color: 'var(--accent-primary)',
                  padding: '1px 6px',
                  borderRadius: '8px',
                }}
              >
                {filteredEvents.length}
              </span>
            </div>
            {sectionsOpen.events ? (
              <ChevronUp size={14} color="var(--text-muted)" />
            ) : (
              <ChevronDown size={14} color="var(--text-muted)" />
            )}
          </button>

          {sectionsOpen.events && (
            <div
              style={{
                padding: '0 12px 10px',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
              }}
            >
              {filteredEvents.length > 0 ? (
                filteredEvents.map((evt) => (
                  <div
                    key={evt.id}
                    style={{
                      padding: '8px 10px',
                      borderRadius: '8px',
                      backgroundColor: 'var(--bg-app)',
                      border: '1px solid var(--border)',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '4px',
                    }}
                  >
                    <span
                      style={{
                        fontSize: '12px',
                        fontWeight: 600,
                        color: 'var(--text-main)',
                        lineHeight: 1.3,
                      }}
                    >
                      {evt.title}
                    </span>
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        fontSize: '10px',
                        color: 'var(--text-muted)',
                      }}
                    >
                      <span>
                        {formatShortTime(evt.startTime)} - {formatShortTime(evt.endTime)}
                      </span>
                      <span>{formatShortDate(evt.startTime)}</span>
                    </div>
                  </div>
                ))
              ) : (
                <p
                  style={{
                    fontSize: '11px',
                    color: 'var(--text-muted)',
                    fontStyle: 'italic',
                    padding: '6px 0',
                    margin: 0,
                  }}
                >
                  No calendar events.
                </p>
              )}
            </div>
          )}
        </div>
      </div>
    </aside>
  );
};
