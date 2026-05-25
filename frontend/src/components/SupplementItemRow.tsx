import type { Supplement } from '../types/api'

const SCHEDULE_WINDOWS = [
  { value: '',          label: '— any —' },
  { value: 'MORNING',   label: 'Morning (06:00–10:00)' },
  { value: 'MIDDAY',    label: 'Midday (11:00–14:00)' },
  { value: 'EVENING',   label: 'Evening (17:00–20:00)' },
  { value: 'NIGHT',     label: 'Night (21:00–23:00)' },
  { value: 'WITH_MEAL', label: 'With a meal' },
]

export interface ItemFormState {
  supplementId: number
  doseQty: string
  doseUnit: string
  frequencyPerDay: string
  scheduleWindow: string
}

interface Props {
  index: number
  item: ItemFormState
  supplements: Supplement[]
  onChange: (index: number, field: keyof ItemFormState, value: string | number) => void
  onRemove: (index: number) => void
}

export default function SupplementItemRow({ index, item, supplements, onChange, onRemove }: Props) {
  const selected = supplements.find(s => s.id === Number(item.supplementId))

  return (
    <div className="item-row">
      <select
        value={item.supplementId}
        onChange={e => {
          const sup = supplements.find(s => s.id === Number(e.target.value))
          onChange(index, 'supplementId', Number(e.target.value))
          if (sup) onChange(index, 'doseUnit', sup.defaultUnit)
        }}
        required
      >
        <option value="">Select supplement…</option>
        {supplements.map(s => (
          <option key={s.id} value={s.id}>{s.name}</option>
        ))}
      </select>

      <input
        type="number"
        min="0.001"
        step="any"
        placeholder="Dose"
        value={item.doseQty}
        onChange={e => onChange(index, 'doseQty', e.target.value)}
        required
      />

      <input
        type="text"
        placeholder={selected?.defaultUnit ?? 'unit'}
        value={item.doseUnit}
        onChange={e => onChange(index, 'doseUnit', e.target.value)}
        required
        style={{ width: 72 }}
      />

      <select
        value={item.frequencyPerDay}
        onChange={e => onChange(index, 'frequencyPerDay', e.target.value)}
      >
        {[1,2,3,4].map(n => (
          <option key={n} value={n}>{n}×/day</option>
        ))}
      </select>

      <select
        value={item.scheduleWindow}
        onChange={e => onChange(index, 'scheduleWindow', e.target.value)}
      >
        {SCHEDULE_WINDOWS.map(w => (
          <option key={w.value} value={w.value}>{w.label}</option>
        ))}
      </select>

      <button type="button" className="btn-icon" onClick={() => onRemove(index)} title="Remove">✕</button>
    </div>
  )
}
