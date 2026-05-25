import { api } from './client'
import type { RegimenResponse } from '../types/api'

export const createRegimen = (req: CreateRegimenRequest) =>
  api.post<RegimenResponse>('/regimens', req)

export const listRegimens = () => api.get<RegimenResponse[]>('/regimens')

export const getRegimenSchedule = (regimenId: number) =>
  api.get<RegimenScheduleResponse>(`/regimens/${regimenId}/schedule`)

export type { CreateRegimenRequest, RegimenScheduleResponse } from '../types/api'
