import { useEffect, useState } from 'react';
import type React from 'react';
import { Calendar, CheckSquare, Bell, Clock, RefreshCw, CalendarDays } from 'lucide-react';
import type { DashboardData } from '../types';
import { fetchDashboard } from '../api';

export const DashboardView: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await fetchDashboard();
      setData(res);
    } catch (err) {
      console.error('Failed to load dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(() => {
      fetchDashboard().then(setData).catch(() => {});
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  const formatDateTime = (isoString?: string) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return isoString;
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
            Productivity & Schedule
          </h1>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
            Unified live overview of your commitments, tasks, alarms, and background jobs.
          </p>
        </div>
        <button
          onClick={loadData}
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
            boxShadow: 'var(--shadow-sm)',
            transition: 'all 0.15s ease'
          }}
        >
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          <span>Refresh</span>
        </button>
      </div>

      {/* KPI Stats Row */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
        gap: '16px'
      }}>
        <div style={{
          padding: '20px',
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Upcoming Events</span>
            <Calendar size={18} color="var(--accent-primary)" />
          </div>
          <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text-main)', marginTop: '10px' }}>
            {data?.upcomingEvents?.length || 0}
          </div>
        </div>

        <div style={{
          padding: '20px',
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Active Tasks</span>
            <CheckSquare size={18} color="var(--accent-sage)" />
          </div>
          <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text-main)', marginTop: '10px' }}>
            {data?.activeTasks?.length || 0}
          </div>
        </div>

        <div style={{
          padding: '20px',
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Pending Reminders</span>
            <Bell size={18} color="var(--accent-amber)" />
          </div>
          <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text-main)', marginTop: '10px' }}>
            {data?.activeReminders?.length || 0}
          </div>
        </div>

        <div style={{
          padding: '20px',
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-muted)' }}>
            <span style={{ fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Scheduled Jobs</span>
            <Clock size={18} color="var(--accent-lavender)" />
          </div>
          <div style={{ fontSize: '28px', fontWeight: 700, color: 'var(--text-main)', marginTop: '10px' }}>
            {data?.scheduledJobs?.length || 0}
          </div>
        </div>
      </div>

      {/* Main 2-Column Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))',
        gap: '20px'
      }}>
        {/* Calendar Events Card */}
        <div style={{
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          padding: '22px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <CalendarDays size={18} color="var(--accent-primary)" />
            <h2 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-main)' }}>
              Calendar Schedule
            </h2>
          </div>
          {data?.upcomingEvents && data.upcomingEvents.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {data.upcomingEvents.map((evt) => (
                <div
                  key={evt.id}
                  style={{
                    padding: '12px 14px',
                    borderRadius: '10px',
                    backgroundColor: 'var(--bg-app)',
                    border: '1px solid var(--border)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}
                >
                  <div>
                    <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-main)' }}>
                      {evt.title}
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '3px' }}>
                      {formatDateTime(evt.startTime)} — {formatDateTime(evt.endTime)}
                      {evt.location && ` · 📍 ${evt.location}`}
                    </div>
                  </div>
                  <span style={{
                    fontSize: '11px',
                    padding: '3px 8px',
                    borderRadius: '6px',
                    backgroundColor: 'var(--accent-light)',
                    color: 'var(--accent-primary)',
                    fontWeight: 600
                  }}>
                    Upcoming
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', fontStyle: 'italic', padding: '12px 0' }}>
              No upcoming calendar events scheduled.
            </p>
          )}
        </div>

        {/* Tasks Card */}
        <div style={{
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          padding: '22px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <CheckSquare size={18} color="var(--accent-sage)" />
            <h2 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-main)' }}>
              Todo & Tasks
            </h2>
          </div>
          {data?.activeTasks && data.activeTasks.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {data.activeTasks.map((task) => {
                const pStyle = getPriorityStyle(task.priority);
                return (
                  <div
                    key={task.id}
                    style={{
                      padding: '12px 14px',
                      borderRadius: '10px',
                      backgroundColor: 'var(--bg-app)',
                      border: '1px solid var(--border)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div>
                      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-main)' }}>
                        {task.title}
                      </div>
                      {task.description && (
                        <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                          {task.description}
                        </div>
                      )}
                    </div>
                    <div style={{ display: 'flex', gap: '6px' }}>
                      <span style={{
                        fontSize: '11px',
                        padding: '3px 8px',
                        borderRadius: '6px',
                        backgroundColor: pStyle.bg,
                        color: pStyle.color,
                        fontWeight: 600
                      }}>
                        {task.priority}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', fontStyle: 'italic', padding: '12px 0' }}>
              No active tasks found.
            </p>
          )}
        </div>

        {/* Reminders & Alarms */}
        <div style={{
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          padding: '22px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <Bell size={18} color="var(--accent-amber)" />
            <h2 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-main)' }}>
              Active Reminders & Alarms
            </h2>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {data?.activeReminders?.map((rem) => (
              <div
                key={rem.id}
                style={{
                  padding: '12px 14px',
                  borderRadius: '10px',
                  backgroundColor: 'var(--bg-app)',
                  border: '1px solid var(--border)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}
              >
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-main)' }}>
                    🔔 {rem.message}
                  </div>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '2px' }}>
                    Trigger: {formatDateTime(rem.remindAt)}
                  </div>
                </div>
                <span style={{
                  fontSize: '11px',
                  padding: '3px 8px',
                  borderRadius: '6px',
                  backgroundColor: 'var(--accent-amber-bg)',
                  color: 'var(--accent-amber)',
                  fontWeight: 600
                }}>
                  Pending
                </span>
              </div>
            ))}

            {data?.activeAlarms?.map((alarm) => (
              <div
                key={alarm.id}
                style={{
                  padding: '12px 14px',
                  borderRadius: '10px',
                  backgroundColor: 'var(--bg-app)',
                  border: '1px solid var(--border)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}
              >
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-main)' }}>
                    ⏰ {alarm.label}
                  </div>
                  <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '2px' }}>
                    Trigger: {formatDateTime(alarm.triggerAt)}
                  </div>
                </div>
                <span style={{
                  fontSize: '11px',
                  padding: '3px 8px',
                  borderRadius: '6px',
                  backgroundColor: 'var(--accent-red-bg)',
                  color: 'var(--accent-red)',
                  fontWeight: 600
                }}>
                  Armed
                </span>
              </div>
            ))}

            {(!data?.activeReminders?.length && !data?.activeAlarms?.length) && (
              <p style={{ fontSize: '13px', color: 'var(--text-muted)', fontStyle: 'italic', padding: '12px 0' }}>
                No pending reminders or armed alarms.
              </p>
            )}
          </div>
        </div>

        {/* Scheduled Jobs */}
        <div style={{
          backgroundColor: 'var(--bg-card)',
          borderRadius: '14px',
          border: '1px solid var(--border)',
          padding: '22px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <Clock size={18} color="var(--accent-lavender)" />
            <h2 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-main)' }}>
              Scheduled Cron Jobs
            </h2>
          </div>
          {data?.scheduledJobs && data.scheduledJobs.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {data.scheduledJobs.map((job) => (
                <div
                  key={job.id}
                  style={{
                    padding: '12px 14px',
                    borderRadius: '10px',
                    backgroundColor: 'var(--bg-app)',
                    border: '1px solid var(--border)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}
                >
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-main)' }}>
                      {job.jobName}
                    </div>
                    <code style={{
                      fontSize: '11px',
                      backgroundColor: 'var(--bg-subtle)',
                      padding: '2px 6px',
                      borderRadius: '4px',
                      color: 'var(--accent-primary)',
                      marginTop: '4px',
                      display: 'inline-block'
                    }}>
                      {job.cronExpression}
                    </code>
                  </div>
                  <span style={{
                    fontSize: '11px',
                    padding: '3px 8px',
                    borderRadius: '6px',
                    backgroundColor: 'var(--accent-lavender-bg)',
                    color: 'var(--accent-lavender)',
                    fontWeight: 600
                  }}>
                    {job.status}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', fontStyle: 'italic', padding: '12px 0' }}>
              No recurring cron jobs configured.
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
