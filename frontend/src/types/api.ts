// ──────────────────────────────────────────────
// Shared API types (aligned with backend DTOs)
// ──────────────────────────────────────────────

export interface Supplement {
  id: number
  name: string
  code: string
  defaultUnit: string
}

export interface RegimenItemRequest {
  supplementId: number
  supplementCode: string
  doseQty: number
  doseUnit: string
  frequencyPerDay: number
  scheduleWindow: string | null
}

export interface CreateRegimenRequest {
  patientId: string
  name: string
  items: RegimenItemRequest[]
}

export interface RegimenItemResponse {
  itemId: number
  supplementId: number
  supplementCode: string
  doseQty: number
  doseUnit: string
  frequencyPerDay: number
  scheduleWindow: string | null
}

export interface RegimenResponse {
  regimenId: number
  patientId: string
  name: string
  status: string
  items: RegimenItemResponse[]
  createdAt: string
}
