import { useState } from 'react';
import type React from 'react';
import { UploadCloud, FileText, CheckCircle2, AlertCircle, HardDrive } from 'lucide-react';
import { uploadDocument } from '../api';
import type { UploadResult } from '../types';

export const DocumentsView: React.FC = () => {
  const [uploading, setUploading] = useState(false);
  const [results, setResults] = useState<UploadResult[]>([]);
  const [error, setError] = useState<string | null>(null);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    setError(null);

    try {
      const res = await uploadDocument(file);
      setResults((prev) => [res, ...prev]);
    } catch (err: any) {
      setError(err.message || 'Failed to upload document');
    } finally {
      setUploading(false);
      e.target.value = '';
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
      <div>
        <h1 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-main)', letterSpacing: '-0.3px' }}>
          Knowledge Base
        </h1>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
          Ingest personal notes, policies, or documentation for intelligent context retrieval during conversations.
        </p>
      </div>

      {/* Upload Drop Area */}
      <div style={{
        padding: '36px 24px',
        backgroundColor: 'var(--bg-card)',
        borderRadius: '14px',
        border: '2px dashed var(--border)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '12px',
        textAlign: 'center',
        boxShadow: 'var(--shadow-sm)'
      }}>
        <div style={{
          width: '48px',
          height: '48px',
          borderRadius: '12px',
          backgroundColor: 'var(--accent-light)',
          color: 'var(--accent-primary)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <UploadCloud size={24} />
        </div>
        <div>
          <h3 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-main)' }}>
            Upload text or markdown files
          </h3>
          <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '2px' }}>
            Supports .txt and .md files (indexed for smart search and conversation context)
          </p>
        </div>
        <label style={{
          padding: '8px 16px',
          borderRadius: '8px',
          backgroundColor: 'var(--accent-primary)',
          color: '#ffffff',
          fontSize: '13px',
          fontWeight: 600,
          cursor: uploading ? 'not-allowed' : 'pointer',
          marginTop: '6px',
          boxShadow: '0 4px 12px rgba(84, 87, 205, 0.25)',
          display: 'inline-block'
        }}>
          {uploading ? 'Indexing Document...' : 'Choose File to Add'}
          <input
            type="file"
            accept=".txt,.md"
            onChange={handleFileUpload}
            disabled={uploading}
            style={{ display: 'none' }}
          />
        </label>
      </div>

      {error && (
        <div style={{
          padding: '12px 16px',
          borderRadius: '10px',
          backgroundColor: 'var(--accent-red-bg)',
          color: 'var(--accent-red)',
          fontSize: '13px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Uploaded Documents List */}
      <div>
        <h2 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-main)', marginBottom: '14px' }}>
          Ingested Documents
        </h2>

        {results.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {results.map((r) => (
              <div
                key={r.documentId}
                style={{
                  padding: '14px 18px',
                  borderRadius: '12px',
                  backgroundColor: 'var(--bg-card)',
                  border: '1px solid var(--border)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  boxShadow: 'var(--shadow-sm)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <FileText size={20} color="var(--accent-primary)" />
                  <div>
                    <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-main)' }}>
                      {r.fileName}
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '2px' }}>
                      Indexed and ready for search across conversations
                    </div>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--accent-sage)', fontSize: '12px', fontWeight: 600 }}>
                  <CheckCircle2 size={16} />
                  <span>Indexed</span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div style={{
            padding: '24px',
            backgroundColor: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border)',
            color: 'var(--text-secondary)',
            fontSize: '13px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px'
          }}>
            <HardDrive size={18} color="var(--text-muted)" />
            <span>
              Place text or markdown files in your documents directory to index them automatically.
            </span>
          </div>
        )}
      </div>
    </div>
  );
};
