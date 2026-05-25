import { logIntake } from '../api/intake'
import type { RegimenTodayResponse, TodayDoseResponse } from '../types/api'
import './TodayChecklist.css'

interface Props {
  today: RegimenTodayResponse
  setToday: React.Dispatch<React.SetStateAction<RegimenTodayResponse | null>>
}

function adherenceClass(status: string | null) {
  if (!status) return 'status-none'
  if (status === 'ON_TIME') return 'status-on-time'
  if (status === 'LATE' || status === 'EARLY') return 'status-late'
  return 'status-none'
}

export default function TodayChecklist({ today, setToday }: Props) {
  async function markTaken(dose: TodayDoseResponse) {
    setToday(prev => {
      if (!prev) return prev
      return {
        ...prev,
        doses: prev.doses.map(d =>
          d.regimenItemId === dose.regimenItemId
            ? { ...d, taken: true }
            : d,
        ),
      }
    })

    try {
      await logIntake({
        patientId: today.patientId,
        regimenItemId: dose.regimenItemId,
        takenDateTime: new Date().toISOString(),
        quantity: 1,
      })
    } catch (e) {
      setToday(prev => {
        if (!prev) return prev
        return {
          ...prev,
          doses: prev.doses.map(d =>
            d.regimenItemId === dose.regimenItemId
              ? { ...d, taken: false }
              : d,
          ),
        }
      })
      throw e
    }
  }

  return (
    <div className="today-panel">
      <h3>Today’s Supplements</h3>
      <div className="today-list">
        {today.doses.map(dose => (
          <div key={dose.regimenItemId} className="today-item">
            <label className="today-item-main">
              <input
                type="checkbox"
                checked={dose.taken}
                disabled={dose.taken}
                onChange={() => markTaken(dose)}
              />
              <div>
                <div className="today-title">{dose.supplementCode}</div>
                <div className="today-meta">{dose.doseLabel} · {dose.window}</div>
              </div>
            </label>

            <span className={`adherence-pill ${adherenceClass(dose.adherenceStatus)}`}>
              {dose.adherenceStatus ?? 'Pending'}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}
