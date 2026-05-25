import { api } from './client'
import type {
  CreateRegimenRequest,
  RegimenResponse,
  RegimenScheduleResponse,
  RegimenTodayResponse,
} from '../types/api'

export const createRegimen = (req: CreateRegimenRequest) =>
  api.post<RegimenResponse>('/regimens', req)

export const listRegimens = () => api.get<RegimenResponse[]>('/regimens')

export const getRegimenSchedule = (regimenId: number) =>
  api.get<RegimenScheduleResponse>(`/regimens/${regimenId}/schedule`)

export const getRegimenToday = (regimenId: number) =>
  api.get<RegimenTodayResponse>(`/regimens/${regimenId}/today`)

export type { CreateRegimenRequest, RegimenScheduleResponse, RegimenTodayResponse } from '../types/api'
