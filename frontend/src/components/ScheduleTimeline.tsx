import type { RegimenScheduleResponse } from '../types/api'
import './ScheduleTimeline.css'

interface Props {
  schedule: RegimenScheduleResponse
}

export default function ScheduleTimeline({ schedule }: Props) {
  return (
    <div className="schedule-wrap">
      <table className="schedule-table">
        <thead>
          <tr>
            <th>Supplement</th>
            {schedule.windows.map(window => (
              <th key={window}>{window}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {schedule.rows.map(row => (
            <tr key={row.regimenItemId}>
              <td className="supp-cell">
                <strong>{row.supplementCode}</strong>
                <div className="supp-sub">{row.supplementName}</div>
              </td>
              {row.assignments.map(cell => (
                <td key={cell.window} className={cell.assigned ? 'assigned' : 'empty'}>
                  {cell.assigned ? cell.label : '—'}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      <div className="notes-panel">
        <h3>Optimization Notes</h3>
        {schedule.optimizationNotes.length === 0 ? (
          <p className="muted">No optimization notes available yet.</p>
        ) : (
          <ul>
            {schedule.optimizationNotes.map(note => (
              <li key={note}>{note}</li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
