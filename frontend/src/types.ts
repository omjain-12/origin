export interface ChatSession {
  sessionId: string;
  title: string;
}

export interface ChatMessageItem {
  id: string;
  role: 'user' | 'assistant' | 'system' | 'tool';
  content: string;
  timestamp: string;
}

export interface CalendarEvent {
  id: string;
  userId: string;
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  location?: string;
  createdAt: string;
}

export interface TaskItem {
  id: string;
  userId: string;
  title: string;
  description?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReminderItem {
  id: string;
  userId: string;
  message: string;
  remindAt: string;
  fired: boolean;
  createdAt: string;
}

export interface AlarmItem {
  id: string;
  userId: string;
  label: string;
  triggerAt: string;
  dismissed: boolean;
  createdAt: string;
}

export interface ScheduledJobItem {
  id: string;
  userId: string;
  jobName: string;
  description?: string;
  cronExpression: string;
  status: 'ACTIVE' | 'PAUSED' | 'CANCELLED';
  lastRunAt?: string;
  nextRunAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardData {
  upcomingEvents: CalendarEvent[];
  activeTasks: TaskItem[];
  activeReminders: ReminderItem[];
  activeAlarms: AlarmItem[];
  scheduledJobs: ScheduledJobItem[];
}

export interface UserMemory {
  id: string;
  userId: string;
  factText: string;
  category: string;
  createdAt: string;
  updatedAt: string;
}

export interface UploadResult {
  documentId: string;
  fileName: string;
  status: string;
  chunks: number;
}

