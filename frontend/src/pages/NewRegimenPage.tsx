import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSupplements } from '../hooks/useSupplements'
import SupplementItemRow, { type ItemFormState } from '../components/SupplementItemRow'
import { createRegimen } from '../api/regimens'
import type { RegimenItemRequest } from '../types/api'
import './NewRegimenPage.css'
import '../components/SupplementItemRow.css'

const emptyItem = (): ItemFormState => ({
  supplementId: 0,
  doseQty: '',
  doseUnit: '',
  frequencyPerDay: '1',
  scheduleWindow: '',
})

export default function NewRegimenPage() {
  const navigate = useNavigate()
  const { supplements, loading: suppLoading } = useSupplements()

  const [patientId, setPatientId] = useState('')
  const [name, setName]           = useState('')
  const [items, setItems]         = useState<ItemFormState[]>([emptyItem()])
  const [submitting, setSubmitting] = useState(false)
  const [error, setError]           = useState<string | null>(null)
  const [success, setSuccess]       = useState<{ regimenId: number; name: string } | null>(null)

  function updateItem(index: number, field: keyof ItemFormState, value: string | number) {
    setItems(prev => prev.map((item, i) =>
      i === index ? { ...item, [field]: value } : item
    ))
  }

  function addItem() {
    setItems(prev => [...prev, emptyItem()])
  }

  function removeItem(index: number) {
    setItems(prev => prev.filter((_, i) => i !== index))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const sup = supplements
      const requestItems: RegimenItemRequest[] = items.map(item => {
        const s = sup.find(s => s.id === Number(item.supplementId))
        return {
          supplementId:    Number(item.supplementId),
          supplementCode:  s?.code ?? String(item.supplementId),
          doseQty:         Number(item.doseQty),
          doseUnit:        item.doseUnit,
          frequencyPerDay: Number(item.frequencyPerDay),
          scheduleWindow:  item.scheduleWindow || null,
        }
      })
      const response = await createRegimen({ patientId, name, items: requestItems })
      setSuccess({ regimenId: response.regimenId, name: response.name })
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Unexpected error')
    } finally {
      setSubmitting(false)
    }
  }

  if (success) {
    return (
      <div className="page">
        <div className="success-banner">
          <span className="success-icon">✓</span>
          <div>
            <strong>Regimen created!</strong>
            <p>ID: <code>{success.regimenId}</code> · {success.name}</p>
          </div>
          <div className="success-actions">
            <button className="btn-primary" onClick={() => navigate('/')}>View All Regimens</button>
            <button className="btn-secondary" onClick={() => { setSuccess(null); setPatientId(''); setName(''); setItems([emptyItem()]) }}>
              Create Another
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="page">
      <h1 className="page-title">New Regimen</h1>

      {error && <div className="error-banner">{error}</div>}

      <form onSubmit={handleSubmit} className="form">
        <div className="form-section">
          <label className="label">Patient ID
            <input
              className="input"
              type="text"
              value={patientId}
              onChange={e => setPatientId(e.target.value)}
              placeholder="e.g. patient-001"
              required
            />
          </label>

          <label className="label">Regimen Name
            <input
              className="input"
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="e.g. Morning Wellness Stack"
              required
            />
          </label>
        </div>

        <div className="form-section">
          <div className="section-header">
            <h2 className="section-title">Supplements</h2>
            <button type="button" className="btn-secondary" onClick={addItem}>
              + Add supplement
            </button>
          </div>

          {suppLoading ? (
            <p className="muted">Loading supplements…</p>
          ) : (
            <div className="items-list">
              {items.map((item, i) => (
                <SupplementItemRow
                  key={i}
                  index={i}
                  item={item}
                  supplements={supplements}
                  onChange={updateItem}
                  onRemove={removeItem}
                />
              ))}
            </div>
          )}
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={submitting || items.length === 0}>
            {submitting ? 'Creating…' : 'Create Regimen'}
          </button>
        </div>
      </form>
    </div>
  )
}
