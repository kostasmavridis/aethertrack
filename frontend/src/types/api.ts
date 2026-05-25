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

export interface ScheduleAssignmentResponse {
  window: string
  label: string
  assigned: boolean
}

export interface ScheduleRowResponse {
  regimenItemId: number
  supplementCode: string
  supplementName: string
  assignments: ScheduleAssignmentResponse[]
}

export interface RegimenScheduleResponse {
  regimenId: number
  patientId: string
  regimenName: string
  windows: string[]
  rows: ScheduleRowResponse[]
  optimizationNotes: string[]
}

export interface TodayDoseResponse {
  regimenItemId: number
  supplementCode: string
  supplementName: string
  window: string
  doseLabel: string
  taken: boolean
  adherenceStatus: string | null
}

export interface RegimenTodayResponse {
  regimenId: number
  patientId: string
  regimenName: string
  doses: TodayDoseResponse[]
}
