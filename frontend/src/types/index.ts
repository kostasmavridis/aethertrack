export interface Supplement {
  id: number
  code: string
  name: string
  category: string
}

export interface RegimenItem {
  supplementId: number
  doseQty: number
  doseUnit: string
  frequencyPerDay: number
  scheduleWindow?: string
}

export interface Regimen {
  id: number
  patientId: string
  name: string
  items: RegimenItem[]
}
