import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useRegimens } from '../hooks/useRegimens'
import { useRegimenSchedule } from '../hooks/useRegimenSchedule'
import { useRegimenToday } from '../hooks/useRegimenToday'
import ScheduleTimeline from '../components/ScheduleTimeline'
import TodayChecklist from '../components/TodayChecklist'
import './RegimensPage.css'

export default function RegimensPage() {
  const { regimens, loading, error, refresh } = useRegimens()
  const [selectedRegimenId, setSelectedRegimenId] = useState<number | null>(null)
  const { schedule, loading: scheduleLoading, error: scheduleError } = useRegimenSchedule(selectedRegimenId)
  const { today, setToday, loading: todayLoading, error: todayError } = useRegimenToday(selectedRegimenId)

  function toggleRegimen(regimenId: number) {
    setSelectedRegimenId(prev => prev === regimenId ? null : regimenId)
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Regimens</h1>
        <button className="btn-secondary" onClick={refresh} disabled={loading}>
          {loading ? 'Refreshing…' : '↺ Refresh'}
        </button>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {!loading && regimens.length === 0 && (
        <div className="empty-state">
          <p>No regimens yet.</p>
          <Link to="/regimens/new" className="btn-primary">Create your first regimen →</Link>
        </div>
      )}

      <div className="regimen-list">
        {regimens.map(r => (
          <div key={r.regimenId} className="regimen-card">
            <div className="regimen-card-header">
              <div>
                <h2 className="regimen-name">{r.name}</h2>
                <p className="regimen-meta">
                  Patient: <code>{r.patientId}</code> ·
                  ID: <code>{r.regimenId}</code> ·
                  Status: <span className={`status-badge status-${r.status.toLowerCase()}`}>{r.status}</span>
                </p>
              </div>
              <div className="regimen-actions">
                <time className="regimen-date">{new Date(r.createdAt).toLocaleDateString()}</time>
                <button className="btn-secondary" onClick={() => toggleRegimen(r.regimenId)}>
                  {selectedRegimenId === r.regimenId ? 'Hide Details' : 'View Details'}
                </button>
              </div>
            </div>

            <table className="items-table">
              <thead>
                <tr>
                  <th>Supplement</th>
                  <th>Dose</th>
                  <th>Frequency</th>
                  <th>Window</th>
                </tr>
              </thead>
              <tbody>
                {r.items.map(item => (
                  <tr key={item.itemId}>
                    <td>{item.supplementCode}</td>
                    <td>{item.doseQty} {item.doseUnit}</td>
                    <td>{item.frequencyPerDay}×/day</td>
                    <td>{item.scheduleWindow ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            {selectedRegimenId === r.regimenId && (
              <div className="details-panel">
                <section className="subpanel">
                  {todayLoading && <p className="muted">Loading today’s checklist…</p>}
                  {todayError && <div className="error-banner">{todayError}</div>}
                  {today && <TodayChecklist today={today} setToday={setToday} />}
                </section>

                <section className="subpanel">
                  {scheduleLoading && <p className="muted">Loading schedule…</p>}
                  {scheduleError && <div className="error-banner">{scheduleError}</div>}
                  {schedule && <ScheduleTimeline schedule={schedule} />}
                </section>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
