import { api } from './client'

export interface IntakeLogRequest {
  patientId: string
  regimenItemId: number
  takenDateTime: string
  quantity: number
}

export interface IntakeLogResponse {
  intakeLogId: number
  status: string
  patientId: string
  regimenItemId: number
  takenDateTime: string
  quantity: number
}

export const logIntake = (req: IntakeLogRequest) =>
  api.post<IntakeLogResponse>('/intake', req)
